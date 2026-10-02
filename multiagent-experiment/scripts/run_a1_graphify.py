#!/usr/bin/env python3
from __future__ import annotations

import argparse
import hashlib
import json
import shutil
import socket
import subprocess
import sys
import time
import urllib.error
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

# Local helper copied next to this script.
from graphify_runtime import (
    build_code_graph,
    ensure_graphify_image,
    graphify_allowed_tools,
    write_mcp_config,
)


def ts():
    return datetime.now().strftime("%H:%M:%S")


def log(msg=""):
    print(f"[{ts()}] {msg}", flush=True)


def now_iso():
    return datetime.now(timezone.utc).isoformat()


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def run(cmd, cwd: Path, timeout=None):
    return subprocess.run(
        cmd, cwd=str(cwd), text=True,
        stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
        timeout=timeout
    )


def save_json(path: Path, obj):
    path.write_text(json.dumps(obj, indent=2, ensure_ascii=False), encoding="utf-8")


def port_open(host: str, port: int) -> bool:
    try:
        with socket.create_connection((host, port), timeout=0.5):
            return True
    except OSError:
        return False


def wait_http(base_url: str, timeout_seconds: int = 180) -> bool:
    deadline = time.time() + timeout_seconds
    url = base_url.rstrip("/") + "/api/quotes/AAPL"
    while time.time() < deadline:
        try:
            with urllib.request.urlopen(url, timeout=3):
                return True
        except urllib.error.HTTPError:
            return True
        except Exception:
            time.sleep(2)
    return False


def compose(project: str, workspace: Path, args, timeout=900):
    return run(["docker", "compose", "-p", project] + list(args), workspace, timeout)


def compose_services(workspace: Path):
    p = run(["docker", "compose", "config", "--services"], workspace, 60)
    if p.returncode != 0:
        raise RuntimeError("docker compose config failed:\n" + p.stdout)
    return [x.strip() for x in p.stdout.splitlines() if x.strip()]


def print_claude_event(event: dict):
    etype = event.get("type", "")
    if etype == "assistant":
        message = event.get("message") or {}
        for block in message.get("content", []) or []:
            btype = block.get("type")
            if btype == "tool_use":
                name = block.get("name", "tool")
                log(f"CLAUDE tool -> {name}")
            elif btype == "text":
                text = " ".join((block.get("text") or "").split())
                if text:
                    log("CLAUDE -> " + (text[:217] + "..." if len(text) > 220 else text))
    elif etype == "result":
        log(
            f"CLAUDE result turns={event.get('num_turns')} "
            f"cost=${event.get('total_cost_usd')} error={event.get('is_error')}"
        )


def invoke_claude(
    workspace: Path,
    prompt: str,
    model: str,
    max_turns: int,
    timeout_seconds: int,
    trace_path: Path,
    mcp_config: Path,
    resume_session_id=None,
):
    allowed = graphify_allowed_tools()
    cmd = [
        "claude", "-p", prompt,
        "--output-format", "stream-json",
        "--verbose",
        "--max-turns", str(max_turns),
        "--model", model,
        "--mcp-config", str(mcp_config),
        "--allowedTools", ",".join(allowed),
    ]
    if resume_session_id:
        cmd += ["--resume", resume_session_id]

    log(f"CLAUDE start model={model} max_turns={max_turns} graphify=ON")
    started = time.perf_counter()

    proc = subprocess.Popen(
        cmd, cwd=str(workspace),
        text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
        bufsize=1,
    )

    final_event = None
    deadline = time.time() + timeout_seconds

    with trace_path.open("w", encoding="utf-8") as trace:
        assert proc.stdout is not None
        for line in proc.stdout:
            if time.time() > deadline:
                proc.kill()
                raise TimeoutError(f"Claude exceeded {timeout_seconds}s")

            trace.write(line)
            trace.flush()

            raw = line.strip()
            if not raw:
                continue
            try:
                event = json.loads(raw)
            except json.JSONDecodeError:
                log(f"CLAUDE raw -> {raw[:220]}")
                continue

            print_claude_event(event)
            if event.get("type") == "result":
                final_event = event

    rc = proc.wait()
    elapsed = time.perf_counter() - started

    if rc != 0:
        raise RuntimeError(f"Claude exited with {rc}; see {trace_path}")
    if final_event is None:
        raise RuntimeError(f"No final Claude result event; see {trace_path}")

    final_event["_local_elapsed_seconds"] = round(elapsed, 3)
    return final_event


def run_acceptance(framework_root: Path, iteration_id: str, output_file: Path):
    runner = framework_root / "acceptance-tests" / "run_acceptance.py"
    p = run([
        "python3", str(runner),
        "--base-url", "http://localhost:8080",
        "--run-id", iteration_id,
        "--output", str(output_file),
    ], framework_root, 240)

    data = {}
    if output_file.exists():
        data = json.loads(output_file.read_text(encoding="utf-8"))
    return p.returncode, data, p.stdout


def failed_feedback(acceptance: dict, logs: str) -> str:
    failed = [
        {
            "id": t.get("id"),
            "name": t.get("name"),
            "message": t.get("message"),
            "http_statuses": t.get("http_statuses"),
        }
        for t in acceptance.get("tests", [])
        if not t.get("passed", False)
    ]
    return (
        "The immutable external black-box acceptance gate failed.\n\n"
        + json.dumps({
            "total": acceptance.get("total"),
            "passed": acceptance.get("passed"),
            "failed": acceptance.get("failed"),
            "failed_tests": failed,
        }, indent=2, ensure_ascii=False)
        + "\n\nRecent Docker logs:\n"
        + logs[-12000:]
        + "\n\nBefore reading many source files, use the Graphify MCP tools to "
          "locate the relevant symbols and relationships. Then read/edit only "
          "the source needed for the fix. Preserve the architecture."
    )


def usage_summary(r: dict):
    u = r.get("usage") or {}
    return {
        "session_id": r.get("session_id"),
        "total_cost_usd": r.get("total_cost_usd"),
        "num_turns": r.get("num_turns"),
        "duration_ms": r.get("duration_ms"),
        "duration_api_ms": r.get("duration_api_ms"),
        "input_tokens": u.get("input_tokens"),
        "output_tokens": u.get("output_tokens"),
        "cache_creation_input_tokens": u.get("cache_creation_input_tokens"),
        "cache_read_input_tokens": u.get("cache_read_input_tokens"),
        "modelUsage": r.get("modelUsage"),
        "permission_denials": r.get("permission_denials"),
        "result": r.get("result"),
        "local_elapsed_seconds": r.get("_local_elapsed_seconds"),
    }


def graphify_tool_counts(trace_path: Path) -> dict:
    counts = {}
    if not trace_path.exists():
        return counts
    for line in trace_path.read_text(encoding="utf-8", errors="ignore").splitlines():
        try:
            event = json.loads(line)
        except Exception:
            continue
        if event.get("type") != "assistant":
            continue
        msg = event.get("message") or {}
        for block in msg.get("content", []) or []:
            if block.get("type") == "tool_use":
                name = block.get("name", "")
                if name.startswith("mcp__graphify__"):
                    counts[name] = counts.get(name, 0) + 1
    return counts


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--repo-root", type=Path, default=Path("."))
    ap.add_argument("--framework-root", type=Path, default=Path("multiagent-experiment"))
    ap.add_argument("--seq", type=int, required=True)
    ap.add_argument("--prefix", default="P")
    ap.add_argument("--model", required=True)
    ap.add_argument("--graphify-image", default="graphify-mcp:0.9.73")
    ap.add_argument("--max-iterations", type=int, default=3)
    ap.add_argument("--max-turns-first", type=int, default=15)
    ap.add_argument("--max-turns-fix", type=int, default=8)
    ap.add_argument("--claude-timeout", type=int, default=1800)
    ap.add_argument("--startup-timeout", type=int, default=180)
    ap.add_argument("--keep-running", action="store_true")
    args = ap.parse_args()

    repo = args.repo_root.resolve()
    fw = (repo / args.framework_root).resolve()
    baseline = fw / "baselines" / "monolith"

    acceptance_runner = fw / "acceptance-tests" / "run_acceptance.py"
    acceptance_contract = fw / "acceptance-tests" / "acceptance_contract.json"
    requirements = fw / "contracts" / "requirements.md"
    architecture_contract = fw / "contracts" / "architecture_monolith.md"
    public_api_contract = fw / "contracts" / "public_api_v1.md"
    prompt_file = fw / "prompts" / "builder.md"

    required = [
        baseline / "docker-compose.yml",
        baseline / "financial-monolith" / "pom.xml",
        acceptance_runner, acceptance_contract, requirements,
        architecture_contract, public_api_contract, prompt_file,
    ]
    missing = [str(p) for p in required if not p.exists()]
    if missing:
        print("Missing required files:")
        for p in missing:
            print(" -", p)
        return 2

    graphify_image_meta = ensure_graphify_image(args.graphify_image)

    run_id = f"{args.prefix}{args.seq:04d}_monolith"
    run_dir = fw / "runs" / run_id
    workspace = run_dir / "workspace"

    if run_dir.exists():
        print(f"ABORT: run already exists: {run_dir}")
        return 2
    if port_open("127.0.0.1", 8080):
        print("ABORT: port 8080 is already in use.")
        return 2

    run_dir.mkdir(parents=True)
    shutil.copytree(baseline, workspace)

    compose_hash = sha256(workspace / "docker-compose.yml")
    baseline_services = compose_services(workspace)

    meta = {
        "run_id": run_id,
        "kind": "pilot" if args.prefix.upper().startswith("P") else "official",
        "architecture": "monolith",
        "provider": "claude-code",
        "model_requested": args.model,
        "claude_version": run(["claude", "--version"], workspace, 30).stdout.strip(),
        "graphify": {
            **graphify_image_meta,
            "enabled": True,
            "mode": "code-only-no-cluster",
            "network_for_extraction": "none",
            "network_for_mcp": "none",
        },
        "started_at_utc": now_iso(),
        "acceptance_contract_sha256": sha256(acceptance_contract),
        "acceptance_runner_sha256": sha256(acceptance_runner),
        "docker_compose_sha256": compose_hash,
        "compose_services": baseline_services,
        "limits": {
            "max_iterations": args.max_iterations,
            "max_turns_first": args.max_turns_first,
            "max_turns_fix": args.max_turns_fix,
        }
    }
    save_json(run_dir / "run_config.json", meta)

    static_prompt = (
        prompt_file.read_text(encoding="utf-8")
        + "\n\n# FIXED ARCHITECTURE CONTRACT\n\n"
        + architecture_contract.read_text(encoding="utf-8")
        + "\n\n# PUBLIC API CONTRACT\n\n"
        + public_api_contract.read_text(encoding="utf-8")
        + "\n\n# FIXED FUNCTIONAL CONTRACT\n\n"
        + requirements.read_text(encoding="utf-8")
        + "\n\n# GRAPH CONTEXT POLICY\n\n"
          "A read-only Graphify MCP server is available. For codebase discovery, "
          "prefer Graphify query_graph/get_node/get_neighbors/shortest_path before "
          "broad Glob/Grep/Read exploration. Use direct Read when you need exact "
          "source before editing. Graphify is context assistance only; the source "
          "files remain authoritative.\n"
        + "\n# EXPERIMENTAL CONSTRAINTS\n"
          "- Work only inside the current workspace.\n"
          "- Do not modify docker-compose.yml.\n"
          "- Do not add microservices, service discovery, an API gateway, or RabbitMQ.\n"
          "- Do not access or modify the external acceptance suite.\n"
          "- You may modify pom.xml, application configuration and Java source.\n"
    )

    project_name = run_id.lower().replace("_", "-")
    session_id = None
    feedback = None
    success = False
    total_cost = 0.0
    records = []
    started = time.perf_counter()

    try:
        for i in range(1, args.max_iterations + 1):
            log("=" * 72)
            log(f"ITERATION {i}/{args.max_iterations}")
            log("=" * 72)

            iter_dir = run_dir / f"iteration_{i:02d}"
            iter_dir.mkdir()

            log("GRAPHIFY extracting current workspace (local AST, code-only)")
            graph_root = iter_dir / "graphify"
            graph_metrics = build_code_graph(
                workspace=workspace,
                graph_root=graph_root,
                image=args.graphify_image,
            )
            save_json(iter_dir / "graphify_metrics.json", graph_metrics)
            log(
                f"GRAPHIFY graph bytes={graph_metrics['graph_bytes']} "
                f"nodes={graph_metrics['nodes']} edges={graph_metrics['edges']} "
                f"time={graph_metrics['extract_elapsed_seconds']}s"
            )

            mcp_config = write_mcp_config(
                workspace=workspace,
                graph_root=graph_root,
                image=args.graphify_image,
                output_path=iter_dir / "mcp_graphify.json",
            )

            prompt = static_prompt if i == 1 else feedback
            max_turns = args.max_turns_first if i == 1 else args.max_turns_fix

            trace_path = iter_dir / "claude_trace.jsonl"
            cr = invoke_claude(
                workspace=workspace,
                prompt=prompt,
                model=args.model,
                max_turns=max_turns,
                timeout_seconds=args.claude_timeout,
                trace_path=trace_path,
                mcp_config=mcp_config,
                resume_session_id=session_id,
            )
            save_json(iter_dir / "claude_result.json", cr)

            us = usage_summary(cr)
            save_json(iter_dir / "claude_usage.json", us)
            session_id = us.get("session_id") or session_id
            try:
                total_cost += float(us.get("total_cost_usd") or 0.0)
            except Exception:
                pass

            tool_counts = graphify_tool_counts(trace_path)
            save_json(iter_dir / "graphify_tool_usage.json", tool_counts)
            log(f"GRAPHIFY MCP tool calls={sum(tool_counts.values())} {tool_counts}")

            log("ARCHITECTURE guard")
            if sha256(workspace / "docker-compose.yml") != compose_hash:
                raise RuntimeError("ARCHITECTURE_VIOLATION: docker-compose.yml changed.")
            current_services = compose_services(workspace)
            guard = {
                "baseline": sorted(set(baseline_services)),
                "current": sorted(set(current_services)),
            }
            save_json(iter_dir / "architecture_guard.json", guard)
            if set(current_services) != set(baseline_services):
                raise RuntimeError(
                    f"ARCHITECTURE_VIOLATION: service membership changed: {guard}"
                )

            log("DOCKER cleanup")
            compose(project_name, workspace, ["down", "-v", "--remove-orphans"], 180)

            log("DOCKER build + up")
            up = compose(project_name, workspace, ["up", "-d", "--build"], 900)
            (iter_dir / "compose_up.log").write_text(up.stdout, encoding="utf-8")
            log(f"DOCKER up returncode={up.returncode}")

            logs = compose(
                project_name, workspace,
                ["logs", "--no-color", "--tail=300"], 120
            ).stdout
            (iter_dir / "compose_logs.txt").write_text(logs, encoding="utf-8")

            if up.returncode != 0:
                feedback = (
                    "Docker Compose build/start failed.\n\n"
                    + up.stdout[-12000:]
                    + "\n\nRecent logs:\n"
                    + logs[-12000:]
                    + "\n\nUse Graphify first to locate affected symbols, then fix only "
                      "the implementation. Preserve docker-compose.yml."
                )
                records.append({
                    "iteration": i,
                    "graphify": graph_metrics,
                    "graphify_tool_usage": tool_counts,
                    "claude": us,
                    "compose_up_ok": False,
                    "acceptance": None,
                })
                continue

            log("HTTP waiting for application on :8080")
            ready = wait_http("http://localhost:8080", args.startup_timeout)
            log(f"HTTP ready={ready}")
            if not ready:
                feedback = (
                    f"Application did not become reachable within {args.startup_timeout}s.\n\n"
                    + logs[-12000:]
                    + "\n\nUse Graphify first to locate affected symbols, then fix "
                      "startup/runtime errors only."
                )
                records.append({
                    "iteration": i,
                    "graphify": graph_metrics,
                    "graphify_tool_usage": tool_counts,
                    "claude": us,
                    "compose_up_ok": True,
                    "http_ready": False,
                    "acceptance": None,
                })
                continue

            log("ACCEPTANCE running immutable 12-test suite")
            afile = iter_dir / "acceptance.json"
            rc, acc, aout = run_acceptance(
                fw, f"{run_id}_i{i:02d}", afile
            )
            (iter_dir / "acceptance_stdout.txt").write_text(aout, encoding="utf-8")
            print(aout, flush=True)

            gate = rc == 0 and acc.get("passed") == 12 and acc.get("total") == 12
            log(f"ACCEPTANCE result={acc.get('passed')}/{acc.get('total')} success={gate}")

            records.append({
                "iteration": i,
                "graphify": graph_metrics,
                "graphify_tool_usage": tool_counts,
                "claude": us,
                "compose_up_ok": True,
                "http_ready": True,
                "acceptance": {
                    "passed": acc.get("passed"),
                    "failed": acc.get("failed"),
                    "total": acc.get("total"),
                    "success": gate,
                },
            })

            if gate:
                success = True
                break

            feedback = failed_feedback(acc, logs)
            log("FEEDBACK prepared for next iteration")

    finally:
        files = [
            str(p.relative_to(workspace))
            for p in sorted(workspace.rglob("*"))
            if p.is_file() and "/target/" not in p.as_posix()
        ]
        (run_dir / "final_file_list.txt").write_text(
            "\n".join(files) + "\n", encoding="utf-8"
        )
        if not args.keep_running:
            log("DOCKER final cleanup")
            try:
                compose(project_name, workspace, ["down", "-v", "--remove-orphans"], 180)
            except Exception:
                pass

    summary = {
        **meta,
        "finished_at_utc": now_iso(),
        "success": success,
        "termination": "SUCCESS" if success else "MAX_ITERATIONS_OR_ERROR",
        "iterations_used": len(records),
        "elapsed_seconds": round(time.perf_counter() - started, 3),
        "total_cost_usd_sum": round(total_cost, 8),
        "session_id": session_id,
        "iterations": records,
    }
    save_json(run_dir / "builder_summary.json", summary)

    md = [
        f"# Graphify A1 pilot — {run_id}",
        "",
        f"- Success: **{success}**",
        f"- Model: `{args.model}`",
        f"- Graphify image: `{args.graphify_image}`",
        f"- Iterations: **{len(records)}**",
        f"- Elapsed: **{summary['elapsed_seconds']} s**",
        f"- CLI-reported cost sum: **${summary['total_cost_usd_sum']}**",
        "",
        "| Iter | Graph nodes | Graph edges | MCP graph calls | Turns | Cost USD | Tests |",
        "|---:|---:|---:|---:|---:|---:|---:|",
    ]
    for r in records:
        a = r.get("acceptance") or {}
        gm = r.get("graphify") or {}
        md.append(
            f"| {r['iteration']} | {gm.get('nodes')} | {gm.get('edges')} "
            f"| {sum((r.get('graphify_tool_usage') or {}).values())} "
            f"| {r['claude'].get('num_turns')} "
            f"| {r['claude'].get('total_cost_usd')} "
            f"| {a.get('passed', '-')}/{a.get('total', '-')} |"
        )
    md += [
        "",
        "> `total_cost_usd` is the cost-equivalent value reported by Claude Code, "
        "not proof of the amount actually billed.",
        "",
    ]
    (run_dir / "RUN_REPORT.md").write_text("\n".join(md), encoding="utf-8")

    print()
    print("=" * 88)
    print("A1 + GRAPHIFY PILOT")
    print("=" * 88)
    print("Run             :", run_id)
    print("Model           :", args.model)
    print("Graphify        :", args.graphify_image)
    print("Iterations      :", len(records))
    print("Success         :", success)
    print("Claude cost USD :", round(total_cost, 8))
    print("Summary         :", run_dir / "builder_summary.json")
    print("Markdown report :", run_dir / "RUN_REPORT.md")
    return 0 if success else 1


if __name__ == "__main__":
    sys.exit(main())

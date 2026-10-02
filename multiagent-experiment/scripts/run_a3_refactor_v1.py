#!/usr/bin/env python3
from __future__ import annotations

import argparse
import hashlib
import json
import os
import shutil
import socket
import subprocess
import sys
import time
import urllib.error
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

from graphify_runtime_v4 import ensure_graphify_image, build_code_graph, query_graph


def ts():
    return datetime.now().strftime("%H:%M:%S")


def log(msg=""):
    print(f"[{ts()}] {msg}", flush=True)


def now_iso():
    return datetime.now(timezone.utc).isoformat()


def sha256(path: Path):
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def run(cmd, cwd=None, timeout=None):
    return subprocess.run(
        cmd,
        cwd=str(cwd) if cwd else None,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        timeout=timeout,
    )


def save_json(path: Path, obj):
    path.write_text(json.dumps(obj, indent=2, ensure_ascii=False), encoding="utf-8")


def load_json(path: Path):
    return json.loads(path.read_text(encoding="utf-8"))


def port_open(host, port):
    try:
        with socket.create_connection((host, port), timeout=.5):
            return True
    except OSError:
        return False


def wait_http(timeout_seconds=360):
    deadline = time.time() + timeout_seconds
    while time.time() < deadline:
        try:
            with urllib.request.urlopen(
                "http://localhost:8080/api/quotes/AAPL", timeout=3
            ):
                return True
        except urllib.error.HTTPError:
            return True
        except Exception:
            time.sleep(2)
    return False


def compose(project, workspace, args, timeout=900):
    return run(["docker", "compose", "-p", project] + list(args), workspace, timeout)


def compose_services(workspace):
    p = run(["docker", "compose", "config", "--services"], workspace, 60)
    if p.returncode:
        raise RuntimeError(p.stdout)
    return [x.strip() for x in p.stdout.splitlines() if x.strip()]


def claude_stream(workspace, prompt, model, max_turns, trace, timeout=1800, resume=None):
    cmd = [
        "claude", "-p", prompt,
        "--output-format", "stream-json",
        "--verbose",
        "--max-turns", str(max_turns),
        "--model", model,
        "--allowedTools", "Read,Write,Edit,Glob,Grep",
    ]
    if resume:
        cmd += ["--resume", resume]

    log(f"A3 CLAUDE start model={model} requested_max_turns={max_turns}")
    proc = subprocess.Popen(
        cmd,
        cwd=str(workspace),
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        bufsize=1,
    )

    final = None
    deadline = time.time() + timeout
    with trace.open("w", encoding="utf-8") as out:
        for line in proc.stdout:
            if time.time() > deadline:
                proc.kill()
                raise TimeoutError("A3 Claude timeout")
            out.write(line)
            out.flush()
            try:
                ev = json.loads(line)
            except Exception:
                continue

            if ev.get("type") == "assistant":
                for b in (ev.get("message") or {}).get("content", []) or []:
                    if b.get("type") == "tool_use":
                        log("A3 tool -> " + b.get("name", "?"))

            if ev.get("type") == "result":
                final = ev
                log(
                    f"A3 result turns={ev.get('num_turns')} "
                    f"cumulative_cost=${ev.get('total_cost_usd')}"
                )

    rc = proc.wait()
    if rc:
        raise RuntimeError(f"A3 Claude exit {rc}")
    if not final:
        raise RuntimeError("No A3 Claude result event")
    return final


def usage(result):
    u = result.get("usage") or {}
    return {
        "session_id": result.get("session_id"),
        "total_cost_usd": result.get("total_cost_usd"),
        "num_turns": result.get("num_turns"),
        "input_tokens": u.get("input_tokens"),
        "output_tokens": u.get("output_tokens"),
        "cache_creation_input_tokens": u.get("cache_creation_input_tokens"),
        "cache_read_input_tokens": u.get("cache_read_input_tokens"),
        "canonical_models": {
            k: v.get("canonicalModel")
            for k, v in (result.get("modelUsage") or {}).items()
        },
        "result": result.get("result"),
    }


def findings_query(findings):
    parts = []
    for f in findings.get("findings", [])[:20]:
        parts.append(
            " | ".join(
                str(x) for x in [
                    f.get("agent"),
                    f.get("type"),
                    f.get("severity"),
                    f.get("file") or f.get("target"),
                    f.get("symbol") or f.get("id") or f.get("package"),
                    f.get("metric"),
                    f.get("value"),
                ]
                if x not in (None, "")
            )
        )
    return (
        "Locate the source symbols and structural relationships most relevant to "
        "these deterministic refactoring findings. Focus only on code/configuration "
        "that may need modification: " + " || ".join(parts)
    )[:7000]


def failure_prompt(kind, evidence, graph_context, findings):
    return f"""A previous A3 refactor attempt did not pass the orchestrator gate.

Failure kind:
{kind}

Evidence:
{evidence[-10000:]}

Graphify context:
{graph_context}

Original deterministic findings:
{json.dumps(findings.get('findings', []), indent=2, ensure_ascii=False)}

Repair only the regression or startup/build issue caused by the refactor.
Preserve the public API and architecture. Do not run shell commands, Docker,
Maven, or tests. The orchestrator will validate your edits.
"""


def run_acceptance(fw, run_id, outfile):
    p = run([
        "python3",
        str(fw / "acceptance-tests" / "run_acceptance.py"),
        "--base-url", "http://localhost:8080",
        "--run-id", run_id,
        "--output", str(outfile),
    ], cwd=fw, timeout=240)
    data = load_json(outfile) if outfile.exists() else {}
    return p.returncode, data, p.stdout


def package_jar(project, workspace, idir):
    p = compose(
        project, workspace,
        ["run", "--rm", "--no-deps", "app",
         "mvn", "-q", "-B", "package", "-DskipTests"],
        900,
    )
    (idir / "package.log").write_text(p.stdout, encoding="utf-8")
    if p.returncode != 0:
        raise RuntimeError("Final Maven package failed:\n" + p.stdout[-8000:])

    jars = sorted(
        x for x in workspace.rglob("target/*.jar")
        if not x.name.endswith("-sources.jar")
        and not x.name.endswith("-javadoc.jar")
        and not x.name.endswith(".original")
    )
    if not jars:
        raise RuntimeError("No target/*.jar found after package")

    artifacts = []
    for jar in jars:
        artifacts.append({
            "path": str(jar.relative_to(workspace)),
            "bytes": jar.stat().st_size,
            "sha256": sha256(jar),
        })
    save_json(idir / "artifacts.json", artifacts)
    return artifacts


def run_a2_after(workspace, out_file, image, quality_policy):
    pol = load_json(quality_policy)["thresholds"]
    p = run([
        "docker", "run", "--rm",
        "-v", f"{workspace.resolve()}:/workspace:ro",
        image,
        "/workspace",
        "--max-function-ccn", str(pol["max_function_ccn"]),
        "--max-function-nloc", str(pol["max_function_nloc"]),
        "--max-file-nloc", str(pol["max_file_nloc"]),
        "--max-parameters", str(pol["max_parameters"]),
    ], timeout=600)
    if p.returncode:
        raise RuntimeError("A2 after failed:\n" + p.stdout[-6000:])
    data = json.loads(p.stdout)
    save_json(out_file, data)
    return data


def compare_metrics(before_q, after_q, before_s, after_s):
    bq, aq = before_q["summary"], after_q["summary"]
    bs, ass = before_s["summary"], after_s["summary"]

    qkeys = [
        "physical_java_loc", "nonblank_java_loc", "java_files",
        "lizard_nloc", "functions", "cc_total", "cc_average",
        "cc_max", "quality_findings",
    ]
    skeys = [
        "vulnerabilities", "secrets", "misconfigurations", "HIGH", "CRITICAL"
    ]

    quality = {}
    for k in qkeys:
        quality[k] = {
            "before": bq.get(k),
            "after": aq.get(k),
            "delta": (
                round(aq.get(k) - bq.get(k), 6)
                if isinstance(aq.get(k), (int, float))
                and isinstance(bq.get(k), (int, float))
                else None
            )
        }

    security = {}
    for k in skeys:
        security[k] = {
            "before": bs.get(k),
            "after": ass.get(k),
            "delta": (
                ass.get(k) - bs.get(k)
                if isinstance(ass.get(k), (int, float))
                and isinstance(bs.get(k), (int, float))
                else None
            )
        }

    return {"quality": quality, "security": security}


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--repo-root", type=Path, default=Path("."))
    ap.add_argument("--framework-root", type=Path, default=Path("multiagent-experiment"))
    ap.add_argument("--source-run", required=True,
                    help="Existing successful A1 run, e.g. P0007_monolith")
    ap.add_argument("--model", default="sonnet")
    ap.add_argument("--findings-json", type=Path, required=True)
    ap.add_argument("--quality-before", type=Path, required=True)
    ap.add_argument("--security-before", type=Path, required=True)
    ap.add_argument("--graphify-image", default="graphify-mcp:0.9.73")
    ap.add_argument("--graph-budget", type=int, default=1200)
    ap.add_argument("--startup-timeout", type=int, default=360)
    ap.add_argument("--max-turns-refactor", type=int, default=8)
    ap.add_argument("--max-turns-repair", type=int, default=5)
    ap.add_argument("--max-repair-attempts", type=int, default=1)
    ap.add_argument("--lizard-image", default="multiagent-lizard:1.24.0")
    ap.add_argument("--trivy-image", default="aquasec/trivy:0.74.0")
    ap.add_argument("--trivy-cache", type=Path, required=True)
    ap.add_argument("--quality-policy", type=Path, required=True)
    ap.add_argument("--security-policy", type=Path, required=True)
    args = ap.parse_args()

    repo = args.repo_root.resolve()
    fw = (repo / args.framework_root).resolve()
    source_run_dir = fw / "runs" / args.source_run
    source_workspace = source_run_dir / "workspace"

    if not source_workspace.exists():
        raise SystemExit(f"Missing source workspace: {source_workspace}")

    findings_path = args.findings_json.resolve()
    quality_before_path = args.quality_before.resolve()
    security_before_path = args.security_before.resolve()

    findings = load_json(findings_path)
    quality_before = load_json(quality_before_path)
    security_before = load_json(security_before_path)

    a3_root = source_run_dir / "a3"
    workspace = a3_root / "workspace"
    if a3_root.exists():
        raise SystemExit(f"A3 output already exists: {a3_root}")
    a3_root.mkdir(parents=True)
    shutil.copytree(source_workspace, workspace)

    total_findings = findings.get("summary", {}).get("total_findings")
    if total_findings is None:
        total_findings = len(findings.get("findings", []))

    compose_hash = sha256(workspace / "docker-compose.yml")
    services = compose_services(workspace)
    graph_meta = ensure_graphify_image(args.graphify_image)
    graph_runtime = graph_meta["runtime_ref"]

    meta = {
        "schema_version": 1,
        "source_run": args.source_run,
        "started_at_utc": now_iso(),
        "model_requested": args.model,
        "claude_version": run(["claude", "--version"], cwd=workspace, timeout=30).stdout.strip(),
        "graphify": graph_meta,
        "graph_budget": args.graph_budget,
        "startup_timeout_seconds": args.startup_timeout,
        "findings_json_sha256": sha256(findings_path),
        "quality_before_sha256": sha256(quality_before_path),
        "security_before_sha256": sha256(security_before_path),
        "docker_compose_sha256": compose_hash,
        "compose_services": services,
        "total_findings": total_findings,
    }
    save_json(a3_root / "a3_config.json", meta)

    if total_findings == 0:
        summary = {
            **meta,
            "finished_at_utc": now_iso(),
            "success": True,
            "termination_reason": "SKIPPED_NO_FINDINGS",
            "claude_calls": 0,
        }
        save_json(a3_root / "a3_summary.json", summary)
        print("A3 skipped: no deterministic findings.")
        return 0

    contracts = {
        "refactor_prompt": fw / "prompts" / "refactor.md",
        "architecture": fw / "contracts" / "architecture_monolith.md",
        "api": fw / "contracts" / "public_api_v1.md",
        "requirements": fw / "contracts" / "requirements.md",
    }
    missing = [str(p) for p in contracts.values() if not p.exists()]
    if missing:
        raise SystemExit("Missing A3 inputs:\n" + "\n".join(missing))

    project = f"{args.source_run}-a3".lower().replace("_", "-")
    if port_open("127.0.0.1", 8080):
        raise SystemExit("Port 8080 already in use")

    # Build Graphify context from the actual generated implementation.
    graph_dir = a3_root / "graphify_before"
    log("GRAPHIFY extract before A3")
    gm = build_code_graph(workspace, graph_dir, graph_runtime)
    q = findings_query(findings)
    (a3_root / "graph_query.txt").write_text(q, encoding="utf-8")
    ctx = query_graph(graph_dir, graph_runtime, q, args.graph_budget)
    (a3_root / "graph_context.txt").write_text(ctx, encoding="utf-8")

    static = (
        contracts["refactor_prompt"].read_text(encoding="utf-8")
        + "\n\n# ARCHITECTURE CONTRACT\n"
        + contracts["architecture"].read_text(encoding="utf-8")
        + "\n\n# PUBLIC API CONTRACT\n"
        + contracts["api"].read_text(encoding="utf-8")
        + "\n\n# REQUIREMENTS\n"
        + contracts["requirements"].read_text(encoding="utf-8")
        + "\n\n# DETERMINISTIC FINDINGS\n"
        + json.dumps(findings.get("findings", []), indent=2, ensure_ascii=False)
        + "\n\n# GRAPHIFY CONTEXT\n"
        + ctx
    )

    session = None
    session_cost = 0.0
    calls = []
    success = False
    termination = None
    started = time.perf_counter()

    try:
        attempts = 1 + args.max_repair_attempts
        prompt = static

        for attempt in range(1, attempts + 1):
            idir = a3_root / f"attempt_{attempt:02d}"
            idir.mkdir()

            result = claude_stream(
                workspace,
                prompt,
                args.model,
                args.max_turns_refactor if attempt == 1 else args.max_turns_repair,
                idir / "claude_trace.jsonl",
                resume=session,
            )
            us = usage(result)
            session = us.get("session_id") or session
            current_cost = float(us.get("total_cost_usd") or 0)
            incremental = max(0.0, current_cost - session_cost)
            session_cost = current_cost
            us["reported_cost_usd_incremental_derived"] = round(incremental, 8)
            save_json(idir / "claude_usage.json", us)

            if sha256(workspace / "docker-compose.yml") != compose_hash:
                raise RuntimeError("ARCHITECTURE_VIOLATION: docker-compose.yml changed")
            if set(compose_services(workspace)) != set(services):
                raise RuntimeError("ARCHITECTURE_VIOLATION: compose services changed")

            compose(project, workspace, ["down", "-v", "--remove-orphans"], 180)
            up = compose(project, workspace, ["up", "-d", "--build"], 900)
            (idir / "compose_up.log").write_text(up.stdout, encoding="utf-8")

            if up.returncode != 0:
                logs = compose(
                    project, workspace,
                    ["logs", "--no-color", "--tail=500"], 120
                ).stdout
                (idir / "compose_logs.txt").write_text(logs, encoding="utf-8")
                calls.append({
                    "attempt": attempt, "claude": us,
                    "gate": "BUILD_OR_START_FAILURE",
                })
                if attempt >= attempts:
                    termination = "BUILD_OR_START_FAILURE"
                    break
                prompt = failure_prompt(
                    "BUILD_OR_START_FAILURE", up.stdout + "\n" + logs, ctx, findings
                )
                continue

            ready = wait_http(args.startup_timeout)
            logs = compose(
                project, workspace,
                ["logs", "--no-color", "--tail=500"], 120
            ).stdout
            (idir / "compose_logs.txt").write_text(logs, encoding="utf-8")

            if not ready:
                calls.append({
                    "attempt": attempt, "claude": us,
                    "gate": "STARTUP_TIMEOUT",
                })
                if attempt >= attempts:
                    termination = "STARTUP_TIMEOUT"
                    break
                prompt = failure_prompt("STARTUP_TIMEOUT", logs, ctx, findings)
                continue

            afile = idir / "acceptance.json"
            rc, acc, stdout = run_acceptance(
                fw, f"{args.source_run}_a3_{attempt:02d}", afile
            )
            (idir / "acceptance_stdout.txt").write_text(stdout, encoding="utf-8")
            print(stdout, flush=True)
            gate = rc == 0 and acc.get("passed") == 12 and acc.get("total") == 12

            calls.append({
                "attempt": attempt,
                "claude": us,
                "gate": "PASS" if gate else "ACCEPTANCE_FAILURE",
                "acceptance": {
                    "passed": acc.get("passed"),
                    "failed": acc.get("failed"),
                    "total": acc.get("total"),
                }
            })

            if gate:
                log("Packaging final A3 artifact")
                artifacts = package_jar(project, workspace, idir)
                success = True
                termination = "SUCCESS"
                break

            if attempt >= attempts:
                termination = "ACCEPTANCE_FAILURE"
                break

            failed = [
                {
                    "id": t.get("id"),
                    "name": t.get("name"),
                    "message": t.get("message"),
                    "http_statuses": t.get("http_statuses"),
                }
                for t in acc.get("tests", [])
                if not t.get("passed")
            ]
            evidence = json.dumps(failed, indent=2, ensure_ascii=False)
            prompt = failure_prompt("ACCEPTANCE_FAILURE", evidence, ctx, findings)

        if not success:
            summary = {
                **meta,
                "finished_at_utc": now_iso(),
                "success": False,
                "termination_reason": termination,
                "elapsed_seconds": round(time.perf_counter() - started, 3),
                "reported_cost_usd_final_session": round(session_cost, 8),
                "calls": calls,
            }
            save_json(a3_root / "a3_summary.json", summary)
            return 1

        # Measure AFTER only after functional equivalence is re-established.
        after_dir = a3_root / "analysis_after"
        after_dir.mkdir()

        log("A2 after")
        quality_after = run_a2_after(
            workspace,
            after_dir / "quality_after.json",
            args.lizard_image,
            args.quality_policy.resolve(),
        )

        # A4 v2 owns an empty output directory. Use a staging dir, then merge.
        a4_stage = a3_root / "a4_after_stage"
        a4_script = fw / "scripts" / "run_a4_artifact_offline_v2.py"
        if not a4_script.exists():
            raise RuntimeError(f"Missing A4 v2 script: {a4_script}")

        log("A4 after")
        p = run([
            "python3", str(a4_script),
            "--workspace", str(workspace),
            "--quality-json", str(after_dir / "quality_after.json"),
            "--output-dir", str(a4_stage),
            "--trivy-image", args.trivy_image,
            "--trivy-cache", str(args.trivy_cache.resolve()),
            "--security-policy", str(args.security_policy.resolve()),
        ], cwd=fw, timeout=1800)
        (a3_root / "a4_after_console.log").write_text(p.stdout, encoding="utf-8")
        if p.returncode != 0:
            raise RuntimeError("A4 after failed:\n" + p.stdout[-8000:])

        for name in [
            "security_before.json",
            "findings.json",
            "analysis_manifest.json",
            "A2_A4_REPORT.md",
            "trivy_source_raw.json",
            "trivy_artifacts_raw.json",
        ]:
            src = a4_stage / name
            if src.exists():
                dest_name = {
                    "security_before.json": "security_after.json",
                    "findings.json": "findings_after.json",
                    "analysis_manifest.json": "analysis_after_manifest.json",
                    "A2_A4_REPORT.md": "A2_A4_AFTER_REPORT.md",
                }.get(name, name.replace(".json", "_after.json"))
                shutil.copy2(src, after_dir / dest_name)

        security_after = load_json(after_dir / "security_after.json")
        comparison = compare_metrics(
            quality_before, quality_after,
            security_before, security_after
        )
        comparison["acceptance_after"] = {"passed": 12, "total": 12}
        comparison["a3_reported_cost_usd"] = round(session_cost, 8)
        comparison["artifacts"] = artifacts
        save_json(a3_root / "before_after.json", comparison)

        summary = {
            **meta,
            "finished_at_utc": now_iso(),
            "success": True,
            "termination_reason": "SUCCESS",
            "elapsed_seconds": round(time.perf_counter() - started, 3),
            "reported_cost_usd_final_session": round(session_cost, 8),
            "calls": calls,
            "artifacts": artifacts,
            "comparison_path": str(a3_root / "before_after.json"),
        }
        save_json(a3_root / "a3_summary.json", summary)

        # Compact markdown.
        qcomp = comparison["quality"]
        scomp = comparison["security"]
        lines = [
            f"# A3 Refactor report — {args.source_run}",
            "",
            "- Success: **True**",
            f"- Model: `{args.model}`",
            f"- Deterministic findings before A3: **{total_findings}**",
            "- Acceptance after A3: **12/12**",
            f"- CLI-reported A3 session cost: **${round(session_cost, 8)}**",
            "",
            "## Quality before → after",
            "",
            "| Metric | Before | After | Delta |",
            "|---|---:|---:|---:|",
        ]
        for k, row in qcomp.items():
            lines.append(
                f"| {k} | {row['before']} | {row['after']} | {row['delta']} |"
            )
        lines += [
            "",
            "## Security before → after",
            "",
            "| Metric | Before | After | Delta |",
            "|---|---:|---:|---:|",
        ]
        for k, row in scomp.items():
            lines.append(
                f"| {k} | {row['before']} | {row['after']} | {row['delta']} |"
            )
        (a3_root / "A3_REPORT.md").write_text(
            "\n".join(lines) + "\n", encoding="utf-8"
        )

        print("\n===== A3 COMPLETE =====")
        print("Summary :", a3_root / "a3_summary.json")
        print("Compare :", a3_root / "before_after.json")
        print("Report  :", a3_root / "A3_REPORT.md")
        return 0

    finally:
        try:
            compose(project, workspace, ["down", "-v", "--remove-orphans"], 180)
        except Exception:
            pass


if __name__ == "__main__":
    sys.exit(main())

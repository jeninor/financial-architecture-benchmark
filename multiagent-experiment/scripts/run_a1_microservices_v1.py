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

from graphify_runtime_v4 import ensure_graphify_image, build_code_graph, query_graph


APP_SERVICES = [
    "discovery-server",
    "api-gateway",
    "user-service",
    "market-service",
    "trade-service",
]


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


def run(cmd, cwd: Path | None = None, timeout=None):
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
        except urllib.error.HTTPError as e:
            # Any application-level HTTP response proves that the gateway is up.
            if e.code in (400, 401, 403, 404, 405, 409, 422, 500, 502, 503):
                # 502/503 can still mean downstreams are not ready. Require a
                # non-gateway-error response for this known-valid endpoint.
                if e.code not in (502, 503):
                    return True
        except Exception:
            pass
        time.sleep(2)
    return False


def compose(project, workspace, args, timeout=900):
    return run(["docker", "compose", "-p", project] + list(args), workspace, timeout)


def compose_services(workspace):
    p = run(["docker", "compose", "config", "--services"], workspace, 60)
    if p.returncode:
        raise RuntimeError(p.stdout)
    return [x.strip() for x in p.stdout.splitlines() if x.strip()]


def relevant_logs(project, workspace, services):
    present = set(compose_services(workspace))
    selected = [s for s in services if s in present]
    if not selected:
        return compose(
            project, workspace, ["logs", "--no-color", "--tail=500"], 120
        ).stdout
    return compose(
        project, workspace,
        ["logs", "--no-color", "--tail=500"] + selected,
        120,
    ).stdout


def claude_stream(workspace, prompt, model, max_turns, timeout_seconds, trace, resume=None):
    cmd = [
        "claude", "-p", prompt,
        "--output-format", "stream-json",
        "--verbose",
        "--max-turns", str(max_turns),
        "--model", model,
        "--allowedTools", "Read,Write,Edit,Glob,Grep,Bash",
    ]
    if resume:
        cmd += ["--resume", resume]

    log(f"CLAUDE start model={model} requested_max_turns={max_turns}")
    proc = subprocess.Popen(
        cmd,
        cwd=str(workspace),
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        bufsize=1,
    )

    final = None
    deadline = time.time() + timeout_seconds
    with trace.open("w", encoding="utf-8") as out:
        for line in proc.stdout:
            if time.time() > deadline:
                proc.kill()
                raise TimeoutError("Claude timeout")
            out.write(line)
            out.flush()
            try:
                ev = json.loads(line)
            except Exception:
                continue

            if ev.get("type") == "assistant":
                for b in (ev.get("message") or {}).get("content", []) or []:
                    if b.get("type") == "tool_use":
                        log("CLAUDE tool -> " + b.get("name", "?"))

            if ev.get("type") == "result":
                final = ev
                log(
                    f"CLAUDE result turns={ev.get('num_turns')} "
                    f"cumulative_cost=${ev.get('total_cost_usd')}"
                )

    rc = proc.wait()
    if rc:
        raise RuntimeError(f"Claude exit {rc}")
    if not final:
        raise RuntimeError("No Claude result event")
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


def run_acceptance(fw, run_id, outfile):
    p = run([
        "python3",
        str(fw / "acceptance-tests" / "run_acceptance.py"),
        "--base-url", "http://localhost:8080",
        "--run-id", run_id,
        "--output", str(outfile),
    ], cwd=fw, timeout=240)
    data = json.loads(outfile.read_text()) if outfile.exists() else {}
    return p.returncode, data, p.stdout


def failed_test_question(acc):
    failed = [t for t in acc.get("tests", []) if not t.get("passed")]
    parts = []
    for t in failed:
        parts.append(
            f"{t.get('id')} {t.get('name')}: {t.get('message')}"
        )
    return (
        "Locate the microservice Java classes, methods and service-to-service "
        "relationships most relevant to these failed public acceptance behaviors. "
        "Include gateway routing, Feign/service calls and state ownership when "
        "relevant: " + " | ".join(parts)
    )[:7000]


def evidence_question(kind, evidence):
    return (
        "Locate the microservice source files, configuration and structural "
        "relationships most relevant to this failure. Focus on the smallest repair "
        f"consistent with the frozen architecture. Failure kind={kind}. Evidence:\n"
        + evidence[-9000:]
    )[:10000]


def build_feedback(kind, evidence, graph_context):
    return f"""The orchestrator detected a failure after your previous implementation attempt.

FAILURE KIND:
{kind}

EVIDENCE:
{evidence[-12000:]}

GRAPHIFY RETRIEVED CONTEXT:
{graph_context}

Repair only the implementation/configuration needed for this failure.
Preserve the frozen Compose topology, service ownership and public API.
Do NOT run docker compose up/down/start/stop/restart. The orchestrator owns
container lifecycle and the external acceptance gate.
Do not access files outside this workspace.
"""


def graph_context_for_failure(workspace, idir, runtime_ref, question, budget):
    # Re-extract AFTER Claude edits, so the graph describes the failing code
    # rather than the pre-edit baseline.
    graph_dir = idir / "graphify_after_failure"
    log("GRAPHIFY extract failing workspace")
    gm = build_code_graph(workspace, graph_dir, runtime_ref)
    save_json(idir / "graphify_after_failure_metrics.json", gm)
    (idir / "graph_query.txt").write_text(question, encoding="utf-8")
    log(f"GRAPHIFY query budget={budget}")
    ctx = query_graph(graph_dir, runtime_ref, question, budget)
    (idir / "graph_context.txt").write_text(ctx, encoding="utf-8")
    return gm, ctx


def package_services(project, workspace, compose_service_names, idir):
    artifacts = []
    package_logs = {}

    for service in APP_SERVICES:
        if service not in compose_service_names:
            continue

        log(f"PACKAGE {service}")
        p = compose(
            project,
            workspace,
            [
                "run", "--rm", "--no-deps", service,
                "mvn", "-q", "-B", "package", "-DskipTests"
            ],
            1200,
        )
        package_logs[service] = {
            "returncode": p.returncode,
            "tail": p.stdout[-6000:],
        }
        (idir / f"package_{service}.log").write_text(
            p.stdout, encoding="utf-8"
        )
        if p.returncode != 0:
            save_json(idir / "package_summary.json", package_logs)
            raise RuntimeError(
                f"Packaging failed for {service}:\n{p.stdout[-8000:]}"
            )

    for jar in sorted(workspace.rglob("target/*.jar")):
        name = jar.name
        if name.endswith("-sources.jar") or name.endswith("-javadoc.jar"):
            continue
        if name.endswith(".original"):
            continue
        artifacts.append({
            "path": str(jar.relative_to(workspace)),
            "bytes": jar.stat().st_size,
            "sha256": sha256(jar),
        })

    save_json(idir / "package_summary.json", package_logs)
    save_json(idir / "artifacts.json", artifacts)
    return artifacts


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--repo-root", type=Path, default=Path("."))
    ap.add_argument("--framework-root", type=Path, default=Path("multiagent-experiment"))
    ap.add_argument("--seq", type=int, required=True)
    ap.add_argument("--prefix", default="P")
    ap.add_argument("--model", required=True)
    ap.add_argument("--graphify-image", default="graphify-mcp:0.9.73")
    ap.add_argument("--graph-budget", type=int, default=1200)
    ap.add_argument("--max-iterations", type=int, default=3)
    ap.add_argument("--max-turns-first", type=int, default=15)
    ap.add_argument("--max-turns-fix", type=int, default=8)
    ap.add_argument("--startup-timeout", type=int, default=360)
    args = ap.parse_args()

    repo = args.repo_root.resolve()
    fw = (repo / args.framework_root).resolve()
    baseline = fw / "baselines" / "microservices"

    files = {
        "acceptance": fw / "acceptance-tests" / "run_acceptance.py",
        "contract": fw / "acceptance-tests" / "acceptance_contract.json",
        "requirements": fw / "contracts" / "requirements.md",
        "architecture": fw / "contracts" / "architecture_microservices.md",
        "api": fw / "contracts" / "public_api_v1.md",
        "prompt": fw / "prompts" / "builder_microservices.md",
    }

    required = [baseline / "docker-compose.yml", *files.values()]
    missing = [str(p) for p in required if not p.exists()]
    if missing:
        print("Missing:", *missing, sep="\n - ")
        return 2

    image_meta = ensure_graphify_image(args.graphify_image)
    runtime_ref = image_meta["runtime_ref"]
    print(f"[PREFLIGHT] Docker context: {image_meta['docker_context']}", flush=True)
    print(f"[PREFLIGHT] Graphify requested: {image_meta['requested_ref']}", flush=True)
    print(f"[PREFLIGHT] Graphify resolved: {runtime_ref}", flush=True)

    run_id = f"{args.prefix}{args.seq:04d}_microservices"
    run_dir = fw / "runs" / run_id
    workspace = run_dir / "workspace"

    if run_dir.exists():
        print("ABORT existing run", run_dir)
        return 2
    if port_open("127.0.0.1", 8080):
        print("ABORT port 8080 in use")
        return 2

    run_dir.mkdir(parents=True)
    shutil.copytree(baseline, workspace)

    compose_hash = sha256(workspace / "docker-compose.yml")
    services = compose_services(workspace)

    expected_roles = set(APP_SERVICES)
    missing_roles = sorted(expected_roles - set(services))
    if missing_roles:
        print(
            "ABORT baseline missing expected microservice roles:",
            ", ".join(missing_roles)
        )
        return 2

    meta = {
        "run_id": run_id,
        "kind": "pilot",
        "architecture": "microservices",
        "provider": "claude-code",
        "model_requested": args.model,
        "claude_version": run(["claude", "--version"], workspace, 30).stdout.strip(),
        "graphify": {
            **image_meta,
            "mode": "orchestrator-side-retrieval-after-failure",
            "budget": args.graph_budget,
        },
        "started_at_utc": now_iso(),
        "acceptance_contract_sha256": sha256(files["contract"]),
        "acceptance_runner_sha256": sha256(files["acceptance"]),
        "architecture_contract_sha256": sha256(files["architecture"]),
        "docker_compose_sha256": compose_hash,
        "compose_services": services,
        "startup_timeout_seconds": args.startup_timeout,
        "limits": {
            "max_iterations": args.max_iterations,
            "max_turns_first": args.max_turns_first,
            "max_turns_fix": args.max_turns_fix,
        },
    }
    save_json(run_dir / "run_config.json", meta)

    static = (
        files["prompt"].read_text(encoding="utf-8")
        + "\n\n# ARCHITECTURE\n"
        + files["architecture"].read_text(encoding="utf-8")
        + "\n\n# PUBLIC API\n"
        + files["api"].read_text(encoding="utf-8")
        + "\n\n# REQUIREMENTS\n"
        + files["requirements"].read_text(encoding="utf-8")
    )

    project = run_id.lower().replace("_", "-")
    session = None
    session_costs = {}
    records = []
    current_feedback = None
    success = False
    artifacts = []
    started = time.perf_counter()

    try:
        for i in range(1, args.max_iterations + 1):
            idir = run_dir / f"iteration_{i:02d}"
            idir.mkdir()
            log("=" * 72)
            log(f"ITERATION {i}/{args.max_iterations}")
            log("=" * 72)

            prompt = static if i == 1 else current_feedback
            result = claude_stream(
                workspace,
                prompt,
                args.model,
                args.max_turns_first if i == 1 else args.max_turns_fix,
                2400,
                idir / "claude_trace.jsonl",
                session,
            )
            us = usage(result)
            session = us.get("session_id") or session

            sid = us.get("session_id") or f"iteration-{i}"
            curr = float(us.get("total_cost_usd") or 0)
            prev = session_costs.get(sid, 0.0)
            us["reported_cost_usd_incremental_derived"] = round(
                max(0.0, curr - prev), 8
            )
            session_costs[sid] = curr
            save_json(idir / "claude_usage.json", us)

            if sha256(workspace / "docker-compose.yml") != compose_hash:
                raise RuntimeError("ARCHITECTURE_VIOLATION: docker-compose.yml changed")
            if set(compose_services(workspace)) != set(services):
                raise RuntimeError("ARCHITECTURE_VIOLATION: Compose service set changed")

            # Clean state for first attempt; preserve Maven/cache volumes between
            # repair attempts to reduce infrastructure noise.
            if i == 1:
                compose(project, workspace, ["down", "-v", "--remove-orphans"], 180)
            else:
                compose(project, workspace, ["down", "--remove-orphans"], 180)

            up = compose(project, workspace, ["up", "-d", "--build"], 1200)
            (idir / "compose_up.log").write_text(up.stdout, encoding="utf-8")

            if up.returncode != 0:
                logs = relevant_logs(project, workspace, APP_SERVICES)
                ps = compose(project, workspace, ["ps", "-a"], 120).stdout
                (idir / "compose_logs_after_wait.txt").write_text(logs, encoding="utf-8")
                (idir / "compose_ps_after_wait.txt").write_text(ps, encoding="utf-8")

                evidence = up.stdout[-10000:] + "\n" + logs[-12000:] + "\n" + ps[-4000:]
                q = evidence_question("BUILD_OR_START_FAILURE", evidence)
                gm, ctx = graph_context_for_failure(
                    workspace, idir, runtime_ref, q, args.graph_budget
                )
                current_feedback = build_feedback(
                    "BUILD_OR_START_FAILURE", evidence, ctx
                )
                records.append({
                    "iteration": i,
                    "claude": us,
                    "compose_up_ok": False,
                    "failure_class": "BUILD_OR_START_FAILURE",
                    "graphify_after_failure": gm,
                })
                continue

            ready = wait_http(args.startup_timeout)
            logs = relevant_logs(project, workspace, APP_SERVICES)
            ps = compose(project, workspace, ["ps", "-a"], 120).stdout
            (idir / "compose_logs_after_wait.txt").write_text(logs, encoding="utf-8")
            (idir / "compose_ps_after_wait.txt").write_text(ps, encoding="utf-8")

            if not ready:
                evidence = logs[-14000:] + "\n\nCOMPOSE PS:\n" + ps[-5000:]
                q = evidence_question("STARTUP_TIMEOUT", evidence)
                gm, ctx = graph_context_for_failure(
                    workspace, idir, runtime_ref, q, args.graph_budget
                )
                current_feedback = build_feedback("STARTUP_TIMEOUT", evidence, ctx)
                records.append({
                    "iteration": i,
                    "claude": us,
                    "compose_up_ok": True,
                    "http_ready": False,
                    "failure_class": "STARTUP_TIMEOUT",
                    "graphify_after_failure": gm,
                })
                continue

            afile = idir / "acceptance.json"
            rc, acc, stdout = run_acceptance(
                fw, f"{run_id}_i{i:02d}", afile
            )
            (idir / "acceptance_stdout.txt").write_text(stdout, encoding="utf-8")
            print(stdout, flush=True)

            gate = (
                rc == 0
                and acc.get("passed") == 12
                and acc.get("total") == 12
            )

            rec = {
                "iteration": i,
                "claude": us,
                "compose_up_ok": True,
                "http_ready": True,
                "acceptance": {
                    "passed": acc.get("passed"),
                    "failed": acc.get("failed"),
                    "total": acc.get("total"),
                    "success": gate,
                },
            }

            if gate:
                log("PACKAGING application service artifacts")
                artifacts = package_services(
                    project, workspace, set(services), idir
                )
                rec["artifacts"] = artifacts
                records.append(rec)
                success = True
                break

            q = failed_test_question(acc)
            gm, ctx = graph_context_for_failure(
                workspace, idir, runtime_ref, q, args.graph_budget
            )
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
            current_feedback = build_feedback(
                "ACCEPTANCE_FAILURE", evidence, ctx
            )
            rec["failure_class"] = "ACCEPTANCE_FAILURE"
            rec["graphify_after_failure"] = gm
            records.append(rec)

    finally:
        try:
            compose(project, workspace, ["down", "-v", "--remove-orphans"], 180)
        except Exception:
            pass

    final_cost = sum(session_costs.values())
    termination = "SUCCESS" if success else (
        records[-1].get("failure_class")
        if records
        else "NO_ITERATION_COMPLETED"
    )

    summary = {
        **meta,
        "finished_at_utc": now_iso(),
        "success": success,
        "termination_reason": termination,
        "iterations_used": len(records),
        "elapsed_seconds": round(time.perf_counter() - started, 3),
        "reported_cost_usd_final_by_session": round(final_cost, 8),
        "session_costs": session_costs,
        "artifacts": artifacts,
        "iterations": records,
    }
    save_json(run_dir / "builder_summary.json", summary)

    lines = [
        f"# A1 Microservices pilot — {run_id}",
        "",
        f"- Success: **{success}**",
        f"- Model: `{args.model}`",
        f"- Iterations: **{len(records)}**",
        f"- Termination: **{termination}**",
        f"- Startup timeout: **{args.startup_timeout} s**",
        f"- Final CLI-reported session cost: **${round(final_cost, 8)}**",
        f"- Packaged JARs: **{len(artifacts)}**",
        "",
        "| Iter | Turns | Incremental cost* | Tests | Failure |",
        "|---:|---:|---:|---:|---|",
    ]
    for r in records:
        a = r.get("acceptance") or {}
        lines.append(
            f"| {r['iteration']} "
            f"| {r['claude'].get('num_turns')} "
            f"| {r['claude'].get('reported_cost_usd_incremental_derived')} "
            f"| {a.get('passed','-')}/{a.get('total','-')} "
            f"| {r.get('failure_class','-')} |"
        )
    lines += [
        "",
        "*Incremental cost is derived from successive cumulative "
        "`total_cost_usd` values within the same resumed Claude session."
    ]
    (run_dir / "RUN_REPORT.md").write_text(
        "\n".join(lines) + "\n", encoding="utf-8"
    )

    print("\n" + "=" * 72)
    print("A1 MICROSERVICES PILOT")
    print("=" * 72)
    print("Run       :", run_id)
    print("Success   :", success)
    print("Iterations:", len(records))
    print("Cost USD  :", round(final_cost, 8))
    print("JARs      :", len(artifacts))
    print("Summary   :", run_dir / "builder_summary.json")
    print("Report    :", run_dir / "RUN_REPORT.md")
    return 0 if success else 1


if __name__ == "__main__":
    sys.exit(main())

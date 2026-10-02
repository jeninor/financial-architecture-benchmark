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
        for c in iter(lambda: f.read(1024 * 1024), b""):
            h.update(c)
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


def save_json(path, obj):
    Path(path).write_text(
        json.dumps(obj, indent=2, ensure_ascii=False), encoding="utf-8"
    )


def load_json(path):
    return json.loads(Path(path).read_text(encoding="utf-8"))


def port_open(host, port):
    try:
        with socket.create_connection((host, port), timeout=.5):
            return True
    except OSError:
        return False


def wait_http(timeout_seconds):
    deadline = time.time() + timeout_seconds
    while time.time() < deadline:
        try:
            with urllib.request.urlopen(
                "http://localhost:8080/api/quotes/AAPL", timeout=3
            ):
                return True
        except urllib.error.HTTPError as e:
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


def relevant_logs(project, workspace):
    services = set(compose_services(workspace))
    selected = [s for s in APP_SERVICES if s in services]
    return compose(
        project, workspace,
        ["logs", "--no-color", "--tail=500"] + selected,
        120,
    ).stdout


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

    with Path(trace).open("w", encoding="utf-8") as out:
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
        raise RuntimeError("No A3 result event")
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
    for f in findings.get("findings", [])[:30]:
        parts.append(" | ".join(str(x) for x in [
            f.get("agent"),
            f.get("type"),
            f.get("severity"),
            f.get("file") or f.get("target"),
            f.get("symbol") or f.get("id") or f.get("package"),
            f.get("metric"),
            f.get("value"),
        ] if x not in (None, "")))
    return (
        "Locate the microservice source symbols and service relationships most "
        "relevant to these deterministic refactoring findings. Respect service "
        "ownership, gateway routing and separate persistence: "
        + " || ".join(parts)
    )[:9000]


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


def package_services(project, workspace, compose_names, idir):
    artifacts = []
    for service in APP_SERVICES:
        if service not in compose_names:
            continue
        log(f"PACKAGE {service}")
        p = compose(
            project, workspace,
            [
                "run", "--rm", "--no-deps", service,
                "mvn", "-q", "-B", "package", "-DskipTests"
            ],
            1200,
        )
        (idir / f"package_{service}.log").write_text(
            p.stdout, encoding="utf-8"
        )
        if p.returncode:
            raise RuntimeError(
                f"Packaging failed for {service}:\n{p.stdout[-8000:]}"
            )

    for jar in sorted(workspace.rglob("target/*.jar")):
        if jar.name.endswith("-sources.jar") or jar.name.endswith("-javadoc.jar"):
            continue
        if jar.name.endswith(".original"):
            continue
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
        image, "/workspace",
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


def comparison(before_q, after_q, before_s, after_s):
    qkeys = [
        "physical_java_loc", "nonblank_java_loc", "java_files",
        "lizard_nloc", "functions", "cc_total", "cc_average",
        "cc_max", "quality_findings",
    ]
    skeys = [
        "vulnerabilities", "secrets", "misconfigurations", "HIGH", "CRITICAL"
    ]
    result = {"quality": {}, "security": {}}

    for k in qkeys:
        b = before_q["summary"].get(k)
        a = after_q["summary"].get(k)
        result["quality"][k] = {
            "before": b, "after": a,
            "delta": round(a - b, 6)
            if isinstance(a, (int, float)) and isinstance(b, (int, float))
            else None,
        }

    for k in skeys:
        b = before_s["summary"].get(k)
        a = after_s["summary"].get(k)
        result["security"][k] = {
            "before": b, "after": a,
            "delta": a - b
            if isinstance(a, (int, float)) and isinstance(b, (int, float))
            else None,
        }
    return result


def repair_prompt(kind, evidence, graph_context, findings):
    return f"""The previous microservices refactor did not pass the orchestrator gate.

FAILURE KIND:
{kind}

EVIDENCE:
{evidence[-12000:]}

GRAPHIFY CONTEXT:
{graph_context}

ORIGINAL FINDINGS:
{json.dumps(findings.get('findings', []), indent=2, ensure_ascii=False)}

Repair only the regression introduced by the refactor. Preserve public behavior,
the Compose topology, service ownership, separate databases, gateway, discovery
and messaging architecture. Do not run Bash, Docker, Maven or tests.
"""


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--repo-root", type=Path, default=Path("."))
    ap.add_argument("--framework-root", type=Path, default=Path("multiagent-experiment"))
    ap.add_argument("--source-run", required=True)
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
    source = fw / "runs" / args.source_run
    source_workspace = source / "workspace"

    if not source_workspace.exists():
        raise SystemExit(f"Missing source workspace: {source_workspace}")
    if not args.source_run.endswith("_microservices"):
        raise SystemExit("A3 micro runner requires a *_microservices source run")

    findings = load_json(args.findings_json.resolve())
    quality_before = load_json(args.quality_before.resolve())
    security_before = load_json(args.security_before.resolve())

    a3_root = source / "a3"
    workspace = a3_root / "workspace"
    if a3_root.exists():
        raise SystemExit(f"A3 already exists: {a3_root}")
    a3_root.mkdir(parents=True)
    shutil.copytree(source_workspace, workspace)

    total_findings = findings.get("summary", {}).get(
        "total_findings", len(findings.get("findings", []))
    )

    compose_hash = sha256(workspace / "docker-compose.yml")
    services = compose_services(workspace)
    graph_meta = ensure_graphify_image(args.graphify_image)
    runtime_ref = graph_meta["runtime_ref"]

    meta = {
        "schema_version": 1,
        "architecture": "microservices",
        "source_run": args.source_run,
        "started_at_utc": now_iso(),
        "model_requested": args.model,
        "claude_version": run(["claude", "--version"], workspace, 30).stdout.strip(),
        "graphify": graph_meta,
        "graph_budget": args.graph_budget,
        "startup_timeout_seconds": args.startup_timeout,
        "findings_json_sha256": sha256(args.findings_json.resolve()),
        "quality_before_sha256": sha256(args.quality_before.resolve()),
        "security_before_sha256": sha256(args.security_before.resolve()),
        "docker_compose_sha256": compose_hash,
        "compose_services": services,
        "total_findings": total_findings,
    }
    save_json(a3_root / "a3_config.json", meta)

    if total_findings == 0:
        save_json(a3_root / "a3_summary.json", {
            **meta,
            "finished_at_utc": now_iso(),
            "success": True,
            "termination_reason": "SKIPPED_NO_FINDINGS",
            "claude_calls": 0,
        })
        print("A3 skipped: no deterministic findings.")
        return 0

    paths = {
        "prompt": fw / "prompts" / "refactor_microservices.md",
        "architecture": fw / "contracts" / "architecture_microservices.md",
        "api": fw / "contracts" / "public_api_v1.md",
        "requirements": fw / "contracts" / "requirements.md",
    }
    missing = [str(p) for p in paths.values() if not p.exists()]
    if missing:
        raise SystemExit("Missing:\n" + "\n".join(missing))

    if port_open("127.0.0.1", 8080):
        raise SystemExit("Port 8080 already in use")

    graph_dir = a3_root / "graphify_before"
    log("GRAPHIFY extract before A3")
    gm = build_code_graph(workspace, graph_dir, runtime_ref)
    q = findings_query(findings)
    (a3_root / "graph_query.txt").write_text(q, encoding="utf-8")
    ctx = query_graph(graph_dir, runtime_ref, q, args.graph_budget)
    (a3_root / "graph_context.txt").write_text(ctx, encoding="utf-8")

    prompt = (
        paths["prompt"].read_text(encoding="utf-8")
        + "\n\n# ARCHITECTURE\n"
        + paths["architecture"].read_text(encoding="utf-8")
        + "\n\n# PUBLIC API\n"
        + paths["api"].read_text(encoding="utf-8")
        + "\n\n# REQUIREMENTS\n"
        + paths["requirements"].read_text(encoding="utf-8")
        + "\n\n# DETERMINISTIC FINDINGS\n"
        + json.dumps(findings.get("findings", []), indent=2, ensure_ascii=False)
        + "\n\n# GRAPHIFY CONTEXT\n"
        + ctx
    )

    project = f"{args.source_run}-a3".lower().replace("_", "-")
    session = None
    session_cost = 0.0
    calls = []
    success = False
    artifacts = []
    termination = None
    started = time.perf_counter()

    try:
        attempts = 1 + args.max_repair_attempts

        for attempt in range(1, attempts + 1):
            idir = a3_root / f"attempt_{attempt:02d}"
            idir.mkdir()

            result = claude_stream(
                workspace, prompt, args.model,
                args.max_turns_refactor if attempt == 1 else args.max_turns_repair,
                idir / "claude_trace.jsonl",
                resume=session,
            )
            us = usage(result)
            session = us.get("session_id") or session
            curr = float(us.get("total_cost_usd") or 0)
            us["reported_cost_usd_incremental_derived"] = round(
                max(0.0, curr - session_cost), 8
            )
            session_cost = curr
            save_json(idir / "claude_usage.json", us)

            if sha256(workspace / "docker-compose.yml") != compose_hash:
                raise RuntimeError("ARCHITECTURE_VIOLATION: compose changed")
            if set(compose_services(workspace)) != set(services):
                raise RuntimeError("ARCHITECTURE_VIOLATION: service set changed")

            compose(project, workspace, ["down", "-v", "--remove-orphans"], 180)
            up = compose(project, workspace, ["up", "-d", "--build"], 1200)
            (idir / "compose_up.log").write_text(up.stdout, encoding="utf-8")

            if up.returncode:
                logs = relevant_logs(project, workspace)
                calls.append({"attempt": attempt, "claude": us, "gate": "BUILD_OR_START_FAILURE"})
                if attempt >= attempts:
                    termination = "BUILD_OR_START_FAILURE"
                    break
                prompt = repair_prompt(
                    "BUILD_OR_START_FAILURE", up.stdout + "\n" + logs, ctx, findings
                )
                continue

            ready = wait_http(args.startup_timeout)
            logs = relevant_logs(project, workspace)
            (idir / "compose_logs.txt").write_text(logs, encoding="utf-8")

            if not ready:
                calls.append({"attempt": attempt, "claude": us, "gate": "STARTUP_TIMEOUT"})
                if attempt >= attempts:
                    termination = "STARTUP_TIMEOUT"
                    break
                prompt = repair_prompt("STARTUP_TIMEOUT", logs, ctx, findings)
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
                artifacts = package_services(
                    project, workspace, set(services), idir
                )
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
            prompt = repair_prompt(
                "ACCEPTANCE_FAILURE",
                json.dumps(failed, indent=2, ensure_ascii=False),
                ctx,
                findings,
            )

        if not success:
            save_json(a3_root / "a3_summary.json", {
                **meta,
                "finished_at_utc": now_iso(),
                "success": False,
                "termination_reason": termination,
                "elapsed_seconds": round(time.perf_counter() - started, 3),
                "reported_cost_usd_final_session": round(session_cost, 8),
                "calls": calls,
            })
            return 1

        after = a3_root / "analysis_after"
        after.mkdir()

        quality_after = run_a2_after(
            workspace,
            after / "quality_after.json",
            args.lizard_image,
            args.quality_policy.resolve(),
        )

        a4_script = fw / "scripts" / "run_a4_artifact_offline_v2.py"
        a4_stage = a3_root / "a4_after_stage"
        p = run([
            "python3", str(a4_script),
            "--workspace", str(workspace),
            "--quality-json", str(after / "quality_after.json"),
            "--output-dir", str(a4_stage),
            "--trivy-image", args.trivy_image,
            "--trivy-cache", str(args.trivy_cache.resolve()),
            "--security-policy", str(args.security_policy.resolve()),
        ], cwd=fw, timeout=2400)
        (a3_root / "a4_after_console.log").write_text(p.stdout, encoding="utf-8")
        if p.returncode:
            raise RuntimeError("A4 after failed:\n" + p.stdout[-8000:])

        mapping = {
            "security_before.json": "security_after.json",
            "findings.json": "findings_after.json",
            "analysis_manifest.json": "analysis_after_manifest.json",
            "A2_A4_REPORT.md": "A2_A4_AFTER_REPORT.md",
            "trivy_source_raw.json": "trivy_source_raw_after.json",
            "trivy_artifacts_raw.json": "trivy_artifacts_raw_after.json",
        }
        for src_name, dst_name in mapping.items():
            src = a4_stage / src_name
            if src.exists():
                shutil.copy2(src, after / dst_name)

        security_after = load_json(after / "security_after.json")
        comp = comparison(
            quality_before, quality_after, security_before, security_after
        )
        comp["acceptance_after"] = {"passed": 12, "total": 12}
        comp["a3_reported_cost_usd"] = round(session_cost, 8)
        comp["artifacts"] = artifacts
        save_json(a3_root / "before_after.json", comp)

        save_json(a3_root / "a3_summary.json", {
            **meta,
            "finished_at_utc": now_iso(),
            "success": True,
            "termination_reason": "SUCCESS",
            "elapsed_seconds": round(time.perf_counter() - started, 3),
            "reported_cost_usd_final_session": round(session_cost, 8),
            "calls": calls,
            "artifacts": artifacts,
            "comparison_path": str(a3_root / "before_after.json"),
        })

        lines = [
            f"# A3 Microservices report — {args.source_run}",
            "",
            "- Success: **True**",
            f"- Model: `{args.model}`",
            f"- Deterministic findings before A3: **{total_findings}**",
            "- Acceptance after A3: **12/12**",
            f"- Packaged JARs: **{len(artifacts)}**",
            f"- CLI-reported A3 session cost: **${round(session_cost, 8)}**",
            "",
            "## Quality before → after",
            "",
            "| Metric | Before | After | Delta |",
            "|---|---:|---:|---:|",
        ]
        for k, row in comp["quality"].items():
            lines.append(f"| {k} | {row['before']} | {row['after']} | {row['delta']} |")
        lines += [
            "",
            "## Security before → after",
            "",
            "| Metric | Before | After | Delta |",
            "|---|---:|---:|---:|",
        ]
        for k, row in comp["security"].items():
            lines.append(f"| {k} | {row['before']} | {row['after']} | {row['delta']} |")

        (a3_root / "A3_REPORT.md").write_text(
            "\n".join(lines) + "\n", encoding="utf-8"
        )

        print("\n===== A3 MICROSERVICES COMPLETE =====")
        print("Summary:", a3_root / "a3_summary.json")
        print("Compare:", a3_root / "before_after.json")
        print("Report :", a3_root / "A3_REPORT.md")
        return 0

    finally:
        try:
            compose(project, workspace, ["down", "-v", "--remove-orphans"], 180)
        except Exception:
            pass


if __name__ == "__main__":
    sys.exit(main())

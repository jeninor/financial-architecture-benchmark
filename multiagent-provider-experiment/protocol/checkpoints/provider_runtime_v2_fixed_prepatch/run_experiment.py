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
import tempfile
import zipfile
import urllib.error
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

from graphify_runtime_v4 import ensure_graphify_image, build_code_graph, query_graph


_PROVIDER_ROOT = Path(__file__).resolve().parents[1]
if str(_PROVIDER_ROOT) not in sys.path:
    sys.path.insert(0, str(_PROVIDER_ROOT))

from providers.claude_provider import ClaudeProvider
from providers.codex_provider import CodexProvider
from providers.antigravity_provider import AntigravityProvider


ARCH = {
    "monolith": {
        "baseline": "baselines/monolith",
        "architecture_contract": "contracts/architecture_monolith.md",
        "builder_prompt": "prompts/builder.md",
        "refactor_prompt": "prompts/refactor.md",
        "app_services": ["app"],
        "infra_services": ["postgres", "postgres-test"],
        "expected_app_services": ["app"],
        "expected_main_classes": 1,
    },
    "microservices": {
        "baseline": "baselines/microservices",
        "architecture_contract": "contracts/architecture_microservices.md",
        "builder_prompt": "prompts/builder_microservices.md",
        "refactor_prompt": "prompts/refactor_microservices.md",
        "app_services": [
            "discovery-server",
            "api-gateway",
            "user-service",
            "market-service",
            "trade-service",
        ],
        "infra_services": ["rabbitmq", "user-postgres", "trade-postgres"],
        "expected_app_services": [
            "discovery-server",
            "api-gateway",
            "user-service",
            "market-service",
            "trade-service",
        ],
        "expected_main_classes": 5,
    },
}


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


def run(cmd, cwd: Path | None = None, timeout=None, env=None):
    return subprocess.run(
        cmd,
        cwd=str(cwd) if cwd else None,
        env=env,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        timeout=timeout,
    )


def save_json(path: Path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(
        json.dumps(obj, indent=2, ensure_ascii=False),
        encoding="utf-8",
    )


def load_json(path: Path):
    return json.loads(path.read_text(encoding="utf-8"))


def port_open(port: int) -> bool:
    try:
        with socket.create_connection(("127.0.0.1", port), timeout=.5):
            return True
    except OSError:
        return False


def image_info(ref: str) -> dict:
    p = run(["docker", "image", "inspect", ref], timeout=60)
    if p.returncode:
        raise RuntimeError(f"Missing Docker image {ref}\n{p.stdout}")
    row = json.loads(p.stdout)[0]
    return {
        "ref": ref,
        "image_id": row.get("Id"),
        "created": row.get("Created"),
    }


def docker_context() -> str:
    p = run(["docker", "context", "show"], timeout=30)
    if p.returncode:
        raise RuntimeError("Cannot determine Docker context:\n" + p.stdout)
    return p.stdout.strip()


def compose(project: str, workspace: Path, args, timeout=1200):
    return run(
        ["docker", "compose", "-p", project] + list(args),
        cwd=workspace,
        timeout=timeout,
    )


def compose_services(workspace: Path):
    p = run(["docker", "compose", "config", "--services"], cwd=workspace, timeout=60)
    if p.returncode:
        raise RuntimeError(p.stdout)
    return [x.strip() for x in p.stdout.splitlines() if x.strip()]


def compose_config_json(workspace: Path, project: str) -> dict:
    p = run(
        ["docker", "compose", "-p", project, "config", "--format", "json"],
        cwd=workspace,
        timeout=60,
    )
    if p.returncode:
        raise RuntimeError("docker compose config --format json failed:\n" + p.stdout)
    return json.loads(p.stdout)


def volume_policy(workspace: Path, project: str) -> dict:
    cfg = compose_config_json(workspace, project)
    maven_logical = set()

    for _, svc in (cfg.get("services") or {}).items():
        for mount in svc.get("volumes", []) or []:
            if (
                mount.get("type") == "volume"
                and mount.get("target") == "/root/.m2"
                and mount.get("source")
            ):
                maven_logical.add(mount["source"])

    actual = {}
    for logical, vcfg in (cfg.get("volumes") or {}).items():
        vcfg = vcfg or {}
        name = vcfg.get("name") or f"{project}_{logical}"
        actual[logical] = {
            "name": name,
            "external": bool(vcfg.get("external")),
            "preserve": logical in maven_logical,
        }

    return {
        "maven_logical_volumes": sorted(maven_logical),
        "volumes": actual,
    }


def reset_state(project: str, workspace: Path, policy: dict):
    # Stop containers first, then remove only non-Maven, non-external named
    # volumes. This gives clean DB/message-broker state while preserving the
    # dependency cache across attempts/runs.
    compose(project, workspace, ["down", "--remove-orphans"], 180)

    removed, preserved = [], []
    for logical, meta in policy["volumes"].items():
        name = meta["name"]
        if meta["external"] or meta["preserve"]:
            preserved.append(name)
            continue
        p = run(["docker", "volume", "rm", "-f", name], timeout=60)
        # Missing volume is fine.
        if p.returncode == 0:
            removed.append(name)

    return {"removed": removed, "preserved": preserved}


def all_service_logs(project: str, workspace: Path, tail: int = 800) -> str:
    present = compose_services(workspace)
    if not present:
        return ""
    args = ["logs", "--no-color", f"--tail={tail}"] + present
    return compose(project, workspace, args, 240).stdout


def relevant_logs(project: str, workspace: Path, app_services: list[str]) -> str:
    # v3 intentionally captures ALL Compose services, including infrastructure.
    # The app_services parameter is retained for call-site compatibility.
    return all_service_logs(project, workspace)


def inspect_service_state(project: str, workspace: Path, service: str) -> dict:
    p = compose(project, workspace, ["ps", "-q", service], 60)
    ids = [x.strip() for x in p.stdout.splitlines() if x.strip()]
    if not ids:
        return {
            "service": service,
            "container_id": None,
            "status": "missing",
            "health": None,
            "exit_code": None,
        }

    cid = ids[0]
    q = run(["docker", "inspect", cid], timeout=60)
    if q.returncode:
        return {
            "service": service,
            "container_id": cid,
            "status": "inspect-error",
            "health": None,
            "exit_code": None,
            "inspect_error": q.stdout[-4000:],
        }

    row = json.loads(q.stdout)[0]
    state = row.get("State") or {}
    health_obj = state.get("Health") or {}
    health_log = []
    for item in (health_obj.get("Log") or [])[-10:]:
        health_log.append({
            "start": item.get("Start"),
            "end": item.get("End"),
            "exit_code": item.get("ExitCode"),
            "output": item.get("Output"),
        })

    return {
        "service": service,
        "container_id": cid,
        "container_name": (row.get("Name") or "").lstrip("/"),
        "status": state.get("Status"),
        "running": state.get("Running"),
        "exit_code": state.get("ExitCode"),
        "error": state.get("Error"),
        "health": health_obj.get("Status"),
        "health_log": health_log,
    }


def compose_state_snapshot(project: str, workspace: Path) -> dict:
    return {
        service: inspect_service_state(project, workspace, service)
        for service in compose_services(workspace)
    }


def infra_states_ready(states: dict, infra_services: list[str]) -> bool:
    for service in infra_services:
        s = states.get(service) or {}
        if s.get("status") != "running":
            return False
        health = s.get("health")
        if health not in (None, "healthy"):
            return False
    return True


def infra_has_terminal_failure(states: dict, infra_services: list[str]) -> bool:
    for service in infra_services:
        s = states.get(service) or {}
        if s.get("status") in ("exited", "dead"):
            return True
    return False


def wait_infrastructure(
    project: str,
    workspace: Path,
    infra_services: list[str],
    timeout_seconds: int,
    stability_seconds: int,
) -> dict:
    if not infra_services:
        return {"success": True, "states": {}, "elapsed_seconds": 0.0}

    started = time.perf_counter()
    deadline = time.time() + timeout_seconds
    stable_since = None
    last = {}

    while time.time() < deadline:
        last = compose_state_snapshot(project, workspace)
        if infra_states_ready(last, infra_services):
            if stable_since is None:
                stable_since = time.time()
            if time.time() - stable_since >= stability_seconds:
                return {
                    "success": True,
                    "states": last,
                    "elapsed_seconds": round(time.perf_counter() - started, 3),
                }
        else:
            stable_since = None

        if infra_has_terminal_failure(last, infra_services):
            return {
                "success": False,
                "terminal_failure": True,
                "states": last,
                "elapsed_seconds": round(time.perf_counter() - started, 3),
            }
        time.sleep(2)

    return {
        "success": False,
        "terminal_failure": False,
        "states": last,
        "elapsed_seconds": round(time.perf_counter() - started, 3),
    }


def capture_runtime_evidence(
    project: str,
    workspace: Path,
    outdir: Path,
) -> dict:
    outdir.mkdir(parents=True, exist_ok=True)
    ps = compose(project, workspace, ["ps", "-a"], 120).stdout
    logs = all_service_logs(project, workspace)
    states = compose_state_snapshot(project, workspace)
    (outdir / "compose_ps.txt").write_text(ps, encoding="utf-8")
    (outdir / "compose_logs_all.txt").write_text(logs, encoding="utf-8")
    save_json(outdir / "compose_states.json", states)
    evidence = (
        "COMPOSE PS:\n" + ps[-8000:]
        + "\n\nCONTAINER STATES:\n"
        + json.dumps(states, indent=2, ensure_ascii=False)[-16000:]
        + "\n\nALL SERVICE LOGS:\n" + logs[-24000:]
    )
    return {"ps": ps, "logs": logs, "states": states, "evidence": evidence}


def prepare_infrastructure(
    project: str,
    workspace: Path,
    vpolicy: dict,
    infra_services: list[str],
    limits: dict,
    outdir: Path,
) -> dict:
    history = []
    max_attempts = int(limits.get("infra_preflight_max_attempts", 3))
    timeout = int(limits.get("infra_preflight_timeout_seconds", 120))
    stability = int(limits.get("infra_stability_seconds", 8))
    settle = int(limits.get("docker_settle_seconds", 3))

    for attempt in range(1, max_attempts + 1):
        adir = outdir / f"attempt_{attempt:02d}"
        adir.mkdir(parents=True, exist_ok=True)
        reset = reset_state(project, workspace, vpolicy)
        save_json(adir / "state_reset.json", reset)
        if settle > 0:
            time.sleep(settle)

        up = compose(project, workspace, ["up", "-d"] + list(infra_services), 600)
        (adir / "infra_up.log").write_text(up.stdout, encoding="utf-8")
        waited = wait_infrastructure(
            project, workspace, infra_services, timeout, stability
        )
        ev = capture_runtime_evidence(project, workspace, adir / "evidence")
        row = {
            "attempt": attempt,
            "compose_up_returncode": up.returncode,
            "wait": waited,
            "evidence_dir": str(adir / "evidence"),
        }
        history.append(row)
        if waited.get("success"):
            return {"success": True, "attempts": history, "states": waited.get("states", {})}

    return {"success": False, "attempts": history, "states": history[-1]["wait"].get("states", {}) if history else {}}


def route_reachable(url: str) -> bool:
    try:
        with urllib.request.urlopen(url, timeout=3) as resp:
            return True
    except urllib.error.HTTPError as e:
        # 502/503 mean the gateway/downstream path is not ready. Any other
        # HTTP response proves the route is reachable; correctness belongs to
        # the immutable acceptance suite, not the readiness gate.
        return e.code not in (502, 503)
    except Exception:
        return False


def wait_application_ready(
    project: str,
    workspace: Path,
    infra_services: list[str],
    timeout_seconds: int,
    stable_checks: int,
    poll_seconds: int,
) -> dict:
    urls = [
        "http://localhost:8080/api/quotes/AAPL",
        "http://localhost:8080/api/users/00000000-0000-0000-0000-000000000001/portfolio",
        "http://localhost:8080/api/users/00000000-0000-0000-0000-000000000001/trades",
    ]
    deadline = time.time() + timeout_seconds
    streak = 0
    last_checks = {}

    while time.time() < deadline:
        states = compose_state_snapshot(project, workspace)
        if not infra_states_ready(states, infra_services):
            return {
                "ready": False,
                "infra_failure": True,
                "states": states,
                "route_checks": last_checks,
            }

        last_checks = {url: route_reachable(url) for url in urls}
        if all(last_checks.values()):
            streak += 1
            if streak >= stable_checks:
                return {
                    "ready": True,
                    "infra_failure": False,
                    "states": states,
                    "route_checks": last_checks,
                    "stable_checks": streak,
                }
        else:
            streak = 0
        time.sleep(poll_seconds)

    return {
        "ready": False,
        "infra_failure": False,
        "states": compose_state_snapshot(project, workspace),
        "route_checks": last_checks,
        "stable_checks": streak,
    }


def start_runtime_with_infra_recovery(
    project: str,
    workspace: Path,
    vpolicy: dict,
    infra_services: list[str],
    limits: dict,
    outdir: Path,
) -> dict:
    # Cycle 1 uses the infrastructure already verified BEFORE the coding provider call.
    # Extra cycles rebuild only runtime state using the SAME generated code and
    # therefore consume no additional coding provider iteration/token budget.
    recovery_attempts = int(limits.get("infra_runtime_recovery_attempts", 2))
    cycles = 1 + recovery_attempts
    history = []

    for cycle in range(1, cycles + 1):
        cdir = outdir / f"runtime_cycle_{cycle:02d}"
        cdir.mkdir(parents=True, exist_ok=True)

        if cycle > 1:
            pre = prepare_infrastructure(
                project, workspace, vpolicy, infra_services, limits,
                cdir / "infra_recovery",
            )
            save_json(cdir / "infra_recovery.json", pre)
            if not pre.get("success"):
                history.append({"cycle": cycle, "status": "INFRASTRUCTURE_FAILURE", "preflight": pre})
                continue

        up = compose(project, workspace, ["up", "-d", "--build"], 1500)
        (cdir / "compose_up.log").write_text(up.stdout, encoding="utf-8")

        if up.returncode:
            ev = capture_runtime_evidence(project, workspace, cdir / "evidence")
            infra_ok = infra_states_ready(ev["states"], infra_services)
            status = "BUILD_OR_START_FAILURE" if infra_ok else "INFRASTRUCTURE_FAILURE"
            history.append({
                "cycle": cycle,
                "status": status,
                "compose_up_returncode": up.returncode,
                "evidence_dir": str(cdir / "evidence"),
            })
            if status == "INFRASTRUCTURE_FAILURE" and cycle < cycles:
                continue
            return {"success": False, "status": status, "history": history, "evidence": ev["evidence"]}

        ready = wait_application_ready(
            project,
            workspace,
            infra_services,
            int(limits["startup_timeout_seconds"]),
            int(limits.get("application_readiness_stable_checks", 3)),
            int(limits.get("application_readiness_poll_seconds", 2)),
        )
        ev = capture_runtime_evidence(project, workspace, cdir / "evidence")
        save_json(cdir / "readiness.json", ready)

        if ready.get("ready"):
            history.append({"cycle": cycle, "status": "READY", "evidence_dir": str(cdir / "evidence")})
            return {"success": True, "status": "READY", "history": history, "evidence": ev["evidence"]}

        if ready.get("infra_failure"):
            history.append({"cycle": cycle, "status": "INFRASTRUCTURE_FAILURE", "evidence_dir": str(cdir / "evidence")})
            if cycle < cycles:
                continue
            return {"success": False, "status": "INFRASTRUCTURE_FAILURE", "history": history, "evidence": ev["evidence"]}

        history.append({"cycle": cycle, "status": "STARTUP_TIMEOUT", "evidence_dir": str(cdir / "evidence")})
        return {"success": False, "status": "STARTUP_TIMEOUT", "history": history, "evidence": ev["evidence"]}

    return {"success": False, "status": "INFRASTRUCTURE_FAILURE", "history": history, "evidence": "Infrastructure could not be stabilized."}


def wait_http(timeout_seconds: int) -> bool:
    deadline = time.time() + timeout_seconds
    while time.time() < deadline:
        try:
            with urllib.request.urlopen(
                "http://localhost:8080/api/quotes/AAPL", timeout=3
            ) as resp:
                if resp.status < 500:
                    return True
        except urllib.error.HTTPError as e:
            # For the known-valid quote endpoint, 502/503 means gateway/downstream
            # is still not usable. Other HTTP responses prove the app is reachable.
            if e.code not in (502, 503):
                return True
        except Exception:
            pass
        time.sleep(2)
    return False


def run_acceptance(fw: Path, run_id: str, outfile: Path):
    p = run(
        [
            "python3",
            str(fw / "acceptance-tests" / "run_acceptance.py"),
            "--base-url",
            "http://localhost:8080",
            "--run-id",
            run_id,
            "--output",
            str(outfile),
        ],
        cwd=fw,
        timeout=300,
    )
    data = load_json(outfile) if outfile.exists() else {}
    return p.returncode, data, p.stdout



PROVIDER_CODES = {
    "claude": "H",
    "codex": "C",
    "antigravity": "A",
}


def make_provider(name: str):
    if name == "claude":
        return ClaudeProvider()
    if name == "codex":
        return CodexProvider()
    if name == "antigravity":
        return AntigravityProvider()
    raise RuntimeError(f"Unknown provider: {name}")


def provider_cli_versions(protocol: dict) -> dict[str, str]:
    versions = {}

    for name, spec in protocol["providers"].items():
        provider = make_provider(name)
        version = provider.version()
        expected = spec.get("expected_cli_version")

        if expected and version != expected:
            raise RuntimeError(
                f"Provider CLI drift for {name}: "
                f"expected={expected!r} current={version!r}"
            )

        versions[name] = version

    return versions


def provider_policy_text(stage: str) -> str:
    if stage == "a3":
        return """
# PROVIDER EXECUTION POLICY — OVERRIDES EARLIER TOOL WORDING
Work only inside the current workspace.
Never execute any Docker command.
Do not access external network resources.
Do not use Web/browser tools, MCP tools, plugins, marketplaces or subagents.
Local shell commands are permitted only for inspecting or editing files.
Do not run Maven, Gradle, builds, tests or application processes.
The orchestrator exclusively owns build, runtime, acceptance and measurement.
"""

    return """
# PROVIDER EXECUTION POLICY — OVERRIDES EARLIER TOOL WORDING
Work only inside the current workspace.
Never execute any Docker command.
Do not access external network resources.
Do not use Web/browser tools, MCP tools, plugins, marketplaces or subagents.
Local shell commands and Maven may be used only for local inspection/build work.
Do not inspect parent directories, historical implementations, runner code or
external acceptance-test implementation.
The orchestrator exclusively owns Docker lifecycle and external acceptance.
"""


def provider_invocation_summary(rows: list[dict]) -> dict:
    return {
        "invocations": len(rows),
        "elapsed_seconds": round(
            sum(float(x.get("elapsed_seconds") or 0.0) for x in rows),
            3,
        ),
        "tool_calls": sum(int(x.get("tool_calls") or 0) for x in rows),
        "failed_tool_calls": sum(
            int(x.get("failed_tool_calls") or 0) for x in rows
        ),
        "policy_violation_invocations": sum(
            1 for x in rows if x.get("policy_violation")
        ),
        "reported_cost_usd": None,
        "native_usage_by_invocation": [
            x.get("native_usage") for x in rows
        ],
    }


def failed_test_question(acc: dict, architecture: str) -> str:
    failed = [t for t in acc.get("tests", []) if not t.get("passed")]
    parts = []
    for t in failed:
        parts.append(f"{t.get('id')} {t.get('name')}: {t.get('message')}")

    prefix = (
        "Locate the Java classes, methods and structural relationships most "
        "relevant to these failed public acceptance behaviors."
    )
    if architecture == "microservices":
        prefix += (
            " Include gateway routing, Feign/service calls, discovery and state "
            "ownership when relevant."
        )
    return (prefix + " " + " | ".join(parts))[:9000]


def failure_question(kind: str, evidence: str, architecture: str) -> str:
    extra = ""
    if architecture == "microservices":
        extra = (
            " Include service startup dependencies, gateway routing, Eureka/Feign "
            "relationships and service-owned persistence where relevant."
        )
    return (
        "Locate the source files, configuration, symbols and structural "
        f"relationships most relevant to this {kind} failure.{extra}\n"
        + evidence[-10000:]
    )[:12000]


def graph_context_after_failure(
    workspace: Path,
    idir: Path,
    runtime_ref: str,
    question: str,
    budget: int,
):
    graph_dir = idir / "graphify_after_failure"
    log("GRAPHIFY extract failing workspace")
    gm = build_code_graph(workspace, graph_dir, runtime_ref)
    save_json(idir / "graphify_after_failure_metrics.json", gm)
    (idir / "graph_query.txt").write_text(question, encoding="utf-8")
    log(f"GRAPHIFY query budget={budget}")
    ctx = query_graph(graph_dir, runtime_ref, question, budget)
    (idir / "graph_context.txt").write_text(ctx, encoding="utf-8")
    return gm, ctx


def builder_feedback(kind: str, evidence: str, graph_context: str) -> str:
    return f"""The orchestrator detected a failure after your previous implementation attempt.

FAILURE KIND:
{kind}

EVIDENCE:
{evidence[-14000:]}

GRAPHIFY RETRIEVED CONTEXT:
{graph_context}

Repair only the implementation/configuration needed for this failure.
Preserve the frozen architecture and public API.
Do NOT run docker compose up/down/start/stop/restart.
The orchestrator owns container lifecycle and the external acceptance gate.
Do not access files outside this workspace.
"""


def findings_query(findings: dict, architecture: str) -> str:
    parts = []
    for f in findings.get("findings", [])[:30]:
        parts.append(
            " | ".join(
                str(x)
                for x in [
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

    prefix = (
        "Locate the source symbols and structural relationships most relevant "
        "to these deterministic refactoring findings."
    )
    if architecture == "microservices":
        prefix += (
            " Respect service ownership, gateway routing and separate persistence."
        )
    return (prefix + " " + " || ".join(parts))[:10000]


def refactor_repair_prompt(
    kind: str, evidence: str, graph_context: str, findings: dict
) -> str:
    return f"""The previous refactor did not pass the orchestrator gate.

FAILURE KIND:
{kind}

EVIDENCE:
{evidence[-12000:]}

GRAPHIFY CONTEXT:
{graph_context}

ORIGINAL DETERMINISTIC FINDINGS:
{json.dumps(findings.get('findings', []), indent=2, ensure_ascii=False)}

Repair only the regression introduced by the refactor.
Preserve public behavior and the frozen architecture.
Do not run Bash, Docker, Maven or tests.
"""


def package_services(
    project: str,
    workspace: Path,
    app_services: list[str],
    compose_names: set[str],
    out_dir: Path,
):
    artifacts = []

    for service in app_services:
        if service not in compose_names:
            continue
        log(f"PACKAGE {service}")
        p = compose(
            project,
            workspace,
            [
                "run",
                "--rm",
                "--no-deps",
                service,
                "mvn",
                "-q",
                "-B",
                "package",
                "-DskipTests",
            ],
            1200,
        )
        (out_dir / f"package_{service}.log").write_text(
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
        artifacts.append(
            {
                "path": str(jar.relative_to(workspace)),
                "bytes": jar.stat().st_size,
                "sha256": sha256(jar),
            }
        )

    save_json(out_dir / "artifacts.json", artifacts)
    return artifacts


def quality_scan(
    workspace: Path,
    output_json: Path,
    lizard_image: str,
    quality_policy: Path,
):
    pol = load_json(quality_policy)["thresholds"]
    p = run(
        [
            "docker",
            "run",
            "--rm",
            "-v",
            f"{workspace.resolve()}:/workspace:ro",
            lizard_image,
            "/workspace",
            "--max-function-ccn",
            str(pol["max_function_ccn"]),
            "--max-function-nloc",
            str(pol["max_function_nloc"]),
            "--max-file-nloc",
            str(pol["max_file_nloc"]),
            "--max-parameters",
            str(pol["max_parameters"]),
        ],
        timeout=600,
    )
    if p.returncode:
        raise RuntimeError("A2 failed:\n" + p.stdout[-8000:])
    data = json.loads(p.stdout)
    save_json(output_json, data)
    return data


def security_scan(
    fw: Path,
    workspace: Path,
    quality_json: Path,
    output_dir: Path,
    trivy_image: str,
    trivy_cache: Path,
    security_policy: Path,
):
    script = fw / "scripts" / "run_a4_artifact_offline_v2.py"
    if not script.exists():
        raise RuntimeError(f"Missing offline A4 runner: {script}")

    p = run(
        [
            "python3",
            str(script),
            "--workspace",
            str(workspace),
            "--quality-json",
            str(quality_json),
            "--output-dir",
            str(output_dir),
            "--trivy-image",
            trivy_image,
            "--trivy-cache",
            str(trivy_cache),
            "--security-policy",
            str(security_policy),
        ],
        cwd=fw,
        timeout=2400,
    )
    (output_dir.parent / f"{output_dir.name}_console.log").write_text(
        p.stdout, encoding="utf-8"
    )
    if p.returncode:
        raise RuntimeError("A4 failed:\n" + p.stdout[-10000:])
    return load_json(output_dir / "security_before.json"), load_json(
        output_dir / "findings.json"
    )


def compare_metrics(before_q, after_q, before_s, after_s):
    qkeys = [
        "physical_java_loc",
        "nonblank_java_loc",
        "java_files",
        "lizard_nloc",
        "functions",
        "cc_total",
        "cc_average",
        "cc_max",
        "quality_findings",
    ]
    skeys = [
        "vulnerabilities",
        "secrets",
        "misconfigurations",
        "HIGH",
        "CRITICAL",
    ]

    out = {"quality": {}, "security": {}}
    for k in qkeys:
        b = before_q["summary"].get(k)
        a = after_q["summary"].get(k)
        delta = (
            round(a - b, 6)
            if isinstance(a, (int, float)) and isinstance(b, (int, float))
            else None
        )
        out["quality"][k] = {"before": b, "after": a, "delta": delta}

    for k in skeys:
        b = before_s["summary"].get(k)
        a = after_s["summary"].get(k)
        delta = (
            a - b
            if isinstance(a, (int, float)) and isinstance(b, (int, float))
            else None
        )
        out["security"][k] = {"before": b, "after": a, "delta": delta}
    return out


def baseline_check(fw: Path, architecture: str) -> dict:
    cfg = ARCH[architecture]
    baseline = fw / cfg["baseline"]
    if not baseline.exists():
        raise RuntimeError(f"Missing baseline: {baseline}")
    if not (baseline / "docker-compose.yml").exists():
        raise RuntimeError(f"Missing baseline docker-compose.yml: {baseline}")

    services = compose_services(baseline)
    missing_services = sorted(set(cfg["expected_app_services"]) - set(services))
    if missing_services:
        raise RuntimeError(
            f"{architecture} baseline missing services: {missing_services}"
        )

    mains = sorted(baseline.glob("*/src/main/java/**/*Application.java"))
    # Monolith is nested under financial-monolith; glob above still catches it.
    if architecture == "monolith":
        mains = sorted(baseline.rglob("*Application.java"))

    business_java = [
        p
        for p in baseline.rglob("*.java")
        if "/src/main/java/" in p.as_posix()
        and not p.name.endswith("Application.java")
        and "/target/" not in p.as_posix()
    ]

    if len(mains) != cfg["expected_main_classes"]:
        raise RuntimeError(
            f"{architecture} baseline expected {cfg['expected_main_classes']} "
            f"main classes, found {len(mains)}"
        )
    if business_java:
        raise RuntimeError(
            f"{architecture} baseline contains business Java: "
            + ", ".join(str(p.relative_to(baseline)) for p in business_java[:10])
        )

    return {
        "baseline": str(baseline),
        "compose_services": services,
        "main_classes": [str(p.relative_to(baseline)) for p in mains],
        "business_java_files": 0,
        "docker_compose_sha256": sha256(baseline / "docker-compose.yml"),
    }


def ensure_tool_images(
    repo: Path,
    fw: Path,
    protocol: dict,
    auto_prepare: bool,
) -> dict:
    # Graphify: use existing robust context-aware resolver; if absent, invoke
    # the existing shell builder used in the pilots.
    graph_ref = protocol["graphify"]["image"]
    try:
        graph = ensure_graphify_image(graph_ref)
    except Exception:
        if not auto_prepare:
            raise
        builder = fw / "scripts" / "build_graphify_image.sh"
        if not builder.exists():
            raise RuntimeError(f"Missing Graphify build helper: {builder}")
        log("Graphify missing -> invoking build_graphify_image.sh")
        env = dict(os.environ)
        env["GRAPHIFY_IMAGE"] = graph_ref
        env["GRAPHIFY_VERSION"] = graph_ref.split(":")[-1]
        p = run([str(builder)], cwd=repo, timeout=1800, env=env)
        print(p.stdout, flush=True)
        if p.returncode:
            raise RuntimeError("Graphify build helper failed")
        graph = ensure_graphify_image(graph_ref)

    lizard_ref = protocol["quality"]["image"]
    try:
        lizard = image_info(lizard_ref)
    except Exception:
        if not auto_prepare:
            raise
        dockerfile_dir = fw / "docker" / "quality"
        log("Lizard missing -> building Docker image automatically")
        p = run(
            ["docker", "build", "-t", lizard_ref, str(dockerfile_dir)],
            cwd=repo,
            timeout=1800,
        )
        print(p.stdout, flush=True)
        if p.returncode:
            raise RuntimeError("Lizard Docker build failed")
        lizard = image_info(lizard_ref)

    trivy_ref = protocol["security"]["image"]
    try:
        trivy = image_info(trivy_ref)
    except Exception:
        if not auto_prepare:
            raise
        log("Trivy missing -> pulling Docker image automatically")
        p = run(["docker", "pull", trivy_ref], cwd=repo, timeout=1800)
        print(p.stdout, flush=True)
        if p.returncode:
            raise RuntimeError("Trivy pull failed")
        trivy = image_info(trivy_ref)

    return {"graphify": graph, "lizard": lizard, "trivy": trivy}


def required_trivy_cache_files(protocol: dict) -> list[str]:
    return protocol["security"].get(
        "required_cache_files",
        [
            "db/trivy.db",
            "db/metadata.json",
            "java-db/trivy-java.db",
            "java-db/metadata.json",
            "policy/metadata.json",
        ],
    )


def validate_trivy_cache_files(cache: Path, protocol: dict):
    missing = [
        rel for rel in required_trivy_cache_files(protocol)
        if not (cache / rel).is_file()
    ]
    if missing:
        raise RuntimeError(
            "Incomplete Trivy cache; required files are missing:\n  - "
            + "\n  - ".join(missing)
        )


def validate_cache_against_manifest(cache: Path, manifest: Path) -> dict:
    data = load_json(manifest)
    rows = data.get("files") or []
    if not rows:
        raise RuntimeError(f"Empty Trivy cache manifest: {manifest}")

    expected = {row["path"]: row for row in rows}
    actual_paths = {
        str(p.relative_to(cache))
        for p in cache.rglob("*")
        if p.is_file()
    }
    expected_paths = set(expected)
    if actual_paths != expected_paths:
        missing = sorted(expected_paths - actual_paths)
        extra = sorted(actual_paths - expected_paths)
        raise RuntimeError(
            "Trivy cache file-set drift detected. "
            f"Missing={missing[:20]} Extra={extra[:20]}"
        )

    checked = 0
    for rel in sorted(expected):
        p = cache / rel
        row = expected[rel]
        size = p.stat().st_size
        if size != row.get("bytes"):
            raise RuntimeError(
                f"Trivy cache size drift for {rel}: "
                f"locked={row.get('bytes')} current={size}"
            )
        digest = sha256(p)
        if digest != row.get("sha256"):
            raise RuntimeError(
                f"Trivy cache SHA256 drift for {rel}: "
                f"locked={row.get('sha256')} current={digest}"
            )
        checked += 1

    return {"files_verified": checked}


def trivy_offline_smoke(repo: Path, protocol: dict) -> dict:
    cache = repo / protocol["security"]["cache"]
    image = protocol["security"]["image"]
    uid, gid = str(os.getuid()), str(os.getgid())

    # Docker Desktop may not share the host /tmp directory.  Keep the
    # smoke-test workspace inside the repository, which is already mounted
    # successfully by the experiment itself.
    smoke_parent = repo / ".experiment-runtime"
    smoke_parent.mkdir(parents=True, exist_ok=True)

    with tempfile.TemporaryDirectory(
        prefix="trivy-offline-smoke-",
        dir=smoke_parent,
    ) as td:
        td = Path(td)
        jar = td / "smoke.jar"
        with zipfile.ZipFile(jar, "w", zipfile.ZIP_DEFLATED) as z:
            z.writestr("META-INF/MANIFEST.MF", "Manifest-Version: 1.0\n")
        (td / "Dockerfile").write_text("FROM scratch\n", encoding="utf-8")

        common = [
            "docker", "run", "--rm",
            "--network", "none",
            "--user", f"{uid}:{gid}",
            "-e", "HOME=/tmp",
            "-v", f"{td.resolve()}:/smoke:ro",
            "-v", f"{cache.resolve()}:/cache:ro",
            image, "fs",
            "--cache-dir", "/cache",
            "--format", "json",
            "--quiet",
            "--skip-db-update",
            "--skip-java-db-update",
            "--skip-check-update",
            "--skip-version-check",
            "--offline-scan",
        ]

        vuln = run(
            common + ["--scanners", "vuln", "/smoke/smoke.jar"],
            cwd=repo,
            timeout=300,
        )
        if vuln.returncode:
            raise RuntimeError(
                "Trivy offline vulnerability smoke failed:\n" + vuln.stdout[-8000:]
            )

        source = run(
            common + ["--scanners", "secret,misconfig", "/smoke"],
            cwd=repo,
            timeout=300,
        )
        if source.returncode:
            raise RuntimeError(
                "Trivy offline secret/misconfig smoke failed:\n"
                + source.stdout[-8000:]
            )

    return {
        "vulnerability_smoke": "PASS",
        "secret_misconfig_smoke": "PASS",
        "network": "none",
        "offline_scan": True,
    }


def cache_manifest_info(fw: Path, protocol: dict, verify_contents: bool = True) -> dict:
    manifest_rel = protocol["security"]["cache_manifest"]
    repo = fw.parent
    manifest = repo / manifest_rel
    cache = repo / protocol["security"]["cache"]

    if not manifest.exists():
        raise RuntimeError(
            f"Missing frozen Trivy cache manifest: {manifest}\n"
            "Run prepare-only with --initialize-security-cache once before "
            "official replicates."
        )
    if not cache.exists():
        raise RuntimeError(f"Missing Trivy cache directory: {cache}")

    validate_trivy_cache_files(cache, protocol)
    verified = (
        validate_cache_against_manifest(cache, manifest)
        if verify_contents else {"files_verified": None}
    )

    return {
        "manifest_path": str(manifest),
        "manifest_sha256": sha256(manifest),
        "cache_path": str(cache),
        **verified,
    }

def initialize_security_cache(repo: Path, fw: Path, protocol: dict):
    prep = fw / "scripts" / "prepare_a2_a4_tools.sh"
    freeze = fw / "scripts" / "freeze_trivy_cache_manifest.py"
    if not freeze.exists():
        raise RuntimeError("Missing freeze_trivy_cache_manifest.py")

    cache = repo / protocol["security"]["cache"]
    manifest = repo / protocol["security"]["cache_manifest"]

    existing_files = [
        x for x in cache.rglob("*")
        if x.is_file()
    ] if cache.exists() else []

    missing_required = [
        rel for rel in required_trivy_cache_files(protocol)
        if not (cache / rel).is_file()
    ]

    if existing_files and not missing_required:
        log(
            "Complete Trivy cache detected -> freezing current snapshot "
            "without refreshing databases"
        )
    else:
        if not prep.exists():
            raise RuntimeError("Missing prepare_a2_a4_tools.sh")
        if missing_required:
            log(
                "Trivy cache is incomplete -> invoking prepare_a2_a4_tools.sh "
                "before freezing the snapshot"
            )
        else:
            log(
                "Trivy cache is empty -> invoking prepare_a2_a4_tools.sh once "
                "before freezing the snapshot"
            )
        p = run([str(prep), str(repo)], cwd=repo, timeout=3600)
        print(p.stdout, flush=True)
        if p.returncode:
            raise RuntimeError("prepare_a2_a4_tools.sh failed")

    validate_trivy_cache_files(cache, protocol)

    p = run(
        [
            "python3",
            str(freeze),
            "--cache",
            str(cache),
            "--output",
            str(manifest),
        ],
        cwd=repo,
        timeout=600,
    )
    print(p.stdout, flush=True)
    if p.returncode:
        raise RuntimeError("freeze_trivy_cache_manifest.py failed")


def protocol_paths(repo: Path, fw: Path, protocol: dict, architecture: str):
    cfg = ARCH[architecture]
    return {
        "baseline": fw / cfg["baseline"],
        "architecture_contract": fw / cfg["architecture_contract"],
        "builder_prompt": fw / cfg["builder_prompt"],
        "refactor_prompt": fw / cfg["refactor_prompt"],
        "public_api": fw / "contracts" / "public_api_v1.md",
        "requirements": fw / "contracts" / "requirements.md",
        "acceptance_runner": fw / "acceptance-tests" / "run_acceptance.py",
        "acceptance_contract": fw / "acceptance-tests" / "acceptance_contract.json",
        "quality_policy": repo / protocol["quality"]["policy"],
        "security_policy": repo / protocol["security"]["policy"],
        "trivy_cache": repo / protocol["security"]["cache"],
        "trivy_cache_manifest": repo / protocol["security"]["cache_manifest"],
    }



def build_environment_lock(
    repo: Path,
    fw: Path,
    protocol: dict,
    tools: dict,
) -> dict:

    unfrozen = [
        name
        for name, spec in protocol["providers"].items()
        if not spec.get("model_requested")
    ]

    if unfrozen:
        raise RuntimeError(
            "Cannot write official environment lock while provider models "
            "are not explicit: " + ", ".join(unfrozen)
        )

    versions = provider_cli_versions(protocol)

    lock = {
        "schema_version": 3,
        "created_at_utc": now_iso(),
        "protocol_sha256": sha256(repo / protocol["protocol_file"]),
        "docker_context": docker_context(),

        "providers": {
            name: {
                "cli_version": versions[name],
                "model_requested": spec["model_requested"],
                "model_verification": spec.get("model_verification"),
            }
            for name, spec in protocol["providers"].items()
        },

        "images": {
            "graphify": tools["graphify"]["image_id"],
            "lizard": tools["lizard"]["image_id"],
            "trivy": tools["trivy"]["image_id"],
        },

        "trivy_cache_manifest_sha256": sha256(
            repo / protocol["security"]["cache_manifest"]
        ),

        "common_hashes": {},
        "architectures": {},
    }

    common = {
        "official_runner":
            fw / "scripts" / "run_experiment.py",

        "provider_base":
            fw / "providers" / "base.py",

        "provider_bwrap":
            fw / "providers" / "bwrap_sandbox.py",

        "provider_claude":
            fw / "providers" / "claude_provider.py",

        "provider_codex":
            fw / "providers" / "codex_provider.py",

        "provider_antigravity":
            fw / "providers" / "antigravity_provider.py",

        "a4_runner":
            fw / "scripts" / "run_a4_artifact_offline_v2.py",

        "graphify_runtime":
            fw / "scripts" / "graphify_runtime_v4.py",

        "public_api":
            fw / "contracts" / "public_api_v1.md",

        "requirements":
            fw / "contracts" / "requirements.md",

        "acceptance_runner":
            fw / "acceptance-tests" / "run_acceptance.py",

        "acceptance_contract":
            fw / "acceptance-tests" / "acceptance_contract.json",

        "quality_policy":
            repo / protocol["quality"]["policy"],

        "security_policy":
            repo / protocol["security"]["policy"],
    }

    for name, path in common.items():
        if not path.exists():
            raise RuntimeError(f"Missing protocol input: {path}")

        lock["common_hashes"][name] = sha256(path)

    for architecture in ("monolith", "microservices"):
        cfg = ARCH[architecture]

        items = {
            "docker_compose":
                fw / cfg["baseline"] / "docker-compose.yml",

            "architecture_contract":
                fw / cfg["architecture_contract"],

            "builder_prompt":
                fw / cfg["builder_prompt"],

            "refactor_prompt":
                fw / cfg["refactor_prompt"],
        }

        lock["architectures"][architecture] = {}

        for name, path in items.items():
            if not path.exists():
                raise RuntimeError(
                    f"Missing {architecture} protocol input: {path}"
                )

            lock["architectures"][architecture][name] = sha256(path)

    return lock


def verify_lock(
    repo: Path,
    fw: Path,
    protocol: dict,
    tools: dict,
    architecture: str,
):
    lock_path = repo / protocol["environment_lock"]

    if not lock_path.exists():
        raise RuntimeError(
            f"Missing environment lock: {lock_path}\n"
            "Freeze all provider models, then run "
            "--prepare-only --architecture both --write-lock."
        )

    lock = load_json(lock_path)

    current_protocol_sha = sha256(
        repo / protocol["protocol_file"]
    )

    if current_protocol_sha != lock.get("protocol_sha256"):
        raise RuntimeError(
            "Protocol file drift: "
            f"locked={lock.get('protocol_sha256')} "
            f"current={current_protocol_sha}"
        )

    # Re-hash actual cache contents, not only the manifest.
    cache_manifest_info(
        fw,
        protocol,
        verify_contents=True,
    )

    current_versions = provider_cli_versions(protocol)

    for name, version in current_versions.items():
        locked = lock["providers"][name]

        if version != locked["cli_version"]:
            raise RuntimeError(
                f"Provider CLI drift for {name}: "
                f"locked={locked['cli_version']} current={version}"
            )

        current_model = protocol["providers"][name].get(
            "model_requested"
        )

        if current_model != locked.get("model_requested"):
            raise RuntimeError(
                f"Provider model drift for {name}: "
                f"locked={locked.get('model_requested')} "
                f"current={current_model}"
            )

    if docker_context() != lock["docker_context"]:
        raise RuntimeError(
            f"Docker context drift: "
            f"locked={lock['docker_context']} "
            f"current={docker_context()}"
        )

    ids = {
        "graphify": tools["graphify"]["image_id"],
        "lizard": tools["lizard"]["image_id"],
        "trivy": tools["trivy"]["image_id"],
    }

    for name, image_id in ids.items():
        if image_id != lock["images"][name]:
            raise RuntimeError(
                f"{name} image drift: "
                f"locked={lock['images'][name]} "
                f"current={image_id}"
            )

    cache_manifest = (
        repo / protocol["security"]["cache_manifest"]
    )

    current_cache_sha = sha256(cache_manifest)

    if current_cache_sha != lock["trivy_cache_manifest_sha256"]:
        raise RuntimeError(
            "Trivy cache manifest drift: "
            f"locked={lock['trivy_cache_manifest_sha256']} "
            f"current={current_cache_sha}"
        )

    common = {
        "official_runner":
            fw / "scripts" / "run_experiment.py",

        "provider_base":
            fw / "providers" / "base.py",

        "provider_bwrap":
            fw / "providers" / "bwrap_sandbox.py",

        "provider_claude":
            fw / "providers" / "claude_provider.py",

        "provider_codex":
            fw / "providers" / "codex_provider.py",

        "provider_antigravity":
            fw / "providers" / "antigravity_provider.py",

        "a4_runner":
            fw / "scripts" / "run_a4_artifact_offline_v2.py",

        "graphify_runtime":
            fw / "scripts" / "graphify_runtime_v4.py",

        "public_api":
            fw / "contracts" / "public_api_v1.md",

        "requirements":
            fw / "contracts" / "requirements.md",

        "acceptance_runner":
            fw / "acceptance-tests" / "run_acceptance.py",

        "acceptance_contract":
            fw / "acceptance-tests" / "acceptance_contract.json",

        "quality_policy":
            repo / protocol["quality"]["policy"],

        "security_policy":
            repo / protocol["security"]["policy"],
    }

    for name, path in common.items():
        current = sha256(path)

        if current != lock["common_hashes"][name]:
            raise RuntimeError(
                f"Protocol drift in {name}"
            )

    cfg = ARCH[architecture]

    arch_items = {
        "docker_compose":
            fw / cfg["baseline"] / "docker-compose.yml",

        "architecture_contract":
            fw / cfg["architecture_contract"],

        "builder_prompt":
            fw / cfg["builder_prompt"],

        "refactor_prompt":
            fw / cfg["refactor_prompt"],
    }

    for name, path in arch_items.items():
        current = sha256(path)

        if current != lock["architectures"][architecture][name]:
            raise RuntimeError(
                f"{architecture} protocol drift in {name}"
            )

    return lock_path, lock


def prepare(
    repo: Path,
    fw: Path,
    protocol: dict,
    architectures: list[str],
    auto_prepare: bool,
    initialize_cache: bool,
    write_lock: bool,
):
    log("Docker daemon/context preflight")

    p = run(["docker", "info"], timeout=60)

    if p.returncode:
        raise RuntimeError(
            "Docker daemon is not reachable. "
            "Start Docker Desktop and retry."
        )

    print("Docker context:", docker_context())

    versions = provider_cli_versions(protocol)

    for name, version in versions.items():
        print(f"Provider {name}: {version}")

    tools = ensure_tool_images(
        repo,
        fw,
        protocol,
        auto_prepare,
    )

    if initialize_cache:
        if not protocol["security"].get(
            "allow_initialization",
            False,
        ):
            raise RuntimeError(
                "Security cache initialization is disabled for "
                "provider-v1. The experiment reuses the immutable "
                "frozen v3 Trivy snapshot."
            )

        initialize_security_cache(
            repo,
            fw,
            protocol,
        )

    cache_info = cache_manifest_info(
        fw,
        protocol,
        verify_contents=True,
    )

    print(
        "Trivy cache manifest:",
        cache_info["manifest_sha256"],
    )

    print(
        "Trivy cache files verified:",
        cache_info["files_verified"],
    )

    log("Trivy offline cache smoke")

    trivy_smoke = trivy_offline_smoke(
        repo,
        protocol,
    )

    print("Trivy offline smoke: PASS")

    baseline_results = {}

    for architecture in architectures:
        baseline_results[architecture] = baseline_check(
            fw,
            architecture,
        )

        print(
            f"{architecture}: services="
            + ",".join(
                baseline_results[architecture][
                    "compose_services"
                ]
            )
        )

    lock_path = repo / protocol["environment_lock"]

    if write_lock:
        # One lock covers BOTH architectures and ALL THREE providers.
        for architecture in ("monolith", "microservices"):
            baseline_check(
                fw,
                architecture,
            )

        lock = build_environment_lock(
            repo,
            fw,
            protocol,
            tools,
        )

        save_json(
            lock_path,
            lock,
        )

        print(
            "Environment lock written:",
            lock_path,
        )

    return {
        "docker_context": docker_context(),
        "provider_versions": versions,
        "tools": tools,
        "cache": cache_info,
        "trivy_offline_smoke": trivy_smoke,
        "baselines": baseline_results,
        "lock_path": (
            str(lock_path)
            if lock_path.exists()
            else None
        ),
    }



def a1_stage(
    repo: Path,
    fw: Path,
    protocol: dict,
    architecture: str,
    run_dir: Path,
    workspace: Path,
    runtime_ref: str,
    provider_name: str,
    provider,
):
    cfg = ARCH[architecture]
    paths = protocol_paths(
        repo,
        fw,
        protocol,
        architecture,
    )

    limits = protocol["limits"]
    provider_cfg = protocol["providers"][provider_name]
    model_requested = provider_cfg["model_requested"]

    compose_hash = sha256(
        workspace / "docker-compose.yml"
    )

    services = compose_services(workspace)

    project = (
        run_dir.name
        .lower()
        .replace("_", "-")
    )

    vpolicy = volume_policy(
        workspace,
        project,
    )

    infra_services = [
        service
        for service in cfg.get("infra_services", [])
        if service in services
    ]

    static = (
        paths["builder_prompt"].read_text(
            encoding="utf-8"
        )
        + "\n\n# ARCHITECTURE\n"
        + paths["architecture_contract"].read_text(
            encoding="utf-8"
        )
        + "\n\n# PUBLIC API\n"
        + paths["public_api"].read_text(
            encoding="utf-8"
        )
        + "\n\n# REQUIREMENTS\n"
        + paths["requirements"].read_text(
            encoding="utf-8"
        )
        + "\n\n"
        + provider_policy_text("a1")
    )

    session = None
    records = []
    provider_invocations = []
    infra_preflights = []
    current_feedback = None

    success = False
    artifacts = []
    terminal_reason = None

    started = time.perf_counter()

    max_iterations = int(
        limits["a1_max_iterations"]
    )

    for i in range(
        1,
        max_iterations + 1,
    ):
        idir = (
            run_dir
            / f"a1_iteration_{i:02d}"
        )

        idir.mkdir()

        log("=" * 72)
        log(
            f"A1 ITERATION "
            f"{i}/{max_iterations}"
        )
        log("=" * 72)

        # Infrastructure is proven before spending a provider
        # invocation. Infrastructure recovery does not consume an
        # additional provider iteration.
        log(
            "A1 infrastructure preflight "
            "(no coding provider)"
        )

        pre = prepare_infrastructure(
            project,
            workspace,
            vpolicy,
            infra_services,
            limits,
            idir / "infra_preflight",
        )

        save_json(
            idir / "infra_preflight.json",
            pre,
        )

        infra_preflights.append({
            "iteration_target": i,
            "result": pre,
        })

        if not pre.get("success"):
            terminal_reason = (
                "INFRASTRUCTURE_FAILURE"
            )
            break

        prompt = (
            static
            if i == 1
            else current_feedback
        )

        timeout_seconds = int(
            limits[
                "a1_initial_timeout_seconds"
                if i == 1
                else "a1_repair_timeout_seconds"
            ]
        )

        trace = (
            idir / "provider_trace.jsonl"
        )

        call_started = time.perf_counter()

        try:
            result = provider.run(
                workspace=workspace,
                prompt=prompt,
                trace_path=trace,
                session_id=session,
                model=model_requested,
                stage="a1",
                timeout_seconds=timeout_seconds,
            )

            call = result.to_dict()

        except Exception as exc:
            call = {
                "provider": provider_name,
                "model": model_requested,
                "session_id": session,
                "success": False,
                "returncode": None,
                "elapsed_seconds": round(
                    time.perf_counter()
                    - call_started,
                    3,
                ),
                "tool_calls": 0,
                "failed_tool_calls": 0,
                "policy_violation": False,
                "policy_violations": [],
                "final_response": "",
                "native_usage": None,
                "trace_path": str(trace),
                "stderr_path": None,
                "error_type": type(exc).__name__,
                "error": str(exc),
            }

        provider_invocations.append(call)

        save_json(
            idir / "provider_usage.json",
            call,
        )

        session = (
            call.get("session_id")
            or session
        )

        if call.get("policy_violation"):
            records.append({
                "iteration": i,
                "provider_call": call,
                "failure_class":
                    "PROVIDER_POLICY_VIOLATION",
            })

            terminal_reason = (
                "PROVIDER_POLICY_VIOLATION"
            )
            break

        observed_model = call.get("model")

        if (
            model_requested
            and observed_model
            and observed_model != model_requested
        ):
            records.append({
                "iteration": i,
                "provider_call": call,
                "failure_class":
                    "PROVIDER_MODEL_DRIFT",
            })

            terminal_reason = (
                "PROVIDER_MODEL_DRIFT"
            )
            break

        if not call.get("success"):
            records.append({
                "iteration": i,
                "provider_call": call,
                "failure_class":
                    "PROVIDER_RUNTIME_FAILURE",
            })

            terminal_reason = (
                "PROVIDER_RUNTIME_FAILURE"
            )
            break

        if (
            sha256(
                workspace
                / "docker-compose.yml"
            )
            != compose_hash
        ):
            raise RuntimeError(
                "ARCHITECTURE_VIOLATION: "
                "docker-compose.yml changed"
            )

        if (
            set(compose_services(workspace))
            != set(services)
        ):
            raise RuntimeError(
                "ARCHITECTURE_VIOLATION: "
                "Compose service set changed"
            )

        runtime = start_runtime_with_infra_recovery(
            project,
            workspace,
            vpolicy,
            infra_services,
            limits,
            idir / "runtime",
        )

        save_json(
            idir / "runtime_gate.json",
            runtime,
        )

        if not runtime.get("success"):
            failure_class = (
                runtime.get("status")
                or "BUILD_OR_START_FAILURE"
            )

            rec = {
                "iteration": i,
                "provider_call": call,
                "failure_class": failure_class,
                "runtime":
                    runtime.get("history", []),
            }

            records.append(rec)

            if (
                failure_class
                == "INFRASTRUCTURE_FAILURE"
            ):
                terminal_reason = (
                    "INFRASTRUCTURE_FAILURE"
                )
                break

            # No Graphify extraction after final provider attempt.
            if i >= max_iterations:
                terminal_reason = failure_class
                break

            evidence = runtime.get(
                "evidence",
                "",
            )

            q = failure_question(
                failure_class,
                evidence,
                architecture,
            )

            gm, ctx = graph_context_after_failure(
                workspace,
                idir,
                runtime_ref,
                q,
                protocol["graphify"]["budget"],
            )

            current_feedback = (
                builder_feedback(
                    failure_class,
                    evidence,
                    ctx,
                )
                + "\n\n"
                + provider_policy_text("a1")
            )

            rec["graphify_after_failure"] = gm
            continue

        afile = (
            idir / "acceptance.json"
        )

        rc, acc, stdout = run_acceptance(
            fw,
            f"{run_dir.name}_a1_{i:02d}",
            afile,
        )

        (
            idir
            / "acceptance_stdout.txt"
        ).write_text(
            stdout,
            encoding="utf-8",
        )

        print(
            stdout,
            flush=True,
        )

        gate = (
            rc == 0
            and acc.get("passed")
                == protocol[
                    "acceptance"
                ]["required_passed"]
            and acc.get("total")
                == protocol[
                    "acceptance"
                ]["required_total"]
        )

        rec = {
            "iteration": i,
            "provider_call": call,
            "compose_up_ok": True,
            "http_ready": True,
            "acceptance": {
                "passed":
                    acc.get("passed"),
                "failed":
                    acc.get("failed"),
                "total":
                    acc.get("total"),
                "success":
                    gate,
            },
            "runtime":
                runtime.get("history", []),
        }

        if gate:
            artifacts = package_services(
                project,
                workspace,
                cfg["app_services"],
                set(services),
                idir,
            )

            rec["artifacts"] = artifacts
            records.append(rec)

            success = True
            terminal_reason = "SUCCESS"
            break

        rec["failure_class"] = (
            "ACCEPTANCE_FAILURE"
        )

        records.append(rec)

        if i >= max_iterations:
            terminal_reason = (
                "ACCEPTANCE_FAILURE"
            )
            break

        ev = capture_runtime_evidence(
            project,
            workspace,
            idir
            / "acceptance_failure_evidence",
        )

        q = failed_test_question(
            acc,
            architecture,
        )

        gm, ctx = graph_context_after_failure(
            workspace,
            idir,
            runtime_ref,
            q,
            protocol["graphify"]["budget"],
        )

        failed = [
            {
                "id": t.get("id"),
                "name": t.get("name"),
                "message": t.get("message"),
                "http_statuses":
                    t.get("http_statuses"),
            }
            for t in acc.get("tests", [])
            if not t.get("passed")
        ]

        evidence = (
            json.dumps(
                failed,
                indent=2,
                ensure_ascii=False,
            )
            + "\n\nRUNTIME EVIDENCE:\n"
            + ev["evidence"]
        )

        current_feedback = (
            builder_feedback(
                "ACCEPTANCE_FAILURE",
                evidence,
                ctx,
            )
            + "\n\n"
            + provider_policy_text("a1")
        )

        rec["graphify_after_failure"] = gm

    termination = (
        terminal_reason
        or (
            "SUCCESS"
            if success
            else (
                records[-1].get(
                    "failure_class"
                )
                if records
                else "NO_ITERATION_COMPLETED"
            )
        )
    )

    summary = {
        "success": success,
        "termination_reason": termination,
        "iterations_used": len(records),
        "elapsed_seconds": round(
            time.perf_counter()
            - started,
            3,
        ),
        "provider_metrics":
            provider_invocation_summary(
                provider_invocations
            ),
        "artifacts": artifacts,
        "infrastructure_preflights":
            infra_preflights,
        "iterations": records,
    }

    save_json(
        run_dir / "a1_summary.json",
        summary,
    )

    reset_state(
        project,
        workspace,
        vpolicy,
    )

    return summary


def analysis_before_stage(
    repo: Path,
    fw: Path,
    protocol: dict,
    run_dir: Path,
    workspace: Path,
):
    paths = protocol_paths(repo, fw, protocol, "monolith")  # common policies only
    qdir = run_dir / "analysis_before"
    sdir = run_dir / "analysis_before_v2"
    qdir.mkdir()

    log("A2 QUALITY before A3")
    quality = quality_scan(
        workspace,
        qdir / "quality_before.json",
        protocol["quality"]["image"],
        repo / protocol["quality"]["policy"],
    )

    log("A4 SECURITY before A3")
    security, findings = security_scan(
        fw,
        workspace,
        qdir / "quality_before.json",
        sdir,
        protocol["security"]["image"],
        repo / protocol["security"]["cache"],
        repo / protocol["security"]["policy"],
    )

    return quality, security, findings



def a3_stage(
    repo: Path,
    fw: Path,
    protocol: dict,
    architecture: str,
    run_dir: Path,
    a1_workspace: Path,
    runtime_ref: str,
    quality_before: dict,
    security_before: dict,
    findings: dict,
    provider_name: str,
    provider,
):
    total_findings = (
        findings
        .get("summary", {})
        .get(
            "total_findings",
            len(
                findings.get(
                    "findings",
                    [],
                )
            ),
        )
    )

    a3_root = run_dir / "a3"
    a3_root.mkdir()

    if total_findings == 0:
        summary = {
            "success": True,
            "termination_reason":
                "SKIPPED_NO_FINDINGS",
            "provider_metrics":
                provider_invocation_summary([]),
        }

        save_json(
            a3_root / "a3_summary.json",
            summary,
        )

        return summary, None

    cfg = ARCH[architecture]

    paths = protocol_paths(
        repo,
        fw,
        protocol,
        architecture,
    )

    limits = protocol["limits"]

    provider_cfg = (
        protocol["providers"][provider_name]
    )

    model_requested = (
        provider_cfg["model_requested"]
    )

    workspace = (
        a3_root / "workspace"
    )

    shutil.copytree(
        a1_workspace,
        workspace,
    )

    compose_hash = sha256(
        workspace / "docker-compose.yml"
    )

    services = compose_services(workspace)

    infra_services = [
        service
        for service in cfg.get(
            "infra_services",
            [],
        )
        if service in services
    ]

    project = (
        f"{run_dir.name}-a3"
        .lower()
        .replace("_", "-")
    )

    vpolicy = volume_policy(
        workspace,
        project,
    )

    graph_dir = (
        a3_root / "graphify_before"
    )

    log("GRAPHIFY extract before A3")

    gm = build_code_graph(
        workspace,
        graph_dir,
        runtime_ref,
    )

    save_json(
        a3_root / "graphify_metrics.json",
        gm,
    )

    q = findings_query(
        findings,
        architecture,
    )

    (
        a3_root / "graph_query.txt"
    ).write_text(
        q,
        encoding="utf-8",
    )

    ctx = query_graph(
        graph_dir,
        runtime_ref,
        q,
        protocol["graphify"]["budget"],
    )

    (
        a3_root / "graph_context.txt"
    ).write_text(
        ctx,
        encoding="utf-8",
    )

    prompt = (
        paths["refactor_prompt"].read_text(
            encoding="utf-8"
        )
        + "\n\n# ARCHITECTURE\n"
        + paths["architecture_contract"].read_text(
            encoding="utf-8"
        )
        + "\n\n# PUBLIC API\n"
        + paths["public_api"].read_text(
            encoding="utf-8"
        )
        + "\n\n# REQUIREMENTS\n"
        + paths["requirements"].read_text(
            encoding="utf-8"
        )
        + "\n\n# DETERMINISTIC FINDINGS\n"
        + json.dumps(
            findings.get("findings", []),
            indent=2,
            ensure_ascii=False,
        )
        + "\n\n# GRAPHIFY CONTEXT\n"
        + ctx
        + "\n\n"
        + provider_policy_text("a3")
    )

    session = None
    calls = []
    provider_invocations = []
    infra_preflights = []

    success = False
    artifacts = []
    termination = None

    started = time.perf_counter()

    attempts = (
        1
        + int(
            limits[
                "a3_max_repair_attempts"
            ]
        )
    )

    for attempt in range(
        1,
        attempts + 1,
    ):
        idir = (
            a3_root
            / f"attempt_{attempt:02d}"
        )

        idir.mkdir()

        log(
            f"A3 infrastructure preflight "
            f"{attempt}/{attempts} "
            "(no coding provider)"
        )

        pre = prepare_infrastructure(
            project,
            workspace,
            vpolicy,
            infra_services,
            limits,
            idir / "infra_preflight",
        )

        save_json(
            idir / "infra_preflight.json",
            pre,
        )

        infra_preflights.append({
            "attempt_target": attempt,
            "result": pre,
        })

        if not pre.get("success"):
            termination = (
                "INFRASTRUCTURE_FAILURE"
            )
            break

        timeout_seconds = int(
            limits[
                "a3_refactor_timeout_seconds"
                if attempt == 1
                else "a3_repair_timeout_seconds"
            ]
        )

        trace = (
            idir / "provider_trace.jsonl"
        )

        call_started = time.perf_counter()

        try:
            result = provider.run(
                workspace=workspace,
                prompt=prompt,
                trace_path=trace,
                session_id=session,
                model=model_requested,
                stage="a3",
                timeout_seconds=timeout_seconds,
            )

            call = result.to_dict()

        except Exception as exc:
            call = {
                "provider": provider_name,
                "model": model_requested,
                "session_id": session,
                "success": False,
                "returncode": None,
                "elapsed_seconds": round(
                    time.perf_counter()
                    - call_started,
                    3,
                ),
                "tool_calls": 0,
                "failed_tool_calls": 0,
                "policy_violation": False,
                "policy_violations": [],
                "final_response": "",
                "native_usage": None,
                "trace_path": str(trace),
                "stderr_path": None,
                "error_type":
                    type(exc).__name__,
                "error":
                    str(exc),
            }

        provider_invocations.append(call)

        save_json(
            idir / "provider_usage.json",
            call,
        )

        session = (
            call.get("session_id")
            or session
        )

        if call.get("policy_violation"):
            calls.append({
                "attempt": attempt,
                "provider_call": call,
                "gate":
                    "PROVIDER_POLICY_VIOLATION",
            })

            termination = (
                "PROVIDER_POLICY_VIOLATION"
            )
            break

        observed_model = call.get("model")

        if (
            model_requested
            and observed_model
            and observed_model != model_requested
        ):
            calls.append({
                "attempt": attempt,
                "provider_call": call,
                "gate":
                    "PROVIDER_MODEL_DRIFT",
            })

            termination = (
                "PROVIDER_MODEL_DRIFT"
            )
            break

        if not call.get("success"):
            calls.append({
                "attempt": attempt,
                "provider_call": call,
                "gate":
                    "PROVIDER_RUNTIME_FAILURE",
            })

            termination = (
                "PROVIDER_RUNTIME_FAILURE"
            )
            break

        if (
            sha256(
                workspace
                / "docker-compose.yml"
            )
            != compose_hash
        ):
            raise RuntimeError(
                "ARCHITECTURE_VIOLATION: "
                "compose changed during A3"
            )

        if (
            set(compose_services(workspace))
            != set(services)
        ):
            raise RuntimeError(
                "ARCHITECTURE_VIOLATION: "
                "service set changed during A3"
            )

        runtime = start_runtime_with_infra_recovery(
            project,
            workspace,
            vpolicy,
            infra_services,
            limits,
            idir / "runtime",
        )

        save_json(
            idir / "runtime_gate.json",
            runtime,
        )

        if not runtime.get("success"):
            gate = (
                runtime.get("status")
                or "BUILD_OR_START_FAILURE"
            )

            calls.append({
                "attempt": attempt,
                "provider_call": call,
                "gate": gate,
                "runtime":
                    runtime.get("history", []),
            })

            if gate == "INFRASTRUCTURE_FAILURE":
                termination = (
                    "INFRASTRUCTURE_FAILURE"
                )
                break

            if attempt >= attempts:
                termination = gate
                break

            prompt = (
                refactor_repair_prompt(
                    gate,
                    runtime.get(
                        "evidence",
                        "",
                    ),
                    ctx,
                    findings,
                )
                + "\n\n"
                + provider_policy_text("a3")
            )

            continue

        afile = (
            idir / "acceptance.json"
        )

        rc, acc, stdout = run_acceptance(
            fw,
            f"{run_dir.name}_a3_{attempt:02d}",
            afile,
        )

        (
            idir
            / "acceptance_stdout.txt"
        ).write_text(
            stdout,
            encoding="utf-8",
        )

        print(
            stdout,
            flush=True,
        )

        gate = (
            rc == 0
            and acc.get("passed")
                == protocol[
                    "acceptance"
                ]["required_passed"]
            and acc.get("total")
                == protocol[
                    "acceptance"
                ]["required_total"]
        )

        calls.append({
            "attempt": attempt,
            "provider_call": call,
            "gate":
                "PASS"
                if gate
                else "ACCEPTANCE_FAILURE",
            "acceptance": {
                "passed":
                    acc.get("passed"),
                "failed":
                    acc.get("failed"),
                "total":
                    acc.get("total"),
            },
            "runtime":
                runtime.get("history", []),
        })

        if gate:
            artifacts = package_services(
                project,
                workspace,
                cfg["app_services"],
                set(services),
                idir,
            )

            success = True
            termination = "SUCCESS"
            break

        if attempt >= attempts:
            termination = (
                "ACCEPTANCE_FAILURE"
            )
            break

        failed = [
            {
                "id": t.get("id"),
                "name": t.get("name"),
                "message": t.get("message"),
                "http_statuses":
                    t.get("http_statuses"),
            }
            for t in acc.get("tests", [])
            if not t.get("passed")
        ]

        ev = capture_runtime_evidence(
            project,
            workspace,
            idir
            / "acceptance_failure_evidence",
        )

        prompt = (
            refactor_repair_prompt(
                "ACCEPTANCE_FAILURE",
                json.dumps(
                    failed,
                    indent=2,
                    ensure_ascii=False,
                )
                + "\n\nRUNTIME EVIDENCE:\n"
                + ev["evidence"],
                ctx,
                findings,
            )
            + "\n\n"
            + provider_policy_text("a3")
        )

    reset_state(
        project,
        workspace,
        vpolicy,
    )

    metrics = provider_invocation_summary(
        provider_invocations
    )

    if not success:
        summary = {
            "success": False,
            "termination_reason":
                termination,
            "elapsed_seconds": round(
                time.perf_counter()
                - started,
                3,
            ),
            "provider_metrics": metrics,
            "calls": calls,
            "infrastructure_preflights":
                infra_preflights,
        }

        save_json(
            a3_root / "a3_summary.json",
            summary,
        )

        return summary, None

    after = (
        a3_root / "analysis_after"
    )

    after.mkdir()

    log("A2 QUALITY after A3")

    quality_after = quality_scan(
        workspace,
        after / "quality_after.json",
        protocol["quality"]["image"],
        repo / protocol["quality"]["policy"],
    )

    log("A4 SECURITY after A3")

    a4_stage = (
        a3_root / "a4_after_stage"
    )

    security_after, findings_after = security_scan(
        fw,
        workspace,
        after / "quality_after.json",
        a4_stage,
        protocol["security"]["image"],
        repo / protocol["security"]["cache"],
        repo / protocol["security"]["policy"],
    )

    mapping = {
        "security_before.json":
            "security_after.json",

        "findings.json":
            "findings_after.json",

        "analysis_manifest.json":
            "analysis_after_manifest.json",

        "A2_A4_REPORT.md":
            "A2_A4_AFTER_REPORT.md",

        "trivy_source_raw.json":
            "trivy_source_raw_after.json",

        "trivy_artifacts_raw.json":
            "trivy_artifacts_raw_after.json",
    }

    for src_name, dst_name in mapping.items():
        src = a4_stage / src_name

        if src.exists():
            shutil.copy2(
                src,
                after / dst_name,
            )

    comp = compare_metrics(
        quality_before,
        quality_after,
        security_before,
        security_after,
    )

    comp["acceptance_after"] = {
        "passed":
            protocol["acceptance"][
                "required_passed"
            ],
        "total":
            protocol["acceptance"][
                "required_total"
            ],
    }

    comp["a3_provider_metrics"] = metrics
    comp["artifacts"] = artifacts

    save_json(
        a3_root / "before_after.json",
        comp,
    )

    summary = {
        "success": True,
        "termination_reason": "SUCCESS",
        "elapsed_seconds": round(
            time.perf_counter()
            - started,
            3,
        ),
        "provider_metrics": metrics,
        "calls": calls,
        "infrastructure_preflights":
            infra_preflights,
        "artifacts": artifacts,
        "findings_after":
            findings_after.get(
                "summary",
                {},
            ),
        "comparison_path":
            str(
                a3_root
                / "before_after.json"
            ),
    }

    save_json(
        a3_root / "a3_summary.json",
        summary,
    )

    return summary, comp



def stage_failure_outcome(
    reason: str,
    stage: str,
):
    if reason == "INFRASTRUCTURE_FAILURE":
        return (
            "INFRASTRUCTURE_ABORT",
            "ABORTED_INFRASTRUCTURE",
            False,
            2,
        )

    if reason == "PROVIDER_POLICY_VIOLATION":
        return (
            "PROVIDER_POLICY_VIOLATION",
            "INVALID_PROVIDER_POLICY",
            False,
            3,
        )

    if reason == "PROVIDER_MODEL_DRIFT":
        return (
            "PROVIDER_MODEL_DRIFT",
            "INVALID_PROVIDER_MODEL",
            False,
            3,
        )

    if reason == "PROVIDER_RUNTIME_FAILURE":
        return (
            "PROVIDER_RUNTIME_ABORT",
            "ABORTED_PROVIDER_RUNTIME",
            False,
            3,
        )

    return (
        f"{stage}_FAILED",
        "VALID_AGENT_FAILURE",
        True,
        1,
    )


def run_official(
    repo: Path,
    fw: Path,
    protocol: dict,
    architecture: str,
    seq: int,
    tools: dict,
    lock_path: Path,
    lock: dict,
    provider_name: str,
    provider,
):
    if port_open(8080):
        raise RuntimeError(
            "Port 8080 is already in use"
        )

    code = PROVIDER_CODES[provider_name]

    run_id = (
        f"R{code}{seq:04d}_"
        f"{provider_name}_"
        f"{architecture}"
    )

    run_dir = (
        fw / "runs" / run_id
    )

    if run_dir.exists():
        raise RuntimeError(
            f"Official run already exists: "
            f"{run_dir}. "
            "Never overwrite evidence."
        )

    cfg = ARCH[architecture]

    baseline = (
        fw / cfg["baseline"]
    )

    workspace = (
        run_dir / "workspace"
    )

    run_dir.mkdir(
        parents=True
    )

    shutil.copytree(
        baseline,
        workspace,
    )

    provider_cfg = (
        protocol["providers"][provider_name]
    )

    started = time.perf_counter()

    run_config = {
        "schema_version": 2,
        "run_id": run_id,
        "kind": "official",
        "architecture": architecture,

        "provider": provider_name,

        "provider_cli_version":
            lock["providers"][
                provider_name
            ]["cli_version"],

        "model_requested":
            provider_cfg[
                "model_requested"
            ],

        "model_verification":
            provider_cfg.get(
                "model_verification"
            ),

        "provider_policy":
            provider_cfg.get(
                "policy",
                {},
            ),

        "started_at_utc": now_iso(),

        "protocol_name":
            protocol["protocol_name"],

        "protocol_sha256":
            sha256(
                repo
                / protocol["protocol_file"]
            ),

        "environment_lock_path":
            str(lock_path),

        "environment_lock_sha256":
            sha256(lock_path),

        "docker_context":
            lock["docker_context"],

        "graphify_image_id":
            tools["graphify"]["image_id"],

        "lizard_image_id":
            tools["lizard"]["image_id"],

        "trivy_image_id":
            tools["trivy"]["image_id"],

        "trivy_cache_manifest_sha256":
            lock[
                "trivy_cache_manifest_sha256"
            ],

        "limits":
            protocol["limits"],

        "acceptance":
            protocol["acceptance"],

        "cache_policy":
            protocol["cache_policy"],

        "infrastructure_policy":
            protocol.get(
                "infrastructure_policy",
                {},
            ),

        "monetary_cost": {
            "available": False,
            "usd": None,
        },
    }

    save_json(
        run_dir / "run_config.json",
        run_config,
    )

    try:
        a1 = a1_stage(
            repo,
            fw,
            protocol,
            architecture,
            run_dir,
            workspace,
            tools["graphify"][
                "runtime_ref"
            ],
            provider_name,
            provider,
        )

        if not a1["success"]:
            (
                termination,
                status,
                eligible,
                rc,
            ) = stage_failure_outcome(
                a1.get(
                    "termination_reason",
                    "",
                ),
                "A1",
            )

            final = {
                **run_config,
                "finished_at_utc":
                    now_iso(),
                "success": False,
                "termination_reason":
                    termination,
                "experimental_status":
                    status,
                "primary_analysis_eligible":
                    eligible,
                "failure_stage": "A1",
                "a1": a1,
                "elapsed_seconds": round(
                    time.perf_counter()
                    - started,
                    3,
                ),
            }

            save_json(
                run_dir
                / "FINAL_SUMMARY.json",
                final,
            )

            return rc

        (
            quality_before,
            security_before,
            findings,
        ) = analysis_before_stage(
            repo,
            fw,
            protocol,
            run_dir,
            workspace,
        )

        a3, comp = a3_stage(
            repo,
            fw,
            protocol,
            architecture,
            run_dir,
            workspace,
            tools["graphify"][
                "runtime_ref"
            ],
            quality_before,
            security_before,
            findings,
            provider_name,
            provider,
        )

        overall_success = bool(
            a1["success"]
            and a3["success"]
        )

        if overall_success:
            termination = "SUCCESS"
            status = "VALID_SUCCESS"
            eligible = True
            final_rc = 0

        else:
            (
                termination,
                status,
                eligible,
                final_rc,
            ) = stage_failure_outcome(
                a3.get(
                    "termination_reason",
                    "",
                ),
                "A3",
            )

        final = {
            **run_config,

            "finished_at_utc":
                now_iso(),

            "success":
                overall_success,

            "termination_reason":
                termination,

            "experimental_status":
                status,

            "primary_analysis_eligible":
                eligible,

            "elapsed_seconds":
                round(
                    time.perf_counter()
                    - started,
                    3,
                ),

            "a1": {
                "success":
                    a1["success"],

                "iterations_used":
                    a1["iterations_used"],

                "provider_metrics":
                    a1["provider_metrics"],

                "artifacts":
                    a1["artifacts"],
            },

            "analysis_before": {
                "quality":
                    quality_before[
                        "summary"
                    ],

                "security":
                    security_before[
                        "summary"
                    ],

                "findings":
                    findings["summary"],
            },

            "a3": a3,

            "before_after": comp,

            "monetary_cost": {
                "available": False,
                "usd": None,
            },
        }

        save_json(
            run_dir
            / "FINAL_SUMMARY.json",
            final,
        )

        a1m = a1["provider_metrics"]
        a3m = a3.get(
            "provider_metrics",
            provider_invocation_summary([]),
        )

        lines = [
            f"# Official run — {run_id}",
            "",
            f"- Architecture: **{architecture}**",
            f"- Provider: **{provider_name}**",
            (
                "- Provider CLI: "
                f"**{run_config['provider_cli_version']}**"
            ),
            (
                "- Model requested: "
                f"**{run_config['model_requested']}**"
            ),
            f"- Success: **{overall_success}**",
            (
                "- A1 iterations: "
                f"**{a1['iterations_used']}**"
            ),
            (
                "- A1 provider invocations: "
                f"**{a1m['invocations']}**"
            ),
            (
                "- A1 provider elapsed: "
                f"**{a1m['elapsed_seconds']} s**"
            ),
            (
                "- Findings before A3: "
                f"**{findings['summary']['total_findings']}**"
            ),
            (
                "- A3 termination: "
                f"**{a3['termination_reason']}**"
            ),
            (
                "- A3 provider invocations: "
                f"**{a3m['invocations']}**"
            ),
            (
                "- A3 provider elapsed: "
                f"**{a3m['elapsed_seconds']} s**"
            ),
            (
                "- Monetary provider cost: "
                "**not available as a comparable CLI metric**"
            ),
            "",
            (
                "The same provider-neutral `run_experiment.py`, "
                "protocol and frozen environment lock are used "
                "for both architecture arms."
            ),
        ]

        (
            run_dir
            / "FINAL_REPORT.md"
        ).write_text(
            "\n".join(lines) + "\n",
            encoding="utf-8",
        )

        print(
            "\n" + "=" * 72
        )

        print(
            "OFFICIAL PROVIDER EXPERIMENT COMPLETE"
        )

        print("=" * 72)
        print("Run      :", run_id)
        print("Provider :", provider_name)
        print(
            "Model    :",
            provider_cfg["model_requested"],
        )
        print(
            "Success  :",
            overall_success,
        )
        print(
            "Summary  :",
            run_dir
            / "FINAL_SUMMARY.json",
        )
        print(
            "Report   :",
            run_dir
            / "FINAL_REPORT.md",
        )

        return final_rc

    except Exception as exc:
        save_json(
            run_dir / "RUN_ABORTED.json",
            {
                **run_config,
                "aborted_at_utc":
                    now_iso(),
                "error_type":
                    type(exc).__name__,
                "error":
                    str(exc),
            },
        )

        raise



def parse_args():
    ap = argparse.ArgumentParser(
        description=(
            "Provider-neutral official runner for "
            "monolith and microservices."
        )
    )

    ap.add_argument(
        "--repo-root",
        type=Path,
        default=Path("."),
    )

    ap.add_argument(
        "--framework-root",
        type=Path,
        default=Path(
            "multiagent-provider-experiment"
        ),
    )

    ap.add_argument(
        "--protocol",
        type=Path,
        default=Path(
            "multiagent-provider-experiment/"
            "protocol/"
            "official_protocol_provider_v1.json"
        ),
    )

    ap.add_argument(
        "--architecture",
        choices=[
            "monolith",
            "microservices",
            "both",
        ],
        required=True,
    )

    ap.add_argument(
        "--provider",
        choices=[
            "codex",
            "antigravity",
        ],
    )

    ap.add_argument(
        "--seq",
        type=int,
    )

    ap.add_argument(
        "--prepare-only",
        action="store_true",
        help=(
            "Prepare/validate environment without "
            "calling a coding provider."
        ),
    )

    ap.add_argument(
        "--auto-prepare",
        action=argparse.BooleanOptionalAction,
        default=True,
        help=(
            "Automatically build/pull missing "
            "Graphify/Lizard/Trivy images."
        ),
    )

    ap.add_argument(
        "--initialize-security-cache",
        action="store_true",
        help=(
            "Initialize Trivy cache only when "
            "explicitly allowed by the protocol."
        ),
    )

    ap.add_argument(
        "--write-lock",
        action="store_true",
        help=(
            "Write the shared provider environment "
            "lock after successful preparation."
        ),
    )

    return ap.parse_args()


def main():
    args = parse_args()

    repo = (
        args.repo_root
        .resolve()
    )

    fw = (
        repo
        / args.framework_root
    ).resolve()

    protocol_path = (
        repo
        / args.protocol
    ).resolve()

    protocol = load_json(
        protocol_path
    )

    if (
        args.architecture == "both"
        and not args.prepare_only
    ):
        raise SystemExit(
            "--architecture both is allowed "
            "only with --prepare-only. "
            "Official runs are started one "
            "architecture at a time."
        )

    if not args.prepare_only:
        if args.seq is None:
            raise SystemExit(
                "--seq is required for an "
                "official run"
            )

        if not args.provider:
            raise SystemExit(
                "--provider is required for an "
                "official run"
            )

        provider_cfg = (
            protocol["providers"].get(
                args.provider
            )
        )

        if not provider_cfg:
            raise SystemExit(
                f"Provider not present in protocol: "
                f"{args.provider}"
            )

        if not provider_cfg.get(
            "model_requested"
        ):
            raise SystemExit(
                f"Official run blocked: "
                f"{args.provider} has "
                "model_requested=null. "
                "Freeze an explicit model first, "
                "then regenerate the shared "
                "environment lock."
            )

    architectures = (
        [
            "monolith",
            "microservices",
        ]
        if args.architecture == "both"
        else [
            args.architecture
        ]
    )

    prep = prepare(
        repo,
        fw,
        protocol,
        architectures,
        args.auto_prepare,
        args.initialize_security_cache,
        args.write_lock,
    )

    if args.prepare_only:
        if not args.write_lock:
            lock_target = (
                repo
                / protocol[
                    "environment_lock"
                ]
            )

            if lock_target.exists():
                for architecture in architectures:
                    verify_lock(
                        repo,
                        fw,
                        protocol,
                        prep["tools"],
                        architecture,
                    )

                print(
                    "Frozen environment lock "
                    "verification: PASS"
                )

        print(
            "\nRESULT: OK — preparation only; "
            "no coding provider was called."
        )

        if prep["lock_path"]:
            print(
                "Environment lock:",
                prep["lock_path"],
            )

        return 0

    lock_path, lock = verify_lock(
        repo,
        fw,
        protocol,
        prep["tools"],
        args.architecture,
    )

    provider = make_provider(
        args.provider
    )

    return run_official(
        repo,
        fw,
        protocol,
        args.architecture,
        args.seq,
        prep["tools"],
        lock_path,
        lock,
        args.provider,
        provider,
    )


if __name__ == "__main__":
    raise SystemExit(main())

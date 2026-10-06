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


ARCH = {
    "monolith": {
        "baseline": "baselines/monolith",
        "architecture_contract": "contracts/architecture_monolith.md",
        "builder_prompt": "prompts/builder.md",
        "refactor_prompt": "prompts/refactor.md",
        "app_services": ["app"],
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


def relevant_logs(project: str, workspace: Path, app_services: list[str]) -> str:
    present = set(compose_services(workspace))
    selected = [s for s in app_services if s in present]
    args = ["logs", "--no-color", "--tail=600"] + selected
    return compose(project, workspace, args, 180).stdout


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


def claude_stream(
    workspace: Path,
    prompt: str,
    model: str,
    max_turns: int,
    trace: Path,
    allowed_tools: str,
    resume: str | None = None,
    timeout=2400,
):
    cmd = [
        "claude",
        "-p",
        prompt,
        "--output-format",
        "stream-json",
        "--verbose",
        "--max-turns",
        str(max_turns),
        "--model",
        model,
        "--allowedTools",
        allowed_tools,
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
    deadline = time.time() + timeout
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
                for block in (ev.get("message") or {}).get("content", []) or []:
                    if block.get("type") == "tool_use":
                        log("CLAUDE tool -> " + block.get("name", "?"))

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


def usage(result: dict) -> dict:
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


def canonical_models(us: dict) -> set[str]:
    return {x for x in (us.get("canonical_models") or {}).values() if x}


def check_canonical(us: dict, expected: str):
    models = canonical_models(us)
    if expected not in models:
        raise RuntimeError(
            f"MODEL_DRIFT: expected canonical model {expected}, got {sorted(models)}"
        )


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
    lock = {
        "schema_version": 2,
        "created_at_utc": now_iso(),
        "protocol_sha256": sha256(repo / protocol["protocol_file"]),
        "docker_context": docker_context(),
        "claude_version": run(["claude", "--version"], cwd=repo, timeout=30).stdout.strip(),
        "model_requested": protocol["model_requested"],
        "expected_canonical_model": protocol["expected_canonical_model"],
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
        "official_runner": fw / "scripts" / "run_experiment.py",
        "a4_runner": fw / "scripts" / "run_a4_artifact_offline_v2.py",
        "graphify_runtime": fw / "scripts" / "graphify_runtime_v4.py",
        "public_api": fw / "contracts" / "public_api_v1.md",
        "requirements": fw / "contracts" / "requirements.md",
        "acceptance_runner": fw / "acceptance-tests" / "run_acceptance.py",
        "acceptance_contract": fw / "acceptance-tests" / "acceptance_contract.json",
        "quality_policy": repo / protocol["quality"]["policy"],
        "security_policy": repo / protocol["security"]["policy"],
    }
    for name, p in common.items():
        if not p.exists():
            raise RuntimeError(f"Missing protocol input: {p}")
        lock["common_hashes"][name] = sha256(p)

    for architecture in ("monolith", "microservices"):
        cfg = ARCH[architecture]
        items = {
            "docker_compose": fw / cfg["baseline"] / "docker-compose.yml",
            "architecture_contract": fw / cfg["architecture_contract"],
            "builder_prompt": fw / cfg["builder_prompt"],
            "refactor_prompt": fw / cfg["refactor_prompt"],
        }
        lock["architectures"][architecture] = {}
        for name, p in items.items():
            if not p.exists():
                raise RuntimeError(f"Missing {architecture} protocol input: {p}")
            lock["architectures"][architecture][name] = sha256(p)

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
            "Run --prepare-only --architecture both --write-lock first."
        )
    lock = load_json(lock_path)

    current_protocol_sha = sha256(repo / protocol["protocol_file"])
    if current_protocol_sha != lock.get("protocol_sha256"):
        raise RuntimeError(
            "Protocol file drift: "
            f"locked={lock.get('protocol_sha256')} current={current_protocol_sha}"
        )

    # Re-hash the actual cache, not only the manifest file.
    cache_manifest_info(fw, protocol, verify_contents=True)

    current_claude = run(["claude", "--version"], cwd=repo, timeout=30).stdout.strip()
    if current_claude != lock["claude_version"]:
        raise RuntimeError(
            f"Claude Code drift: locked={lock['claude_version']} current={current_claude}"
        )

    if docker_context() != lock["docker_context"]:
        raise RuntimeError(
            f"Docker context drift: locked={lock['docker_context']} "
            f"current={docker_context()}"
        )

    ids = {
        "graphify": tools["graphify"]["image_id"],
        "lizard": tools["lizard"]["image_id"],
        "trivy": tools["trivy"]["image_id"],
    }
    for k, v in ids.items():
        if v != lock["images"][k]:
            raise RuntimeError(
                f"{k} image drift: locked={lock['images'][k]} current={v}"
            )

    cache_manifest = repo / protocol["security"]["cache_manifest"]
    current_cache_sha = sha256(cache_manifest)
    if current_cache_sha != lock["trivy_cache_manifest_sha256"]:
        raise RuntimeError(
            "Trivy cache manifest drift: "
            f"locked={lock['trivy_cache_manifest_sha256']} "
            f"current={current_cache_sha}"
        )

    common = {
        "official_runner": fw / "scripts" / "run_experiment.py",
        "a4_runner": fw / "scripts" / "run_a4_artifact_offline_v2.py",
        "graphify_runtime": fw / "scripts" / "graphify_runtime_v4.py",
        "public_api": fw / "contracts" / "public_api_v1.md",
        "requirements": fw / "contracts" / "requirements.md",
        "acceptance_runner": fw / "acceptance-tests" / "run_acceptance.py",
        "acceptance_contract": fw / "acceptance-tests" / "acceptance_contract.json",
        "quality_policy": repo / protocol["quality"]["policy"],
        "security_policy": repo / protocol["security"]["policy"],
    }
    for name, p in common.items():
        cur = sha256(p)
        if cur != lock["common_hashes"][name]:
            raise RuntimeError(f"Protocol drift in {name}")

    cfg = ARCH[architecture]
    arch_items = {
        "docker_compose": fw / cfg["baseline"] / "docker-compose.yml",
        "architecture_contract": fw / cfg["architecture_contract"],
        "builder_prompt": fw / cfg["builder_prompt"],
        "refactor_prompt": fw / cfg["refactor_prompt"],
    }
    for name, p in arch_items.items():
        cur = sha256(p)
        if cur != lock["architectures"][architecture][name]:
            raise RuntimeError(f"{architecture} protocol drift in {name}")

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
            "Docker daemon is not reachable. Start Docker Desktop and retry."
        )
    print("Docker context:", docker_context())

    tools = ensure_tool_images(repo, fw, protocol, auto_prepare)

    if initialize_cache:
        initialize_security_cache(repo, fw, protocol)

    cache_info = cache_manifest_info(fw, protocol, verify_contents=True)
    print("Trivy cache manifest:", cache_info["manifest_sha256"])
    print("Trivy cache files verified:", cache_info["files_verified"])

    log("Trivy offline cache smoke")
    trivy_smoke = trivy_offline_smoke(repo, protocol)
    print("Trivy offline smoke: PASS")

    baseline_results = {}
    for architecture in architectures:
        baseline_results[architecture] = baseline_check(fw, architecture)
        print(
            f"{architecture}: services="
            + ",".join(baseline_results[architecture]["compose_services"])
        )

    claude_version = run(["claude", "--version"], cwd=repo, timeout=30).stdout.strip()
    expected_prefix = protocol.get("expected_claude_code_version_prefix")
    if expected_prefix and not claude_version.startswith(expected_prefix):
        raise RuntimeError(
            f"Claude Code version drift: expected prefix {expected_prefix}, "
            f"got {claude_version}"
        )

    # Exact image IDs are frozen in environment lock.
    # Before the lock exists, versioned image refs are sufficient preparation.

    lock_path = repo / protocol["environment_lock"]
    if write_lock:
        # A single lock always covers both architectures so paired official runs
        # are frozen against identical common instruments.
        for architecture in ("monolith", "microservices"):
            baseline_check(fw, architecture)
        lock = build_environment_lock(repo, fw, protocol, tools)
        save_json(lock_path, lock)
        print("Environment lock written:", lock_path)

    return {
        "docker_context": docker_context(),
        "claude_version": claude_version,
        "tools": tools,
        "cache": cache_info,
        "trivy_offline_smoke": trivy_smoke,
        "baselines": baseline_results,
        "lock_path": str(lock_path) if lock_path.exists() else None,
    }


def a1_stage(
    repo: Path,
    fw: Path,
    protocol: dict,
    architecture: str,
    run_dir: Path,
    workspace: Path,
    runtime_ref: str,
):
    cfg = ARCH[architecture]
    paths = protocol_paths(repo, fw, protocol, architecture)
    limits = protocol["limits"]

    compose_hash = sha256(workspace / "docker-compose.yml")
    services = compose_services(workspace)
    vpolicy = volume_policy(workspace, run_dir.name.lower().replace("_", "-"))

    static = (
        paths["builder_prompt"].read_text(encoding="utf-8")
        + "\n\n# ARCHITECTURE\n"
        + paths["architecture_contract"].read_text(encoding="utf-8")
        + "\n\n# PUBLIC API\n"
        + paths["public_api"].read_text(encoding="utf-8")
        + "\n\n# REQUIREMENTS\n"
        + paths["requirements"].read_text(encoding="utf-8")
        + """
\n# ORCHESTRATION RULES
The orchestrator owns Docker lifecycle and external acceptance tests.
Do NOT run docker compose up/down/start/stop/restart.
You may use Bash only inside this workspace for inspection/build-only work.
Do not access parent directories or experiment/acceptance implementation.
"""
    )

    project = run_dir.name.lower().replace("_", "-")
    session = None
    session_costs = {}
    records = []
    current_feedback = None
    success = False
    artifacts = []
    started = time.perf_counter()

    for i in range(1, limits["a1_max_iterations"] + 1):
        idir = run_dir / f"a1_iteration_{i:02d}"
        idir.mkdir()

        log("=" * 72)
        log(f"A1 ITERATION {i}/{limits['a1_max_iterations']}")
        log("=" * 72)

        prompt = static if i == 1 else current_feedback
        result = claude_stream(
            workspace,
            prompt,
            protocol["model_requested"],
            limits["a1_first_max_turns"] if i == 1 else limits["a1_fix_max_turns"],
            idir / "claude_trace.jsonl",
            allowed_tools="Read,Write,Edit,Glob,Grep,Bash",
            resume=session,
        )
        us = usage(result)
        check_canonical(us, protocol["expected_canonical_model"])
        session = us.get("session_id") or session

        sid = us.get("session_id") or f"a1-{i}"
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

        state_reset = reset_state(project, workspace, vpolicy)
        save_json(idir / "state_reset.json", state_reset)

        up = compose(project, workspace, ["up", "-d", "--build"], 1500)
        (idir / "compose_up.log").write_text(up.stdout, encoding="utf-8")

        if up.returncode:
            logs = relevant_logs(project, workspace, cfg["app_services"])
            ps = compose(project, workspace, ["ps", "-a"], 120).stdout
            (idir / "compose_logs_after_wait.txt").write_text(
                logs, encoding="utf-8"
            )
            (idir / "compose_ps_after_wait.txt").write_text(ps, encoding="utf-8")

            evidence = up.stdout[-10000:] + "\n" + logs[-14000:] + "\n" + ps[-5000:]
            q = failure_question("BUILD_OR_START_FAILURE", evidence, architecture)
            gm, ctx = graph_context_after_failure(
                workspace, idir, runtime_ref, q, protocol["graphify"]["budget"]
            )
            current_feedback = builder_feedback(
                "BUILD_OR_START_FAILURE", evidence, ctx
            )
            records.append(
                {
                    "iteration": i,
                    "claude": us,
                    "compose_up_ok": False,
                    "failure_class": "BUILD_OR_START_FAILURE",
                    "graphify_after_failure": gm,
                }
            )
            continue

        ready = wait_http(limits["startup_timeout_seconds"])
        logs = relevant_logs(project, workspace, cfg["app_services"])
        ps = compose(project, workspace, ["ps", "-a"], 120).stdout
        (idir / "compose_logs_after_wait.txt").write_text(logs, encoding="utf-8")
        (idir / "compose_ps_after_wait.txt").write_text(ps, encoding="utf-8")

        if not ready:
            evidence = logs[-16000:] + "\n\nCOMPOSE PS:\n" + ps[-5000:]
            q = failure_question("STARTUP_TIMEOUT", evidence, architecture)
            gm, ctx = graph_context_after_failure(
                workspace, idir, runtime_ref, q, protocol["graphify"]["budget"]
            )
            current_feedback = builder_feedback("STARTUP_TIMEOUT", evidence, ctx)
            records.append(
                {
                    "iteration": i,
                    "claude": us,
                    "compose_up_ok": True,
                    "http_ready": False,
                    "failure_class": "STARTUP_TIMEOUT",
                    "graphify_after_failure": gm,
                }
            )
            continue

        afile = idir / "acceptance.json"
        rc, acc, stdout = run_acceptance(
            fw, f"{run_dir.name}_a1_{i:02d}", afile
        )
        (idir / "acceptance_stdout.txt").write_text(stdout, encoding="utf-8")
        print(stdout, flush=True)

        gate = (
            rc == 0
            and acc.get("passed") == protocol["acceptance"]["required_passed"]
            and acc.get("total") == protocol["acceptance"]["required_total"]
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
            break

        q = failed_test_question(acc, architecture)
        gm, ctx = graph_context_after_failure(
            workspace, idir, runtime_ref, q, protocol["graphify"]["budget"]
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
        current_feedback = builder_feedback("ACCEPTANCE_FAILURE", evidence, ctx)
        rec["failure_class"] = "ACCEPTANCE_FAILURE"
        rec["graphify_after_failure"] = gm
        records.append(rec)

    final_cost = sum(session_costs.values())
    termination = "SUCCESS" if success else (
        records[-1].get("failure_class") if records else "NO_ITERATION_COMPLETED"
    )

    summary = {
        "success": success,
        "termination_reason": termination,
        "iterations_used": len(records),
        "elapsed_seconds": round(time.perf_counter() - started, 3),
        "reported_cost_usd_final_by_session": round(final_cost, 8),
        "session_costs": session_costs,
        "artifacts": artifacts,
        "iterations": records,
    }
    save_json(run_dir / "a1_summary.json", summary)

    # Leave no application containers/data state running after A1, but preserve
    # Maven cache volumes.
    reset_state(project, workspace, vpolicy)
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
):
    total_findings = findings.get("summary", {}).get(
        "total_findings", len(findings.get("findings", []))
    )
    a3_root = run_dir / "a3"
    a3_root.mkdir()

    if total_findings == 0:
        summary = {
            "success": True,
            "termination_reason": "SKIPPED_NO_FINDINGS",
            "claude_calls": 0,
            "reported_cost_usd_final_session": 0.0,
        }
        save_json(a3_root / "a3_summary.json", summary)
        return summary, None

    cfg = ARCH[architecture]
    paths = protocol_paths(repo, fw, protocol, architecture)
    limits = protocol["limits"]

    workspace = a3_root / "workspace"
    shutil.copytree(a1_workspace, workspace)

    compose_hash = sha256(workspace / "docker-compose.yml")
    services = compose_services(workspace)
    project = f"{run_dir.name}-a3".lower().replace("_", "-")
    vpolicy = volume_policy(workspace, project)

    graph_dir = a3_root / "graphify_before"
    log("GRAPHIFY extract before A3")
    gm = build_code_graph(workspace, graph_dir, runtime_ref)
    save_json(a3_root / "graphify_metrics.json", gm)
    q = findings_query(findings, architecture)
    (a3_root / "graph_query.txt").write_text(q, encoding="utf-8")
    ctx = query_graph(graph_dir, runtime_ref, q, protocol["graphify"]["budget"])
    (a3_root / "graph_context.txt").write_text(ctx, encoding="utf-8")

    prompt = (
        paths["refactor_prompt"].read_text(encoding="utf-8")
        + "\n\n# ARCHITECTURE\n"
        + paths["architecture_contract"].read_text(encoding="utf-8")
        + "\n\n# PUBLIC API\n"
        + paths["public_api"].read_text(encoding="utf-8")
        + "\n\n# REQUIREMENTS\n"
        + paths["requirements"].read_text(encoding="utf-8")
        + "\n\n# DETERMINISTIC FINDINGS\n"
        + json.dumps(findings.get("findings", []), indent=2, ensure_ascii=False)
        + "\n\n# GRAPHIFY CONTEXT\n"
        + ctx
    )

    session = None
    session_cost = 0.0
    calls = []
    success = False
    artifacts = []
    termination = None
    started = time.perf_counter()

    attempts = 1 + limits["a3_max_repair_attempts"]
    for attempt in range(1, attempts + 1):
        idir = a3_root / f"attempt_{attempt:02d}"
        idir.mkdir()

        result = claude_stream(
            workspace,
            prompt,
            protocol["model_requested"],
            limits["a3_refactor_max_turns"]
            if attempt == 1
            else limits["a3_repair_max_turns"],
            idir / "claude_trace.jsonl",
            allowed_tools="Read,Write,Edit,Glob,Grep",
            resume=session,
        )
        us = usage(result)
        check_canonical(us, protocol["expected_canonical_model"])
        session = us.get("session_id") or session

        curr = float(us.get("total_cost_usd") or 0)
        us["reported_cost_usd_incremental_derived"] = round(
            max(0.0, curr - session_cost), 8
        )
        session_cost = curr
        save_json(idir / "claude_usage.json", us)

        if sha256(workspace / "docker-compose.yml") != compose_hash:
            raise RuntimeError("ARCHITECTURE_VIOLATION: compose changed during A3")
        if set(compose_services(workspace)) != set(services):
            raise RuntimeError("ARCHITECTURE_VIOLATION: service set changed during A3")

        state_reset = reset_state(project, workspace, vpolicy)
        save_json(idir / "state_reset.json", state_reset)

        up = compose(project, workspace, ["up", "-d", "--build"], 1500)
        (idir / "compose_up.log").write_text(up.stdout, encoding="utf-8")

        if up.returncode:
            logs = relevant_logs(project, workspace, cfg["app_services"])
            calls.append(
                {"attempt": attempt, "claude": us, "gate": "BUILD_OR_START_FAILURE"}
            )
            if attempt >= attempts:
                termination = "BUILD_OR_START_FAILURE"
                break
            prompt = refactor_repair_prompt(
                "BUILD_OR_START_FAILURE", up.stdout + "\n" + logs, ctx, findings
            )
            continue

        ready = wait_http(limits["startup_timeout_seconds"])
        logs = relevant_logs(project, workspace, cfg["app_services"])
        (idir / "compose_logs.txt").write_text(logs, encoding="utf-8")

        if not ready:
            calls.append(
                {"attempt": attempt, "claude": us, "gate": "STARTUP_TIMEOUT"}
            )
            if attempt >= attempts:
                termination = "STARTUP_TIMEOUT"
                break
            prompt = refactor_repair_prompt(
                "STARTUP_TIMEOUT", logs, ctx, findings
            )
            continue

        afile = idir / "acceptance.json"
        rc, acc, stdout = run_acceptance(
            fw, f"{run_dir.name}_a3_{attempt:02d}", afile
        )
        (idir / "acceptance_stdout.txt").write_text(stdout, encoding="utf-8")
        print(stdout, flush=True)

        gate = (
            rc == 0
            and acc.get("passed") == protocol["acceptance"]["required_passed"]
            and acc.get("total") == protocol["acceptance"]["required_total"]
        )
        calls.append(
            {
                "attempt": attempt,
                "claude": us,
                "gate": "PASS" if gate else "ACCEPTANCE_FAILURE",
                "acceptance": {
                    "passed": acc.get("passed"),
                    "failed": acc.get("failed"),
                    "total": acc.get("total"),
                },
            }
        )

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
        prompt = refactor_repair_prompt(
            "ACCEPTANCE_FAILURE",
            json.dumps(failed, indent=2, ensure_ascii=False),
            ctx,
            findings,
        )

    reset_state(project, workspace, vpolicy)

    if not success:
        summary = {
            "success": False,
            "termination_reason": termination,
            "elapsed_seconds": round(time.perf_counter() - started, 3),
            "reported_cost_usd_final_session": round(session_cost, 8),
            "calls": calls,
        }
        save_json(a3_root / "a3_summary.json", summary)
        return summary, None

    after = a3_root / "analysis_after"
    after.mkdir()

    log("A2 QUALITY after A3")
    quality_after = quality_scan(
        workspace,
        after / "quality_after.json",
        protocol["quality"]["image"],
        repo / protocol["quality"]["policy"],
    )

    log("A4 SECURITY after A3")
    a4_stage = a3_root / "a4_after_stage"
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

    comp = compare_metrics(
        quality_before, quality_after, security_before, security_after
    )
    comp["acceptance_after"] = {
        "passed": protocol["acceptance"]["required_passed"],
        "total": protocol["acceptance"]["required_total"],
    }
    comp["a3_reported_cost_usd"] = round(session_cost, 8)
    comp["artifacts"] = artifacts
    save_json(a3_root / "before_after.json", comp)

    summary = {
        "success": True,
        "termination_reason": "SUCCESS",
        "elapsed_seconds": round(time.perf_counter() - started, 3),
        "reported_cost_usd_final_session": round(session_cost, 8),
        "calls": calls,
        "artifacts": artifacts,
        "findings_after": findings_after.get("summary", {}),
        "comparison_path": str(a3_root / "before_after.json"),
    }
    save_json(a3_root / "a3_summary.json", summary)
    return summary, comp


def run_official(
    repo: Path,
    fw: Path,
    protocol: dict,
    architecture: str,
    seq: int,
    tools: dict,
    lock_path: Path,
    lock: dict,
):
    if port_open(8080):
        raise RuntimeError("Port 8080 is already in use")

    run_id = f"R{seq:04d}_{architecture}"
    run_dir = fw / "runs" / run_id
    if run_dir.exists():
        raise RuntimeError(
            f"Official run already exists: {run_dir}. Never overwrite evidence."
        )

    cfg = ARCH[architecture]
    baseline = fw / cfg["baseline"]
    workspace = run_dir / "workspace"
    run_dir.mkdir(parents=True)
    shutil.copytree(baseline, workspace)

    started = time.perf_counter()
    run_config = {
        "schema_version": 1,
        "run_id": run_id,
        "kind": "official",
        "architecture": architecture,
        "started_at_utc": now_iso(),
        "protocol_name": protocol["protocol_name"],
        "protocol_sha256": sha256(
            repo / "multiagent-experiment" / "protocol" / "official_protocol_v1.json"
        ),
        "environment_lock_path": str(lock_path),
        "environment_lock_sha256": sha256(lock_path),
        "docker_context": lock["docker_context"],
        "model_requested": protocol["model_requested"],
        "expected_canonical_model": protocol["expected_canonical_model"],
        "graphify_image_id": tools["graphify"]["image_id"],
        "lizard_image_id": tools["lizard"]["image_id"],
        "trivy_image_id": tools["trivy"]["image_id"],
        "trivy_cache_manifest_sha256": lock["trivy_cache_manifest_sha256"],
        "limits": protocol["limits"],
        "acceptance": protocol["acceptance"],
        "cache_policy": protocol["cache_policy"],
    }
    save_json(run_dir / "run_config.json", run_config)

    try:
        a1 = a1_stage(
            repo,
            fw,
            protocol,
            architecture,
            run_dir,
            workspace,
            tools["graphify"]["runtime_ref"],
        )
        if not a1["success"]:
            final = {
                **run_config,
                "finished_at_utc": now_iso(),
                "success": False,
                "termination_reason": "A1_FAILED",
                "a1": a1,
                "elapsed_seconds": round(time.perf_counter() - started, 3),
            }
            save_json(run_dir / "FINAL_SUMMARY.json", final)
            return 1

        quality_before, security_before, findings = analysis_before_stage(
            repo, fw, protocol, run_dir, workspace
        )

        a3, comp = a3_stage(
            repo,
            fw,
            protocol,
            architecture,
            run_dir,
            workspace,
            tools["graphify"]["runtime_ref"],
            quality_before,
            security_before,
            findings,
        )

        overall_success = bool(a1["success"] and a3["success"])
        final = {
            **run_config,
            "finished_at_utc": now_iso(),
            "success": overall_success,
            "termination_reason": (
                "SUCCESS"
                if overall_success
                else "A3_FAILED"
            ),
            "elapsed_seconds": round(time.perf_counter() - started, 3),
            "a1": {
                "success": a1["success"],
                "iterations_used": a1["iterations_used"],
                "reported_cost_usd": a1["reported_cost_usd_final_by_session"],
                "artifacts": a1["artifacts"],
            },
            "analysis_before": {
                "quality": quality_before["summary"],
                "security": security_before["summary"],
                "findings": findings["summary"],
            },
            "a3": a3,
            "before_after": comp,
            "total_reported_claude_cost_usd": round(
                float(a1["reported_cost_usd_final_by_session"])
                + float(a3.get("reported_cost_usd_final_session", 0.0)),
                8,
            ),
        }
        save_json(run_dir / "FINAL_SUMMARY.json", final)

        lines = [
            f"# Official run — {run_id}",
            "",
            f"- Architecture: **{architecture}**",
            f"- Success: **{overall_success}**",
            f"- A1 iterations: **{a1['iterations_used']}**",
            f"- A1 reported cost: **${a1['reported_cost_usd_final_by_session']}**",
            f"- Findings before A3: **{findings['summary']['total_findings']}**",
            f"- A3 termination: **{a3['termination_reason']}**",
            f"- A3 reported cost: **${a3.get('reported_cost_usd_final_session', 0.0)}**",
            f"- Total reported Claude cost: **${final['total_reported_claude_cost_usd']}**",
            "",
            "The same `run_experiment.py` and frozen environment lock are used for "
            "both architecture arms.",
        ]
        (run_dir / "FINAL_REPORT.md").write_text(
            "\n".join(lines) + "\n", encoding="utf-8"
        )

        print("\n" + "=" * 72)
        print("OFFICIAL EXPERIMENT COMPLETE")
        print("=" * 72)
        print("Run     :", run_id)
        print("Success :", overall_success)
        print("Summary :", run_dir / "FINAL_SUMMARY.json")
        print("Report  :", run_dir / "FINAL_REPORT.md")
        return 0 if overall_success else 1

    except Exception as e:
        save_json(
            run_dir / "RUN_ABORTED.json",
            {
                **run_config,
                "aborted_at_utc": now_iso(),
                "error_type": type(e).__name__,
                "error": str(e),
            },
        )
        raise


def parse_args():
    ap = argparse.ArgumentParser(
        description="Unified official runner for monolith and microservices."
    )
    ap.add_argument(
        "--repo-root",
        type=Path,
        default=Path("."),
    )
    ap.add_argument(
        "--framework-root",
        type=Path,
        default=Path("multiagent-experiment"),
    )
    ap.add_argument(
        "--protocol",
        type=Path,
        default=Path("multiagent-experiment/protocol/official_protocol_v2.json"),
    )
    ap.add_argument(
        "--architecture",
        choices=["monolith", "microservices", "both"],
        required=True,
    )
    ap.add_argument("--seq", type=int)
    ap.add_argument(
        "--prepare-only",
        action="store_true",
        help="Prepare/validate environment without calling Claude.",
    )
    ap.add_argument(
        "--auto-prepare",
        action=argparse.BooleanOptionalAction,
        default=True,
        help="Automatically build/pull missing Graphify/Lizard/Trivy images.",
    )
    ap.add_argument(
        "--initialize-security-cache",
        action="store_true",
        help="Initialize/freeze Trivy DB snapshot. Use only before official runs.",
    )
    ap.add_argument(
        "--write-lock",
        action="store_true",
        help="Write environment lock after successful preparation.",
    )
    return ap.parse_args()


def main():
    args = parse_args()
    repo = args.repo_root.resolve()
    fw = (repo / args.framework_root).resolve()
    protocol_path = (repo / args.protocol).resolve()
    protocol = load_json(protocol_path)

    if args.architecture == "both" and not args.prepare_only:
        raise SystemExit(
            "--architecture both is allowed only with --prepare-only. "
            "Official runs are started one architecture at a time."
        )
    if not args.prepare_only and args.seq is None:
        raise SystemExit("--seq is required for an official run")

    architectures = (
        ["monolith", "microservices"]
        if args.architecture == "both"
        else [args.architecture]
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
            lock_target = repo / protocol["environment_lock"]
            if lock_target.exists():
                for architecture in architectures:
                    verify_lock(
                        repo, fw, protocol, prep["tools"], architecture
                    )
                print("Frozen environment lock verification: PASS")
        print("\nRESULT: OK — preparation only; Claude was not called.")
        if prep["lock_path"]:
            print("Environment lock:", prep["lock_path"])
        return 0

    lock_path, lock = verify_lock(
        repo,
        fw,
        protocol,
        prep["tools"],
        args.architecture,
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
    )


if __name__ == "__main__":
    sys.exit(main())

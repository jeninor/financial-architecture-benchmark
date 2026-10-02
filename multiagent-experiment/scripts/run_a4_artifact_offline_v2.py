#!/usr/bin/env python3
from __future__ import annotations

import argparse
import hashlib
import json
import os
import subprocess
import sys
import time
from datetime import datetime, timezone
from pathlib import Path


def now_iso():
    return datetime.now(timezone.utc).isoformat()


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def run(cmd, timeout=1800):
    return subprocess.run(
        cmd,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        timeout=timeout,
    )


def load_json(path: Path):
    return json.loads(path.read_text(encoding="utf-8"))


def image_info(ref: str) -> dict:
    p = run(["docker", "image", "inspect", ref], 60)
    if p.returncode != 0:
        raise RuntimeError(f"Missing Docker image {ref}\n{p.stdout}\n{p.stderr}")
    row = json.loads(p.stdout)[0]
    return {"ref": ref, "image_id": row.get("Id"), "created": row.get("Created")}


def docker_context() -> str:
    p = run(["docker", "context", "show"], 30)
    if p.returncode != 0:
        raise RuntimeError(p.stdout + p.stderr)
    return p.stdout.strip()


def cache_manifest(root: Path) -> dict:
    rows = []
    if root.exists():
        for p in sorted(root.rglob("*")):
            if p.is_file():
                rows.append({
                    "path": str(p.relative_to(root)),
                    "bytes": p.stat().st_size,
                    "sha256": sha256(p),
                })
    return {"root": str(root), "files": rows}


def find_artifacts(workspace: Path, excluded_suffixes: list[str]) -> list[Path]:
    artifacts = []
    for p in sorted(workspace.rglob("*.jar")):
        s = p.as_posix()
        if "/target/" not in s:
            continue
        name = p.name
        if any(name.endswith(x) for x in excluded_suffixes):
            continue
        # Spring Boot/Maven sometimes leaves an original non-executable jar.
        if name.endswith(".original"):
            continue
        artifacts.append(p)
    return artifacts


def trivy_source_scan(workspace: Path, image: str, cache: Path, policy: dict):
    uid, gid = str(os.getuid()), str(os.getgid())
    cmd = [
        "docker", "run", "--rm",
        "--network", "none",
        "--user", f"{uid}:{gid}",
        "-e", "HOME=/tmp",
        "-v", f"{workspace.resolve()}:/workspace:ro",
        "-v", f"{cache.resolve()}:/cache:ro",
        image, "fs",
        "--cache-dir", "/cache",
        "--format", "json",
        "--quiet",
        "--scanners", ",".join(policy["source_scanners"]),
        "--severity", ",".join(policy["severities"]),
        "--skip-db-update",
        "--skip-java-db-update",
        "--skip-check-update",
        "--skip-version-check",
        "--offline-scan",
        "/workspace",
    ]
    started = time.perf_counter()
    p = run(cmd)
    elapsed = time.perf_counter() - started
    if p.returncode != 0:
        raise RuntimeError(
            "Trivy source scan failed:\n" + p.stdout[-5000:] + "\n" + p.stderr[-5000:]
        )
    return json.loads(p.stdout), elapsed, cmd


def trivy_jar_scan(jar: Path, workspace: Path, image: str, cache: Path, policy: dict):
    uid, gid = str(os.getuid()), str(os.getgid())
    rel = jar.relative_to(workspace)
    cmd = [
        "docker", "run", "--rm",
        "--network", "none",
        "--user", f"{uid}:{gid}",
        "-e", "HOME=/tmp",
        "-v", f"{workspace.resolve()}:/workspace:ro",
        "-v", f"{cache.resolve()}:/cache:ro",
        image, "fs",
        "--cache-dir", "/cache",
        "--format", "json",
        "--quiet",
        "--scanners", "vuln",
        "--severity", ",".join(policy["severities"]),
        "--skip-db-update",
        "--skip-java-db-update",
        "--skip-check-update",
        "--skip-version-check",
        "--offline-scan",
        f"/workspace/{rel.as_posix()}",
    ]
    started = time.perf_counter()
    p = run(cmd)
    elapsed = time.perf_counter() - started
    if p.returncode != 0:
        raise RuntimeError(
            f"Trivy JAR scan failed for {rel}:\n"
            + p.stdout[-5000:] + "\n" + p.stderr[-5000:]
        )
    return json.loads(p.stdout), elapsed, cmd


def sanitize_results(raw_docs: list[dict]) -> dict:
    findings = []
    counts = {
        "vulnerabilities": 0,
        "secrets": 0,
        "misconfigurations": 0,
        "HIGH": 0,
        "CRITICAL": 0,
    }

    for raw in raw_docs:
        for result in raw.get("Results", []) or []:
            target = result.get("Target")

            for v in result.get("Vulnerabilities", []) or []:
                sev = (v.get("Severity") or "UNKNOWN").upper()
                counts["vulnerabilities"] += 1
                if sev in counts:
                    counts[sev] += 1
                findings.append({
                    "agent": "A4",
                    "category": "security",
                    "type": "VULNERABILITY",
                    "severity": sev,
                    "target": target,
                    "id": v.get("VulnerabilityID"),
                    "package": v.get("PkgName"),
                    "installed_version": v.get("InstalledVersion"),
                    "fixed_version": v.get("FixedVersion"),
                    "title": v.get("Title"),
                    "primary_url": v.get("PrimaryURL"),
                })

            for s in result.get("Secrets", []) or []:
                sev = (s.get("Severity") or "UNKNOWN").upper()
                counts["secrets"] += 1
                if sev in counts:
                    counts[sev] += 1
                findings.append({
                    "agent": "A4",
                    "category": "security",
                    "type": "SECRET",
                    "severity": sev,
                    "target": target,
                    "rule_id": s.get("RuleID"),
                    "category_name": s.get("Category"),
                    "title": s.get("Title"),
                    "start_line": s.get("StartLine"),
                    "end_line": s.get("EndLine"),
                })

            for m in result.get("Misconfigurations", []) or []:
                sev = (m.get("Severity") or "UNKNOWN").upper()
                counts["misconfigurations"] += 1
                if sev in counts:
                    counts[sev] += 1
                cm = m.get("CauseMetadata") or {}
                findings.append({
                    "agent": "A4",
                    "category": "security",
                    "type": "MISCONFIGURATION",
                    "severity": sev,
                    "target": target,
                    "id": m.get("ID") or m.get("AVDID"),
                    "title": m.get("Title"),
                    "message": m.get("Message"),
                    "resolution": m.get("Resolution"),
                    "start_line": cm.get("StartLine"),
                    "end_line": cm.get("EndLine"),
                })

    return {
        "schema_version": 2,
        "agent": "A4",
        "summary": counts,
        "findings": findings,
    }


def combine(quality: dict, security: dict):
    q = quality.get("findings", []) or []
    s = security.get("findings", []) or []
    all_f = q + s
    by_agent = {"A2": len(q), "A4": len(s)}
    by_sev, by_type = {}, {}
    for f in all_f:
        sev, typ = f.get("severity", "UNKNOWN"), f.get("type", "UNKNOWN")
        by_sev[sev] = by_sev.get(sev, 0) + 1
        by_type[typ] = by_type.get(typ, 0) + 1
    return {
        "schema_version": 2,
        "generated_at_utc": now_iso(),
        "agents": ["A2", "A4"],
        "summary": {
            "total_findings": len(all_f),
            "by_agent": by_agent,
            "by_severity": by_sev,
            "by_type": by_type,
        },
        "quality_summary": quality.get("summary"),
        "security_summary": security.get("summary"),
        "findings": all_f,
    }


def report(path: Path, workspace: Path, quality: dict, security: dict,
           findings: dict, artifacts: list[dict], timings: dict, meta: dict):
    qs = quality["summary"]
    ss = security["summary"]
    lines = [
        "# A2 Quality + A4 Security report — v2",
        "",
        f"- Workspace: `{workspace}`",
        f"- Docker context: `{meta['docker_context']}`",
        f"- Trivy image: `{meta['trivy']['ref']}`",
        f"- Network during A4: **disabled**",
        f"- Trivy offline scan: **enabled**",
        "",
        "## A2 — Quality",
        "",
        f"- Physical Java LOC: **{qs['physical_java_loc']}**",
        f"- Java files: **{qs['java_files']}**",
        f"- Lizard NLOC: **{qs['lizard_nloc']}**",
        f"- Functions/methods: **{qs['functions']}**",
        f"- CC total: **{qs['cc_total']}**",
        f"- CC average: **{qs['cc_average']}**",
        f"- CC max: **{qs['cc_max']}**",
        f"- Quality findings: **{qs['quality_findings']}**",
        "",
        "## A4 — Security",
        "",
        f"- Built JAR artifacts scanned: **{len(artifacts)}**",
        f"- Vulnerabilities: **{ss['vulnerabilities']}**",
        f"- Secrets: **{ss['secrets']}**",
        f"- Misconfigurations: **{ss['misconfigurations']}**",
        f"- HIGH: **{ss['HIGH']}**",
        f"- CRITICAL: **{ss['CRITICAL']}**",
        "",
        "### Artifacts",
        "",
    ]
    for a in artifacts:
        lines.append(f"- `{a['path']}` — `{a['sha256']}` ({a['bytes']} bytes)")

    lines += [
        "",
        "## Findings for A3",
        "",
        f"- Total: **{findings['summary']['total_findings']}**",
        "",
        "| Agent | Type | Severity | Target | Detail |",
        "|---|---|---|---|---|",
    ]
    for f in findings["findings"][:100]:
        target = str(f.get("file") or f.get("target") or "").replace("|", "\\|")
        detail = str(
            f.get("symbol") or f.get("id") or f.get("package")
            or f.get("title") or f.get("message") or ""
        ).replace("|", "\\|").replace("\n", " ")
        lines.append(
            f"| {f.get('agent','')} | {f.get('type','')} | "
            f"{f.get('severity','')} | {target} | {detail} |"
        )
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--workspace", type=Path, required=True)
    ap.add_argument("--quality-json", type=Path, required=True,
                    help="Existing successful A2 quality_before.json")
    ap.add_argument("--output-dir", type=Path, required=True)
    ap.add_argument("--trivy-image", default="aquasec/trivy:0.74.0")
    ap.add_argument("--trivy-cache", type=Path, required=True)
    ap.add_argument("--security-policy", type=Path, required=True)
    args = ap.parse_args()

    workspace = args.workspace.resolve()
    quality_json = args.quality_json.resolve()
    out = args.output_dir.resolve()
    cache = args.trivy_cache.resolve()
    policy_path = args.security_policy.resolve()

    if out.exists() and any(out.iterdir()):
        raise SystemExit(f"Output directory not empty: {out}")
    out.mkdir(parents=True, exist_ok=True)

    quality = load_json(quality_json)
    policy = load_json(policy_path)

    artifacts = find_artifacts(workspace, policy.get("exclude_jar_suffixes", []))
    if policy.get("require_built_jar", True) and not artifacts:
        raise SystemExit(
            "A4_ARTIFACT_MISSING: no target/*.jar artifact found.\n"
            "Do not fall back to online pom.xml resolution. Build/package the "
            "application first, then rerun A4."
        )

    artifact_meta = [{
        "path": str(p.relative_to(workspace)),
        "bytes": p.stat().st_size,
        "sha256": sha256(p),
    } for p in artifacts]

    meta = {
        "generated_at_utc": now_iso(),
        "docker_context": docker_context(),
        "trivy": image_info(args.trivy_image),
        "security_policy_sha256": sha256(policy_path),
        "trivy_cache_manifest": cache_manifest(cache),
        "quality_source": str(quality_json),
        "quality_source_sha256": sha256(quality_json),
        "network_disabled": True,
        "offline_scan": True,
        "artifacts": artifact_meta,
    }

    print("===== A4 SOURCE: SECRET + MISCONFIG =====", flush=True)
    raw_source, t_source, cmd_source = trivy_source_scan(
        workspace, args.trivy_image, cache, policy
    )

    raw_artifacts = []
    artifact_timings = []
    for jar in artifacts:
        rel = jar.relative_to(workspace)
        print(f"===== A4 JAR VULN: {rel} =====", flush=True)
        raw, elapsed, cmd = trivy_jar_scan(
            jar, workspace, args.trivy_image, cache, policy
        )
        raw_artifacts.append(raw)
        artifact_timings.append({
            "artifact": str(rel),
            "elapsed_seconds": round(elapsed, 3),
        })

    all_raw = [raw_source] + raw_artifacts
    security = sanitize_results(all_raw)
    findings = combine(quality, security)

    (out/"trivy_source_raw.json").write_text(
        json.dumps(raw_source, indent=2, ensure_ascii=False), encoding="utf-8"
    )
    (out/"trivy_artifacts_raw.json").write_text(
        json.dumps(raw_artifacts, indent=2, ensure_ascii=False), encoding="utf-8"
    )
    (out/"security_before.json").write_text(
        json.dumps(security, indent=2, ensure_ascii=False), encoding="utf-8"
    )
    (out/"findings.json").write_text(
        json.dumps(findings, indent=2, ensure_ascii=False), encoding="utf-8"
    )

    timings = {
        "source_secret_misconfig_seconds": round(t_source, 3),
        "artifact_vulnerability_scans": artifact_timings,
        "total_a4_seconds": round(
            t_source + sum(x["elapsed_seconds"] for x in artifact_timings), 3
        ),
    }
    meta["timings"] = timings
    (out/"analysis_manifest.json").write_text(
        json.dumps(meta, indent=2, ensure_ascii=False), encoding="utf-8"
    )

    report(
        out/"A2_A4_REPORT.md",
        workspace, quality, security, findings,
        artifact_meta, timings, meta
    )

    print(json.dumps(security["summary"], indent=2), flush=True)
    print("\n===== A4 COMPLETE =====")
    print("Security:", out/"security_before.json")
    print("Findings:", out/"findings.json")
    print("Report  :", out/"A2_A4_REPORT.md")
    return 0


if __name__ == "__main__":
    sys.exit(main())

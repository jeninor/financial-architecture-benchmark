#!/usr/bin/env python3
from __future__ import annotations
import argparse
from pathlib import Path

DEFAULT_MS_SERVICES = {
    "discovery-server",
    "api-gateway",
    "user-service",
    "market-service",
    "trade-service",
}

def prod_java(root: Path):
    return [
        p for p in root.rglob("*.java")
        if "/src/main/java/" in p.as_posix()
    ]

def only_entrypoints(root: Path):
    java = prod_java(root)
    extra = [p for p in java if not p.name.endswith("Application.java")]
    return java, extra

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--framework-root", required=True, type=Path)
    ap.add_argument(
        "--microservice-names",
        default=",".join(sorted(DEFAULT_MS_SERVICES)),
    )
    args = ap.parse_args()

    framework = args.framework_root.resolve()
    base = framework / "baselines"

    problems = []

    mono = base / "monolith"
    for rel in ["docker-compose.yml", "financial-monolith/pom.xml"]:
        if not (mono / rel).exists():
            problems.append(f"monolith missing: {rel}")

    mono_java, mono_extra = only_entrypoints(mono)
    if mono_extra:
        problems.append(
            "monolith business Java leaked into baseline: "
            + ", ".join(str(x.relative_to(mono)) for x in mono_extra)
        )

    services = [x.strip() for x in args.microservice_names.split(",") if x.strip()]
    ms = base / "microservices"
    if not (ms / "docker-compose.yml").exists():
        problems.append("microservices missing: docker-compose.yml")
    for svc in services:
        if not (ms / svc / "pom.xml").exists():
            problems.append(f"microservices missing: {svc}/pom.xml")

    ms_java, ms_extra = only_entrypoints(ms)
    if ms_extra:
        problems.append(
            "microservices business Java leaked into baseline: "
            + ", ".join(str(x.relative_to(ms)) for x in ms_extra)
        )

    print(f"Monolith production Java files: {len(mono_java)}")
    print(f"Microservices production Java files: {len(ms_java)}")

    if problems:
        print("[FAIL]")
        for p in problems:
            print(" -", p)
        raise SystemExit(1)

    print("[OK] Baselines preserve architecture skeletons only.")
    print("[OK] No business Java implementation detected.")

if __name__ == "__main__":
    main()

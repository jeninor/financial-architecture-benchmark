#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
from pathlib import Path

EXPECTED_MS = {
    "discovery-server",
    "api-gateway",
    "user-service",
    "market-service",
    "trade-service",
}

def production_java_files(root: Path) -> list[Path]:
    return [
        p for p in root.rglob("*.java")
        if "/src/main/java/" in p.as_posix()
    ]

def check_monolith(root: Path) -> list[str]:
    problems = []
    expected = [
        root / "docker-compose.yml",
        root / "financial-monolith/pom.xml",
    ]
    for p in expected:
        if not p.exists():
            problems.append(f"missing: {p}")

    java = production_java_files(root)
    non_main = [p for p in java if not p.name.endswith("Application.java")]
    if non_main:
        problems.append(
            "monolith contains business Java files: "
            + ", ".join(str(p.relative_to(root)) for p in non_main)
        )
    return problems

def check_microservices(root: Path) -> list[str]:
    problems = []
    if not (root / "docker-compose.yml").exists():
        problems.append("missing docker-compose.yml")

    for service in EXPECTED_MS:
        if not (root / service / "pom.xml").exists():
            problems.append(f"{service}: missing pom.xml")

    java = production_java_files(root)
    non_main = [p for p in java if not p.name.endswith("Application.java")]
    if non_main:
        problems.append(
            "microservices baseline contains business Java files: "
            + ", ".join(str(p.relative_to(root)) for p in non_main)
        )
    return problems

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--framework-root", required=True, type=Path)
    args = ap.parse_args()
    base = args.framework_root.resolve() / "baselines"

    all_problems = []
    for arch, checker in [
        ("monolith", check_monolith),
        ("microservices", check_microservices),
    ]:
        root = base / arch
        problems = checker(root)
        if problems:
            print(f"[FAIL] {arch}")
            for p in problems:
                print("  -", p)
            all_problems.extend(problems)
        else:
            java = production_java_files(root)
            print(f"[OK] {arch}: {len(java)} production Java file(s), only application entrypoints")

    if all_problems:
        raise SystemExit(1)

    print("[OK] Baselines contain infrastructure/build skeletons only.")

if __name__ == "__main__":
    main()

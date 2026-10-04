#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import socket
import subprocess
import sys
from pathlib import Path

from graphify_runtime_v4 import ensure_graphify_image


EXPECTED_APP_SERVICES = {
    "discovery-server",
    "api-gateway",
    "user-service",
    "market-service",
    "trade-service",
}


def run(cmd, cwd=None, timeout=120):
    return subprocess.run(
        cmd,
        cwd=str(cwd) if cwd else None,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        timeout=timeout,
    )


def port_open(port):
    try:
        with socket.create_connection(("127.0.0.1", port), timeout=.5):
            return True
    except OSError:
        return False


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--repo-root", type=Path, default=Path("."))
    ap.add_argument("--framework-root", type=Path, default=Path("multiagent-experiment"))
    ap.add_argument("--graphify-image", default="graphify-mcp:0.9.73")
    ap.add_argument("--lizard-image", default="multiagent-lizard:1.24.0")
    ap.add_argument("--trivy-image", default="aquasec/trivy:0.74.0")
    args = ap.parse_args()

    repo = args.repo_root.resolve()
    fw = (repo / args.framework_root).resolve()
    baseline = fw / "baselines" / "microservices"

    errors = []
    print("===== MICROSERVICES PREFLIGHT =====")

    if not baseline.exists():
        errors.append(f"Missing baseline: {baseline}")
    if not (baseline / "docker-compose.yml").exists():
        errors.append("Missing microservices docker-compose.yml")

    required_files = [
        fw / "contracts" / "architecture_microservices.md",
        fw / "contracts" / "public_api_v1.md",
        fw / "contracts" / "requirements.md",
        fw / "acceptance-tests" / "run_acceptance.py",
        fw / "acceptance-tests" / "acceptance_contract.json",
        fw / "prompts" / "builder_microservices.md",
    ]
    for p in required_files:
        if not p.exists():
            errors.append(f"Missing {p}")

    if not errors:
        p = run(["docker", "compose", "config", "--services"], baseline)
        if p.returncode:
            errors.append("docker compose config failed:\n" + p.stdout)
            services = []
        else:
            services = [x.strip() for x in p.stdout.splitlines() if x.strip()]
            print("Compose services:", ", ".join(services))
            missing = sorted(EXPECTED_APP_SERVICES - set(services))
            if missing:
                errors.append("Missing expected app services: " + ", ".join(missing))

        main_classes = sorted(
            baseline.glob("*/src/main/java/**/*Application.java")
        )
        poms = sorted(baseline.glob("*/pom.xml"))
        business_java = [
            p for p in baseline.glob("*/src/main/java/**/*.java")
            if not p.name.endswith("Application.java")
        ]

        print("Spring Boot main classes:", len(main_classes))
        for p in main_classes:
            print("  ", p.relative_to(baseline))
        print("Module POMs:", len(poms))
        print("Non-main production Java in baseline:", len(business_java))

        if len(main_classes) < 5:
            errors.append(
                f"Expected at least 5 Spring Boot skeleton main classes, found {len(main_classes)}"
            )
        if business_java:
            errors.append(
                "Baseline contains business Java; expected architecture skeleton only"
            )

    try:
        g = ensure_graphify_image(args.graphify_image)
        print("Graphify:", g["runtime_ref"])
        print("Docker context:", g["docker_context"])
    except Exception as e:
        errors.append(str(e))

    for image in [args.lizard_image, args.trivy_image]:
        p = run(["docker", "image", "inspect", image])
        if p.returncode:
            errors.append(f"Missing Docker image: {image}")
        else:
            row = json.loads(p.stdout)[0]
            print(f"{image}: {row.get('Id')}")

    if port_open(8080):
        errors.append("Port 8080 is already in use")

    if errors:
        print("\nRESULT: FAIL")
        for e in errors:
            print(" -", e)
        return 1

    print("\nRESULT: OK")
    return 0


if __name__ == "__main__":
    sys.exit(main())

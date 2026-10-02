#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import subprocess
import sys
import time
from pathlib import Path


def run(cmd, timeout=600):
    return subprocess.run(
        cmd, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
        timeout=timeout
    )


def load_json(path: Path):
    return json.loads(path.read_text(encoding="utf-8"))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--workspace", type=Path, required=True)
    ap.add_argument("--output-dir", type=Path, required=True)
    ap.add_argument("--lizard-image", default="multiagent-lizard:1.24.0")
    ap.add_argument("--quality-policy", type=Path, required=True)
    args = ap.parse_args()

    workspace = args.workspace.resolve()
    out = args.output_dir.resolve()
    policy = load_json(args.quality_policy.resolve())["thresholds"]

    if not workspace.exists():
        raise SystemExit(f"Missing workspace: {workspace}")
    if out.exists() and any(out.iterdir()):
        raise SystemExit(f"Output directory not empty: {out}")
    out.mkdir(parents=True, exist_ok=True)

    cmd = [
        "docker", "run", "--rm",
        "-v", f"{workspace}:/workspace:ro",
        args.lizard_image,
        "/workspace",
        "--max-function-ccn", str(policy["max_function_ccn"]),
        "--max-function-nloc", str(policy["max_function_nloc"]),
        "--max-file-nloc", str(policy["max_file_nloc"]),
        "--max-parameters", str(policy["max_parameters"]),
    ]

    started = time.perf_counter()
    p = run(cmd)
    elapsed = time.perf_counter() - started
    if p.returncode:
        raise RuntimeError("A2 failed:\n" + p.stdout[-8000:])

    data = json.loads(p.stdout)
    (out / "quality_before.json").write_text(
        json.dumps(data, indent=2, ensure_ascii=False), encoding="utf-8"
    )

    s = data["summary"]
    report = [
        "# A2 Quality report",
        "",
        f"- Physical Java LOC: **{s['physical_java_loc']}**",
        f"- Java files: **{s['java_files']}**",
        f"- Lizard NLOC: **{s['lizard_nloc']}**",
        f"- Functions/methods: **{s['functions']}**",
        f"- CC total: **{s['cc_total']}**",
        f"- CC average: **{s['cc_average']}**",
        f"- CC max: **{s['cc_max']}**",
        f"- Quality findings: **{s['quality_findings']}**",
        f"- Analysis time: **{elapsed:.3f} s**",
    ]
    (out / "A2_REPORT.md").write_text("\n".join(report) + "\n", encoding="utf-8")

    print(json.dumps(s, indent=2))
    print("Quality:", out / "quality_before.json")
    return 0


if __name__ == "__main__":
    sys.exit(main())

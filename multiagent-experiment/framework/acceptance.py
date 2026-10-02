from __future__ import annotations

import json
import subprocess
from pathlib import Path


def run_acceptance_suite(
    project_root: Path,
    run_id: str,
    base_url: str,
    output_path: Path,
    timeout_seconds: int = 180,
) -> dict:
    runner = project_root / "acceptance-tests" / "run_acceptance.py"

    cmd = [
        "python3",
        str(runner),
        "--base-url", base_url,
        "--run-id", run_id,
        "--output", str(output_path),
    ]

    proc = subprocess.run(
        cmd,
        cwd=str(project_root),
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        timeout=timeout_seconds,
    )

    result = {}
    if output_path.exists():
        result = json.loads(output_path.read_text(encoding="utf-8"))

    result["process_returncode"] = proc.returncode
    result["process_output"] = proc.stdout
    result["success"] = proc.returncode == 0
    return result

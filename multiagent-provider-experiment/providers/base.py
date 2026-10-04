from __future__ import annotations

import json
import re
import subprocess
import time
from dataclasses import dataclass, asdict
from pathlib import Path
from typing import Any


# Provider agents never own Docker. Docker belongs to the orchestrator.
#
# Detect Docker only when it is being invoked as a shell command.
# References to project files such as docker-compose.yml are allowed.
# Bubblewrap remains the isolation boundary; this regex is the
# trace-level policy audit.
FORBIDDEN_DOCKER = re.compile(
    r"(?:^|(?:&&|\|\||[;|\n])\s*)"
    r"(?:(?:sudo(?:\s+-\S+)*|command)\s+)?"
    r"(?:env\s+)?"
    r"(?:[A-Za-z_][A-Za-z0-9_]*=[^\s;&|]+\s+)*"
    r"(?:[^\s;&|]*/)?"
    r"docker(?:-compose)?"
    r"(?=\s|$|[;&|])",
    re.IGNORECASE,
)

# Explicit network clients / remote Git operations.
FORBIDDEN_NETWORK_COMMAND = re.compile(
    r"(?<![\w.-])(?:curl|wget|scp|ssh|ftp|telnet)\b"
    r"|\bgit\s+(?:clone|fetch|pull|push|ls-remote)\b",
    re.IGNORECASE,
)

# A3 may use shell only as a file-inspection/editing mechanism.
FORBIDDEN_A3_BUILD_TEST = re.compile(
    r"(?<![\w.-])(?:mvn|mvnw|gradle|gradlew|javac)\b"
    r"|(?:^|\s)\./(?:mvnw|gradlew)\b"
    r"|\bpytest\b"
    r"|\bnpm\s+(?:test|run)\b"
    r"|\byarn\s+(?:test|run)\b",
    re.IGNORECASE,
)


@dataclass
class ProviderResult:
    provider: str
    model: str | None
    session_id: str | None
    success: bool
    returncode: int
    elapsed_seconds: float

    input_tokens: int | None = None
    output_tokens: int | None = None
    cached_tokens: int | None = None
    thinking_tokens: int | None = None

    native_turns: int | None = None

    tool_calls: int = 0
    failed_tool_calls: int = 0

    policy_violation: bool = False
    policy_violations: list[str] | None = None

    final_response: str = ""
    native_usage: dict[str, Any] | None = None

    trace_path: str | None = None
    stderr_path: str | None = None

    def to_dict(self):
        return asdict(self)


def write_json(path: Path, obj: Any) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(
        json.dumps(obj, indent=2, ensure_ascii=False),
        encoding="utf-8",
    )


def command_policy_violations(
    command: str,
    stage: str = "a1",
) -> list[str]:

    violations: list[str] = []

    if FORBIDDEN_DOCKER.search(command):
        violations.append(
            f"forbidden provider Docker command: {command}"
        )

    if FORBIDDEN_NETWORK_COMMAND.search(command):
        violations.append(
            f"forbidden provider network command: {command}"
        )

    if stage.lower() == "a3" and FORBIDDEN_A3_BUILD_TEST.search(command):
        violations.append(
            f"forbidden A3 build/test command: {command}"
        )

    return violations


def run_streaming_process(
    cmd: list[str],
    cwd: Path,
    trace_path: Path,
    stderr_path: Path,
    timeout_seconds: int,
) -> tuple[int, float]:

    trace_path.parent.mkdir(parents=True, exist_ok=True)

    started = time.perf_counter()

    with trace_path.open("w", encoding="utf-8") as trace, \
         stderr_path.open("w", encoding="utf-8") as err:

        proc = subprocess.Popen(
            cmd,
            cwd=str(cwd),
            text=True,
            stdout=subprocess.PIPE,
            stderr=err,
            bufsize=1,
        )

        assert proc.stdout is not None
        deadline = time.monotonic() + timeout_seconds

        try:
            for line in proc.stdout:
                trace.write(line)
                trace.flush()

                if time.monotonic() > deadline:
                    proc.kill()
                    raise TimeoutError(
                        f"Provider timeout after {timeout_seconds}s"
                    )

            rc = proc.wait()

        except Exception:
            proc.kill()
            proc.wait()
            raise

    return rc, time.perf_counter() - started

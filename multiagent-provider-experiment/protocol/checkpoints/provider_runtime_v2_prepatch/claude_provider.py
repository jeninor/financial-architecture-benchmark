from __future__ import annotations

import json
import subprocess
from pathlib import Path

from .base import (
    ProviderResult,
    command_policy_violations,
    run_streaming_process,
)

from .bwrap_sandbox import (
    build_bwrap_command,
    provider_binary_path,
)


A1_ALLOWED_TOOLS = {
    "Read",
    "Write",
    "Edit",
    "Glob",
    "Grep",
    "Bash",
}

A3_ALLOWED_TOOLS = {
    "Read",
    "Write",
    "Edit",
    "Glob",
    "Grep",
}


class ClaudeProvider:

    name = "claude"

    def __init__(self):
        self.executable = provider_binary_path(
            self.name,
            "claude",
        )

    def version(self) -> str:
        p = subprocess.run(
            [str(self.executable), "--version"],
            text=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            timeout=30,
        )

        return p.stdout.strip()

    @staticmethod
    def _allowed_tools(stage: str) -> set[str]:
        if stage.lower() == "a3":
            return A3_ALLOWED_TOOLS

        return A1_ALLOWED_TOOLS

    @staticmethod
    def _max_turns(
        stage: str,
        session_id: str | None,
    ) -> int:

        repair = session_id is not None

        if stage.lower() == "a3":
            return 5 if repair else 8

        return 8 if repair else 15

    def run(
        self,
        *,
        workspace: Path,
        prompt: str,
        trace_path: Path,
        session_id: str | None = None,
        stage: str = "a1",
        model: str = "claude-sonnet-5-5",
        timeout_seconds: int = 2400,
    ) -> ProviderResult:

        workspace = workspace.resolve()

        allowed = self._allowed_tools(stage)

        argv = [
            "-p",
            prompt,

            "--output-format",
            "stream-json",

            "--verbose",

            "--max-turns",
            str(
                self._max_turns(
                    stage,
                    session_id,
                )
            ),

            "--model",
            model,

            "--allowedTools",
            ",".join(sorted(allowed)),
        ]

        if session_id:
            argv += [
                "--resume",
                session_id,
            ]

        cmd = build_bwrap_command(
            provider=self.name,
            workspace=workspace,
            executable_name="claude",
            argv=argv,
        )

        stderr_path = trace_path.with_suffix(
            ".stderr.log"
        )

        rc, elapsed = run_streaming_process(
            cmd,
            workspace,
            trace_path,
            stderr_path,
            timeout_seconds,
        )

        observed_session = session_id
        observed_model = model

        final_response = ""
        usage: dict = {}
        native_usage: dict = {}
        native_turns = None

        tool_calls = 0
        failed_tool_calls = 0

        violations: list[str] = []

        result_success = False

        with trace_path.open(
            encoding="utf-8"
        ) as f:

            for line in f:
                try:
                    event = json.loads(line)
                except Exception:
                    continue

                etype = event.get("type")

                if etype == "system":
                    if event.get("subtype") == "init":
                        observed_session = (
                            event.get("session_id")
                            or observed_session
                        )

                        observed_model = (
                            event.get("model")
                            or observed_model
                        )

                elif etype == "assistant":
                    message = event.get("message") or {}

                    message_model = message.get("model")

                    if message_model:
                        observed_model = message_model

                    for block in (
                        message.get("content") or []
                    ):
                        if block.get("type") != "tool_use":
                            continue

                        tool_calls += 1

                        tool_name = (
                            block.get("name") or ""
                        )

                        tool_input = (
                            block.get("input") or {}
                        )

                        if tool_name not in allowed:
                            violations.append(
                                "forbidden Claude tool: "
                                f"{tool_name}"
                            )

                        if tool_name == "Bash":
                            command = (
                                tool_input.get("command")
                                or tool_input.get(
                                    "CommandLine"
                                )
                                or ""
                            )

                            violations.extend(
                                command_policy_violations(
                                    command,
                                    stage=stage,
                                )
                            )

                elif etype == "result":
                    observed_session = (
                        event.get("session_id")
                        or observed_session
                    )

                    final_response = (
                        event.get("result")
                        or ""
                    )

                    usage = event.get("usage") or {}
                    native_turns = event.get(
                        "num_turns"
                    )

                    permission_denials = (
                        event.get(
                            "permission_denials"
                        )
                        or []
                    )

                    failed_tool_calls += len(
                        permission_denials
                    )

                    model_usage = (
                        event.get("modelUsage")
                        or {}
                    )

                    canonical_models = {
                        data.get("canonicalModel")
                        for data in model_usage.values()
                        if isinstance(data, dict)
                        and data.get(
                            "canonicalModel"
                        )
                    }

                    if len(canonical_models) == 1:
                        observed_model = next(
                            iter(canonical_models)
                        )

                    native_usage = {
                        "usage": usage,
                        "modelUsage": model_usage,
                        "total_cost_usd":
                            event.get(
                                "total_cost_usd"
                            ),
                        "permission_denials":
                            permission_denials,
                        "terminal_reason":
                            event.get(
                                "terminal_reason"
                            ),
                        "subtype":
                            event.get("subtype"),
                    }

                    result_success = (
                        event.get("subtype")
                        == "success"
                        and not event.get(
                            "is_error",
                            False,
                        )
                    )

        thinking = (
            (
                usage.get(
                    "output_tokens_details"
                )
                or {}
            ).get(
                "thinking_tokens"
            )
        )

        return ProviderResult(
            provider=self.name,
            model=observed_model,
            session_id=observed_session,

            success=(
                rc == 0
                and result_success
                and not violations
            ),

            returncode=rc,
            elapsed_seconds=round(
                elapsed,
                3,
            ),

            input_tokens=usage.get(
                "input_tokens"
            ),
            output_tokens=usage.get(
                "output_tokens"
            ),
            cached_tokens=usage.get(
                "cache_read_input_tokens"
            ),
            thinking_tokens=thinking,

            native_turns=native_turns,

            tool_calls=tool_calls,
            failed_tool_calls=
                failed_tool_calls,

            policy_violation=bool(
                violations
            ),
            policy_violations=violations,

            final_response=
                final_response,

            native_usage=native_usage,

            trace_path=str(trace_path),
            stderr_path=str(
                stderr_path
            ),
        )

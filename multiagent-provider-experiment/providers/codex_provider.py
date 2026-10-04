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


class CodexProvider:

    name = "codex"

    def __init__(self):
        self.executable = provider_binary_path(
            self.name,
            "codex",
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
    def _common_args() -> list[str]:
        return [
            "--skip-git-repo-check",
            "--ignore-user-config",
            "--ignore-rules",

            "-c",
            'model_reasoning_effort="medium"',

            "-c",
            'sandbox_mode="workspace-write"',

            "-c",
            'approval_policy="never"',

            "-c",
            "sandbox_workspace_write.exclude_slash_tmp=true",

            "-c",
            "sandbox_workspace_write.exclude_tmpdir_env_var=true",

            "-c",
            "sandbox_workspace_write.network_access=false",

            "--json",
        ]

    def run(
        self,
        *,
        workspace: Path,
        prompt: str,
        trace_path: Path,
        session_id: str | None = None,
        stage: str = "a1",
        model: str | None = None,
        timeout_seconds: int = 2400,
    ) -> ProviderResult:

        workspace = workspace.resolve()

        if not model:
            raise RuntimeError(
                "Codex model must be explicit"
            )

        if session_id:
            argv = [
                "exec",
                "resume",
                *self._common_args(),
                "--model",
                model,
                session_id,
                prompt,
            ]

        else:
            argv = [
                "exec",
                *self._common_args(),
                "--model",
                model,
                prompt,
            ]

        cmd = build_bwrap_command(
            provider=self.name,
            workspace=workspace,
            executable_name="codex",
            argv=argv,
            fresh_runtime=(session_id is None),
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

        thread_id = session_id

        usage: dict = {}
        final_response = ""

        tool_calls = 0
        failed_tool_calls = 0

        violations: list[str] = []

        completed_turn = False
        provider_runtime_error = False

        with trace_path.open(
            encoding="utf-8"
        ) as f:

            for line in f:
                try:
                    event = json.loads(line)
                except Exception:
                    continue

                etype = event.get("type")

                if etype == "thread.started":
                    thread_id = (
                        event.get("thread_id")
                        or thread_id
                    )

                elif etype == "item.completed":
                    item = event.get("item") or {}
                    itype = item.get("type")

                    if itype == "agent_message":
                        text = item.get("text")

                        if text:
                            final_response = text

                    elif itype == "command_execution":
                        tool_calls += 1

                        status = (
                            item.get("status")
                            or ""
                        )

                        exit_code = item.get(
                            "exit_code"
                        )

                        nonzero_exit = False

                        if isinstance(exit_code, int):
                            nonzero_exit = (
                                exit_code != 0
                            )

                        elif isinstance(
                            exit_code,
                            str,
                        ):
                            try:
                                nonzero_exit = (
                                    int(exit_code) != 0
                                )
                            except ValueError:
                                pass

                        if (
                            status == "failed"
                            or nonzero_exit
                        ):
                            failed_tool_calls += 1

                        command = (
                            item.get("command")
                            or ""
                        )

                        violations.extend(
                            command_policy_violations(
                                command,
                                stage=stage,
                            )
                        )

                    elif itype == "error":
                        # Provider/runtime-level errors such as a
                        # missing code-mode host must not be treated
                        # as successful merely because turn.completed
                        # is later emitted.
                        provider_runtime_error = True
                        failed_tool_calls += 1

                        message = (
                            item.get("message")
                            or ""
                        )

                        if message:
                            final_response = message

                elif etype == "error":
                    provider_runtime_error = True

                elif etype == "turn.completed":
                    completed_turn = True
                    usage = (
                        event.get("usage")
                        or {}
                    )

        return ProviderResult(
            provider=self.name,
            model=model,
            session_id=thread_id,

            success=(
                rc == 0
                and completed_turn
                and not provider_runtime_error
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
                "cached_input_tokens"
            ),
            thinking_tokens=usage.get(
                "reasoning_output_tokens"
            ),

            # Codex CLI's event model is provider-native and
            # not cross-provider comparable.
            native_turns=(
                1
                if completed_turn
                else 0
            ),

            tool_calls=tool_calls,
            failed_tool_calls=
                failed_tool_calls,

            policy_violation=bool(
                violations
            ),
            policy_violations=violations,

            final_response=
                final_response,

            native_usage=usage,

            trace_path=str(
                trace_path
            ),
            stderr_path=str(
                stderr_path
            ),
        )

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


FORBIDDEN_TOOLS = {
    "search_web",
    "search_marketplace",
    "read_url_content",
    "open_browser_url",

    "browser_click_element",
    "browser_drag_pixel_to_pixel",
    "browser_get_dom",
    "browser_get_network_request",
    "browser_input",
    "browser_list_network_requests",
    "browser_mouse_down",
    "browser_mouse_up",
    "browser_move_mouse",
    "browser_press_key",
    "browser_refresh_page",
    "browser_resize_window",
    "browser_scroll",
    "browser_scroll_dom",
    "browser_select_option",
    "browser_subagent",
    "capture_browser_console_logs",
    "capture_browser_screenshot",
    "click_browser_pixel",
    "execute_browser_javascript",
    "list_browser_pages",
    "read_browser_page",

    "call_mcp_tool",

    "define_subagent",
    "invoke_subagent",
    "manage_subagents",
}


class AntigravityProvider:

    name = "antigravity"

    def __init__(self):
        self.executable = provider_binary_path(
            self.name,
            "agy",
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

    def run(
        self,
        *,
        workspace: Path,
        prompt: str,
        trace_path: Path,
        session_id: str | None = None,
        stage: str = "a1",
        model: str = "gemini-3.1-pro-high",
        timeout_seconds: int = 2400,
    ) -> ProviderResult:

        workspace = workspace.resolve()

        if not model:
            raise RuntimeError(
                "Antigravity model must be explicit"
            )

        argv = [
            "--model",
            model,

            "--mode",
            "accept-edits",

            "--sandbox",

            "--output-format",
            "stream-json",
        ]

        if session_id:
            argv += [
                "--conversation",
                session_id,
            ]

        argv += [
            f"--print={prompt}",
        ]

        cmd = build_bwrap_command(
            provider=self.name,
            workspace=workspace,
            executable_name="agy",
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

        conversation_id = session_id
        native_model = model

        final_response = ""
        usage: dict = {}
        native_turns = None

        tool_calls = 0
        failed_tool_calls = 0

        violations: list[str] = []

        seen_tool_steps: set[int] = set()

        result_status_success = False

        with trace_path.open(
            encoding="utf-8"
        ) as f:

            for line in f:
                try:
                    event = json.loads(line)
                except Exception:
                    continue

                etype = event.get("event")

                if etype == "init":
                    conversation_id = (
                        event.get(
                            "conversation_id"
                        )
                        or conversation_id
                    )

                    init = (
                        event.get("init")
                        or {}
                    )

                    native_model = (
                        init.get("model")
                        or native_model
                    )

                elif etype == "step_update":
                    step = (
                        event.get(
                            "step_update"
                        )
                        or {}
                    )

                    if (
                        step.get("step_type")
                        != "tool"
                    ):
                        continue

                    idx = step.get(
                        "step_index"
                    )

                    state = step.get(
                        "state"
                    )

                    if (
                        state == "DONE"
                        and idx
                        not in seen_tool_steps
                    ):
                        seen_tool_steps.add(idx)
                        tool_calls += 1

                        tool_name = (
                            step.get(
                                "tool_name"
                            )
                            or ""
                        )

                        if (
                            tool_name
                            in FORBIDDEN_TOOLS
                        ):
                            violations.append(
                                "forbidden "
                                "Antigravity tool: "
                                f"{tool_name}"
                            )

                        info = (
                            step.get(
                                "tool_info"
                            )
                            or {}
                        )

                        if (
                            tool_name
                            == "run_command"
                        ):
                            params = (
                                info.get(
                                    "parameters"
                                )
                                or {}
                            )

                            command = (
                                params.get(
                                    "CommandLine"
                                )
                                or params.get(
                                    "command"
                                )
                                or ""
                            )

                            violations.extend(
                                command_policy_violations(
                                    command,
                                    stage=stage,
                                )
                            )

                        output = str(
                            info.get(
                                "output"
                            )
                            or ""
                        ).lower()

                        if (
                            "permission denied"
                            in output
                            or
                            "not permitted"
                            in output
                        ):
                            failed_tool_calls += 1

                elif etype == "result":
                    result = (
                        event.get("result")
                        or {}
                    )

                    conversation_id = (
                        result.get(
                            "conversation_id"
                        )
                        or conversation_id
                    )

                    final_response = (
                        result.get(
                            "response"
                        )
                        or ""
                    )

                    usage = (
                        result.get("usage")
                        or {}
                    )

                    native_turns = (
                        result.get(
                            "num_turns"
                        )
                    )

                    result_status_success = (
                        result.get(
                            "status"
                        )
                        == "SUCCESS"
                    )

        return ProviderResult(
            provider=self.name,
            model=native_model,
            session_id=conversation_id,

            success=(
                rc == 0
                and result_status_success
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
                "cache_read_tokens"
            ),
            thinking_tokens=usage.get(
                "thinking_tokens"
            ),

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

            native_usage=usage,

            trace_path=str(
                trace_path
            ),
            stderr_path=str(
                stderr_path
            ),
        )

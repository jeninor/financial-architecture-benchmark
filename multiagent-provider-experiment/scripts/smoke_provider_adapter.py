#!/usr/bin/env python3

from __future__ import annotations

import argparse
import json
import shutil
import sys
import tempfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
FW = ROOT / "multiagent-provider-experiment"

sys.path.insert(0, str(FW))


from providers.base import write_json
from providers.codex_provider import CodexProvider
from providers.antigravity_provider import AntigravityProvider


def main():
    ap = argparse.ArgumentParser()

    ap.add_argument(
        "--provider",
        choices=["codex", "antigravity"],
        required=True,
    )

    ap.add_argument("--model")

    args = ap.parse_args()

    if args.provider == "codex":
        provider = CodexProvider()
    else:
        provider = AntigravityProvider()

    print("Provider :", provider.name)
    print("Version  :", provider.version())

    work = Path(
        tempfile.mkdtemp(
            prefix=f"provider-adapter-{provider.name}-"
        )
    )

    try:
        (work / "state.txt").write_text(
            "state=0\n",
            encoding="utf-8",
        )

        print("Workspace:", work)

        result1 = provider.run(
            workspace=work,
            prompt=(
                "Work only inside the current workspace. "
                "Do not use web, browser, MCP, subagents or Docker. "
                "Change state.txt to state=1. "
                "Remember the word ORANGE."
            ),
            trace_path=work / "turn1.jsonl",
            model=args.model,
            timeout_seconds=300,
        )

        write_json(work / "turn1_summary.json", result1.to_dict())

        print()
        print("TURN 1")
        print(json.dumps(result1.to_dict(), indent=2))

        if not result1.success:
            print("TURN 1 FAILED")
            return 2

        result2 = provider.run(
            workspace=work,
            prompt=(
                "Do not use web, browser, MCP, subagents or Docker. "
                "Tell me the word I asked you to remember, "
                "then change state.txt to state=2."
            ),
            trace_path=work / "turn2.jsonl",
            session_id=result1.session_id,
            model=args.model,
            timeout_seconds=300,
        )

        write_json(work / "turn2_summary.json", result2.to_dict())

        print()
        print("TURN 2")
        print(json.dumps(result2.to_dict(), indent=2))

        state = (work / "state.txt").read_text().strip()

        print()
        print("FINAL STATE:", state)

        ok = (
            result2.success
            and state == "state=2"
            and "ORANGE" in result2.final_response.upper()
        )

        print(
            "RESULT:",
            "PASS" if ok else "FAIL"
        )

        if not ok:
            return 2

        return 0

    finally:
        # Keep traces when debugging by commenting this line.
        shutil.rmtree(work, ignore_errors=True)


if __name__ == "__main__":
    raise SystemExit(main())

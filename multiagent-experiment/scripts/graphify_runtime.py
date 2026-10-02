from __future__ import annotations

import hashlib
import json
import subprocess
import time
from pathlib import Path
from typing import Any


GRAPHIFY_MCP_TOOLS = [
    "mcp__graphify__query_graph",
    "mcp__graphify__get_node",
    "mcp__graphify__get_neighbors",
    "mcp__graphify__shortest_path",
]


def _sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def _run(cmd: list[str], cwd: Path | None = None, timeout: int = 600) -> subprocess.CompletedProcess:
    return subprocess.run(
        cmd,
        cwd=str(cwd) if cwd else None,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        timeout=timeout,
    )


def ensure_graphify_image(image: str) -> dict[str, Any]:
    p = _run(["docker", "image", "inspect", image], timeout=60)
    if p.returncode != 0:
        raise RuntimeError(
            f"Graphify image not found: {image}\n"
            "Build it first with scripts/build_graphify_image.sh"
        )

    info = json.loads(p.stdout)[0]
    return {
        "image": image,
        "image_id": info.get("Id"),
        "created": info.get("Created"),
    }


def build_code_graph(
    workspace: Path,
    graph_root: Path,
    image: str,
    timeout: int = 600,
) -> dict[str, Any]:
    """
    Deterministic/local code-only extraction.

    Source is mounted read-only. Graphify writes only to graph_root.
    No LLM/API backend is used.
    """
    workspace = workspace.resolve()
    graph_root = graph_root.resolve()
    graph_root.mkdir(parents=True, exist_ok=True)

    started = time.perf_counter()
    cmd = [
        "docker", "run", "--rm",
        "--network", "none",
        "-v", f"{workspace}:/project:ro",
        "-v", f"{graph_root}:/output",
        image,
        "graphify", "extract", "/project",
        "--code-only",
        "--no-cluster",
        "--out", "/output",
    ]

    p = _run(cmd, timeout=timeout)
    elapsed = time.perf_counter() - started

    (graph_root / "extract.log").write_text(p.stdout, encoding="utf-8")

    if p.returncode != 0:
        raise RuntimeError(
            f"Graphify extraction failed ({p.returncode}). "
            f"See {graph_root / 'extract.log'}\n{p.stdout[-6000:]}"
        )

    graph_path = graph_root / "graphify-out" / "graph.json"
    if not graph_path.exists():
        raise RuntimeError(
            f"Graphify completed but graph.json was not found at {graph_path}"
        )

    data = json.loads(graph_path.read_text(encoding="utf-8"))
    nodes = data.get("nodes")
    edges = data.get("edges")

    metrics = {
        "graph_path": str(graph_path),
        "graph_sha256": _sha256(graph_path),
        "graph_bytes": graph_path.stat().st_size,
        "extract_elapsed_seconds": round(elapsed, 3),
        "nodes": len(nodes) if isinstance(nodes, list) else None,
        "edges": len(edges) if isinstance(edges, list) else None,
        "command": cmd,
    }

    (graph_root / "graph_metrics.json").write_text(
        json.dumps(metrics, indent=2, ensure_ascii=False),
        encoding="utf-8",
    )
    return metrics


def write_mcp_config(
    workspace: Path,
    graph_root: Path,
    image: str,
    output_path: Path,
) -> Path:
    """
    Graphify MCP runs in a fresh read-only container over the per-iteration graph.
    It has no network access and cannot modify the experiment workspace.
    """
    workspace = workspace.resolve()
    graph_root = graph_root.resolve()
    output_path = output_path.resolve()

    graph_dir = graph_root / "graphify-out"
    graph_file = graph_dir / "graph.json"
    if not graph_file.exists():
        raise FileNotFoundError(graph_file)

    config = {
        "mcpServers": {
            "graphify": {
                "command": "docker",
                "args": [
                    "run", "--rm", "-i",
                    "--network", "none",
                    "-v", f"{workspace}:/project:ro",
                    "-v", f"{graph_dir}:/graphify-out:ro",
                    image,
                    "python", "-m", "graphify.serve",
                    "/graphify-out/graph.json",
                ],
            }
        }
    }

    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(
        json.dumps(config, indent=2, ensure_ascii=False),
        encoding="utf-8",
    )
    return output_path


def graphify_allowed_tools(base_tools: list[str] | None = None) -> list[str]:
    base = base_tools or ["Read", "Write", "Edit", "Glob", "Grep", "Bash"]
    return base + GRAPHIFY_MCP_TOOLS

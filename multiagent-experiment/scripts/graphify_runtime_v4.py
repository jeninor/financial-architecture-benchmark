from __future__ import annotations

import hashlib
import json
import subprocess
import time
from pathlib import Path
from typing import Any


def _run(cmd: list[str], timeout: int = 120) -> subprocess.CompletedProcess:
    return subprocess.run(
        cmd,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        timeout=timeout,
    )


def _sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def _current_context() -> str:
    p = _run(["docker", "context", "show"], timeout=30)
    if p.returncode != 0:
        raise RuntimeError("Cannot determine Docker context:\n" + p.stdout)
    return p.stdout.strip()


def _contexts() -> list[str]:
    p = _run(["docker", "context", "ls", "--format", "{{.Name}}"], timeout=30)
    if p.returncode != 0:
        return []
    return [x.strip() for x in p.stdout.splitlines() if x.strip()]


def _inspect(context: str, ref: str) -> dict[str, Any] | None:
    p = _run(
        ["docker", "--context", context, "image", "inspect", ref],
        timeout=60,
    )
    if p.returncode != 0:
        return None
    try:
        rows = json.loads(p.stdout)
        return rows[0] if rows else None
    except Exception:
        return None


def _list_images(context: str) -> list[dict[str, str]]:
    p = _run(
        [
            "docker", "--context", context, "image", "ls",
            "--no-trunc",
            "--format", "{{.Repository}}\t{{.Tag}}\t{{.ID}}",
        ],
        timeout=60,
    )
    if p.returncode != 0:
        return []

    rows = []
    for line in p.stdout.splitlines():
        parts = line.split("\t")
        if len(parts) == 3:
            rows.append({
                "repository": parts[0],
                "tag": parts[1],
                "id": parts[2],
                "ref": f"{parts[0]}:{parts[1]}",
            })
    return rows


def ensure_graphify_image(requested_ref: str) -> dict[str, Any]:
    """
    Resolve Graphify in the CURRENT Docker context only.

    Strategy:
      1. normal inspect by requested tag;
      2. if Docker oddly lists the tag but inspect(tag) fails, resolve the
         immutable image ID from `docker image ls` and inspect by ID;
      3. only for diagnostics, scan other contexts if current context cannot
         resolve it. Never silently switch context.

    The returned `runtime_ref` is the immutable image ID whenever possible.
    """
    current = _current_context()

    direct = _inspect(current, requested_ref)
    if direct:
        image_id = direct.get("Id") or requested_ref
        return {
            "requested_ref": requested_ref,
            "runtime_ref": image_id,
            "image_id": image_id,
            "created": direct.get("Created"),
            "docker_context": current,
            "resolution_method": "inspect-tag",
            "other_context_matches": [],
        }

    # Defensive fallback for the exact symptom observed by the user:
    # image ls shows repo:tag, while image inspect repo:tag reports not found.
    candidates = [
        row for row in _list_images(current)
        if row["ref"] == requested_ref
    ]
    for row in candidates:
        by_id = _inspect(current, row["id"])
        if by_id:
            image_id = by_id.get("Id") or row["id"]
            return {
                "requested_ref": requested_ref,
                "runtime_ref": image_id,
                "image_id": image_id,
                "created": by_id.get("Created"),
                "docker_context": current,
                "resolution_method": "image-ls-id-fallback",
                "tag_listing": row,
                "other_context_matches": [],
            }

    # Diagnostic only. An image existing in another context is not usable by
    # the current Compose daemon, so do not silently switch daemons.
    other_matches = []
    for ctx in _contexts():
        if ctx == current:
            continue

        info = _inspect(ctx, requested_ref)
        if info:
            other_matches.append({
                "context": ctx,
                "ref": requested_ref,
                "image_id": info.get("Id"),
                "method": "inspect-tag",
            })
            continue

        for row in _list_images(ctx):
            if row["ref"] != requested_ref:
                continue
            by_id = _inspect(ctx, row["id"])
            if by_id:
                other_matches.append({
                    "context": ctx,
                    "ref": requested_ref,
                    "image_id": by_id.get("Id") or row["id"],
                    "method": "image-ls-id-fallback",
                })

    details = json.dumps(other_matches, indent=2, ensure_ascii=False)
    raise RuntimeError(
        f"Graphify image is not usable in the current Docker context.\n"
        f"requested_ref={requested_ref}\n"
        f"current_context={current}\n"
        f"matches_in_other_contexts={details}\n"
        "Build/load the image into the current context. The harness will not "
        "silently switch Docker daemons."
    )


def build_code_graph(
    workspace: Path,
    graph_root: Path,
    image_ref: str,
    timeout: int = 600,
) -> dict[str, Any]:
    workspace = workspace.resolve()
    graph_root = graph_root.resolve()
    graph_root.mkdir(parents=True, exist_ok=True)

    cmd = [
        "docker", "run", "--rm", "--network", "none",
        "-v", f"{workspace}:/project:ro",
        "-v", f"{graph_root}:/output",
        image_ref,
        "graphify", "extract", "/project",
        "--code-only", "--no-cluster", "--out", "/output",
    ]

    started = time.perf_counter()
    p = _run(cmd, timeout=timeout)
    elapsed = time.perf_counter() - started
    (graph_root / "extract.log").write_text(p.stdout, encoding="utf-8")

    if p.returncode != 0:
        raise RuntimeError(
            f"Graphify extraction failed ({p.returncode}).\n{p.stdout[-6000:]}"
        )

    graph = graph_root / "graphify-out" / "graph.json"
    if not graph.exists():
        raise RuntimeError(f"Graphify finished but graph.json is missing: {graph}")

    data = json.loads(graph.read_text(encoding="utf-8"))
    metrics = {
        "graph_path": str(graph),
        "graph_sha256": _sha256(graph),
        "graph_bytes": graph.stat().st_size,
        "extract_elapsed_seconds": round(elapsed, 3),
        "nodes": len(data.get("nodes", [])) if isinstance(data.get("nodes"), list) else None,
        "edges": len(data.get("edges", [])) if isinstance(data.get("edges"), list) else None,
        "runtime_image_ref": image_ref,
    }
    (graph_root / "graph_metrics.json").write_text(
        json.dumps(metrics, indent=2, ensure_ascii=False),
        encoding="utf-8",
    )
    return metrics


def query_graph(
    graph_root: Path,
    image_ref: str,
    question: str,
    budget: int = 1200,
    timeout: int = 120,
) -> str:
    graph_dir = (graph_root / "graphify-out").resolve()
    graph = graph_dir / "graph.json"
    if not graph.exists():
        raise FileNotFoundError(graph)

    cmd = [
        "docker", "run", "--rm", "--network", "none",
        "-v", f"{graph_dir}:/graphify-out:ro",
        image_ref,
        "graphify", "query", question,
        "--budget", str(budget),
        "--graph", "/graphify-out/graph.json",
    ]

    p = _run(cmd, timeout=timeout)
    if p.returncode != 0:
        raise RuntimeError(f"Graphify query failed:\n{p.stdout[-4000:]}")
    return p.stdout.strip()

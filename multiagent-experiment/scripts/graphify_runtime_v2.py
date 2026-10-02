from __future__ import annotations
import hashlib
import json
import subprocess
import time
from pathlib import Path

def _run(cmd, timeout=600):
    return subprocess.run(
        cmd, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
        timeout=timeout
    )

def _sha256(path: Path):
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()

def ensure_graphify_image(image: str):
    p = _run(["docker", "image", "inspect", image], timeout=60)
    if p.returncode != 0:
        raise RuntimeError(f"Graphify image not found: {image}")
    info = json.loads(p.stdout)[0]
    return {"image": image, "image_id": info.get("Id"), "created": info.get("Created")}

def build_code_graph(workspace: Path, graph_root: Path, image: str, timeout=600):
    workspace = workspace.resolve()
    graph_root = graph_root.resolve()
    graph_root.mkdir(parents=True, exist_ok=True)

    cmd = [
        "docker", "run", "--rm", "--network", "none",
        "-v", f"{workspace}:/project:ro",
        "-v", f"{graph_root}:/output",
        image,
        "graphify", "extract", "/project",
        "--code-only", "--no-cluster", "--out", "/output",
    ]
    started = time.perf_counter()
    p = _run(cmd, timeout=timeout)
    elapsed = time.perf_counter() - started
    (graph_root/"extract.log").write_text(p.stdout, encoding="utf-8")
    if p.returncode != 0:
        raise RuntimeError(f"Graphify extraction failed:\n{p.stdout[-6000:]}")

    graph = graph_root/"graphify-out"/"graph.json"
    if not graph.exists():
        raise RuntimeError(f"Missing graph: {graph}")

    data = json.loads(graph.read_text(encoding="utf-8"))
    metrics = {
        "graph_path": str(graph),
        "graph_sha256": _sha256(graph),
        "graph_bytes": graph.stat().st_size,
        "extract_elapsed_seconds": round(elapsed, 3),
        "nodes": len(data.get("nodes", [])) if isinstance(data.get("nodes"), list) else None,
        "edges": len(data.get("edges", [])) if isinstance(data.get("edges"), list) else None,
    }
    (graph_root/"graph_metrics.json").write_text(
        json.dumps(metrics, indent=2), encoding="utf-8"
    )
    return metrics

def query_graph(graph_root: Path, image: str, question: str, budget=1200, timeout=120):
    graph_dir = (graph_root/"graphify-out").resolve()
    graph = graph_dir/"graph.json"
    if not graph.exists():
        raise FileNotFoundError(graph)

    cmd = [
        "docker", "run", "--rm", "--network", "none",
        "-v", f"{graph_dir}:/graphify-out:ro",
        image,
        "graphify", "query", question,
        "--budget", str(budget),
        "--graph", "/graphify-out/graph.json",
    ]
    p = _run(cmd, timeout=timeout)
    if p.returncode != 0:
        raise RuntimeError(f"Graphify query failed:\n{p.stdout[-4000:]}")
    return p.stdout.strip()

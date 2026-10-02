#!/usr/bin/env python3
from __future__ import annotations

import argparse
import hashlib
import json
import shutil
import subprocess
from datetime import datetime, timezone
from pathlib import Path

MS_SERVICES = [
    "discovery-server",
    "api-gateway",
    "user-service",
    "market-service",
    "trade-service",
]

def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()

def git_value(repo: Path, *args: str) -> str | None:
    try:
        return subprocess.check_output(
            ["git", "-C", str(repo), *args],
            text=True,
            stderr=subprocess.DEVNULL,
        ).strip()
    except Exception:
        return None

def copy_file(src: Path, dst: Path, copied: list[dict]) -> None:
    if not src.exists():
        raise FileNotFoundError(src)
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst)
    copied.append({
        "source": str(src),
        "destination": str(dst),
        "sha256": sha256(dst),
    })

def copy_tree(src: Path, dst: Path, copied: list[dict]) -> None:
    if not src.exists():
        return
    for p in sorted(src.rglob("*")):
        if p.is_file():
            rel = p.relative_to(src)
            copy_file(p, dst / rel, copied)

def find_application_class(java_root: Path) -> Path:
    candidates = sorted(java_root.rglob("*Application.java"))
    if len(candidates) != 1:
        raise RuntimeError(
            f"Expected exactly one *Application.java under {java_root}, "
            f"found {len(candidates)}: {[str(x) for x in candidates]}"
        )
    return candidates[0]

def reset_dir(path: Path, force: bool) -> None:
    if path.exists():
        nonempty = any(path.iterdir())
        if nonempty and not force:
            raise RuntimeError(
                f"{path} is not empty. Re-run with --force only after reviewing it."
            )
        if force:
            shutil.rmtree(path)
    path.mkdir(parents=True, exist_ok=True)

def copy_optional_root_files(source: Path, dest: Path, copied: list[dict]) -> None:
    # Only infrastructure/build helpers, never production business code.
    for name in [
        ".dockerignore",
        ".gitignore",
        "mvnw",
        "mvnw.cmd",
    ]:
        p = source / name
        if p.exists() and p.is_file():
            copy_file(p, dest / name, copied)
    if (source / ".mvn").exists():
        copy_tree(source / ".mvn", dest / ".mvn", copied)

def prepare_monolith(repo: Path, dest: Path, copied: list[dict]) -> None:
    mono_root = repo / "monolith"
    app_root = mono_root / "financial-monolith"

    copy_file(mono_root / "docker-compose.yml", dest / "docker-compose.yml", copied)
    copy_file(app_root / "pom.xml", dest / "financial-monolith/pom.xml", copied)
    copy_tree(
        app_root / "src/main/resources",
        dest / "financial-monolith/src/main/resources",
        copied,
    )

    app_class = find_application_class(app_root / "src/main/java")
    rel = app_class.relative_to(app_root)
    copy_file(app_class, dest / "financial-monolith" / rel, copied)

    copy_optional_root_files(app_root, dest / "financial-monolith", copied)

def prepare_microservices(repo: Path, dest: Path, copied: list[dict]) -> None:
    ms_root = repo / "microservices"
    copy_file(ms_root / "docker-compose.yml", dest / "docker-compose.yml", copied)

    for service in MS_SERVICES:
        src_service = ms_root / service
        dst_service = dest / service

        copy_file(src_service / "pom.xml", dst_service / "pom.xml", copied)
        copy_tree(
            src_service / "src/main/resources",
            dst_service / "src/main/resources",
            copied,
        )

        app_class = find_application_class(src_service / "src/main/java")
        rel = app_class.relative_to(src_service)
        copy_file(app_class, dst_service / rel, copied)

        copy_optional_root_files(src_service, dst_service, copied)

def write_manifest(
    repo: Path,
    framework: Path,
    architecture: str,
    copied: list[dict],
) -> None:
    dest = framework / "baselines" / architecture

    status = git_value(repo, "status", "--porcelain")
    manifest = {
        "baseline_version": "v1",
        "architecture": architecture,
        "created_at_utc": datetime.now(timezone.utc).isoformat(),
        "source_repository": str(repo.resolve()),
        "source_git_commit": git_value(repo, "rev-parse", "HEAD"),
        "source_git_branch": git_value(repo, "branch", "--show-current"),
        "source_worktree_clean": status == "" if status is not None else None,
        "policy": {
            "keep": [
                "Docker Compose topology",
                "Maven dependency/build configuration",
                "Spring application configuration",
                "Spring Boot main application classes",
            ],
            "remove": [
                "business/domain implementation",
                "controllers",
                "services",
                "repositories",
                "DTOs",
                "internal integration implementations",
                "project tests",
                "generated target directories",
            ],
            "external_acceptance_tests_editable_by_agent": False,
        },
        "files": copied,
    }

    (dest / "BASELINE_MANIFEST.json").write_text(
        json.dumps(manifest, indent=2, ensure_ascii=False),
        encoding="utf-8",
    )

def main() -> None:
    parser = argparse.ArgumentParser(
        description="Prepare frozen architecture skeletons from the completed manual implementation."
    )
    parser.add_argument("--repo-root", required=True, type=Path)
    parser.add_argument("--framework-root", required=True, type=Path)
    parser.add_argument(
        "--architecture",
        choices=["monolith", "microservices", "both"],
        default="both",
    )
    parser.add_argument(
        "--force",
        action="store_true",
        help="Replace an existing non-empty baseline directory.",
    )
    args = parser.parse_args()

    repo = args.repo_root.resolve()
    framework = args.framework_root.resolve()

    targets = (
        ["monolith", "microservices"]
        if args.architecture == "both"
        else [args.architecture]
    )

    for arch in targets:
        dest = framework / "baselines" / arch
        reset_dir(dest, args.force)

        copied: list[dict] = []
        if arch == "monolith":
            prepare_monolith(repo, dest, copied)
        else:
            prepare_microservices(repo, dest, copied)

        write_manifest(repo, framework, arch, copied)
        print(f"[OK] {arch}: {len(copied)} files -> {dest}")

if __name__ == "__main__":
    main()

from __future__ import annotations

import hashlib
import os
import shutil
import subprocess
from pathlib import Path


INSIDE_HOME = Path("/home/alunos")
INSIDE_WORKSPACE = INSIDE_HOME / "workspace"
INSIDE_BIN = INSIDE_HOME / "bin"

DEFAULT_AUTHROOT = (
    Path.home()
    / ".local"
    / "share"
    / "financial-architecture-provider-homes-v1"
)

DEFAULT_RUNTIME_ROOT = (
    Path("/tmp")
    / f"financial-architecture-provider-runtime-v1-{os.getuid()}"
)


def authroot() -> Path:
    value = os.environ.get(
        "FINANCE_PROVIDER_AUTHROOT"
    )

    root = (
        Path(value).expanduser()
        if value
        else DEFAULT_AUTHROOT
    )

    return root.resolve()


def runtime_root() -> Path:
    value = os.environ.get(
        "FINANCE_PROVIDER_RUNTIME_ROOT"
    )

    root = (
        Path(value).expanduser()
        if value
        else DEFAULT_RUNTIME_ROOT
    )

    return root.resolve()


def provider_home(provider: str) -> Path:
    """
    Persistent provider authentication seed.

    This directory is never mounted as the writable
    runtime home of an experimental invocation.
    """

    env_name = (
        "FINANCE_PROVIDER_HOME_"
        + provider.upper().replace("-", "_")
    )

    value = os.environ.get(env_name)

    if value:
        return Path(value).expanduser().resolve()

    return (
        authroot()
        / provider
    ).resolve()


def provider_binary_path(
    provider: str,
    executable_name: str,
) -> Path:

    path = (
        provider_home(provider)
        / "bin"
        / executable_name
    )

    if not path.is_file():
        raise RuntimeError(
            f"Provider binary not found: {path}"
        )

    return path


def bwrap_version() -> str:
    executable = shutil.which("bwrap")

    if not executable:
        raise RuntimeError(
            "bwrap executable not found"
        )

    p = subprocess.run(
        [executable, "--version"],
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        timeout=30,
    )

    if p.returncode != 0:
        raise RuntimeError(
            "bwrap --version failed: "
            + p.stdout.strip()
        )

    return p.stdout.strip()


def _resolver_args() -> list[str]:
    resolver_copy = (
        authroot()
        / "common"
        / "resolv.conf"
    )

    if not resolver_copy.is_file():
        raise RuntimeError(
            "Frozen resolver copy not found: "
            f"{resolver_copy}"
        )

    resolver_target = Path(
        os.path.realpath("/etc/resolv.conf")
    )

    args: list[str] = []

    if (
        resolver_target.is_absolute()
        and len(resolver_target.parts) >= 2
        and resolver_target.parts[1] == "run"
    ):
        current = Path("/run")

        for part in resolver_target.parent.parts[2:]:
            current = current / part
            args += [
                "--dir",
                str(current),
            ]

    args += [
        "--ro-bind",
        str(resolver_copy),
        str(resolver_target),
    ]

    return args


def runtime_home(
    provider: str,
    workspace: Path,
) -> Path:

    workspace = workspace.resolve()

    digest = hashlib.sha256(
        (
            provider
            + "\0"
            + str(workspace)
        ).encode("utf-8")
    ).hexdigest()[:20]

    return (
        runtime_root()
        / f"{provider}-{digest}"
    )


def prepare_runtime_home(
    *,
    provider: str,
    workspace: Path,
    fresh: bool,
) -> Path:

    seed = provider_home(provider)

    if not seed.is_dir():
        raise RuntimeError(
            f"Provider seed home not found: {seed}"
        )

    destination = runtime_home(
        provider,
        workspace,
    )

    root = runtime_root()

    root.mkdir(
        parents=True,
        exist_ok=True,
    )
    os.chmod(root, 0o700)

    if fresh and destination.exists():
        shutil.rmtree(destination)

    if not destination.exists():
        shutil.copytree(
            seed,
            destination,
            symlinks=True,
            ignore=shutil.ignore_patterns(
                "bin",
                "workspace",
            ),
        )

    (destination / "bin").mkdir(
        exist_ok=True
    )

    (destination / "workspace").mkdir(
        exist_ok=True
    )

    os.chmod(destination, 0o700)

    return destination


def cleanup_runtime_home(
    provider: str,
    workspace: Path,
) -> None:

    path = runtime_home(
        provider,
        workspace,
    )

    if path.exists():
        shutil.rmtree(path)


def build_bwrap_command(
    *,
    provider: str,
    workspace: Path,
    executable_name: str,
    argv: list[str],
    fresh_runtime: bool = False,
) -> list[str]:

    bwrap = shutil.which("bwrap")

    if not bwrap:
        raise RuntimeError(
            "bwrap executable not found"
        )

    workspace = workspace.resolve()

    if not workspace.is_dir():
        raise RuntimeError(
            f"Workspace not found: {workspace}"
        )

    seed = provider_home(provider)
    bindir = seed / "bin"

    if not bindir.is_dir():
        raise RuntimeError(
            "Provider bin directory not found: "
            f"{bindir}"
        )

    executable = (
        bindir
        / executable_name
    )

    if not executable.is_file():
        raise RuntimeError(
            f"Provider executable not found: {executable}"
        )

    state_home = prepare_runtime_home(
        provider=provider,
        workspace=workspace,
        fresh=fresh_runtime,
    )

    return [
        bwrap,

        "--die-with-parent",
        "--new-session",

        "--ro-bind", "/", "/",

        "--tmpfs", "/home",
        "--dir", str(INSIDE_HOME),

        "--tmpfs", "/tmp",
        "--tmpfs", "/run",

        *_resolver_args(),

        # Runtime state is disposable and specific
        # to this provider/workspace.
        "--bind",
        str(state_home),
        str(INSIDE_HOME),

        # Binary comes from persistent seed,
        # always read-only.
        "--ro-bind",
        str(bindir),
        str(INSIDE_BIN),

        # Only project workspace is RW code.
        "--bind",
        str(workspace),
        str(INSIDE_WORKSPACE),

        "--chdir",
        str(INSIDE_WORKSPACE),

        "--clearenv",

        "--setenv", "HOME", str(INSIDE_HOME),
        "--setenv", "USER", "alunos",
        "--setenv", "LOGNAME", "alunos",
        "--setenv", "SHELL", "/bin/bash",
        "--setenv", "TMPDIR", "/tmp",

        "--setenv",
        "PATH",
        "/home/alunos/bin:/usr/local/bin:/usr/bin:/bin",

        "--setenv", "LANG", "C.UTF-8",
        "--setenv", "LC_ALL", "C.UTF-8",

        "--proc", "/proc",
        "--dev", "/dev",

        str(
            INSIDE_BIN
            / executable_name
        ),

        *argv,
    ]

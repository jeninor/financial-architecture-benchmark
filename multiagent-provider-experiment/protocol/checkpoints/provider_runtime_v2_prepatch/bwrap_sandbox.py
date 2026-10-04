from __future__ import annotations

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


def authroot() -> Path:
    value = os.environ.get("FINANCE_PROVIDER_AUTHROOT")

    root = (
        Path(value).expanduser()
        if value
        else DEFAULT_AUTHROOT
    )

    return root.resolve()


def provider_home(provider: str) -> Path:
    env_name = (
        "FINANCE_PROVIDER_HOME_"
        + provider.upper().replace("-", "_")
    )

    value = os.environ.get(env_name)

    if value:
        return Path(value).expanduser().resolve()

    return (authroot() / provider).resolve()


def provider_binary_path(
    provider: str,
    executable_name: str,
) -> Path:
    path = provider_home(provider) / "bin" / executable_name

    if not path.is_file():
        raise RuntimeError(
            f"Provider binary not found: {path}"
        )

    return path


def bwrap_version() -> str:
    executable = shutil.which("bwrap")

    if not executable:
        raise RuntimeError("bwrap executable not found")

    p = subprocess.run(
        [executable, "--version"],
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        timeout=30,
    )

    if p.returncode != 0:
        raise RuntimeError(
            f"bwrap --version failed: {p.stdout.strip()}"
        )

    return p.stdout.strip()


def _resolver_args() -> list[str]:
    root = authroot()

    resolver_copy = root / "common" / "resolv.conf"

    if not resolver_copy.is_file():
        raise RuntimeError(
            f"Frozen resolver copy not found: {resolver_copy}"
        )

    resolver_target = Path(
        os.path.realpath("/etc/resolv.conf")
    )

    args: list[str] = []

    # /run is replaced by tmpfs. If resolv.conf ultimately
    # points inside /run, recreate only its parent directories.
    if (
        resolver_target.is_absolute()
        and len(resolver_target.parts) >= 2
        and resolver_target.parts[1] == "run"
    ):
        current = Path("/run")

        for part in resolver_target.parent.parts[2:]:
            current = current / part
            args += ["--dir", str(current)]

    args += [
        "--ro-bind",
        str(resolver_copy),
        str(resolver_target),
    ]

    return args


def build_bwrap_command(
    *,
    provider: str,
    workspace: Path,
    executable_name: str,
    argv: list[str],
) -> list[str]:

    bwrap = shutil.which("bwrap")

    if not bwrap:
        raise RuntimeError("bwrap executable not found")

    workspace = workspace.resolve()

    if not workspace.is_dir():
        raise RuntimeError(
            f"Workspace not found: {workspace}"
        )

    home = provider_home(provider)
    bindir = home / "bin"

    if not home.is_dir():
        raise RuntimeError(
            f"Provider isolated home not found: {home}"
        )

    if not bindir.is_dir():
        raise RuntimeError(
            f"Provider bin directory not found: {bindir}"
        )

    executable = bindir / executable_name

    if not executable.is_file():
        raise RuntimeError(
            f"Provider executable not found: {executable}"
        )

    cmd = [
        bwrap,

        "--die-with-parent",
        "--new-session",

        # Host filesystem is visible read-only, but /home and /run
        # are subsequently replaced.
        "--ro-bind", "/", "/",

        # Hide all real user homes.
        "--tmpfs", "/home",
        "--dir", str(INSIDE_HOME),

        # No host temporary/runtime sockets.
        "--tmpfs", "/tmp",
        "--tmpfs", "/run",

        *_resolver_args(),

        # Provider-specific isolated state/authentication.
        "--bind",
        str(home),
        str(INSIDE_HOME),

        # Provider binaries cannot be modified by the agent.
        "--ro-bind",
        str(bindir),
        str(INSIDE_BIN),

        # Only experimental workspace is writable project state.
        "--bind",
        str(workspace),
        str(INSIDE_WORKSPACE),

        "--chdir",
        str(INSIDE_WORKSPACE),

        # Do not leak arbitrary host environment variables.
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

        str(INSIDE_BIN / executable_name),

        *argv,
    ]

    return cmd

#!/usr/bin/env bash
set -euo pipefail

# Automatic bootstrap + microservices pilot launcher.
#
# Usage:
#   multiagent-experiment/scripts/run_microservices_pilot_auto.sh
#
# Optional environment variables:
#   REPO_ROOT=/path/to/repo
#   GRAPHIFY_IMAGE=graphify-mcp:0.9.73
#   LIZARD_IMAGE=multiagent-lizard:1.24.0
#   TRIVY_IMAGE=aquasec/trivy:0.74.0
#   RUN_SEQ=8
#   MODEL=sonnet
#   GRAPH_BUDGET=1200
#   STARTUP_TIMEOUT=360
#   MAX_ITERATIONS=3
#   MAX_TURNS_FIRST=15
#   MAX_TURNS_FIX=8
#   AUTO_PULL_TRIVY=1
#   AUTO_BUILD_GRAPHIFY=1
#   AUTO_BUILD_LIZARD=1
#   RUN_A1=1

REPO_ROOT="${REPO_ROOT:-/home/alunos/Documentos/Juan/usp/financial-architecture-benchmark}"
FW="${REPO_ROOT}/multiagent-experiment"

GRAPHIFY_IMAGE="${GRAPHIFY_IMAGE:-graphify-mcp:0.9.73}"
LIZARD_IMAGE="${LIZARD_IMAGE:-multiagent-lizard:1.24.0}"
TRIVY_IMAGE="${TRIVY_IMAGE:-aquasec/trivy:0.74.0}"

RUN_SEQ="${RUN_SEQ:-8}"
MODEL="${MODEL:-sonnet}"
GRAPH_BUDGET="${GRAPH_BUDGET:-1200}"
STARTUP_TIMEOUT="${STARTUP_TIMEOUT:-360}"
MAX_ITERATIONS="${MAX_ITERATIONS:-3}"
MAX_TURNS_FIRST="${MAX_TURNS_FIRST:-15}"
MAX_TURNS_FIX="${MAX_TURNS_FIX:-8}"

AUTO_PULL_TRIVY="${AUTO_PULL_TRIVY:-1}"
AUTO_BUILD_GRAPHIFY="${AUTO_BUILD_GRAPHIFY:-1}"
AUTO_BUILD_LIZARD="${AUTO_BUILD_LIZARD:-1}"
RUN_A1="${RUN_A1:-1}"

cd "${REPO_ROOT}"

say() {
  printf '\n[%s] %s\n' "$(date +%H:%M:%S)" "$*"
}

die() {
  echo "[ERROR] $*" >&2
  exit 1
}

say "Docker daemon/context preflight"
docker info >/dev/null 2>&1 || die "Docker Desktop/daemon is not reachable. Start Docker Desktop once, then rerun this script."

CTX="$(docker context show)"
echo "Docker context: ${CTX}"

# Do not silently switch daemon/context. The current context is part of the
# experimental environment and will be recorded by the runners.

say "Ensure Graphify image: ${GRAPHIFY_IMAGE}"
if python3 "${FW}/scripts/preflight_graphify.py" --image "${GRAPHIFY_IMAGE}" >/tmp/graphify-preflight.log 2>&1; then
  cat /tmp/graphify-preflight.log
else
  cat /tmp/graphify-preflight.log

  if [[ "${AUTO_BUILD_GRAPHIFY}" != "1" ]]; then
    die "Graphify image missing/unusable and AUTO_BUILD_GRAPHIFY=0"
  fi

  [[ -x "${FW}/scripts/build_graphify_image.sh" ]] \
    || die "Missing ${FW}/scripts/build_graphify_image.sh"

  say "Graphify missing/unusable -> building automatically"
  GRAPHIFY_IMAGE="${GRAPHIFY_IMAGE}" \
  GRAPHIFY_VERSION="0.9.73" \
    "${FW}/scripts/build_graphify_image.sh"

  python3 "${FW}/scripts/preflight_graphify.py" \
    --image "${GRAPHIFY_IMAGE}" \
    || die "Graphify still unavailable after automatic build"
fi

say "Ensure Lizard image: ${LIZARD_IMAGE}"
if docker image inspect "${LIZARD_IMAGE}" >/dev/null 2>&1; then
  echo "[OK] ${LIZARD_IMAGE}"
else
  [[ "${AUTO_BUILD_LIZARD}" == "1" ]] \
    || die "Lizard image missing and AUTO_BUILD_LIZARD=0"

  [[ -f "${FW}/docker/quality/Dockerfile" ]] \
    || die "Missing ${FW}/docker/quality/Dockerfile"

  say "Lizard image missing -> building automatically"
  docker build \
    -t "${LIZARD_IMAGE}" \
    "${FW}/docker/quality"

  docker image inspect "${LIZARD_IMAGE}" >/dev/null \
    || die "Lizard image build did not produce ${LIZARD_IMAGE}"
fi

say "Ensure Trivy image: ${TRIVY_IMAGE}"
if docker image inspect "${TRIVY_IMAGE}" >/dev/null 2>&1; then
  echo "[OK] ${TRIVY_IMAGE}"
else
  [[ "${AUTO_PULL_TRIVY}" == "1" ]] \
    || die "Trivy image missing and AUTO_PULL_TRIVY=0"

  say "Trivy image missing -> pulling automatically"
  docker pull "${TRIVY_IMAGE}"
fi

say "Microservices preflight"
python3 "${FW}/scripts/preflight_microservices.py" \
  --repo-root "${REPO_ROOT}" \
  --framework-root "multiagent-experiment" \
  --graphify-image "${GRAPHIFY_IMAGE}" \
  --lizard-image "${LIZARD_IMAGE}" \
  --trivy-image "${TRIVY_IMAGE}"

if [[ "${RUN_A1}" != "1" ]]; then
  say "Environment ready. RUN_A1=0, so the pilot was not started."
  exit 0
fi

RUN_ID="$(printf 'P%04d_microservices' "${RUN_SEQ}")"
RUN_DIR="${FW}/runs/${RUN_ID}"

if [[ -d "${RUN_DIR}" ]]; then
  die "Run already exists: ${RUN_DIR}. Choose another RUN_SEQ instead of overwriting experimental evidence."
fi

CONSOLE_LOG="${FW}/P$(printf '%04d' "${RUN_SEQ}")_microservices_console.log"

say "Launching A1 microservices pilot ${RUN_ID}"
python3 "${FW}/scripts/run_a1_microservices_v1.py" \
  --repo-root "${REPO_ROOT}" \
  --framework-root "multiagent-experiment" \
  --prefix P \
  --seq "${RUN_SEQ}" \
  --model "${MODEL}" \
  --graphify-image "${GRAPHIFY_IMAGE}" \
  --graph-budget "${GRAPH_BUDGET}" \
  --startup-timeout "${STARTUP_TIMEOUT}" \
  --max-iterations "${MAX_ITERATIONS}" \
  --max-turns-first "${MAX_TURNS_FIRST}" \
  --max-turns-fix "${MAX_TURNS_FIX}" \
  2>&1 | tee "${CONSOLE_LOG}"

say "Pilot finished"
echo "Console log: ${CONSOLE_LOG}"
echo "Run dir    : ${RUN_DIR}"

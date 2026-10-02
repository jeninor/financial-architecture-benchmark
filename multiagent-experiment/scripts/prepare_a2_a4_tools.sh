#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="${1:-$(pwd)}"
FW="${REPO_ROOT}/multiagent-experiment"
LIZARD_IMAGE="${LIZARD_IMAGE:-multiagent-lizard:1.24.0}"
TRIVY_IMAGE="${TRIVY_IMAGE:-aquasec/trivy:0.74.0}"
CACHE_DIR="${TRIVY_CACHE_DIR:-${FW}/tool-cache/trivy}"

mkdir -p "${CACHE_DIR}"

echo "===== DOCKER CONTEXT ====="
docker context show

echo
echo "===== BUILD LIZARD ====="
docker build -t "${LIZARD_IMAGE}" "${FW}/docker/quality"

echo
echo "===== PULL TRIVY ====="
docker pull "${TRIVY_IMAGE}"

UID_NOW="$(id -u)"
GID_NOW="$(id -g)"

echo
echo "===== TRIVY VULNERABILITY DB ====="
docker run --rm \
  --user "${UID_NOW}:${GID_NOW}" \
  -e HOME=/tmp \
  -v "${CACHE_DIR}:/cache" \
  "${TRIVY_IMAGE}" image \
  --cache-dir /cache \
  --download-db-only

echo
echo "===== TRIVY JAVA DB ====="
docker run --rm \
  --user "${UID_NOW}:${GID_NOW}" \
  -e HOME=/tmp \
  -v "${CACHE_DIR}:/cache" \
  "${TRIVY_IMAGE}" image \
  --cache-dir /cache \
  --download-java-db-only

echo
echo "===== WARM MISCONFIG CHECKS ====="
docker run --rm \
  --user "${UID_NOW}:${GID_NOW}" \
  -e HOME=/tmp \
  -v "${CACHE_DIR}:/cache" \
  "${TRIVY_IMAGE}" fs \
  --cache-dir /cache \
  --scanners misconfig \
  --format json \
  --quiet \
  /tmp >/dev/null

echo
echo "===== IMAGE IDENTITIES ====="
docker image inspect "${LIZARD_IMAGE}" --format 'Lizard {{.Id}} {{.Created}}'
docker image inspect "${TRIVY_IMAGE}" --format 'Trivy  {{.Id}} {{.Created}}'
docker run --rm "${TRIVY_IMAGE}" --version

echo
echo "[OK] A2/A4 tools prepared"

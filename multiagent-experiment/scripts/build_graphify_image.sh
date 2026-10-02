#!/usr/bin/env bash
set -euo pipefail

IMAGE="${GRAPHIFY_IMAGE:-graphify-mcp:0.9.73}"
VERSION="${GRAPHIFY_VERSION:-0.9.73}"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "Building ${IMAGE} with graphifyy==${VERSION}"

docker build \
  --build-arg "GRAPHIFY_VERSION=${VERSION}" \
  -f "${ROOT}/docker/Dockerfile.graphify" \
  -t "${IMAGE}" \
  "${ROOT}"

echo
echo "Image:"
docker image inspect "${IMAGE}" --format '{{.Id}} {{.Created}}'

echo
echo "Graphify smoke test:"
docker run --rm "${IMAGE}" graphify --help >/tmp/graphify-help.txt
head -n 20 /tmp/graphify-help.txt
echo
echo "[OK] ${IMAGE}"

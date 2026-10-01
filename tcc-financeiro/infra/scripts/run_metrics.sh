#!/usr/bin/env bash
# Coleta LOC (cloc) e complexidade ciclomatica (lizard) para as duas arquiteturas,
# usando um container Python temporario (sem instalar nada no host).
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
METRICS_DIR="$ROOT_DIR/metrics"
mkdir -p "$METRICS_DIR"
TIMESTAMP="$(date +%Y%m%d_%H%M%S)"

run_for() {
  local NOME="$1"
  local DIR="$2"
  echo "== Metricas: $NOME =="
  docker run --rm -v "$DIR:/proj" python:3.12-slim bash -c "
    pip install --quiet --no-cache-dir lizard cloc-pyc 2>/dev/null || pip install --quiet --no-cache-dir lizard
    apt-get update -qq && apt-get install -y -qq cloc >/dev/null 2>&1 || true
    echo '--- LIZARD ---'
    lizard /proj -l java
    echo '--- CLOC ---'
    cloc /proj --include-lang=Java || echo 'cloc indisponivel, use lizard como referencia principal'
  " | tee "$METRICS_DIR/metrics_${NOME}_${TIMESTAMP}.txt"
}

run_for "monolito" "$ROOT_DIR/multiagente/monolito"
run_for "microservicos" "$ROOT_DIR/multiagente/microservicos"

echo "Resultados salvos em $METRICS_DIR"

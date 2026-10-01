#!/usr/bin/env bash
# Agente 4 (seguranca) - roda Trivy sobre monolito e microservicos
# e salva resultados em JSON para a coleta de metricas do TCC.
#
# Causa raiz do HTTP 429 recorrente: `mvn clean package` so baixa o que o
# build precisa para compilar; o analisador [pom] do Trivy caminha a arvore
# TRANSITIVA inteira (inclusive ramos que o build nunca toca, ex.:
# netty-codec via reactor-netty dentro do spring-cloud-starter-gateway).
# Entao o cache ~/.m2 fica sempre com buracos e o Trivy tenta buscar os
# POMs faltantes direto no Maven Central -> rate limit (429) que bloqueia
# o IP por ate 30 min (Retry-After).
#
# Correcao: antes de cada scan, resolvemos o fechamento transitivo COMPLETO
# de cada modulo Maven (com o plugin go-offline, mais abrangente que
# `dependency:go-offline`) escrevendo em ~/.m2 (montado RW nesse passo).
# So depois disso o Trivy roda, com ~/.m2 montado read-only, sem precisar
# tocar a rede.
set -uo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
METRICS_DIR="$ROOT_DIR/metrics"
mkdir -p "$METRICS_DIR"
mkdir -p "$HOME/.m2"

TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
MVN_IMAGE="maven:3.9-eclipse-temurin-21"
GO_OFFLINE_PLUGIN="de.qaware.maven:go-offline-maven-plugin:1.2.8:resolve-dependencies"

# Flags de rede mais tolerantes a instabilidade/lentidao do Maven Central,
# para reduzir a chance de disparar o rate limit por timeouts/retries em
# excesso durante a pre-populacao do cache.
MVN_NET_OPTS="-Dmaven.wagon.http.retryHandler.count=3 -Dmaven.wagon.httpconnectionManager.ttlSeconds=120"

# Popula o cache Maven (~/.m2) com o fechamento transitivo completo de um
# modulo, para que o analisador [pom] do Trivy nao precise ir a rede.
# $1 = caminho absoluto do diretorio do modulo (contem pom.xml)
populate_m2() {
  local MODULE_DIR="$1"
  local MODULE_NAME
  MODULE_NAME="$(basename "$MODULE_DIR")"

  if [ ! -f "$MODULE_DIR/pom.xml" ]; then
    echo "   (pulando $MODULE_NAME: sem pom.xml)"
    return 0
  fi

  echo "   -> resolvendo dependencias transitivas: $MODULE_NAME"
  if ! docker run --rm \
    -v "$MODULE_DIR:/app" \
    -v "$HOME/.m2:/root/.m2" \
    -w /app \
    "$MVN_IMAGE" \
    mvn -B -ntp -q $MVN_NET_OPTS "$GO_OFFLINE_PLUGIN"
  then
    echo "   AVISO: go-offline falhou para $MODULE_NAME; tentando dependency:go-offline como fallback"
    docker run --rm \
      -v "$MODULE_DIR:/app" \
      -v "$HOME/.m2:/root/.m2" \
      -w /app \
      "$MVN_IMAGE" \
      mvn -B -ntp -q $MVN_NET_OPTS dependency:go-offline dependency:resolve-plugins || \
      echo "   AVISO: nao foi possivel popular completamente o cache para $MODULE_NAME (scan pode falhar com 429)"
  fi
}

run_trivy_scan() {
  local NOME="$1"
  local DIR="$2"
  echo "== Trivy: $NOME =="
  docker run --rm \
    -v "$DIR:/app" \
    -v "$HOME/.m2:/root/.m2:ro" \
    aquasec/trivy fs --format json --output /app/.trivy_out.json /app || true
  mv "$DIR/.trivy_out.json" \
     "$METRICS_DIR/trivy_${NOME}_${TIMESTAMP}.json" 2>/dev/null || \
     echo "Aviso: nenhum arquivo de saida gerado para $NOME"
}

echo "== Pre-populando ~/.m2 (monolito) =="
populate_m2 "$ROOT_DIR/multiagente/monolito"

echo "== Pre-populando ~/.m2 (microservicos) =="
for MODULE in api-gateway eureka-server market-service trade-service user-service; do
  populate_m2 "$ROOT_DIR/multiagente/microservicos/$MODULE"
done

run_trivy_scan "monolito" "$ROOT_DIR/multiagente/monolito"
run_trivy_scan "microservicos" "$ROOT_DIR/multiagente/microservicos"

echo "Resultados salvos em $METRICS_DIR"

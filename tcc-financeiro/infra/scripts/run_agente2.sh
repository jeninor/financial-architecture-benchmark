#!/usr/bin/env bash
# Agente 2 (revisao estatica) - roda PMD + SpotBugs sobre monolito e
# microservicos via plugins Maven, reaproveitando o cache ~/.m2 ja
# populado pelo Agente 4 (ver run_trivy.sh). Ver AGENTE2_ESPECIFICACAO.md
# para a justificativa de ferramentas/regras/versoes.
set -uo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
METRICS_DIR="$ROOT_DIR/metrics"
mkdir -p "$METRICS_DIR" "$HOME/.m2"

TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
MVN_IMAGE="maven:3.9-eclipse-temurin-21"
MVN_NET_OPTS="-Dmaven.wagon.http.retryHandler.count=3 -Dmaven.wagon.httpconnectionManager.ttlSeconds=120"

# 3.21.2 embarca PMD 6.55.0, que nao le bytecode Java 21 (class file major
# version 65) na resolucao de tipos: todo arquivo falhava com
# "Unsupported class file major version 65" e NENHUMA regra era avaliada
# (o "0 violacoes" era silencioso, nao "codigo limpio" - ver <error> no XML).
# 3.22.0+ exige/usa PMD 7, que suporta Java 21. Usando 3.28.0 (atual).
PMD_PLUGIN="org.apache.maven.plugins:maven-pmd-plugin:3.28.0:pmd"
SPOTBUGS_PLUGIN="com.github.spotbugs:spotbugs-maven-plugin:4.8.6.0:spotbugs"

# O parametro "rulesets" do goal pmd:pmd NAO tem User Property (-D) no
# descritor do plugin: so pode ser configurado dentro do <configuration>
# do plugin no pom.xml. Por isso injetamos esse bloco TEMPORARIAMENTE no
# pom.xml de cada modulo antes de rodar o PMD, e restauramos o pom.xml
# original logo em seguida (nunca fica alterado permanentemente).
PMD_PLUGIN_BLOCK='    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-pmd-plugin</artifactId>
      <version>3.28.0</version>
      <configuration>
        <rulesets>
          <ruleset>category/java/bestpractices.xml</ruleset>
          <ruleset>category/java/errorprone.xml</ruleset>
          <ruleset>category/java/design.xml</ruleset>
          <ruleset>category/java/multithreading.xml</ruleset>
          <ruleset>category/java/performance.xml</ruleset>
        </rulesets>
      </configuration>
    </plugin>'

# Injeta $PMD_PLUGIN_BLOCK no pom.xml de $1, fazendo backup antes.
# Se existir <build><plugins>, insere antes do primeiro </plugins>.
# Caso contrario, insere um <build><plugins>...</plugins></build> novo
# antes de </project>.
inject_pmd_config() {
  local POM="$1"
  cp "$POM" "$POM.agente2.bak"
  if grep -q '<plugins>' "$POM"; then
    awk -v block="$PMD_PLUGIN_BLOCK" '
      !done && /<\/plugins>/ { print block; done=1 }
      { print }
    ' "$POM" > "$POM.tmp" && mv "$POM.tmp" "$POM"
  else
    awk -v block="$PMD_PLUGIN_BLOCK" '
      !done && /<\/project>/ {
        print "  <build><plugins>"; print block; print "  </plugins></build>"; done=1
      }
      { print }
    ' "$POM" > "$POM.tmp" && mv "$POM.tmp" "$POM"
  fi
}

# Restaura o pom.xml original a partir do backup feito por inject_pmd_config.
restore_pom() {
  local POM="$1"
  if [ -f "$POM.agente2.bak" ]; then
    mv "$POM.agente2.bak" "$POM"
  fi
}

LOG_SUMMARY="$METRICS_DIR/agente2_resumo_${TIMESTAMP}.md"
{
  echo "# Agente 2 (revisao estatica) - PMD + SpotBugs"
  echo "Executado em: $(date -Iseconds)"
  echo
} > "$LOG_SUMMARY"

# $1 = nome do modulo (para nomear arquivos de saida)
# $2 = caminho absoluto do diretorio do modulo (contem pom.xml)
analyze_module() {
  local MODULE_NAME="$1"
  local MODULE_DIR="$2"

  if [ ! -f "$MODULE_DIR/pom.xml" ]; then
    echo "   (pulando $MODULE_NAME: sem pom.xml)"
    echo "- **$MODULE_NAME**: pulado (sem pom.xml)" >> "$LOG_SUMMARY"
    return 0
  fi

  echo "== Agente 2: $MODULE_NAME (PMD) =="
  inject_pmd_config "$MODULE_DIR/pom.xml"
  trap "restore_pom '$MODULE_DIR/pom.xml'" RETURN
  if ! docker run --rm \
    -v "$MODULE_DIR:/app" \
    -v "$HOME/.m2:/root/.m2" \
    -w /app \
    "$MVN_IMAGE" \
    mvn -B -ntp -q $MVN_NET_OPTS "$PMD_PLUGIN" -Dformat=xml -Dpmd.skip=false
  then
    echo "   AVISO: PMD falhou em $MODULE_NAME"
  fi
  restore_pom "$MODULE_DIR/pom.xml"
  trap - RETURN

  if [ -f "$MODULE_DIR/target/pmd.xml" ]; then
    cp "$MODULE_DIR/target/pmd.xml" "$METRICS_DIR/agente2_${MODULE_NAME}_pmd_${TIMESTAMP}.xml"
    # grep -o + wc -l (nao grep -c): o PMD/SpotBugs escrevem o XML em uma
    # unica linha, entao grep -c (que conta LINHAS) sempre retornaria 0 ou 1
    # independente de quantas ocorrencias existem na linha.
    PMD_COUNT=$(grep -o '<violation ' "$MODULE_DIR/target/pmd.xml" 2>/dev/null | wc -l | tr -d ' ')
    [ -z "$PMD_COUNT" ] && PMD_COUNT=0
    # <error> no pmd.xml = arquivo nao foi analisado (ex.: falha ao ler
    # bytecode). "0 violacoes" com erros > 0 NAO significa codigo limpo.
    PMD_ERRORS=$(grep -o '<error ' "$MODULE_DIR/target/pmd.xml" 2>/dev/null | wc -l | tr -d ' ')
    [ -z "$PMD_ERRORS" ] && PMD_ERRORS=0
    if [ "$PMD_ERRORS" -gt 0 ]; then
      echo "- **$MODULE_NAME** (PMD): $PMD_COUNT violacoes — ATENCAO: $PMD_ERRORS arquivo(s) falharam na analise (ver <error> no XML), 0 violacoes NAO significa codigo limpo" >> "$LOG_SUMMARY"
    else
      echo "- **$MODULE_NAME** (PMD): $PMD_COUNT violacoes" >> "$LOG_SUMMARY"
    fi
  else
    echo "- **$MODULE_NAME** (PMD): relatorio nao gerado" >> "$LOG_SUMMARY"
  fi

  echo "== Agente 2: $MODULE_NAME (SpotBugs) =="
  if ! docker run --rm \
    -v "$MODULE_DIR:/app" \
    -v "$HOME/.m2:/root/.m2" \
    -w /app \
    "$MVN_IMAGE" \
    mvn -B -ntp -q $MVN_NET_OPTS compile "$SPOTBUGS_PLUGIN" \
      -Dspotbugs.effort=Max -Dspotbugs.threshold=Low \
      -Dspotbugs.xmlOutput=true -Dspotbugs.htmlOutput=true
  then
    echo "   AVISO: SpotBugs falhou em $MODULE_NAME"
  fi

  if [ -f "$MODULE_DIR/target/spotbugsXml.xml" ]; then
    cp "$MODULE_DIR/target/spotbugsXml.xml" "$METRICS_DIR/agente2_${MODULE_NAME}_spotbugs_${TIMESTAMP}.xml"
    [ -f "$MODULE_DIR/target/spotbugs.html" ] && \
      cp "$MODULE_DIR/target/spotbugs.html" "$METRICS_DIR/agente2_${MODULE_NAME}_spotbugs_${TIMESTAMP}.html"
    SB_COUNT=$(grep -o '<BugInstance ' "$MODULE_DIR/target/spotbugsXml.xml" 2>/dev/null | wc -l | tr -d ' ')
    [ -z "$SB_COUNT" ] && SB_COUNT=0
    echo "- **$MODULE_NAME** (SpotBugs): $SB_COUNT achados" >> "$LOG_SUMMARY"
  else
    echo "- **$MODULE_NAME** (SpotBugs): relatorio nao gerado" >> "$LOG_SUMMARY"
  fi

  echo >> "$LOG_SUMMARY"
}

echo "## Monolito" >> "$LOG_SUMMARY"
analyze_module "monolito" "$ROOT_DIR/multiagente/monolito"

echo "## Microservicos" >> "$LOG_SUMMARY"
for MODULE in api-gateway eureka-server market-service trade-service user-service; do
  analyze_module "$MODULE" "$ROOT_DIR/multiagente/microservicos/$MODULE"
done

echo "Resumo salvo em $LOG_SUMMARY"
echo "Relatorios detalhados (XML/HTML) salvos em $METRICS_DIR"

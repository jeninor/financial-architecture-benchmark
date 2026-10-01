# Agente 3 — Log de refatoração (MONÓLITO)

- **Arquitetura:** monólito (`multiagente/monolito`)
- **Agente:** Claude Code (Claude Opus 5.5)
- **Data:** 2026-10-01
- **Especificação seguida:** `infra/scripts/AGENTE3_ESPECIFICACAO.md` (versão "apenas endurecimentos opcionais")
- **Base de comparação:** execução do Agente 2 `20261001_154328` (5 rulesets PMD completos)
- **Reexecução do Agente 2 após a refatoração:** `20261001_161014`
- **Resultado do build:** `mvn clean package` → BUILD SUCCESS, 12/12 testes passaram (T01–T12)

## Arquivos alterados

A especificação prevê "10 + 2 = 12 por arquitetura" contando as duas
arquiteturas juntas. No monólito, a lista da especificação contém
**4 arquivos** (1 `PortfolioResponse` + 3 exceções):

| # | Arquivo | Endurecimento |
|---|---|---|
| 1 | `src/main/java/com/tcc/finance/web/dto/PortfolioResponse.java` | 1 — cópia defensiva |
| 2 | `src/main/java/com/tcc/finance/exception/BadRequestException.java` | 2 — `serialVersionUID` |
| 3 | `src/main/java/com/tcc/finance/exception/ConflictException.java` | 2 — `serialVersionUID` |
| 4 | `src/main/java/com/tcc/finance/exception/NotFoundException.java` | 2 — `serialVersionUID` |

Nenhum outro arquivo foi alterado (nem `pom.xml`, nem testes, nem
`application.properties`). Nenhuma dependência nova. A assinatura pública
do record `PortfolioResponse` (nomes e ordem dos componentes:
`username, positions, saldo, stocksValue, totalValue`) foi mantida.

## Diff aplicado

```diff
--- a/src/main/java/com/tcc/finance/web/dto/PortfolioResponse.java
+++ b/src/main/java/com/tcc/finance/web/dto/PortfolioResponse.java
@@ -5,4 +5,13 @@
 
 public record PortfolioResponse(String username, List<PositionView> positions, BigDecimal saldo,
                                 BigDecimal stocksValue, BigDecimal totalValue) {
+
+    public PortfolioResponse {
+        positions = positions == null ? List.of() : List.copyOf(positions);
+    }
+
+    @Override
+    public List<PositionView> positions() {
+        return List.copyOf(positions);
+    }
 }
--- a/src/main/java/com/tcc/finance/exception/BadRequestException.java
+++ b/src/main/java/com/tcc/finance/exception/BadRequestException.java
@@ -1,6 +1,8 @@
 package com.tcc.finance.exception;
 
 public class BadRequestException extends RuntimeException {
+    private static final long serialVersionUID = 1L;
+
     public BadRequestException(String message) {
         super(message);
     }
--- a/src/main/java/com/tcc/finance/exception/ConflictException.java
+++ b/src/main/java/com/tcc/finance/exception/ConflictException.java
@@ -1,6 +1,8 @@
 package com.tcc.finance.exception;
 
 public class ConflictException extends RuntimeException {
+    private static final long serialVersionUID = 1L;
+
     public ConflictException(String message) {
         super(message);
     }
--- a/src/main/java/com/tcc/finance/exception/NotFoundException.java
+++ b/src/main/java/com/tcc/finance/exception/NotFoundException.java
@@ -1,6 +1,8 @@
 package com.tcc.finance.exception;
 
 public class NotFoundException extends RuntimeException {
+    private static final long serialVersionUID = 1L;
+
     public NotFoundException(String message) {
         super(message);
     }
```

## Confirmação dos cenários T01–T12

Build executado no container `maven:3.9-eclipse-temurin-21` (mesma imagem
do `Dockerfile` e do Agente 1), com o cache `~/.m2` do host:

```
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 14.43 s -- in com.tcc.finance.FinanceScenariosTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Os testes JUnit existentes (`FinanceScenariosTest`) foram usados sem
alteração; nenhum teste novo foi criado.

## PMD / SpotBugs — antes e depois

Agente 2 reexecutado com o mesmo `infra/scripts/run_agente2.sh`
(maven-pmd-plugin 3.28.0 / PMD 7, spotbugs-maven-plugin 4.8.6.0,
`effort=Max`, `threshold=Low`, mesmos 5 rulesets), restrito ao módulo
monólito. O `pom.xml` foi restaurado ao final pelo próprio script.
0 `<error>` no XML do PMD, ou seja, todos os arquivos foram analisados.

| Ferramenta | Antes (`154328`) | Depois (`161014`) | Esperado pela especificação |
|---|---|---|---|
| PMD (total) | 9 | **6** | 9 → 6 ✅ |
| SpotBugs (total) | 2 | **0** | 2 → 0 ✅ |

### PMD por regra

| Regra | Antes | Depois |
|---|---|---|
| `MissingSerialVersionUID` | 3 | **0** |
| `LawOfDemeter` | 3 | 3 |
| `DataClass` | 1 | 1 |
| `LoosePackageCoupling` | 1 | 1 |
| `UseConcurrentHashMap` | 1 | 1 |
| `UseUtilityClass` | 1 | 1 |

### SpotBugs por tipo

| Tipo | Antes | Depois |
|---|---|---|
| `EI_EXPOSE_REP` (`PortfolioResponse.positions()`) | 1 | **0** |
| `EI_EXPOSE_REP2` (construtor de `PortfolioResponse`) | 1 | **0** |

`PortfolioResponse` não aparece mais no relatório do SpotBugs. Os 6
achados PMD restantes são exatamente os listados em "O que NÃO alterar"
da especificação (falsos positivos / estilo, documentados na revisão do
Agente 2) e não foram tocados, nem receberam comentários de supressão.

## Artefatos gerados

- `metrics/agente2_resumo_20261001_161014.md`
- `metrics/agente2_monolito_pmd_20261001_161014.xml`
- `metrics/agente2_monolito_spotbugs_20261001_161014.{xml,html}`

(na pasta `metrics/` da raiz do `tcc-financeiro`)

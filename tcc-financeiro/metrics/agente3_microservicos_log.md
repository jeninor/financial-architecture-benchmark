# Agente 3 (refatoração) — Microsserviços

Executado em: 2026-10-01
Especificação: `infra/scripts/AGENTE3_ESPECIFICACAO.md` (versão pós-revisão IA do Agente 2, execução `154328`)
Escopo: `multiagente/microservicos` (api-gateway, eureka-server, market-service, trade-service, user-service)

Nenhum defeito real foi corrigido (a revisão do Agente 2 não encontrou nenhum). Foram aplicados apenas os
dois endurecimentos opcionais definidos na especificação. Nenhum outro arquivo foi alterado, nenhum teste
foi criado ou modificado e nenhuma dependência foi adicionada.

## Arquivos alterados (8)

| # | Endurecimento | Arquivo |
|---|---|---|
| 1 | Cópia defensiva | `trade-service/src/main/java/com/tcc/finance/trade/web/dto/PortfolioResponse.java` |
| 2 | serialVersionUID | `market-service/src/main/java/com/tcc/finance/market/exception/NotFoundException.java` |
| 3 | serialVersionUID | `trade-service/src/main/java/com/tcc/finance/trade/exception/BadRequestException.java` |
| 4 | serialVersionUID | `trade-service/src/main/java/com/tcc/finance/trade/exception/ConflictException.java` |
| 5 | serialVersionUID | `trade-service/src/main/java/com/tcc/finance/trade/exception/NotFoundException.java` |
| 6 | serialVersionUID | `user-service/src/main/java/com/tcc/finance/user/exception/BadRequestException.java` |
| 7 | serialVersionUID | `user-service/src/main/java/com/tcc/finance/user/exception/ConflictException.java` |
| 8 | serialVersionUID | `user-service/src/main/java/com/tcc/finance/user/exception/NotFoundException.java` |

> Observação sobre a contagem: a especificação cita "10 + 2 = 12 por arquitetura", mas esse total
> soma as duas arquiteturas. Nos microsserviços são **7 exceções + 1 `PortfolioResponse` = 8 arquivos**
> (no monólito: 3 + 1 = 4; 8 + 4 = 12 no total).

## Diff aplicado

### Endurecimento 1 — `PortfolioResponse` (trade-service)

Assinatura pública do record inalterada (componentes `username`, `positions`, `saldo`, `stocksValue`,
`totalValue`, na mesma ordem).

```diff
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
```

### Endurecimento 2 — `serialVersionUID` (7 exceções)

Mesmo diff nos 7 arquivos (exemplo: `trade-service/.../exception/BadRequestException.java`; nos demais
muda apenas o nome da classe/pacote):

```diff
 public class BadRequestException extends RuntimeException {
+    private static final long serialVersionUID = 1L;
+
     public BadRequestException(String message) {
         super(message);
     }
 }
```

## Build e cenários funcionais (T01–T12)

`mvn -B -ntp clean package` em cada módulo (imagem `maven:3.9-eclipse-temurin-21`, a mesma do
`run_agente2.sh`), com os testes JUnit existentes:

| Módulo | Testes | Resultado |
|---|---|---|
| api-gateway | — (sem testes) | BUILD SUCCESS |
| eureka-server | — (sem testes) | BUILD SUCCESS |
| market-service | 2 (T01, T02) | 2/2 OK, BUILD SUCCESS |
| trade-service | 10 (T05–T12 + 2 do `FeignErrorDecoderTest`) | 10/10 OK, BUILD SUCCESS |
| user-service | 5 (T03, T04, T12 + 2 auxiliares de débito/crédito) | 5/5 OK, BUILD SUCCESS |

Todos os 12 cenários (T01–T12) continuam passando: 0 falhas, 0 erros, 0 ignorados.

## PMD / SpotBugs — antes e depois

Execução "depois": `run_agente2.sh` (mesmos plugins, versões e 5 rulesets) restrito aos módulos de
microsserviços, timestamp `20261001_161245` (resumo em `agente2_resumo_20261001_161245.md`).
Nenhum `<error>` nos XMLs do PMD (todos os arquivos foram analisados).

### Totais

| Ferramenta | Antes (`154328`) | Depois (`161245`) | Esperado pela especificação |
|---|---|---|---|
| PMD (violações) | 18 | **11** | 11 ✅ |
| SpotBugs (achados) | 4 | **2** | 2 ✅ |

### PMD por módulo/regra

| Módulo | Regra | Antes | Depois |
|---|---|---|---|
| api-gateway | UseUtilityClass | 1 | 1 |
| eureka-server | UseUtilityClass | 1 | 1 |
| market-service | MissingSerialVersionUID | 1 | **0** |
| market-service | UseConcurrentHashMap | 1 | 1 |
| market-service | UseUtilityClass | 1 | 1 |
| trade-service | MissingSerialVersionUID | 3 | **0** |
| trade-service | DataClass | 1 | 1 |
| trade-service | GuardLogStatement | 1 | 1 |
| trade-service | ImplicitFunctionalInterface | 1 | 1 |
| trade-service | UseConcurrentHashMap | 1 | 1 |
| user-service | MissingSerialVersionUID | 3 | **0** |
| user-service | LawOfDemeter | 1 | 1 |
| user-service | UseConcurrentHashMap | 1 | 1 |
| user-service | UseUtilityClass | 1 | 1 |
| **Total** | | **18** | **11** |

### SpotBugs (trade-service; demais módulos com 0 antes e depois)

| Tipo | Classe | Antes | Depois |
|---|---|---|---|
| EI_EXPOSE_REP | `web.dto.PortfolioResponse` | 1 | **0** |
| EI_EXPOSE_REP2 | `web.dto.PortfolioResponse` | 1 | **0** |
| EI_EXPOSE_REP2 | `client.FeignErrorDecoder` | 1 | 1 |
| EI_EXPOSE_REP2 | `messaging.TradeEventPublisher` | 1 | 1 |

Os 2 achados que permanecem (`FeignErrorDecoder`, `TradeEventPublisher`) são falsos positivos já
documentados pela revisão do Agente 2 (beans Spring injetados) e, conforme a especificação, **não são
regressão** e não foram tocados.

## Conclusão

Os dois endurecimentos foram aplicados exatamente como especificado. Build verde nos 5 módulos, T01–T12
passando, e as contagens de PMD (18 → 11) e SpotBugs (4 → 2) batem com o esperado. Nenhum dos achados
classificados como falso positivo ou estilo foi alterado, e nenhum comentário de supressão foi adicionado.

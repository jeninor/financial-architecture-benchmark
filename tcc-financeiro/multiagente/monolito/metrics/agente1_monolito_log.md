# Agente 1 — Log de geração (MONÓLITO)

- **Arquitetura:** monólito (`multiagente/monolito`)
- **Agente:** Claude Code (Claude Opus 5.5)
- **Data:** 2026-09-30
- **Especificação seguida:** `infra/scripts/AGENTE1_ESPECIFICACAO.md`
- **Resultado do build:** `mvn clean package` → BUILD SUCCESS, 12/12 testes passaram (T01–T12)

## Arquivos criados / modificados

### Modificados
| Arquivo | Alteração |
|---|---|
| `pom.xml` | + `spring-boot-starter-test` (test), + `h2` (test) |
| `src/main/resources/application.properties` | + `spring.jpa.open-in-view=false`, + `spring.jackson.deserialization.accept-float-as-int=false` |

### Criados (código de produção — `com.tcc.finance`)
| Camada | Arquivos |
|---|---|
| `domain` | `UserAccount.java`, `Position.java`, `TradeTransaction.java`, `TransactionType.java` |
| `repository` | `UserAccountRepository.java`, `PositionRepository.java`, `TradeTransactionRepository.java` |
| `service` | `QuoteService.java`, `UserService.java`, `TradeService.java` |
| `web` | `UserController.java`, `QuoteController.java`, `TradeController.java` |
| `web/dto` | `CreateUserRequest`, `UserResponse`, `QuoteResponse`, `TradeRequest`, `TradeResponse`, `PositionView`, `PortfolioResponse`, `HistoryEntry` |
| `exception` | `NotFoundException`, `BadRequestException`, `ConflictException`, `GlobalExceptionHandler` |

### Criados (testes)
- `src/test/java/com/tcc/finance/FinanceScenariosTest.java` — 12 testes `T01_cotacaoValida` … `T12_usuarioInexistente`
- `src/test/resources/application.properties` — H2 em memória (modo PostgreSQL)

`FinanceMonolitoApplication.java` (incluindo o `/health` usado pelos healthchecks) **não foi alterado**.

## Endpoints implementados

| Método | Rota | Corpo | Sucesso | Erros |
|---|---|---|---|---|
| POST | `/users` | `{"username"}` | 201 | 400 (vazio), 409 (duplicado) |
| GET | `/users/{username}` | — | 200 | 404 |
| GET | `/quote/{symbol}` | — | 200 | 404 |
| POST | `/buy` | `{"username","symbol","quantity"}` | 200 | 400, 404 |
| POST | `/sell` | `{"username","symbol","quantity"}` | 200 | 400, 404 |
| GET | `/portfolio/{username}` | — | 200 | 404 |
| GET | `/history/{username}` | — | 200 | 404 |

## Tempo aproximado

- Geração do código + testes + build verde: **~3 min** de relógio (medido do início ao fim da sessão de geração).
- Primeira execução do `mvn clean package` (com download de dependências): ~47 s.

## Erros encontrados e correções

| # | Erro | Causa | Correção |
|---|---|---|---|
| 1 | `java`/`mvn` não encontrados no host | Ambiente sem JDK/Maven instalados | Build executado via container `maven:3.9-eclipse-temurin-21` (mesma imagem do `Dockerfile`) |
| 2 | `Could not create local repository at /var/maven/.m2/repository` | Volume Docker nomeado criado com dono root, container rodando com UID do usuário | Volume descartado; usado `~/.m2` do host montado no container |
| 3 | `mounts denied` ao montar diretório em `/tmp` | Docker Desktop só compartilha caminhos sob `/home` | Cache Maven em `~/.m2` |

Nenhum erro de compilação ou falha de teste no código gerado: o build passou na primeira execução efetiva.

## Decisões de design não especificadas

1. **Formato dos parâmetros de `/buy` e `/sell`:** corpo JSON (`TradeRequest`), não query params.
2. **Endpoint de criação de usuário:** `POST /users` com `{"username"}`; adicionado também `GET /users/{username}` (consulta de saldo, útil nos testes e equivalente ao `user-service`).
3. **Tabela de cotações:** `Map` imutável em memória (`QuoteService`), não persistida — garante reprodutibilidade e evita seed de banco. Símbolos normalizados para maiúsculas (`aapl` → `AAPL`).
4. **Nomes de tabelas:** `users` (evita palavra reservada `user` do PostgreSQL), `positions`, `transactions`.
5. **Valores monetários:** `BigDecimal` com `precision=19, scale=2`; comparação com `compareTo`.
6. **Concorrência:** lock pessimista (`PESSIMISTIC_WRITE`) na linha do usuário durante compra/venda, dentro de `@Transactional`, para evitar saldo negativo/venda dupla em requisições concorrentes.
7. **Quantidade não inteira:** `accept-float-as-int=false` faz `quantity: 1.5` retornar 400 em vez de ser truncado para 1 (default do Jackson). `quantity` ausente/nulo → 400.
8. **Ordem de validação em `/buy` e `/sell`:** quantidade (400) → usuário (404) → símbolo (404) → saldo/posição (400).
9. **Posição zerada após venda total** é removida do banco (não aparece no portfólio).
10. **Portfólio** retorna, além de `positions` e `saldo`, os campos `stocksValue` (soma das posições) e `totalValue` (saldo + ações). Posições ordenadas por símbolo.
11. **Histórico** ordenado por `timestamp` ascendente com `id` como desempate (garante ordem estável para operações no mesmo instante). Inclui o campo `type` (`BUY`/`SELL`).
12. **Formato de erro padronizado:** `{"timestamp","status","error","message"}` via `@RestControllerAdvice`. `DataIntegrityViolationException` (corrida em username duplicado) → 409.
13. **Testes:** `@SpringBootTest` + `MockMvc` com H2 em memória (sem dependência do container Postgres); cada teste usa username aleatório para isolamento.
14. **`spring.jpa.open-in-view=false`:** evita sessões JPA abertas durante a renderização da resposta (todas as conversões para DTO ocorrem no service).

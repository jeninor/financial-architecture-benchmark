# Agente 1 — Log de geração (MICROSSERVIÇOS)

- **Arquitetura:** microsserviços (`multiagente/microservicos`)
- **Agente:** Claude Code (Claude Opus 5.5)
- **Data:** 2026-09-30
- **Especificação seguida:** `infra/scripts/AGENTE1_ESPECIFICACAO.md` (incluindo a seção "Decisões de design replicadas do monólito")
- **Resultado do build:** `mvn clean package` → BUILD SUCCESS nos três serviços

| Serviço | Testes | Resultado |
|---|---|---|
| `market-service` | T01, T02 | 2/2 ✔ |
| `user-service` | T03, T04, T12 + 2 testes dos endpoints internos de débito/crédito | 5/5 ✔ |
| `trade-service` | T05–T12 + 2 testes do `FeignErrorDecoder` | 10/10 ✔ |

Os 12 cenários T01–T12 estão cobertos com **os mesmos nomes de método do monólito**, distribuídos pelo
serviço responsável por cada regra. T12 (usuário inexistente) aparece no `user-service` (GET/débito/crédito)
e no `trade-service` (portfolio/history/buy/sell).

## Arquivos criados / modificados

### Modificados
| Arquivo | Alteração |
|---|---|
| `*/pom.xml` (user, market, trade) | + `spring-boot-starter-test` (test), + `h2` (test); trade-service: + `spring-rabbit-test` (test) |
| `*/src/main/resources/application.properties` (user, market, trade) | + `spring.jpa.open-in-view=false`, + `spring.jackson.deserialization.accept-float-as-int=false` |

`*Application.java` (incluindo `/health` e a declaração da fila `trade.audit.queue`), `eureka-server` e `api-gateway` **não foram alterados**.

### Criados — `market-service` (`com.tcc.finance.market`)
| Camada | Arquivos |
|---|---|
| `service` | `QuoteService` (tabela fixa em memória) |
| `web` | `QuoteController`, `dto/QuoteResponse` |
| `exception` | `NotFoundException`, `GlobalExceptionHandler` |
| testes | `FinanceScenariosTest` (T01, T02), `src/test/resources/application.properties` |

### Criados — `user-service` (`com.tcc.finance.user`)
| Camada | Arquivos |
|---|---|
| `domain` | `UserAccount` |
| `repository` | `UserAccountRepository` (com `findByUsernameForUpdate` — `PESSIMISTIC_WRITE`) |
| `service` | `UserService` (create, get, debit, credit) |
| `web` | `UserController`, `dto/CreateUserRequest`, `dto/UserResponse`, `dto/BalanceOperationRequest` |
| `exception` | `NotFoundException`, `BadRequestException`, `ConflictException`, `GlobalExceptionHandler` |
| testes | `FinanceScenariosTest` (T03, T04, T12 + débito/crédito), `src/test/resources/application.properties` |

### Criados — `trade-service` (`com.tcc.finance.trade`)
| Camada | Arquivos |
|---|---|
| `domain` | `Position`, `TradeTransaction`, `TransactionType` |
| `repository` | `PositionRepository` (com lock `PESSIMISTIC_WRITE`), `TradeTransactionRepository` |
| `client` | `UserClient`, `MarketClient` (OpenFeign), `FeignErrorDecoder`, `dto/UserDto`, `dto/QuoteDto`, `dto/BalanceOperationRequest` |
| `messaging` | `RabbitConfig`, `TradeCompletedEvent`, `TradeEventPublisher` |
| `service` | `TradeService` |
| `web` | `TradeController`, `dto/TradeRequest`, `TradeResponse`, `PositionView`, `PortfolioResponse`, `HistoryEntry` |
| `exception` | `NotFoundException`, `BadRequestException`, `ConflictException`, `GlobalExceptionHandler` |
| testes | `FinanceScenariosTest` (T05–T12), `InMemoryRemoteServices`, `FeignErrorDecoderTest`, `src/test/resources/application.properties` |

## Endpoints implementados

| Serviço | Método | Rota | Corpo | Sucesso | Erros |
|---|---|---|---|---|---|
| user | POST | `/users` | `{"username"}` | 201 | 400, 409 |
| user | GET | `/users/{username}` | — | 200 | 404 |
| user | POST | `/users/{username}/debit` (interno) | `{"amount"}` | 200 | 400 (saldo insuficiente/valor ≤ 0), 404 |
| user | POST | `/users/{username}/credit` (interno) | `{"amount"}` | 200 | 400, 404 |
| market | GET | `/quote/{symbol}` | — | 200 | 404 |
| trade | POST | `/buy` | `{"username","symbol","quantity"}` | 200 | 400, 404, 503 |
| trade | POST | `/sell` | `{"username","symbol","quantity"}` | 200 | 400, 404, 503 |
| trade | GET | `/portfolio/{username}` | — | 200 | 404, 503 |
| trade | GET | `/history/{username}` | — | 200 | 404, 503 |

Via API Gateway: `/user-service/users/...`, `/market-service/quote/...`, `/trade-service/buy`, etc.

## Tempo aproximado

- Geração do código + testes + build verde: ~10 min de relógio (estimativa; o agente não mediu com cronômetro).
- `mvn clean package` (container, cache Maven aquecido): market ~31 s, user ~31 s, trade ~41 s.

## Erros encontrados e correções

| # | Erro | Causa | Correção |
|---|---|---|---|
| 1 | `java`/`mvn` não encontrados no host | Ambiente sem JDK/Maven | Build via container `maven:3.9-eclipse-temurin-21` com `~/.m2` do host montado (mesmo procedimento do monólito) |
| 2 | trade-service: `NoUniqueBeanDefinitionException: more than one 'primary' bean found among candidates: [fakeUserClient, ...UserClient]` (8 testes com erro de contexto) | O OpenFeign registra os clientes como `@Primary`; os fakes de teste também eram `@Primary` | Testes passaram a usar `@MockBean` nos clientes Feign, delegando (`AdditionalAnswers.delegatesTo`) a fakes em memória. Só código de teste foi alterado |

market-service e user-service passaram na primeira execução.

## Decisões de design não especificadas

1. **Equivalência com o monólito:** mesmas rotas, DTOs (`TradeResponse`, `PortfolioResponse` com `stocksValue`/`totalValue`, `HistoryEntry`), formato de erro `{"timestamp","status","error","message"}`, nomes de tabela (`users`, `positions`, `transactions`) e ordem de validação: quantidade (400) → usuário (404) → símbolo (404) → saldo/posição (400).
2. **Lock do saldo (decisão obrigatória nº 5):** `user-service` expõe `POST /users/{username}/debit|credit`; cada chamada abre `@Transactional` e faz `SELECT ... FOR UPDATE` (`PESSIMISTIC_WRITE`) na linha do usuário. Verificação de saldo + débito acontecem dentro desse lock, então compras concorrentes não deixam o saldo negativo. O `trade-service` chama esses endpoints via OpenFeign.
3. **Lock da posição:** o `trade-service` também faz `PESSIMISTIC_WRITE` na linha da posição (`username`, `symbol`) durante compra/venda, para que vendas concorrentes não vendam mais do que o usuário possui.
4. **Sem transação distribuída (conforme a especificação):** compra = débito remoto (confirmado no user-service) → gravação local da posição/transação. Se a gravação local falhar depois do débito, o saldo fica debitado sem a posição. Isso é uma **limitação documentada** (sem Saga/2PC, sem compensação), comentada no Javadoc de `TradeService`.
5. **Referência ao usuário no trade-service:** `username` (string), sem chave estrangeira, pois o usuário vive em outra base.
6. **Existência do usuário:** `buy`/`sell`/`portfolio`/`history` chamam primeiro `GET /users/{username}`, garantindo 404 (T12) antes de qualquer outra validação, como no monólito.
7. **Tradução de erros remotos:** `FeignErrorDecoder` converte 400/404/409 dos serviços chamados nas exceções de domínio do trade-service, preservando status e mensagem. Falhas de rede/5xx (`FeignException`) → **503**.
8. **Evento `trade.completed`:** exchange tópico `trade.events`, routing key `trade.completed`, ligado à fila `trade.audit.queue` já declarada. Payload JSON (`Jackson2JsonMessageConverter`) com id, username, tipo, símbolo, quantidade, preço, total, saldo e timestamp. Publicado via `@TransactionalEventListener(AFTER_COMMIT)`, então trades revertidos não são anunciados. Falha no broker é logada e não desfaz o trade (o evento é de auditoria).
9. **Cotações:** `Map` imutável em memória no market-service (igual ao monólito); a dependência JPA do esqueleto foi mantida sem entidades.
10. **Testes:** `@SpringBootTest` + `MockMvc` + H2 em memória em cada serviço; Eureka desabilitado no perfil de teste (`eureka.client.enabled=false`). No trade-service, user-service e market-service são simulados em memória com o mesmo contrato, e o `RabbitTemplate` é mockado (T05 verifica a publicação do evento). T07 também cobre `quantity: 1.5` → 400.
11. **Dependências do esqueleto não usadas** (amqp/openfeign no user-service) foram mantidas, para não alterar a infraestrutura validada.

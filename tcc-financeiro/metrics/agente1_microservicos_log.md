# Agente 1 — Log de geração de código (MICROSSERVIÇOS)

- **Arquitetura:** microsserviços (`multiagente/microservicos`)
- **Data:** 2026-10-01 (revisão final: 2026-10-01T18:56-03:00)
- **Especificação seguida:** `infra/scripts/AGENTE1_ESPECIFICACAO.md`
- **Implementação oficial:** stack "Original (English)" em user-service, market-service
  e trade-service — pacotes `web/`, `web/dto/`, `domain/`, `client/` (`UserClient`,
  `MarketClient`, `FeignErrorDecoder`), `messaging/` e os testes `FinanceScenariosTest`.

## Resultado real do build/test

Comando executado em cada módulo:

```
docker run --rm -v "$MODULE_DIR:/app" -v "$HOME/.m2:/root/.m2" -w /app \
  maven:3.9-eclipse-temurin-21 mvn -B -ntp clean test
```

| Módulo | Build | Testes | Cenários |
|--------|-------|--------|----------|
| api-gateway | BUILD SUCCESS | sem testes | — |
| eureka-server | BUILD SUCCESS | sem testes | — |
| market-service | BUILD SUCCESS | 2 run, 0 falhas, 0 erros | T01, T02 |
| user-service | BUILD SUCCESS | 5 run, 0 falhas, 0 erros | T03, T04, T12 + débito/crédito (2) |
| trade-service | BUILD SUCCESS | 10 run, 0 falhas, 0 erros | T05–T12 (8) + `FeignErrorDecoderTest` (2) |

Os 12 cenários T01–T12 ficam cobertos no conjunto dos serviços. T12 é testado tanto no
user-service quanto no trade-service. No trade-service, user-service e market-service
são simulados em memória (`InMemoryRemoteServices`), e o `RabbitTemplate` é um
`@MockBean`. O T05 verifica a publicação de `trade.completed`.

## Histórico — o que estava errado

1. **A afirmação do log anterior estava incorreta.** O log anterior dizia que o código
   de negócio "havia sido perdido num reset de container". A stack original (datada de
   2026-09-30) continuava presente nos três serviços. O Agente 1 gerou uma segunda
   stack paralela em português (`controller/`, `dto/`, `model/`, `Usuario*`,
   `MarketDataService`, `*ApplicationTests`) e sobrescreveu `TradeService` e os
   `GlobalExceptionHandler` originais.
2. **Erros reais na primeira execução de `mvn -B -ntp clean test`:**
   - **trade-service: erro de compilação**, idêntico ao do monólito:
     ```
     web/TradeController.java:[27,32] incompatible types: com.tcc.finance.trade.dto.TradeResponse
       cannot be converted to com.tcc.finance.trade.web.dto.TradeResponse
     web/TradeController.java:[37,38] ...dto.PortfolioResponse cannot be converted to ...web.dto.PortfolioResponse
     web/TradeController.java:[42,28] cannot find symbol: method history(java.lang.String)
     ```
   - **user-service:** compilava, mas `Tests run: 11, Errors: 11`. Causa:
     `Ambiguous mapping. Cannot map 'userController' method`, porque `UserController` e
     `UsuarioController` mapeavam as mesmas rotas `/users`.
   - **market-service:** compilava, mas `Tests run: 4, Errors: 4`. Causa:
     `Ambiguous mapping. Cannot map 'quoteController' method`, porque `QuoteController`
     e `MarketController` mapeavam `/quote/{symbol}`.
3. Os testes das duas stacks tinham contratos JSON incompatíveis (`positions`/`type=BUY`
   versus `posicoes`/`tipo=COMPRA`).

## Correções aplicadas

- **trade-service: reescrito `service/TradeService.java`** sobre `domain/`, `web/dto/`,
  `UserClient`/`MarketClient` (OpenFeign) e `messaging/TradeCompletedEvent`. Implementa
  `buy`, `sell`, `portfolio` e `history(String username)`:
  - 404 para usuário ou símbolo inexistente (via `FeignErrorDecoder`);
  - o saldo é validado e debitado no user-service (`/users/{u}/debit`, com lock
    pessimista);
  - a posição é lida com `findByUsernameAndSymbolForUpdate`;
  - o evento é publicado com `ApplicationEventPublisher` e enviado ao RabbitMQ por
    `TradeEventPublisher` somente após o commit (`AFTER_COMMIT`);
  - sem Saga/2PC: consistência eventual, conforme a especificação.
- **Reescrito `exception/GlobalExceptionHandler.java`** em trade-service e user-service
  (`NotFound`→404, `Conflict`→409, `BadRequest`→400, JSON ilegível→400) e em
  market-service (`NotFound`→404, JSON ilegível→400).
- api-gateway e eureka-server: nenhuma alteração.

## Movido para `_descartado/` (fora de `src/`, não compilado), por módulo

Caminhos relativos preservados:

- **user-service/_descartado/** (13): `controller/UsuarioController`,
  `dto/{AmountRequest,UsuarioRequest,UsuarioResponse}`,
  `exception/{DuplicateUsernameException,GlobalExceptionHandler,InsufficientFundsException,UserNotFoundException}`,
  `model/Usuario`, `repository/UsuarioRepository`, `service/UsuarioService`,
  `UserServiceApplicationTests`, `application-test.properties`.
- **market-service/_descartado/** (7): `controller/MarketController`, `dto/QuoteResponse`,
  `exception/{GlobalExceptionHandler,SymbolNotFoundException}`,
  `service/MarketDataService`, `MarketServiceApplicationTests`,
  `application-test.properties`.
- **trade-service/_descartado/** (29):
  `client/{MarketServiceClient,MarketServiceClientConfig,UserServiceClient,UserServiceClientConfig}`,
  `controller/TradeController`,
  `dto/{AmountRequest,PortfolioResponse,PosicaoResponse,QuoteDTO,TradeRequest,TradeResponse,TransacaoResponse,UserDTO}`,
  `event/{TradeAuditPublisher,TradeCompletedEvent}`,
  `exception/{GlobalExceptionHandler,InsufficientFundsException,InsufficientSharesException,InvalidQuantityException,SymbolNotFoundException,UserNotFoundException}`,
  `model/{Posicao,TipoTransacao,Transacao}`,
  `repository/{PosicaoRepository,TransacaoRepository}`, `service/TradeService`
  (versão Agente 1), `TradeServiceApplicationTests`, `application-test.properties`.

Observação: as versões originais de `TradeService` e dos `GlobalExceptionHandler` não
puderam ser recuperadas, porque foram sobrescritas e não há git. As versões atuais são
reimplementações que seguem a estrutura e os nomes da stack original.

# Agente 1 — Log de geração de código (MONÓLITO)

- **Arquitetura:** monólito (`multiagente/monolito`)
- **Data:** 2026-10-01 (revisão final: 2026-10-01T18:56-03:00)
- **Especificação seguida:** `infra/scripts/AGENTE1_ESPECIFICACAO.md`
- **Implementação oficial:** stack "Original (English)" — pacotes `web/`, `web/dto/`,
  `domain/`, `service/UserService`, `service/QuoteService`, `service/TradeService`,
  teste `FinanceScenariosTest` (T01–T12).

## Resultado real do build/test

Comando (mesma imagem usada por `infra/scripts/run_agente2.sh`):

```
docker run --rm -v "$PWD:/app" -v "$HOME/.m2:/root/.m2" -w /app \
  maven:3.9-eclipse-temurin-21 mvn -B -ntp clean test
```

```
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0 -- in com.tcc.finance.FinanceScenariosTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

| Cenário | Resultado |
|---------|-----------|
| T01_cotacaoValida … T12_usuarioInexistente | 12/12 passaram (0 falhas, 0 erros) |

## Histórico — o que estava errado

1. **A afirmação do log anterior estava incorreta.** O log anterior dizia que o código
   de negócio "havia sido perdido num reset de container". Na verdade, a stack original
   (datada de 2026-09-30) continuava presente. O Agente 1 gerou uma **segunda stack
   paralela** (em português: `controller/`, `dto/`, `model/`, `UsuarioService`,
   `MarketDataService`, teste `FinanceMonolitoApplicationTests`) ao lado dela e
   **sobrescreveu** `service/TradeService.java` e `exception/GlobalExceptionHandler.java`
   da stack original.
2. **Erro de compilação** (primeira execução real de `mvn -B -ntp clean test`):
   ```
   web/TradeController.java:[27,32] incompatible types: com.tcc.finance.dto.TradeResponse
     cannot be converted to com.tcc.finance.web.dto.TradeResponse
   web/TradeController.java:[32,33] (idem, sell)
   web/TradeController.java:[37,38] com.tcc.finance.dto.PortfolioResponse cannot be converted
     to com.tcc.finance.web.dto.PortfolioResponse
   web/TradeController.java:[42,28] cannot find symbol: method history(java.lang.String)
   ```
   Causa: o `TradeService` sobrescrito retornava os DTOs de `com.tcc.finance.dto` e
   tinha `historico(...)` em vez de `history(...)`.
3. **Conflitos que surgiriam mesmo corrigindo os imports:** as duas stacks mapeavam as
   mesmas rotas (`/buy`, `/sell`, `/portfolio/{u}`, `/history/{u}`, `/users`,
   `/quote/{s}`) e as duas definiam um bean `tradeController`. O Spring falharia na
   inicialização (ambiguous mapping / bean duplicado).
4. **Contratos de teste contraditórios:** `FinanceScenariosTest` espera `positions`,
   `totalValue`, `stocksValue`, `total` e `type=BUY/SELL`, enquanto
   `FinanceMonolitoApplicationTests` espera `posicoes`, `valorTotal` e `tipo=COMPRA/VENDA`.
   Não é possível satisfazer os dois ao mesmo tempo.
5. O `GlobalExceptionHandler` sobrescrito só tratava as exceções da stack em português.
   As exceções `NotFoundException`, `ConflictException` e `BadRequestException`, usadas
   por `UserService`/`QuoteService`, não eram mapeadas para 404/409/400.

## Correções aplicadas

- Por decisão do autor, a stack "Original (English)" virou a oficial.
- **Reescrito `service/TradeService.java`** sobre `domain/` + `web/dto/`, com
  `buy`, `sell`, `portfolio` e `history(String username)` (este último é necessário
  para o T11):
  - quantidade `null`/≤0 → `BadRequestException` (400);
  - usuário inexistente → `NotFoundException` (404), com lock pessimista
    (`findByUsernameForUpdate`) em compra/venda;
  - símbolo inválido → 404 (via `QuoteService`);
  - saldo insuficiente / venda acima da posição → 400;
  - o portfólio inclui `stocksValue` e `totalValue`;
  - o histórico é ordenado por `timestamp`, `id`.
- **Reescrito `exception/GlobalExceptionHandler.java`**: `NotFoundException`→404,
  `ConflictException`→409, `BadRequestException`→400, e
  `HttpMessageNotReadableException`→400 (JSON inválido ou quantidade não inteira).
- Nenhum outro arquivo da stack original foi alterado. As exceções originais
  (`BadRequestException`, `ConflictException`, `NotFoundException`) já existiam.

## Movido para `multiagente/monolito/_descartado/` (fora de `src/`, não compilado)

Caminhos relativos preservados (30 arquivos):

- `controller/` MarketController, TradeController, UsuarioController
- `dto/` PortfolioResponse, PosicaoResponse, QuoteResponse, TradeRequest, TradeResponse,
  TransacaoResponse, UsuarioRequest, UsuarioResponse
- `exception/` DuplicateUsernameException, GlobalExceptionHandler (versão Agente 1),
  InsufficientFundsException, InsufficientSharesException, InvalidQuantityException,
  SymbolNotFoundException, UserNotFoundException
- `model/` Posicao, TipoTransacao, Transacao, Usuario
- `repository/` PosicaoRepository, TransacaoRepository, UsuarioRepository
- `service/` MarketDataService, TradeService (versão Agente 1), UsuarioService
- `src/test/java/.../FinanceMonolitoApplicationTests.java`,
  `src/test/resources/application-test.properties`

Observação: as versões originais de `TradeService` e `GlobalExceptionHandler` não puderam
ser recuperadas, porque foram sobrescritas, não há git e não sobrou nenhum `target/`
anterior. Por isso, as versões atuais são reimplementações.

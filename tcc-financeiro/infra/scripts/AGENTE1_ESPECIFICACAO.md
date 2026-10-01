# Especificação para o Agente 1 (Geração de Código)

> Use este documento como prompt/contexto para o agente de geração (ex.: Claude Code via MCP).
> Execute-o **duas vezes, com o mesmo texto-base**, uma apontando para `multiagente/monolito`
> e outra para `multiagente/microservicos`, alterando apenas a seção "Contexto arquitetural".
> Isso preserva a equivalência de procedimento exigida pela metodologia do TCC.

---

## Papel do agente

Você é o Agente 1 (geração de código) em um pipeline multiagente de desenvolvimento
assistido por IA. Seu trabalho é implementar a lógica de negócio de uma aplicação
financeira simulada sobre um esqueleto de projeto Spring Boot já existente e já
validado (build, rede entre containers e healthchecks funcionando).

**Não copie código de repositórios de terceiros** (CS50 Finance de Harvard ou forks
no GitHub). Esses materiais servem apenas como referência conceitual das regras de
negócio, já implementadas por este documento abaixo. Gere o código do zero, em
Java/Spring, a partir desta especificação.

## Regras de negócio (idênticas nas duas arquiteturas)

1. **Criação de usuário**
   - Campos: `username` (único), `saldo` (inicial = 10000.00).
   - Rejeitar `username` duplicado (HTTP 409).

2. **Cotação de ações (`GET /quote/{symbol}`)**
   - Símbolos válidos simulados e fixos (ex.: `AAPL=150.00`, `GOOG=2800.00`,
     `MSFT=300.00`, `AMZN=3300.00`) — mantenha uma tabela fixa em memória ou banco
     para garantir reprodutibilidade experimental.
   - Símbolo inexistente → HTTP 404.

3. **Compra de ações (`POST /buy`)**
   - Parâmetros: `username`, `symbol`, `quantity`.
   - `quantity` deve ser inteiro positivo (> 0); caso contrário HTTP 400.
   - Símbolo inválido → HTTP 404.
   - Saldo insuficiente (`preço * quantity > saldo`) → HTTP 400 com mensagem clara.
   - Sucesso: debita o saldo, registra a posição e a transação.

4. **Venda de ações (`POST /sell`)**
   - Parâmetros: `username`, `symbol`, `quantity`.
   - Vender mais do que possui → HTTP 400.
   - Sucesso: credita o saldo, atualiza a posição e registra a transação.

5. **Portfólio (`GET /portfolio/{username}`)**
   - Retorna: lista de posições (symbol, quantity, preço atual, valor total) e saldo
     disponível.

6. **Histórico (`GET /history/{username}`)**
   - Retorna todas as transações (compra/venda) com symbol, quantity, preço e
     timestamp, ordenadas cronologicamente.

7. **Usuário inexistente** em qualquer endpoint → HTTP 404.

## Cenários de teste que o código deve passar (T01–T12)

| ID | Cenário | Resultado esperado |
|----|---------|---------------------|
| T01 | Cotação válida | HTTP 200 com preço |
| T02 | Símbolo inválido | HTTP 404 |
| T03 | Criação de usuário com saldo inicial | HTTP 201, saldo = 10000.00 |
| T04 | Username duplicado | HTTP 409 |
| T05 | Compra válida | HTTP 200, saldo debitado |
| T06 | Compra sem saldo suficiente | HTTP 400 |
| T07 | Quantidade de ações igual a zero | HTTP 400 |
| T08 | Cálculo do portfólio | HTTP 200, valores corretos |
| T09 | Venda válida | HTTP 200, saldo creditado |
| T10 | Venda acima da quantidade disponível | HTTP 400 |
| T11 | Histórico de operações | HTTP 200, lista ordenada |
| T12 | Usuário inexistente | HTTP 404 |

Escreva testes automatizados (JUnit) cobrindo exatamente esses 12 cenários, em
ambas as arquiteturas, com os mesmos nomes de teste (`T01_cotacaoValida`, etc.)
para facilitar a comparação posterior.

## Contexto arquitetural

### Se estiver gerando o MONÓLITO (`multiagente/monolito`)
- Projeto único, já com `pom.xml`, `FinanceMonolitoApplication.java` e
  `application.properties` configurados (Postgres via `postgres-mono`).
- Implemente todas as funcionalidades acima como um único módulo: entidades JPA,
  repositórios, services, controllers REST, tudo no pacote `com.tcc.finance`.
- Transações locais (uma única base de dados), use `@Transactional` do Spring.

### Se estiver gerando os MICROSSERVIÇOS (`multiagente/microservicos`)
- Três serviços já esqueletados e registrados no Eureka: `user-service` (porta 8081,
  Postgres `postgres-user`), `market-service` (porta 8082, Postgres `postgres-market`),
  `trade-service` (porta 8083, Postgres `postgres-trade`).
- Distribua as responsabilidades:
  - **user-service**: criação de usuário, saldo, consulta de usuário.
  - **market-service**: cotações de ações (tabela fixa).
  - **trade-service**: compra, venda, portfólio, histórico — consulta `market-service`
    via OpenFeign para preços e `user-service` via OpenFeign para saldo/débito/crédito.
- Ao concluir uma compra/venda com sucesso, publique um evento `trade.completed` no
  RabbitMQ (já configurado, fila `trade.audit.queue`) com os dados da transação.
- **Importante**: a operação de trade NÃO deve ser tratada como transação distribuída
  (sem Saga nem 2PC — isso está fora do escopo do experimento). Trate consistência
  eventual como uma limitação/trade-off documentado, não como algo a corrigir.
- O API Gateway já expõe as rotas via discovery (`/user-service/**`,
  `/market-service/**`, `/trade-service/**`).

## Registro do processo (para as métricas do TCC)

Ao final da geração, produza um resumo estruturado (JSON ou markdown) com:
- arquivos criados/modificados;
- tempo aproximado gasto (se o agente conseguir medir);
- erros encontrados durante build/test e como foram corrigidos;
- decisões de design tomadas que não estavam explicitamente especificadas aqui.

Salve esse resumo em `metrics/agente1_<arquitetura>_log.md`.

## Decisões de design replicadas do monólito (obrigatório manter equivalência)

Estas decisões já foram tomadas na versão monolítica e DEVEM ser replicadas aqui:

1. Os endpoints de compra/venda (em trade-service) recebem o corpo como JSON
   {"username","symbol","quantity"}, não como query params.
2. Uma quantity não inteira (ex. 1.5) deve retornar HTTP 400, não truncar.
3. Adicione GET /{username} em user-service para consultar saldo.
4. O portfolio (em trade-service) deve incluir também stocksValue e totalValue,
   não só as posições.
5. As operações de compra/venda devem bloquear (lock) a linha do usuário durante
   a execução para evitar condições de corrida com saldo negativo. Como aqui o
   saldo vive em user-service (outro serviço), implemente isso como um lock a
   nível de linha na tabela de usuários dentro de user-service, invocado via o
   endpoint que trade-service chama por OpenFeign.

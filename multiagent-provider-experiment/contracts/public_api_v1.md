# Contrato público da API — v1

Este documento descreve o comportamento público necessário para que as duas
arquiteturas sejam funcionalmente equivalentes. Ele não revela a implementação
histórica.

## Tipos básicos

- `userId`: UUID.
- `shares`: inteiro positivo.
- valores monetários: numéricos decimais.
- símbolos: strings em maiúsculas.

## Cotações fixas

- AAPL = 200.00
- MSFT = 400.00
- GOOGL = 170.00
- AMZN = 190.00
- NVDA = 120.00

## POST /api/users

Request:

```json
{"username":"experiment01"}
```

Comportamento:

- usuário novo: HTTP 201;
- saldo inicial: 10000.00;
- username duplicado: HTTP 400;
- resposta deve expor o identificador UUID do usuário.

## GET /api/quotes/{symbol}

Comportamento:

- símbolo válido: HTTP 200;
- símbolo inválido/desconhecido: HTTP 400.

A resposta deve expor pelo menos:

```json
{"symbol":"AAPL","price":200.00}
```

## POST /api/trades/buy

Request:

```json
{"userId":"<UUID>","symbol":"AAPL","shares":10}
```

Comportamento:

- operação válida: HTTP 201;
- shares <= 0: HTTP 400;
- saldo insuficiente: HTTP 400;
- símbolo inválido: HTTP 400;
- usuário UUID inexistente: HTTP 404.

## POST /api/trades/sell

Request:

```json
{"userId":"<UUID>","symbol":"AAPL","shares":3}
```

Comportamento:

- operação válida: HTTP 201;
- shares <= 0: HTTP 400;
- posição insuficiente: HTTP 400;
- símbolo inválido: HTTP 400;
- usuário UUID inexistente: HTTP 404.

Uma operação rejeitada não pode alterar saldo nem posição.

## GET /api/users/{userId}/portfolio

- usuário existente: HTTP 200;
- UUID de usuário inexistente: HTTP 404.

A resposta deve expor:

- saldo em `cash` ou `cashBalance`;
- coleção `holdings`;
- para cada holding: `symbol`, `shares`, `price`, `marketValue`;
- valor total do portfólio em `totalValue` (ou campo equivalente aceito pelo contrato).

Com preços fixos, comprar/vender ativos não altera o patrimônio total inicial
quando não há taxas.

## GET /api/users/{userId}/trades

- usuário existente: HTTP 200;
- UUID de usuário inexistente: HTTP 404;
- resposta: array JSON;
- cada item deve expor `type` com valor `BUY` ou `SELL`.

## Cenários externos

A aplicação será avaliada por uma suíte black-box externa de 12 cenários.
O agente não pode ler ou modificar essa suíte durante a execução.

# Contrato funcional

As duas arquiteturas devem implementar comportamento equivalente.

## Cotações fixas

- AAPL = 200.00
- MSFT = 400.00
- GOOGL = 170.00
- AMZN = 190.00
- NVDA = 120.00

## API funcional

- POST /api/users
- GET /api/quotes/{symbol}
- POST /api/trades/buy
- POST /api/trades/sell
- GET /api/users/{userId}/portfolio
- GET /api/users/{userId}/trades

## Regras

- novo usuário começa com 10000.00;
- username é único;
- shares > 0;
- símbolo deve existir;
- BUY exige saldo suficiente;
- SELL não pode exceder a posição disponível;
- usuário inexistente deve gerar HTTP 404;
- portfólio deve refletir cash + holdings;
- histórico deve refletir BUY/SELL.

## Fora do escopo

- frontend;
- autenticação/autorização;
- preço de mercado externo;
- Kubernetes;
- novas funcionalidades de negócio;
- mudança da topologia arquitetural.

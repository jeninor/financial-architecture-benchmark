Implemente o Trade Service preservando as regras funcionais do
monólito.

Endpoints:

POST /api/trades/buy

POST /api/trades/sell

GET /api/users/{userId}/portfolio

GET /api/users/{userId}/trades

O Trade Service deverá possuir banco PostgreSQL independente.

Utilize OpenFeign para comunicação com:

User Service
Market Service

BUY:

1. consultar cotação;
2. calcular total;
3. solicitar debit ao User Service;
4. persistir Trade do tipo BUY;
5. retornar saldo resultante.

SELL:

1. consultar cotação;
2. calcular quantidade atualmente possuída;
3. impedir venda superior à quantidade disponível;
4. solicitar credit ao User Service;
5. persistir Trade do tipo SELL.

Portfolio:

- consultar saldo do usuário;
- reconstruir posições pelas transações;
- consultar preços atuais;
- calcular holdingsValue e totalValue.

History:

- retornar BUY e SELL em ordem cronológica.

Não implementar Saga ou transação distribuída nesta etapa.
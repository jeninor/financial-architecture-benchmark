Implemente o User Service da aplicação financeira.

Responsabilidades:

POST /api/users

GET /internal/users/{id}

POST /internal/users/{id}/debit

POST /internal/users/{id}/credit

Um novo usuário deve iniciar com saldo de 10.000,00.

Regras:

- username deve ser único;
- debit deve rejeitar saldo insuficiente;
- valores devem ser positivos;
- usuário inexistente deve produzir HTTP 404.

Utilizar PostgreSQL próprio do User Service.

Não implementar lógica de compra, venda ou cotação neste serviço.

Mantenha as respostas compatíveis com os contratos necessários
para a aplicação equivalente ao monólito.
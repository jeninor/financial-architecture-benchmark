CONTEXTO

Será desenvolvida uma segunda implementação da mesma aplicação
financeira utilizada no experimento monolítico.

Os requisitos funcionais devem permanecer equivalentes.

ARQUITETURA

Utilizar arquitetura de microsserviços com:

- Java 21;
- Spring Boot;
- Spring Cloud;
- PostgreSQL;
- Docker;
- REST.

Separar inicialmente as responsabilidades em:

User Service
Market Service
Trade Service

Cada serviço deve ter responsabilidade bem definida.

User Service:
- criação de usuário;
- saldo;
- debit;
- credit.

Market Service:
- cotação simulada e determinística.

Trade Service:
- BUY;
- SELL;
- histórico;
- portfólio.

Não implementar frontend ou autenticação.

Preservar os mesmos valores simulados e regras funcionais da
implementação monolítica.

Não adicionar funcionalidades que não existam no experimento
monolítico.
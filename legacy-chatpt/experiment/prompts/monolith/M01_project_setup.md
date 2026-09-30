Desenvolva a estrutura inicial de uma aplicação financeira utilizando Java 21, Spring Boot e PostgreSQL.
A aplicação deve permitir criação de usuários com saldo inicial de 10.000, consulta de cotações simuladas e registro de operações financeiras.
Utilize uma API REST e organize o código de forma adequada à arquitetura monolítica.
Não implemente frontend ou autenticação. Utilize cotações fixas para garantir reprodutibilidade do experimento.
Antes de gerar o código, descreva brevemente a estrutura proposta.



CONTEXTO

Estou desenvolvendo uma aplicação financeira simulada para um
experimento acadêmico de comparação entre arquitetura monolítica
e arquitetura de microsserviços.

Esta etapa corresponde à implementação MONOLÍTICA.

TECNOLOGIAS

- Java 21
- Spring Boot
- PostgreSQL
- Maven
- Docker
- API REST

OBJETIVO

Criar a estrutura inicial de uma aplicação monolítica que concentre
em uma única aplicação as funcionalidades de usuários, mercado,
transações e portfólio.

REQUISITOS

A aplicação deverá permitir:

1. criação de usuários;
2. saldo inicial de 10.000,00;
3. consulta de cotação de ações;
4. compra de ações;
5. venda de ações;
6. consulta do portfólio;
7. consulta do histórico de transações.

Não implementar:

- frontend;
- autenticação;
- autorização;
- integrações externas de mercado.

As cotações deverão ser fixas para garantir reprodutibilidade.

Utilizar inicialmente:

AAPL = 200.00
MSFT = 400.00
GOOGL = 170.00
AMZN = 190.00
NVDA = 120.00

REGRAS

- shares deve ser maior que zero;
- não permitir compra sem saldo suficiente;
- não permitir venda superior à quantidade possuída;
- símbolos inválidos devem retornar erro adequado;
- usuários inexistentes devem retornar HTTP 404.

Antes de gerar código, apresente a estrutura de pacotes e classes.

Evite adicionar funcionalidades fora deste escopo.


Integre os microsserviços utilizando Eureka Service Discovery e
Spring Cloud Gateway.

Eureka:

- executar na porta 8761;
- registrar User Service;
- registrar Market Service;
- registrar Trade Service.

API Gateway:

- executar externamente na porta 8080;
- utilizar descoberta Eureka;
- utilizar lb:// para encaminhamento.

Rotas externas:

POST /api/users
GET /api/quotes/**
POST /api/trades/**
GET /api/users/*/portfolio
GET /api/users/*/trades

Preservar uma interface externa equivalente à aplicação monolítica.

Antes de executar cada aplicação Spring Boot, verificar:

- pom.xml;
- classe @SpringBootApplication;
- método public static void main;
- caminho correto do package.

Validar:

GET /actuator/health

e o registro dos quatro componentes no Eureka.

Não alterar regras de negócio.
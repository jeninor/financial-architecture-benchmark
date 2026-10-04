# Contrato de arquitetura — Monólito v1

Este contrato define a arquitetura FIXA do tratamento monolítico.

## Topologia permitida

O workspace deve preservar o `docker-compose.yml` original do baseline.

Serviços Compose esperados:

- `app`: única aplicação Spring Boot de negócio;
- `postgres`: único banco PostgreSQL da aplicação;
- `postgres-test`: infraestrutura auxiliar de teste já existente no baseline.

`postgres-test` não representa um segundo banco funcional da aplicação e não
deve ser usado pela implementação em produção.

## Aplicação

- Uma única aplicação Spring Boot.
- Java 21.
- Maven.
- PostgreSQL.
- API REST pública em `http://localhost:8080`.
- Package base existente: `com.juanesteban.tcc.finance`.

As responsabilidades de usuários, cotações, trades e portfólio devem permanecer
dentro dessa única aplicação.

## Permitido ao agente

- criar pacotes/classes Java dentro da aplicação existente;
- editar `pom.xml` quando necessário;
- editar `application.yml` quando necessário;
- executar Maven e Docker;
- consultar logs;
- criar tabelas por meio do mecanismo já configurado na aplicação.

## Proibido ao agente

- modificar `docker-compose.yml`;
- adicionar/remover serviços Compose;
- criar microsserviços;
- adicionar API Gateway;
- adicionar service discovery;
- adicionar RabbitMQ ou outro broker;
- adicionar outro banco funcional;
- alterar a porta pública 8080;
- acessar projetos fora do workspace;
- acessar/modificar a suíte externa de aceitação.

A arquitetura é conhecida pelo agente. A implementação histórica de
`legacy-chatpt` não é fornecida.

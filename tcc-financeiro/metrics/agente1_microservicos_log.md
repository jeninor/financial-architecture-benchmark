# Agente 1 — Log de geração de código (MICROSSERVIÇOS)

- **Arquitetura:** microsserviços (`multiagente/microservicos`)
- **Agente:** Agente 1 (geração de código), Claude Code / Claude Sonnet 5
- **Data:** 2026-10-01
- **Especificação seguida:** `infra/scripts/AGENTE1_ESPECIFICACAO.md`
- **Contexto:** o código de negócio dos 5 módulos havia sido perdido num
  reset de container; apenas `pom.xml`, a classe de bootstrap (com `/health`)
  e `application.properties` de cada módulo sobreviveram. Este agente
  implementou a lógica de negócio do zero, sem copiar código de terceiros.

## Distribuição de responsabilidades (conforme especificação)

- **user-service** (8081): criação de usuário, saldo, consulta; endpoints
  internos de débito/crédito consumidos pelo trade-service via OpenFeign.
- **market-service** (8082): cotações de ações (tabela fixa em memória).
- **trade-service** (8083): compra, venda, portfólio, histórico; consulta
  `user-service` e `market-service` via OpenFeign; publica `trade.completed`
  na fila RabbitMQ `trade.audit.queue` ao concluir compra/venda.
- **api-gateway** e **eureka-server**: sem alteração — o esqueleto já tinha
  roteamento via discovery (`spring.cloud.gateway.discovery.locator.enabled=true`)
  e o servidor Eureka configurado; nenhuma lógica de negócio nova era
  necessária.

## Arquivos criados por módulo

### user-service (pacote `com.tcc.finance.user`)
- `model/Usuario.java`
- `repository/UsuarioRepository.java`
- `dto/UsuarioRequest.java`, `dto/UsuarioResponse.java`, `dto/AmountRequest.java`
- `exception/UserNotFoundException.java`, `exception/DuplicateUsernameException.java`,
  `exception/InsufficientFundsException.java`, `exception/GlobalExceptionHandler.java`
- `service/UsuarioService.java` (inclui `debitar`/`creditar`, usados pelo trade-service)
- `controller/UsuarioController.java` (`POST /users`, `GET /users/{username}`,
  `POST /users/{username}/debit`, `POST /users/{username}/credit`)
- Teste: `UserServiceApplicationTests.java` (T03, T04, T12 + débito/crédito)
- `src/test/resources/application-test.properties` (H2 + Eureka desabilitado)
- `pom.xml`: adicionadas `com.h2database:h2` e `spring-boot-starter-test` (scope test)

### market-service (pacote `com.tcc.finance.market`)
- `dto/QuoteResponse.java`
- `exception/SymbolNotFoundException.java`, `exception/GlobalExceptionHandler.java`
- `service/MarketDataService.java` (tabela fixa, sem JPA — nenhuma entidade foi criada)
- `controller/MarketController.java` (`GET /quote/{symbol}`)
- Teste: `MarketServiceApplicationTests.java` (T01, T02)
- `src/test/resources/application-test.properties` (H2 + Eureka desabilitado)
- `pom.xml`: adicionadas `com.h2database:h2` e `spring-boot-starter-test` (scope test)

### trade-service (pacote `com.tcc.finance.trade`)
- `model/Posicao.java`, `model/Transacao.java`, `model/TipoTransacao.java`
- `repository/PosicaoRepository.java`, `repository/TransacaoRepository.java`
- `dto/UserDTO.java`, `dto/QuoteDTO.java`, `dto/AmountRequest.java`, `dto/TradeRequest.java`,
  `dto/TradeResponse.java`, `dto/PosicaoResponse.java`, `dto/PortfolioResponse.java`, `dto/TransacaoResponse.java`
- `client/UserServiceClient.java`, `client/MarketServiceClient.java` (interfaces OpenFeign)
- `client/UserServiceClientConfig.java`, `client/MarketServiceClientConfig.java`
  (`ErrorDecoder` por cliente, traduzindo HTTP 404/400 remotos em exceções de domínio)
- `exception/UserNotFoundException.java`, `exception/SymbolNotFoundException.java`,
  `exception/InvalidQuantityException.java`, `exception/InsufficientFundsException.java`,
  `exception/InsufficientSharesException.java`, `exception/GlobalExceptionHandler.java`
- `event/TradeCompletedEvent.java`, `event/TradeAuditPublisher.java` (publica em `trade.audit.queue`)
- `service/TradeService.java`, `controller/TradeController.java`
  (`POST /buy`, `POST /sell`, `GET /portfolio/{username}`, `GET /history/{username}`)
- Teste: `TradeServiceApplicationTests.java` (T05–T12, com `@MockBean` para
  `UserServiceClient`, `MarketServiceClient` e `RabbitTemplate`)
- `src/test/resources/application-test.properties` (H2 + Eureka desabilitado)
- `pom.xml`: adicionadas `com.h2database:h2` e `spring-boot-starter-test`
  (scope test); `spring-cloud-starter-openfeign` e `spring-boot-starter-amqp`
  **já existiam** no `pom.xml` do esqueleto, não precisaram ser adicionadas.

### api-gateway / eureka-server
Nenhum arquivo criado ou alterado — confirmado que o esqueleto já expõe as
rotas via discovery e já registra o servidor Eureka corretamente.

## Decisões de design não explicitadas na especificação

1. **Mocks de Feign em vez de WireMock/Docker Compose**: `UserServiceClient`
   e `MarketServiceClient` são substituídos por `@MockBean` nos testes do
   trade-service, para que `mvn test` nesse módulo não exija os outros 4
   serviços no ar. Isso também elimina a necessidade do Eureka em tempo de
   teste.
2. **`RabbitTemplate` também mockado** nos testes do trade-service, para que
   a publicação do evento de auditoria não dependa de um broker RabbitMQ
   real durante `mvn test`.
3. **`ErrorDecoder` por cliente Feign, sem `@Configuration`**: as classes
   `UserServiceClientConfig`/`MarketServiceClientConfig` são passadas via
   `@FeignClient(configuration = ...)` e deliberadamente **não** anotadas com
   `@Configuration`, para evitar que o Spring as registre como beans globais
   (o que faria o decoder de um cliente vazar para o outro).
4. **Consistência eventual sem Saga/2PC**, conforme pedido pela especificação:
   o trade-service debita/credita o saldo no user-service e só então grava a
   posição/transação local; se a escrita local falhar após o débito remoto
   ter sucesso (ou vice-versa), os dados podem ficar temporariamente
   inconsistentes. Isso é tratado como *trade-off* documentado do desenho de
   microsserviços, não como bug. Da mesma forma, uma falha ao publicar
   `trade.completed` no RabbitMQ é apenas registrada em log (`TradeAuditPublisher`)
   e não desfaz a operação já confirmada.
5. **market-service sem JPA**: embora o `pom.xml` do esqueleto já trouxesse
   `spring-boot-starter-data-jpa` e o driver Postgres, a tabela de cotações é
   fixa e em memória (`Map.of(...)`), então nenhuma entidade/repositório foi
   criado nesse módulo — mantém a dependência existente (não removida, para
   não alterar o esqueleto além do necessário) mas sem uso de JPA.
6. **Normalização de símbolo para maiúsculas** em todos os módulos, igual ao
   monólito, para evitar posições duplicadas por diferença de caixa.
7. **Eureka/discovery desabilitados no perfil de teste** (`eureka.client.enabled=false`)
   em todos os módulos com JPA, para que o `ApplicationContext` dos testes
   suba rapidamente e sem tentar registrar no Eureka.

## Resultado do build e dos testes — LIMITAÇÃO DE AMBIENTE (não executado com sucesso)

**Não foi possível confirmar `BUILD SUCCESS` nem o resultado real de
`mvn -B -ntp clean test`/`package` em nenhum dos 5 módulos nesta sessão.**
Isso não é um defeito do código gerado, e sim uma restrição do ambiente de
execução desta sessão do Agente 1 — a mesma limitação documentada em
`metrics/agente1_monolito_log.md`:

- Acesso de saída bloqueado por política organizacional para o Maven Central
  e todos os espelhos testados (`403 Forbidden` em `repo.maven.apache.org`,
  `repo1.maven.org`, `maven.google.com`, `jitpack.io`, `plugins.gradle.org`,
  `repo.spring.io`, `oss.sonatype.org`, `dl.google.com`).
- Sem repositório `~/.m2` local pré-populado com os artefatos necessários
  (Spring Boot, Spring Cloud, OpenFeign, AMQP etc.).
- Sem daemon Docker em execução nesta sessão (`/var/run/docker.sock`
  inexistente), impossibilitando o uso do container
  `maven:3.9-eclipse-temurin-21` sugerido na especificação — e, mesmo que o
  daemon estivesse disponível, tanto o `docker pull` da imagem quanto o `mvn`
  executado dentro do container estariam sujeitos à mesma política de rede.

A tentativa de build falha já na resolução do parent POM do esqueleto
(anterior a este agente), antes de qualquer código de negócio ser compilado:

```
[FATAL] Non-resolvable parent POM ...:
Could not transfer artifact org.springframework.boot:spring-boot-starter-parent:pom:3.3.4
from/to central (https://repo.maven.apache.org/maven2): status code: 403, reason phrase: Forbidden (403)
```

**Nenhum número de "Tests run" foi inventado**: nenhuma execução de teste
chegou a acontecer em nenhum dos 5 módulos.

### Recomendação

Executar, em um ambiente com acesso ao Maven Central (máquina do autor do
TCC ou CI sem essa restrição), para cada módulo:

```
docker run --rm -v "$MODULE_DIR:/app" -v "$HOME/.m2:/root/.m2" -w /app \
  maven:3.9-eclipse-temurin-21 mvn -B -ntp clean package
```

ou, localmente, `mvn -B -ntp clean test` em cada um dos 5 diretórios de
módulo. O código das três lógicas de negócio (user-service, market-service,
trade-service) está completo e pronto para essa verificação; api-gateway e
eureka-server não precisam de testes de negócio (apenas roteamento/discovery,
já presentes no esqueleto).

## Erros encontrados e correções

- Único erro encontrado foi o bloqueio de rede acima, que é de ambiente, não
  de código; não havia correção possível dentro das restrições desta sessão
  (reportado em vez de contornado, conforme a orientação de não tentar burlar
  negações de política organizacional).

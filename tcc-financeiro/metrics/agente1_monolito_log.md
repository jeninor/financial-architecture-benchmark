# Agente 1 — Log de geração de código (MONÓLITO)

- **Arquitetura:** monólito (`multiagente/monolito`)
- **Agente:** Agente 1 (geração de código), Claude Code / Claude Sonnet 5
- **Data:** 2026-10-01
- **Especificação seguida:** `infra/scripts/AGENTE1_ESPECIFICACAO.md`
- **Contexto:** o código de negócio deste módulo havia sido perdido num reset
  de container; apenas `pom.xml`, `FinanceMonolitoApplication.java` (bootstrap
  com `/health`) e `application.properties` sobreviveram. Este agente
  implementou a lógica de negócio do zero, sem copiar código de terceiros.

## Arquivos criados

Pacote `com.tcc.finance`:

- `model/Usuario.java`, `model/Posicao.java`, `model/Transacao.java`, `model/TipoTransacao.java`
- `repository/UsuarioRepository.java`, `repository/PosicaoRepository.java`, `repository/TransacaoRepository.java`
- `dto/UsuarioRequest.java`, `dto/UsuarioResponse.java`, `dto/QuoteResponse.java`, `dto/TradeRequest.java`,
  `dto/TradeResponse.java`, `dto/PosicaoResponse.java`, `dto/PortfolioResponse.java`, `dto/TransacaoResponse.java`
- `exception/UserNotFoundException.java`, `exception/DuplicateUsernameException.java`,
  `exception/SymbolNotFoundException.java`, `exception/InvalidQuantityException.java`,
  `exception/InsufficientFundsException.java`, `exception/InsufficientSharesException.java`,
  `exception/GlobalExceptionHandler.java`
- `service/MarketDataService.java`, `service/UsuarioService.java`, `service/TradeService.java`
- `controller/UsuarioController.java`, `controller/MarketController.java`, `controller/TradeController.java`
- Teste: `src/test/java/com/tcc/finance/FinanceMonolitoApplicationTests.java` (12 métodos `T01_...`–`T12_...`)
- `src/test/resources/application-test.properties` (perfil H2 em memória)
- `pom.xml`: adicionadas as dependências `com.h2database:h2` (scope test) e
  `spring-boot-starter-test` (scope test); nada mais foi alterado no `pom.xml`
  original.

Nenhum arquivo do esqueleto original (`FinanceMonolitoApplication.java`,
`application.properties` de produção) foi modificado, exceto o `pom.xml`
(apenas adição de dependências de teste).

## Endpoints implementados

- `POST /users` — cria usuário (`username`), saldo inicial 10000.00; 409 se duplicado.
- `GET /users/{username}` — consulta usuário; 404 se inexistente.
- `GET /quote/{symbol}` — cotação fixa em memória (AAPL=150.00, GOOG=2800.00, MSFT=300.00, AMZN=3300.00); 404 se símbolo inválido.
- `POST /buy` — compra; 400 (quantidade ≤ 0), 404 (usuário ou símbolo inexistente), 400 (saldo insuficiente).
- `POST /sell` — venda; 400 (quantidade ≤ 0 ou quantidade acima da posição).
- `GET /portfolio/{username}` — posições + saldo; 404 se usuário inexistente.
- `GET /history/{username}` — transações ordenadas por `timestamp` ascendente; 404 se usuário inexistente.

## Decisões de design não explicitadas na especificação

1. **H2 em memória para testes** (`@ActiveProfiles("test")` +
   `application-test.properties` com `spring.jpa.hibernate.ddl-auto=create-drop`),
   para que `mvn test` não dependa de um Postgres real. O `application.properties`
   de produção continua apontando para Postgres via `SPRING_DATASOURCE_URL`,
   sem alteração.
2. **Símbolos normalizados para maiúsculas** (`symbol.toUpperCase()`) antes de
   consultar a tabela de cotações e de persistir posições/transações, para
   evitar duplicidade de posições por causa de caixa (`aapl` vs `AAPL`).
3. **Venda de símbolo sem posição aberta** tratada como `InsufficientSharesException`
   (HTTP 400), já que a especificação só define o código para "vender mais do
   que possui" — vender um símbolo nunca comprado é um caso particular disso
   (quantidade possuída = 0).
4. **Corpo de erro padronizado** (`timestamp`, `status`, `message`) via
   `@RestControllerAdvice`, não especificado no documento mas necessário para
   ter uma resposta HTTP coerente nos códigos 400/404/409.
5. **Testes de cenário único por método** (`T01`...`T12`) usam usernames
   exclusivos por teste (`t05user`, `t06user`, ...) porque o contexto Spring
   (e o banco H2 em memória) é compartilhado entre os métodos da mesma classe
   de teste; isso evita que um teste interfira no saldo/posições de outro.

## Resultado do build e dos testes — LIMITAÇÃO DE AMBIENTE (não executado com sucesso)

**Não foi possível confirmar `BUILD SUCCESS` nem o resultado real de
`mvn test` nesta sessão.** Isso não é um defeito do código gerado, e sim uma
restrição do ambiente de execução desta sessão do Agente 1:

- O acesso de saída (egress) desta sessão passa por um proxy que **bloqueia
  por política organizacional** qualquer tentativa de alcançar o Maven
  Central e seus espelhos conhecidos. Testado e confirmado com `403 Forbidden`
  (CONNECT tunnel) para: `repo.maven.apache.org`, `repo1.maven.org`,
  `maven.google.com`, `jitpack.io`, `plugins.gradle.org`, `repo.spring.io`,
  `oss.sonatype.org`, `dl.google.com`.
- Não havia um repositório Maven local (`~/.m2/repository`) pré-populado com
  os artefatos do Spring Boot/Spring Cloud — apenas um resquício parcial e
  incompleto de uma tentativa anterior.
- O daemon do Docker **não está em execução** nesta sessão
  (`/var/run/docker.sock` inexistente), então também não foi possível usar o
  container `maven:3.9-eclipse-temurin-21` sugerido na especificação (e,
  mesmo que o daemon estivesse ativo, o `docker pull` da imagem e o `mvn`
  dentro do container estariam sujeitos à mesma política de rede).

Saída real obtida ao tentar `mvn -B -ntp clean test`:

```
[FATAL] Non-resolvable parent POM for com.tcc:finance-monolito:0.1.0-VALIDACAO:
The following artifacts could not be resolved:
org.springframework.boot:spring-boot-starter-parent:pom:3.3.4 (absent):
Could not transfer artifact org.springframework.boot:spring-boot-starter-parent:pom:3.3.4
from/to central (https://repo.maven.apache.org/maven2): status code: 403, reason phrase: Forbidden (403)
```

Ou seja: a falha ocorre na resolução do **parent POM do próprio esqueleto**
(já existente antes deste agente), antes de qualquer código deste agente ser
compilado — não há evidência de que o código de negócio escrito aqui tenha
algum problema de compilação, mas também não há confirmação positiva.
**Nenhum número de "Tests run" foi inventado**: nenhuma execução de teste
chegou a acontecer.

### Recomendação

Executar `mvn -B -ntp clean test` (ou o comando Docker da especificação) em
um ambiente com acesso ao Maven Central — por exemplo, a máquina do autor do
TCC ou um runner de CI sem essa restrição de rede — para obter a confirmação
real de `BUILD SUCCESS` e do resultado dos 12 cenários (T01–T12). O código
está completo e pronto para essa verificação.

## Erros encontrados e correções

- Único erro encontrado foi o bloqueio de rede acima, que é de ambiente, não
  de código; não havia correção possível dentro das restrições desta sessão
  (reportado em vez de contornado, conforme a orientação de não tentar burlar
  negações de política organizacional).

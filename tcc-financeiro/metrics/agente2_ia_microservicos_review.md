# Revisão por IA - Agente 2 (microservicos)

## Rastreabilidade

Relatórios efetivamente utilizados (timestamp mais recente disponível para
cada módulo em `metrics/`). Havia quatro execuções do Agente 2
(`20261001_141623`, `20261001_145328`, `20261001_151627` e
`20261001_154328`); conforme o procedimento, foi usada apenas a mais recente,
e as anteriores foram ignoradas:

| Módulo | PMD | SpotBugs |
|---|---|---|
| api-gateway | `agente2_api-gateway_pmd_20261001_154328.xml` | `agente2_api-gateway_spotbugs_20261001_154328.xml` |
| eureka-server | `agente2_eureka-server_pmd_20261001_154328.xml` | `agente2_eureka-server_spotbugs_20261001_154328.xml` |
| market-service | `agente2_market-service_pmd_20261001_154328.xml` | `agente2_market-service_spotbugs_20261001_154328.xml` |
| trade-service | `agente2_trade-service_pmd_20261001_154328.xml` | `agente2_trade-service_spotbugs_20261001_154328.xml` |
| user-service | `agente2_user-service_pmd_20261001_154328.xml` | `agente2_user-service_spotbugs_20261001_154328.xml` |

Resumo consolidado: `metrics/agente2_resumo_20261001_154328.md`.

Código-fonte consultado (apenas os arquivos e linhas apontados pelos
relatórios), com caminhos relativos a `multiagente/microservicos/`:

- `api-gateway/src/main/java/com/tcc/finance/ApiGatewayApplication.java`
- `eureka-server/src/main/java/com/tcc/finance/EurekaServerApplication.java`
- `market-service/.../market/MarketServiceApplication.java`,
  `market/exception/GlobalExceptionHandler.java`,
  `market/exception/NotFoundException.java`
- `trade-service/.../trade/client/MarketClient.java`,
  `trade/client/FeignErrorDecoder.java`,
  `trade/domain/TradeTransaction.java`,
  `trade/exception/{BadRequest,Conflict,NotFound}Exception.java`,
  `trade/exception/GlobalExceptionHandler.java`,
  `trade/messaging/TradeEventPublisher.java`,
  `trade/web/dto/PortfolioResponse.java`
- `user-service/.../user/UserServiceApplication.java`,
  `user/exception/{BadRequest,Conflict,NotFound}Exception.java`,
  `user/exception/GlobalExceptionHandler.java`,
  `user/service/UserService.java`

Para estabelecer o contexto de uso de `PortfolioResponse` e de
`TradeTransaction`, foram lidos também os pontos de instanciação em
`trade-service/.../trade/service/TradeService.java` (linhas 75, 105 e
110-124) e o tipo `PositionView`. Para o achado de `UserService`, foi lida a
entidade `UserAccount` (métodos `getSaldo`, `debit` e `credit`).

### Mudança em relação à revisão anterior

A versão anterior desta revisão (sobre `151627`) registrava 0 violações de
PMD e uma ressalva: o `-Drulesets` era ignorado pelo `maven-pmd-plugin` e o
plugin caía no ruleset padrão (42 regras). Essa pendência foi resolvida. O
`infra/scripts/run_agente2.sh` (linhas 24-64) agora injeta temporariamente um
bloco `<configuration><rulesets>` no `pom.xml` de cada módulo. Na execução
`154328`, o diretório `target/pmd/rulesets/` de cada microsserviço contém
`001-bestpractices.xml`, `002-errorprone.xml`, `003-design.xml`,
`004-multithreading.xml` e `005-performance.xml`, gerados entre 15:44 e
15:46 (o `001-maven-pmd-plugin-default.xml` restante é de 15:17, de uma
execução anterior). Não há `pom.xml.agente2.bak` remanescente, ou seja, os
POMs originais foram restaurados.

Por isso o PMD passou de 0 para 18 violações. O código não mudou: as
violações novas vêm da ampliação dos rulesets. Os quatro achados de
SpotBugs são os mesmos das execuções anteriores (mesmos `instanceHash`:
`6fe4311f…`, `18701685…`, `d0aa2036…` e `b55e60a8…`), e as classificações
deles foram mantidas.

## Resumo

- Total de achados analisados: 22 (18 de PMD, 4 de SpotBugs)
- Defeitos reais: 0 | Falsos positivos: 14 | Estilo/baixa prioridade: 8

| Módulo | PMD | SpotBugs | Falso positivo | Estilo/baixa | Defeito real |
|---|---|---|---|---|---|
| api-gateway | 1 | 0 | 1 | 0 | 0 |
| eureka-server | 1 | 0 | 1 | 0 | 0 |
| market-service | 3 | 0 | 2 | 1 | 0 |
| trade-service | 7 | 4 | 8 | 3 | 0 |
| user-service | 6 | 0 | 2 | 4 | 0 |
| **Total** | **18** | **4** | **14** | **8** | **0** |

Distribuição por regra:

| Regra | Ferramenta | Ocorrências | Classificação |
|---|---|---|---|
| `MissingSerialVersionUID` | PMD (errorprone) | 7 | estilo/baixa prioridade |
| `UseUtilityClass` | PMD (design) | 4 | falso positivo |
| `UseConcurrentHashMap` | PMD (multithreading) | 3 | falso positivo |
| `ImplicitFunctionalInterface` | PMD (bestpractices) | 1 | falso positivo |
| `DataClass` | PMD (design) | 1 | falso positivo |
| `GuardLogStatement` | PMD (bestpractices) | 1 | falso positivo |
| `LawOfDemeter` | PMD (design) | 1 | estilo/baixa prioridade |
| `EI_EXPOSE_REP2` | SpotBugs | 3 | falso positivo |
| `EI_EXPOSE_REP` | SpotBugs | 1 | falso positivo |

Os números conferem com o resumo consolidado de `154328` (PMD 1/1/3/7/6;
SpotBugs 0/0/0/4/0).

Observação sobre `configerror`: os cinco XMLs do PMD trazem
`<configerror rule="LoosePackageCoupling" msg="No packages or classes specified"/>`.
Não se trata de um achado sobre o código. A regra `LoosePackageCoupling` da
categoria `design` só funciona com a propriedade `packages` configurada, e
como o ruleset é aplicado sem parametrização, o PMD a desativa e registra o
aviso. O mesmo vale para o monólito. Não afeta a contagem.

## Achados

Os achados se agrupam em padrões repetidos entre os serviços, porque cada
microsserviço tem a mesma estrutura de classe principal, exceções e
`GlobalExceptionHandler`. Cada ocorrência aparece individualmente (para
rastreabilidade), e a justificativa comum fica na primeira ocorrência de
cada regra.

### SpotBugs

#### [n/a] EI_EXPOSE_REP - trade-service/.../web/dto/PortfolioResponse.java:6
- **Classificação:** falso positivo
- **Justificativa:** O acessor `positions()` do record devolve a lista
  recebida, mas o único ponto de construção (`TradeService.portfolio`,
  linhas 112-123) monta essa lista com `Stream.toList()`, que produz uma
  lista não modificável de `PositionView` (record com componentes
  imutáveis: `String`, `int` e `BigDecimal`). O objeto serve apenas para ser
  serializado em JSON, então não existe caminho de mutação das posições.
- **Recomendação:** documentar como falso positivo

#### [n/a] EI_EXPOSE_REP2 - trade-service/.../web/dto/PortfolioResponse.java:6
- **Classificação:** falso positivo
- **Justificativa:** O construtor canônico guarda a `List` sem cópia
  defensiva, mas o único chamador (`new PortfolioResponse(user.username(),
  positions, ...)`, `TradeService.java:122`) passa a lista imutável criada
  localmente por `.toList()` e não mantém outra referência a ela. A regra
  dispara pela forma do record (componente `List`), não por um risco real.
- **Recomendação:** documentar como falso positivo

#### [n/a] EI_EXPOSE_REP2 - trade-service/.../client/FeignErrorDecoder.java:26
- **Classificação:** falso positivo
- **Justificativa:** `this.objectMapper = objectMapper;` recebe o
  `ObjectMapper` por injeção de construtor em um `@Component`. É o bean
  singleton do Spring, compartilhado por desenho, e a classe só o usa para
  leitura (`objectMapper.readTree(body)`, linha 44). Uma cópia defensiva
  descartaria a configuração centralizada do Jackson.
- **Recomendação:** documentar como falso positivo

#### [n/a] EI_EXPOSE_REP2 - trade-service/.../messaging/TradeEventPublisher.java:24
- **Classificação:** falso positivo
- **Justificativa:** `this.rabbitTemplate = rabbitTemplate;` guarda o
  `RabbitTemplate` injetado pelo Spring. É um bean de infraestrutura
  thread-safe e compartilhado por desenho, e a classe não expõe a
  referência. O padrão é a injeção de dependência idiomática do framework.
- **Recomendação:** documentar como falso positivo

### PMD - `UseUtilityClass` (4 ocorrências)

#### [n/a] UseUtilityClass - api-gateway/.../ApiGatewayApplication.java:7
- **Classificação:** falso positivo
- **Justificativa:** A classe tem apenas `public static void main`, mas é
  anotada com `@SpringBootApplication`, o que a torna uma classe
  `@Configuration` que o Spring instancia (via proxy CGLIB). Adicionar um
  construtor privado, como a regra sugere, impediria essa instanciação e
  quebraria a inicialização da aplicação.
- **Recomendação:** documentar como falso positivo

#### [n/a] UseUtilityClass - eureka-server/.../EurekaServerApplication.java:9
- **Classificação:** falso positivo
- **Justificativa:** Mesmo padrão: classe `@SpringBootApplication
  @EnableEurekaServer` com apenas `main`. Ela precisa ser instanciável pelo
  contêiner do Spring.
- **Recomendação:** documentar como falso positivo

#### [n/a] UseUtilityClass - market-service/.../market/MarketServiceApplication.java:11
- **Classificação:** falso positivo
- **Justificativa:** Mesmo padrão (`@SpringBootApplication
  @EnableDiscoveryClient`). A classe interna estática `HealthController` não
  altera a análise.
- **Recomendação:** documentar como falso positivo

#### [n/a] UseUtilityClass - user-service/.../user/UserServiceApplication.java:13
- **Classificação:** falso positivo
- **Justificativa:** Mesmo padrão (`@SpringBootApplication
  @EnableDiscoveryClient @EnableFeignClients`).
- **Recomendação:** documentar como falso positivo

### PMD - `UseConcurrentHashMap` (3 ocorrências)

#### [n/a] UseConcurrentHashMap - market-service/.../market/exception/GlobalExceptionHandler.java:21
- **Classificação:** falso positivo
- **Justificativa:** `Map<String, Object> body = new LinkedHashMap<>();` é
  uma variável local do método `error`, criada a cada requisição e
  devolvida no `ResponseEntity`. Não há acesso concorrente. Além disso,
  `LinkedHashMap` foi escolhido para preservar a ordem dos campos no JSON
  (`timestamp`, `status`, `error`, `message`), ordem que `ConcurrentHashMap`
  não garante. Trocar pioraria o comportamento observável.
- **Recomendação:** documentar como falso positivo

#### [n/a] UseConcurrentHashMap - trade-service/.../trade/exception/GlobalExceptionHandler.java:59
- **Classificação:** falso positivo
- **Justificativa:** Mesmo método `error` com `LinkedHashMap` local e
  confinado à thread da requisição.
- **Recomendação:** documentar como falso positivo

#### [n/a] UseConcurrentHashMap - user-service/.../user/exception/GlobalExceptionHandler.java:53
- **Classificação:** falso positivo
- **Justificativa:** Mesmo método `error` com `LinkedHashMap` local e
  confinado à thread da requisição.
- **Recomendação:** documentar como falso positivo

### PMD - `MissingSerialVersionUID` (7 ocorrências)

#### [n/a] MissingSerialVersionUID - market-service/.../market/exception/NotFoundException.java:3
- **Classificação:** estilo/baixa prioridade
- **Justificativa:** `NotFoundException extends RuntimeException` herda
  `Serializable` sem declarar `serialVersionUID`. A regra está tecnicamente
  correta, mas as exceções deste sistema nunca passam por serialização Java:
  são capturadas pelo `GlobalExceptionHandler` e convertidas em JSON, e entre
  serviços trafegam como HTTP (Feign) ou mensagens AMQP. A falta do campo não
  tem efeito prático no escopo do TCC.
- **Recomendação:** ignorar

#### [n/a] MissingSerialVersionUID - trade-service/.../trade/exception/BadRequestException.java:3
- **Classificação:** estilo/baixa prioridade
- **Justificativa:** Mesmo padrão. No `trade-service`, a exceção também é
  criada pelo `FeignErrorDecoder` a partir da resposta HTTP de outro
  serviço, sem serialização Java.
- **Recomendação:** ignorar

#### [n/a] MissingSerialVersionUID - trade-service/.../trade/exception/ConflictException.java:3
- **Classificação:** estilo/baixa prioridade
- **Justificativa:** Mesmo padrão.
- **Recomendação:** ignorar

#### [n/a] MissingSerialVersionUID - trade-service/.../trade/exception/NotFoundException.java:3
- **Classificação:** estilo/baixa prioridade
- **Justificativa:** Mesmo padrão.
- **Recomendação:** ignorar

#### [n/a] MissingSerialVersionUID - user-service/.../user/exception/BadRequestException.java:3
- **Classificação:** estilo/baixa prioridade
- **Justificativa:** Mesmo padrão.
- **Recomendação:** ignorar

#### [n/a] MissingSerialVersionUID - user-service/.../user/exception/ConflictException.java:3
- **Classificação:** estilo/baixa prioridade
- **Justificativa:** Mesmo padrão.
- **Recomendação:** ignorar

#### [n/a] MissingSerialVersionUID - user-service/.../user/exception/NotFoundException.java:3
- **Classificação:** estilo/baixa prioridade
- **Justificativa:** Mesmo padrão.
- **Recomendação:** ignorar

### PMD - demais regras

#### [n/a] ImplicitFunctionalInterface - trade-service/.../trade/client/MarketClient.java:9
- **Classificação:** falso positivo
- **Justificativa:** `MarketClient` é uma interface `@FeignClient(name =
  "market-service")`, e o Spring Cloud OpenFeign gera sua implementação. Ter
  um único método (`quote`) é circunstancial: a interface descreve um
  contrato HTTP remoto, não um alvo de lambda. Anotá-la com
  `@FunctionalInterface` comunicaria uma intenção falsa e impediria a adição
  de novos endpoints.
- **Recomendação:** documentar como falso positivo

#### [n/a] DataClass - trade-service/.../trade/domain/TradeTransaction.java:18
- **Classificação:** falso positivo
- **Justificativa:** `TradeTransaction` é uma entidade JPA que representa um
  registro de histórico (compra/venda). Só tem getters e nenhum setter
  (`NOPA=0`), e todos os campos são atribuídos uma única vez no construtor
  usado em `TradeService` (linhas 75 e 105). Para um lançamento imutável,
  ser um portador de dados é o comportamento desejado, e não há lógica de
  negócio deslocada para fora da classe que justifique a métrica.
- **Recomendação:** documentar como falso positivo

#### [n/a] GuardLogStatement - trade-service/.../trade/messaging/TradeEventPublisher.java:33
- **Classificação:** falso positivo
- **Justificativa:** A chamada é `log.error("Falha ao publicar
  trade.completed (transacao {}): {}", event.transactionId(),
  ex.getMessage())`. Ela usa mensagem parametrizada do SLF4J, os argumentos
  são acessores triviais e o nível `ERROR` fica habilitado em qualquer
  configuração realista. Um `if (log.isErrorEnabled())` não evitaria nenhum
  custo, e a chamada só ocorre no caminho de exceção do broker.
- **Recomendação:** documentar como falso positivo

#### [n/a] LawOfDemeter - user-service/.../user/service/UserService.java:52
- **Classificação:** estilo/baixa prioridade
- **Justificativa:** O trecho apontado é o segundo `user.getSaldo()`, usado
  só para compor a mensagem de erro (`"... excede o saldo disponivel " +
  user.getSaldo()`). `user` é a entidade `UserAccount` obtida no próprio
  método por `lockUser` (lock pessimista), então o acesso é de grau 1 a um
  objeto do próprio agregado e não um encadeamento entre objetos
  estranhos. A regra de saldo não negativo é garantida pela verificação da
  linha 50 dentro da transação com lock. Mover essa verificação para
  `UserAccount.debit` seria uma melhoria de encapsulamento, mas é uma
  escolha de desenho, não um defeito, e a regra é sabidamente ruidosa em
  código de serviço.
- **Recomendação:** ignorar

## Consistência com o monólito

A execução `154328` também ampliou os rulesets do monólito (9 violações de
PMD e os mesmos 2 achados de SpotBugs em `PortfolioResponse`). A revisão
atual do monólito (`agente2_ia_monolito_review.md`) ainda se refere à
execução `151627` e precisa ser refeita sobre `154328`. Pela restrição de
consistência, os achados equivalentes devem receber a mesma classificação
nos dois relatórios:

| Padrão | Microsserviços | Monólito (`154328`) | Classificação esperada |
|---|---|---|---|
| `EI_EXPOSE_REP`/`EI_EXPOSE_REP2` em `PortfolioResponse` (record textualmente equivalente, lista criada com `.toList()`) | `trade-service`, linha 6 | `web/dto/PortfolioResponse.java:6` | falso positivo |
| `UseUtilityClass` em classe `@SpringBootApplication` | 4 módulos | `FinanceMonolitoApplication.java:9` | falso positivo |
| `UseConcurrentHashMap` no `LinkedHashMap` local de `GlobalExceptionHandler.error` | 3 módulos | `GlobalExceptionHandler.java:53` | falso positivo |
| `MissingSerialVersionUID` nas exceções de domínio | 7 classes | `BadRequest`/`Conflict`/`NotFoundException.java:3` | estilo/baixa prioridade |
| `DataClass` em `TradeTransaction` (entidade imutável, mesma métrica `WOC=0, NOPA=0, NOAM=7, WMC=9`) | `trade-service`, linha 18 | `domain/TradeTransaction.java:20` | falso positivo |
| `LawOfDemeter` em `getSaldo` na mensagem de "Saldo insuficiente" | `UserService.java:52` | `TradeService.java:51` (mesmo trecho de mensagem) | estilo/baixa prioridade |

Não têm equivalente no monólito: `ImplicitFunctionalInterface`
(`MarketClient`, cliente Feign), `GuardLogStatement` (publicador
RabbitMQ) e os `EI_EXPOSE_REP2` de `FeignErrorDecoder` e
`TradeEventPublisher`. Esses achados vêm da infraestrutura de comunicação
distribuída, que só existe nos microsserviços. Do lado do monólito, as
violações `LawOfDemeter` em `TradeService.java:107` (`portfolio`) e
`:136` (`toResponse`, `tx.getUser()`) não têm contrapartida direta aqui e
devem ser classificadas na revisão do monólito.

## Observação para a análise comparativa

Quase toda a diferença de contagem entre as arquiteturas (18 contra 9
violações de PMD; 4 contra 2 achados de SpotBugs) vem da **replicação** de
padrões idênticos em cada serviço (classe principal, exceções e
`GlobalExceptionHandler` repetidos por módulo) e dos componentes de
integração (Feign e RabbitMQ). A diferença não vem de uma qualidade
intrinsecamente pior do código distribuído. Para o TCC, o número bruto de
violações deve ser lido junto com essa classificação: nenhuma das duas
arquiteturas tem defeito real apontado pelas ferramentas estáticas.

## Lista para o Agente 3

Nenhum achado dos microsserviços foi classificado como `defeito real`,
então não há itens de correção obrigatória para o Agente 3.

Endurecimentos opcionais, fora da lista de correção, que só devem ser
aplicados se forem replicados de forma idêntica no monólito para preservar
a comparabilidade:

1. `trade-service/.../web/dto/PortfolioResponse.java:6`: construtor
   compacto com `positions = List.copyOf(positions);` (protege o DTO contra
   chamadores futuros que passem uma lista mutável).
2. Exceções de domínio dos três serviços: declarar
   `private static final long serialVersionUID = 1L;` (elimina 7 violações
   de estilo sem mudar comportamento).

Os demais falsos positivos podem ser suprimidos com
`@SuppressWarnings("PMD.<Regra>")` ou por exclusão no ruleset, se o objetivo
for zerar o relatório. Nesse caso, a supressão deve ser registrada na
metodologia, para não ser confundida com correção.

## Pendência residual de pipeline

O diretório aninhado `tcc-financeiro/tcc-financeiro/` (com
`infra/` e `metrics/agente2_resumo_20261001_153156.md`) ainda existe. Ele
não interfere na descoberta, que só considera `metrics/` do projeto, mas
convém removê-lo para evitar confusão na consolidação final.

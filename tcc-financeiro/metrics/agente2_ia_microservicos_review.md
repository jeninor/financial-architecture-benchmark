# Revisão por IA - Agente 2 (microservicos)

## Rastreabilidade

Foi usada a execução mais recente do Agente 2 (`20261001_185651`) para todos os
módulos. As execuções anteriores foram ignoradas, conforme o procedimento.

| Módulo | PMD | SpotBugs |
|---|---|---|
| api-gateway | `agente2_api-gateway_pmd_20261001_185651.xml` | `agente2_api-gateway_spotbugs_20261001_185651.xml` |
| eureka-server | `agente2_eureka-server_pmd_20261001_185651.xml` | `agente2_eureka-server_spotbugs_20261001_185651.xml` |
| market-service | `agente2_market-service_pmd_20261001_185651.xml` | `agente2_market-service_spotbugs_20261001_185651.xml` |
| trade-service | `agente2_trade-service_pmd_20261001_185651.xml` | `agente2_trade-service_spotbugs_20261001_185651.xml` |
| user-service | `agente2_user-service_pmd_20261001_185651.xml` | `agente2_user-service_spotbugs_20261001_185651.xml` |

Resumo consolidado: `metrics/agente2_resumo_20261001_185651.md`.

Nenhum XML do PMD contém `<error>`. Todos trazem apenas o `<configerror>` de
`LoosePackageCoupling` ("No packages or classes specified"). Essa regra não está
parametrizada e fica inativa, igual ao monólito, sem gerar achados.

Código-fonte consultado (apenas os arquivos e linhas apontados), com caminhos relativos
a `multiagente/microservicos/`:
- `api-gateway/.../ApiGatewayApplication.java:7`
- `eureka-server/.../EurekaServerApplication.java:9`
- `market-service/.../market/MarketServiceApplication.java:11`
- `market-service/.../market/exception/GlobalExceptionHandler.java:27`
- `trade-service/.../trade/client/MarketClient.java:9`
- `trade-service/.../trade/client/FeignErrorDecoder.java:26`
- `trade-service/.../trade/domain/TradeTransaction.java:18`
- `trade-service/.../trade/exception/GlobalExceptionHandler.java:37`
- `trade-service/.../trade/messaging/TradeEventPublisher.java:24,33`
- `user-service/.../user/UserServiceApplication.java:13`
- `user-service/.../user/exception/GlobalExceptionHandler.java:37`
- `user-service/.../user/service/UserService.java:52`

### Contexto desta execução (importante para a comparação)

A execução `185651` foi feita **depois** da correção do build registrada em
`metrics/agente1_microservicos_log.md`. Nessa correção, a stack paralela do Agente 1 foi
movida para `_descartado/` em user-service, market-service e trade-service. Em
trade-service, o `TradeService` foi reimplementado, e o `GlobalExceptionHandler` foi
reimplementado nos três serviços. Os três achados `UseConcurrentHashMap` recaem sobre os
`GlobalExceptionHandler` reimplementados.

Em relação à revisão anterior (`154328`), desapareceram:
- os 7 `MissingSerialVersionUID`, porque as exceções agora declaram `serialVersionUID`;
- os 2 achados de SpotBugs em `trade-service/.../web/dto/PortfolioResponse`, porque o
  record agora tem um construtor compacto com `List.copyOf`.

Os 2 achados de SpotBugs restantes têm os mesmos `instanceHash` da revisão anterior
(`6fe4311f…` e `18701685…`).

## Resumo

- Total de achados analisados: 13 (11 de PMD, 2 de SpotBugs)
- Defeitos reais: 0 | Falsos positivos: 12 | Estilo/baixa prioridade: 1

| Módulo | PMD | SpotBugs | Falso positivo | Estilo/baixa | Defeito real |
|---|---|---|---|---|---|
| api-gateway | 1 | 0 | 1 | 0 | 0 |
| eureka-server | 1 | 0 | 1 | 0 | 0 |
| market-service | 2 | 0 | 2 | 0 | 0 |
| trade-service | 4 | 2 | 6 | 0 | 0 |
| user-service | 3 | 0 | 2 | 1 | 0 |
| **Total** | **11** | **2** | **12** | **1** | **0** |

Os totais conferem com o resumo consolidado (PMD 1/1/2/4/3; SpotBugs 0/0/0/2/0). Somando
o monólito (5 de PMD), chega-se às 16 violações de PMD da execução `185651`.

## Achados

### SpotBugs (trade-service)

Os dois achados são `EI_EXPOSE_REP2` (categoria `MALICIOUS_CODE`, CWE-374, rank 18,
prioridade 2). Ambos recaem sobre **beans do Spring injetados por construtor** (um
`ObjectMapper` e um `RabbitTemplate`), e não sobre dados mutáveis de domínio. Trata-se do
falso positivo documentado dessa regra em código com injeção de dependência.

| # | Regra | Arquivo:linha | Classificação | Justificativa | Recomendação |
|---|---|---|---|---|---|
| S1 | `EI_EXPOSE_REP2` | `trade-service/.../client/FeignErrorDecoder.java:26` | falso positivo | `this.objectMapper = objectMapper;` guarda o `ObjectMapper` singleton injetado em um `@Component`. Ele é compartilhado por desenho (configuração central do Jackson) e só é usado para leitura (`objectMapper.readTree(body)`). Uma cópia defensiva descartaria essa configuração, e a referência nunca é exposta. | documentar como falso positivo |
| S2 | `EI_EXPOSE_REP2` | `trade-service/.../messaging/TradeEventPublisher.java:24` | falso positivo | `this.rabbitTemplate = rabbitTemplate;` guarda o `RabbitTemplate` injetado, um bean de infraestrutura thread-safe e compartilhado por desenho. A classe não expõe a referência. É injeção de dependência idiomática, sem estado de domínio envolvido. | documentar como falso positivo |

### PMD

| # | Regra (categoria, prioridade) | Arquivo:linha | Classificação | Justificativa | Recomendação |
|---|---|---|---|---|---|
| P1 | `UseUtilityClass` (design, P3) | `api-gateway/.../ApiGatewayApplication.java:7` | falso positivo | Classe `@SpringBootApplication`, portanto `@Configuration`: o Spring precisa instanciá-la e criar um proxy. Um construtor privado impediria o contexto de subir. | documentar como falso positivo |
| P2 | `UseUtilityClass` (design, P3) | `eureka-server/.../EurekaServerApplication.java:9` | falso positivo | Mesmo caso de P1, com `@EnableEurekaServer`. | documentar como falso positivo |
| P3 | `UseUtilityClass` (design, P3) | `market-service/.../MarketServiceApplication.java:11` | falso positivo | Mesmo caso de P1. Além disso, declara o `HealthController` aninhado. | documentar como falso positivo |
| P4 | `UseUtilityClass` (design, P3) | `user-service/.../UserServiceApplication.java:13` | falso positivo | Mesmo caso de P1. Além disso, declara o `HealthController` aninhado. | documentar como falso positivo |
| P5 | `UseConcurrentHashMap` (multithreading, P3) | `market-service/.../exception/GlobalExceptionHandler.java:27` | falso positivo | `new LinkedHashMap<>()` é uma variável local do método `body`, confinada à thread da requisição. A ordem dos campos do JSON de erro é intencional e um `ConcurrentHashMap` não a preservaria. | documentar como falso positivo |
| P6 | `UseConcurrentHashMap` (multithreading, P3) | `trade-service/.../exception/GlobalExceptionHandler.java:37` | falso positivo | Idem P5 (código idêntico). | documentar como falso positivo |
| P7 | `UseConcurrentHashMap` (multithreading, P3) | `user-service/.../exception/GlobalExceptionHandler.java:37` | falso positivo | Idem P5 (código idêntico). | documentar como falso positivo |
| P8 | `ImplicitFunctionalInterface` (bestpractices, P2) | `trade-service/.../client/MarketClient.java:9` | falso positivo | Interface `@FeignClient(name = "market-service")`, cuja implementação é gerada pelo OpenFeign. Ter um único método (`quote`) é circunstancial: a interface descreve um contrato HTTP, não um alvo de lambda. `@FunctionalInterface` comunicaria uma intenção falsa e bloquearia a adição de endpoints. | documentar como falso positivo |
| P9 | `DataClass` (design, P3) | `trade-service/.../domain/TradeTransaction.java:18` | falso positivo | Entidade JPA de histórico (`WOC=0, NOPA=0, NOAM=7, WMC=9`, as mesmas métricas do monólito). Os campos são atribuídos só no construtor (linha 46), não há setters, e o construtor JPA é `protected`. Ser um portador de dados é o desejado para um lançamento imutável. | documentar como falso positivo |
| P10 | `GuardLogStatement` (bestpractices, P2) | `trade-service/.../messaging/TradeEventPublisher.java:33` | falso positivo | `log.error("... (transacao {}): {}", event.transactionId(), ex.getMessage())` usa mensagem parametrizada do SLF4J, com argumentos triviais, no nível `ERROR` (sempre habilitado na prática) e só no caminho de falha do broker. Um `isErrorEnabled()` não evitaria nenhum custo. | documentar como falso positivo |
| P11 | `LawOfDemeter` (design, P3) | `user-service/.../service/UserService.java:52` | estilo/baixa prioridade | O segundo `user.getSaldo()` só compõe a mensagem de "Saldo insuficiente". `user` é o `UserAccount` obtido com lock pessimista (`lockUser`) no próprio método, um acesso de grau 1. O invariante de saldo não negativo é garantido pela verificação da linha 50 dentro da transação. Mover a regra para `UserAccount.debit` seria uma melhoria de encapsulamento, não uma correção. | ignorar |

### Distribuição por regra

| Regra | Ferramenta | Ocorrências | Classificação |
|---|---|---|---|
| `UseUtilityClass` | PMD (design) | 4 | falso positivo |
| `UseConcurrentHashMap` | PMD (multithreading) | 3 | falso positivo |
| `ImplicitFunctionalInterface` | PMD (bestpractices) | 1 | falso positivo |
| `DataClass` | PMD (design) | 1 | falso positivo |
| `GuardLogStatement` | PMD (bestpractices) | 1 | falso positivo |
| `LawOfDemeter` | PMD (design) | 1 | estilo/baixa prioridade |
| `EI_EXPOSE_REP2` | SpotBugs | 2 | falso positivo |

## Consistência com o monólito

| Padrão | Microsserviços | Monólito (`185651`) | Classificação |
|---|---|---|---|
| `UseUtilityClass` em `*Application` | P1–P4 | `FinanceMonolitoApplication:9` | falso positivo |
| `UseConcurrentHashMap` no mapa local de `GlobalExceptionHandler.body` | P5–P7 | `GlobalExceptionHandler:37` | falso positivo |
| `DataClass` em `TradeTransaction` | P9 | `domain/TradeTransaction:20` | falso positivo |
| `LawOfDemeter` em `getSaldo` na mensagem de saldo insuficiente | P11 | `TradeService:50` | estilo/baixa prioridade |

Sem equivalente no monólito: P8 (`MarketClient`, Feign), P10 e S2 (publicador RabbitMQ) e
S1 (`FeignErrorDecoder`). Todos vêm da infraestrutura de comunicação distribuída, que só
existe nos microsserviços.

## Observação para a análise comparativa

A diferença de contagem (11 contra 5 violações de PMD; 2 contra 0 achados de SpotBugs)
vem de duas fontes:
- a **replicação** de padrões idênticos por serviço (classe `*Application` e
  `GlobalExceptionHandler` repetidos em cada módulo);
- os **componentes de integração** (Feign e RabbitMQ).

Ela não vem de uma qualidade intrinsecamente pior do código distribuído. Nenhuma das duas
arquiteturas tem defeito real apontado pelas ferramentas estáticas. Ressalva
metodológica: parte dos achados (`GlobalExceptionHandler` e o `TradeService` do monólito)
recai sobre arquivos reimplementados na correção do build, e não sobre a geração original
do Agente 1. Isso deve ser registrado ao comparar com execuções anteriores a `185651`.

## Lista para o Agente 3

Nenhum achado dos microsserviços foi classificado como `defeito real`. Não há itens de
correção obrigatória para o Agente 3. Se o objetivo for zerar o relatório, os falsos
positivos podem ser suprimidos com `@SuppressWarnings("PMD.<Regra>")` ou com um filtro
de exclusão do SpotBugs. Nesse caso, isso deve ser feito de forma simétrica nas duas
arquiteturas e registrado na metodologia como supressão, não como correção.

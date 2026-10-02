# Revisão por IA - Agente 2 (monolito)

## Rastreabilidade

Foi usada a execução mais recente do Agente 2 (`20261001_185651`). As execuções
anteriores (`141623`, `145328`, `151627`, `154328`, `161014`, `161245`, `184041`) foram
ignoradas, conforme o procedimento.

| Ferramenta | Arquivo |
|---|---|
| PMD (7.x, 5 categorias: bestpractices, errorprone, design, multithreading, performance) | `metrics/agente2_monolito_pmd_20261001_185651.xml` |
| SpotBugs | `metrics/agente2_monolito_spotbugs_20261001_185651.xml` |
| Resumo consolidado | `metrics/agente2_resumo_20261001_185651.md` |

O XML do PMD não contém nenhum `<error>`, ou seja, todos os arquivos foram analisados.
O único `<configerror>` é o de `LoosePackageCoupling` ("No packages or classes
specified"). Essa regra exige parametrização de pacotes e fica inativa sem ela. Ela
aparece igualmente nos seis módulos e não gera achados.

Código-fonte consultado (apenas os arquivos e linhas apontados), em
`multiagente/monolito/src/main/java/com/tcc/finance/`:
`FinanceMonolitoApplication.java:9`, `domain/TradeTransaction.java:20`,
`exception/GlobalExceptionHandler.java:37` e `service/TradeService.java:50,99`.

### Contexto desta execução (importante para a comparação)

A execução `185651` foi feita **depois** da correção do build do monólito, registrada em
`metrics/agente1_monolito_log.md`. Nessa correção, a stack paralela gerada pelo Agente 1
(em português) foi movida para `_descartado/`. A stack original (em inglês) foi mantida.
`service/TradeService.java` e `exception/GlobalExceptionHandler.java` foram
**reimplementados**, porque as versões originais haviam sido sobrescritas. Três dos cinco
achados abaixo (`LawOfDemeter` ×2 e `UseConcurrentHashMap`) recaem justamente sobre esses
arquivos reimplementados. Por isso, eles refletem o código da correção, e não a geração
original do Agente 1. A mesma situação ocorre nos microsserviços.

Em relação à revisão anterior (`154328`), desapareceram:
- os 3 `MissingSerialVersionUID`, porque as exceções agora declaram `serialVersionUID`;
- os 2 achados de SpotBugs em `PortfolioResponse`, porque o record agora tem um
  construtor compacto com `List.copyOf` e o acessor também devolve uma cópia;
- o `LawOfDemeter` em `tx.getUser().getUsername()`, porque o método `toResponse` deixou
  de existir na reimplementação.

## Resumo

- Total de achados analisados: 5 (5 de PMD, 0 de SpotBugs)
- Defeitos reais: 0 | Falsos positivos: 3 | Estilo/baixa prioridade: 2

## Achados

| # | Regra (ferramenta/categoria) | Arquivo:linha | Classificação | Justificativa | Recomendação |
|---|---|---|---|---|---|
| 1 | `UseUtilityClass` (PMD/design, P3) | `FinanceMonolitoApplication.java:9` | falso positivo | Classe `@SpringBootApplication`, portanto também `@Configuration`: o Spring precisa instanciá-la (proxy CGLIB), e um construtor privado quebraria a inicialização do contexto. Além do `main` estático, ela declara o `HealthController` aninhado (linhas 15-22). | documentar como falso positivo |
| 2 | `DataClass` (PMD/design, P3) | `domain/TradeTransaction.java:20` | falso positivo | Entidade JPA de livro-razão (`WOC=0, NOPA=0, NOAM=7`). Os campos são atribuídos uma única vez no construtor (linha 49), não há setters, e o construtor sem argumentos (linha 46) é `protected`, exigido pelo JPA. Para um registro histórico imutável, não ter comportamento é o desenho correto. | documentar como falso positivo |
| 3 | `UseConcurrentHashMap` (PMD/multithreading, P3) | `exception/GlobalExceptionHandler.java:37` | falso positivo | `Map<String, Object> payload = new LinkedHashMap<>();` é uma variável local do método `body`, criada e devolvida dentro da mesma requisição, sem compartilhamento entre threads. O `LinkedHashMap` é intencional porque preserva a ordem dos campos do JSON de erro (`timestamp`, `status`, `message`). | documentar como falso positivo |
| 4 | `LawOfDemeter` (PMD/design, P3) | `service/TradeService.java:50` | estilo/baixa prioridade | O segundo `user.getSaldo()` só compõe a mensagem de "Saldo insuficiente". `user` é o `UserAccount` carregado no próprio método com lock pessimista (`findByUsernameForUpdate`), um acesso de grau 1 à raiz do agregado. A alternativa *tell, don't ask* (`user.canAfford(total)`) melhoraria o encapsulamento, mas não corrige nenhum risco. | ignorar |
| 5 | `LawOfDemeter` (PMD/design, P3) | `service/TradeService.java:99` | estilo/baixa prioridade | `user.getSaldo().add(stocksValue)` calcula `totalValue` do portfólio. `getSaldo()` retorna `BigDecimal`, que é imutável, então o encadeamento não expõe nenhum estado mutável do agregado. É o uso idiomático de um serviço de aplicação com JPA. | ignorar |

SpotBugs (`effort=Max`, `threshold=Low`): **0 achados** no monólito nesta execução.

### Distribuição por regra

| Regra | Ferramenta | Ocorrências | Classificação |
|---|---|---|---|
| `UseUtilityClass` | PMD (design) | 1 | falso positivo |
| `DataClass` | PMD (design) | 1 | falso positivo |
| `UseConcurrentHashMap` | PMD (multithreading) | 1 | falso positivo |
| `LawOfDemeter` | PMD (design) | 2 | estilo/baixa prioridade |

## Consistência com microsserviços

As ocorrências equivalentes receberam a mesma classificação em
`agente2_ia_microservicos_review.md`:

| Padrão | Monólito | Microsserviços (`185651`) | Classificação |
|---|---|---|---|
| `UseUtilityClass` em `*Application` | `FinanceMonolitoApplication:9` | api-gateway, eureka-server, market-service, user-service | falso positivo |
| `DataClass` em `TradeTransaction` (mesmas métricas) | `domain/TradeTransaction:20` | trade-service `domain/TradeTransaction:18` | falso positivo |
| `UseConcurrentHashMap` no mapa local de `GlobalExceptionHandler.body` | linha 37 | market-service:27, trade-service:37, user-service:37 | falso positivo |
| `LawOfDemeter` em `getSaldo` na mensagem de saldo insuficiente | `TradeService:50` | user-service `UserService:52` | estilo/baixa prioridade |

O `LawOfDemeter` em `TradeService:99` (portfólio) não tem equivalente nos
microsserviços. Lá, o saldo chega como `UserDto` (um record), via Feign, e o PMD não
dispara a regra sobre acessores de record.

## Lista para o Agente 3

Nenhum achado do monólito foi classificado como `defeito real`. Não há itens de
correção obrigatória para o Agente 3.

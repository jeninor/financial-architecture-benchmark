# Revisão por IA - Agente 2 (monolito)

## Rastreabilidade

Foram usados os relatórios com o timestamp mais recente para o módulo
`monolito`. O diretório `metrics/` tem quatro execuções do Agente 2
(`20261001_141623`, `20261001_145328`, `20261001_151627` e
`20261001_154328`). Conforme o procedimento, só a mais recente foi
considerada:

| Ferramenta | Arquivo |
|---|---|
| PMD | `metrics/agente2_monolito_pmd_20261001_154328.xml` |
| SpotBugs | `metrics/agente2_monolito_spotbugs_20261001_154328.xml` |
| Resumo consolidado | `metrics/agente2_resumo_20261001_154328.md` |

O resumo `tcc-financeiro/tcc-financeiro/metrics/agente2_resumo_20261001_153156.md`
fica fora do `metrics/` do projeto, não tem XMLs associados e é anterior a
`154328`. Por isso não entra na análise.

Código-fonte consultado (somente os arquivos apontados pelos relatórios), em
`multiagente/monolito/src/main/java/com/tcc/finance/`:
`FinanceMonolitoApplication.java`, `domain/TradeTransaction.java`,
`exception/BadRequestException.java`, `exception/ConflictException.java`,
`exception/NotFoundException.java`, `exception/GlobalExceptionHandler.java`,
`service/TradeService.java` e `web/dto/PortfolioResponse.java`. Para
entender o contexto de uso do record apontado pelo SpotBugs, também foi lido
o tipo dos elementos da lista (`web/dto/PositionView.java`).

Esta versão substitui a revisão anterior, que se baseava em `151627`.

### Validade da execução `154328`

A pendência de pipeline registrada na revisão anterior foi resolvida.
O script `infra/scripts/run_agente2.sh` (versão de 15:28) injeta
temporariamente o bloco `<configuration><rulesets>` no `pom.xml` (linhas
34-40), em vez de passar `-Drulesets`, parâmetro que o plugin ignorava. A
execução `154328` gravou em `target/pmd/rulesets/` os arquivos
`001-bestpractices.xml`, `002-errorprone.xml`, `003-design.xml`,
`004-multithreading.xml` e `005-performance.xml`, todos com data de 15:43. O
arquivo `001-maven-pmd-plugin-default.xml` que ainda está nesse diretório é
de 15:16 e sobrou da execução anterior. O XML do PMD (versão 7.17.0) não tem
nenhum `<error>`. **Pela primeira vez, os números do PMD correspondem às
cinco categorias declaradas na especificação do Agente 2.** O salto de 0
para 9 violações é efeito dessa correção do pipeline, e não de mudança no
código.

O XML contém um `<configerror rule="LoosePackageCoupling" msg="No packages or
classes specified"/>`. Essa regra da categoria `design` exige que os pacotes
sejam configurados e, sem essa configuração, fica inativa. O mesmo
`configerror` aparece nos seis módulos das duas arquiteturas. A regra não
gera achados e não afeta a simetria da comparação. Por isso, só fica
registrada aqui.

## Resumo

- Total de achados analisados: 11 (9 de PMD, 2 de SpotBugs)
- Defeitos reais: 0 | Falsos positivos: 5 | Estilo/baixa prioridade: 6

Os totais batem com o resumo consolidado de `154328` (9 violações de PMD e
2 achados de SpotBugs).

| Regra | Ferramenta | Ocorrências | Classificação |
|---|---|---|---|
| UseUtilityClass | PMD (design) | 1 | falso positivo |
| DataClass | PMD (design) | 1 | falso positivo |
| UseConcurrentHashMap | PMD (multithreading) | 1 | falso positivo |
| EI_EXPOSE_REP / EI_EXPOSE_REP2 | SpotBugs | 2 | falso positivo |
| MissingSerialVersionUID | PMD (errorprone) | 3 | estilo/baixa prioridade |
| LawOfDemeter | PMD (design) | 3 | estilo/baixa prioridade |

## Achados

### [n/a] PMD UseUtilityClass - FinanceMonolitoApplication.java:9
- **Classificação:** falso positivo
- **Justificativa:** A classe é anotada com `@SpringBootApplication`, o que a
  torna também uma classe `@Configuration`. O Spring precisa instanciá-la e
  criar um proxy CGLIB dela. A regra sugere um construtor privado, mas essa
  mudança faria o contexto falhar ao subir. Além disso, a classe não contém
  apenas o `main` estático: ela também declara o `HealthController` aninhado
  (linhas 15-22), usado pelos healthchecks.
- **Recomendação:** documentar como falso positivo

### [n/a] PMD DataClass - TradeTransaction.java:20
- **Classificação:** falso positivo
- **Justificativa:** `TradeTransaction` é uma entidade JPA que funciona como
  registro de livro-razão das operações (compra ou venda, símbolo,
  quantidade, preço e instante). Ela é imutável depois de criada: os campos
  são definidos só no construtor (linhas 49-57), há apenas getters e o
  construtor sem argumentos é `protected`, exigido pelo JPA. Para um registro
  histórico de transações financeiras, não ter comportamento é a decisão de
  projeto correta. Acrescentar lógica ou setters pioraria a integridade do
  histórico.
- **Recomendação:** documentar como falso positivo

### [n/a] PMD UseConcurrentHashMap - GlobalExceptionHandler.java:53
- **Classificação:** falso positivo
- **Justificativa:** `Map<String, Object> body = new LinkedHashMap<>();` é uma
  variável local do método `error`. O mapa é criado, preenchido e devolvido
  dentro da mesma requisição e não é compartilhado entre threads. A escolha
  de `LinkedHashMap` também é intencional: ela mantém a ordem dos campos do
  JSON de erro (`timestamp`, `status`, `error`, `message`), que um
  `ConcurrentHashMap` não garante.
- **Recomendação:** documentar como falso positivo

### [n/a] SpotBugs EI_EXPOSE_REP - PortfolioResponse.java:6
- **Classificação:** falso positivo
- **Justificativa:** O acessor `positions()` do record devolve a referência
  da lista recebida. Porém, o único ponto de construção
  (`TradeService.portfolio`, linhas 96-102) produz essa lista com
  `Stream.toList()`, que é **não modificável**. Os elementos são
  `PositionView`, um record com componentes imutáveis (`String`, `int`,
  `BigDecimal`). Assim, quem chama o acessor não consegue alterar a
  representação exposta. O achado (`instanceHash 421c5471…`) é o mesmo das
  execuções anteriores, e a classificação se mantém.
- **Recomendação:** documentar como falso positivo

### [n/a] SpotBugs EI_EXPOSE_REP2 - PortfolioResponse.java:6
- **Classificação:** falso positivo
- **Justificativa:** O construtor canônico guarda a `List` sem cópia
  defensiva. O único chamador, porém
  (`new PortfolioResponse(user.getUsername(), positions, ...)` em
  `TradeService.java:106`), passa uma lista imutável criada localmente e não
  guarda nenhuma outra referência a ela. A regra é disparada pela forma do
  record (um componente `List`), e não por um caminho real de mutação
  (`instanceHash f3e18662…`, também inalterado).
- **Recomendação:** documentar como falso positivo

### [baixa] PMD MissingSerialVersionUID - BadRequestException.java:3, ConflictException.java:3, NotFoundException.java:3
- **Classificação:** estilo/baixa prioridade
- **Justificativa:** As três classes (`public class XxxException extends
  RuntimeException`) herdam `Serializable` de `Throwable` e não declaram
  `serialVersionUID`. Por isso, o achado é tecnicamente correto. No entanto,
  essas exceções nunca passam por serialização Java: o
  `GlobalExceptionHandler` as converte em JSON dentro do próprio processo
  (linhas 19-32). O risco de incompatibilidade entre versões serializadas
  não existe nesse uso. As três ocorrências são o mesmo padrão e foram
  agrupadas.
- **Recomendação:** ignorar. Se o Agente 3 quiser zerar o achado, basta
  adicionar `private static final long serialVersionUID = 1L;` nas três
  classes.

### [baixa] PMD LawOfDemeter - TradeService.java:51, TradeService.java:107
- **Classificação:** estilo/baixa prioridade
- **Justificativa:** As duas chamadas são `user.getSaldo()`, em que `user` é
  a entidade `UserAccount` obtida por `lockUser`/`findUser` no próprio
  serviço. Na linha 51, ela monta a mensagem de saldo insuficiente; na linha
  107, calcula `totalValue`. Consultar o estado da raiz de agregado que o
  serviço carregou é o uso idiomático de um serviço de aplicação com JPA. O
  PMD trata como "foreign value" todo valor retornado por método. Há uma
  alternativa no estilo *tell, don't ask* (por exemplo,
  `user.canAfford(total)`), mas ela só deixaria o encapsulamento um pouco
  melhor. Não corrige nenhum problema de saldo, que está protegido por lock
  pessimista e `@Transactional`.
- **Recomendação:** ignorar

### [baixa] PMD LawOfDemeter - TradeService.java:136
- **Classificação:** estilo/baixa prioridade
- **Justificativa:** `tx.getUser().getUsername()` em `toResponse` navega pela
  associação `@ManyToOne(fetch = LAZY)` da transação. Como o método é sempre
  chamado dentro de `buy`/`sell` (`@Transactional`) e o `UserAccount` já está
  no contexto de persistência, essa chamada não gera consulta extra nem
  `LazyInitializationException`. O achado é tecnicamente correto. Uma
  simplificação seria receber o `username` como parâmetro, já que `buy` e
  `sell` têm o `user` em mãos. Mesmo assim, o impacto é desprezível.
- **Recomendação:** ignorar

## Consistência com microsserviços

Na execução `154328`, as mesmas regras aparecem em classes equivalentes dos
microsserviços. Para cumprir a restrição de consistência, essas ocorrências
devem receber as classificações abaixo na revisão de microsserviços:

| Regra / classe | Monólito | Equivalentes nos microsserviços (`154328`) | Classificação esperada |
|---|---|---|---|
| UseUtilityClass em `*Application` | `FinanceMonolitoApplication` | `ApiGatewayApplication`, `EurekaServerApplication`, `MarketServiceApplication`, `UserServiceApplication` | falso positivo |
| DataClass `TradeTransaction` | `domain/TradeTransaction` | `trade-service` `TradeTransaction` | falso positivo, se a entidade também for imutável (verificar na revisão de microsserviços) |
| UseConcurrentHashMap em `GlobalExceptionHandler` | linha 53 | `market-service`, `trade-service`, `user-service` | falso positivo, se for o mesmo mapa local |
| MissingSerialVersionUID em exceções | 3 classes | `market-service` (1), `trade-service` (3), `user-service` (3) | estilo/baixa prioridade |
| LawOfDemeter `getSaldo` | `TradeService` 51, 107 | `user-service` `UserService.debit`:52 | estilo/baixa prioridade |
| EI_EXPOSE_REP/REP2 `PortfolioResponse` | linha 6 | `trade-service` `PortfolioResponse` | falso positivo |

A revisão `agente2_ia_microservicos_review.md` atual ainda usa `151627`, a
execução com o ruleset padrão (0 violações de PMD). Ela precisa ser refeita
sobre `154328` para que as duas revisões se refiram à mesma execução.

## Lista para o Agente 3

Nenhum achado do monólito foi classificado como `defeito real`, então não há
itens obrigatórios de correção para o Agente 3.

Endurecimentos opcionais. Nenhum deles corrige defeito, e cada um deve ser
aplicado nas duas arquiteturas ou em nenhuma, para manter a comparação
simétrica:

1. `web/dto/PortfolioResponse.java:6`: adicionar um construtor compacto com
   `positions = List.copyOf(positions);` (elimina EI_EXPOSE_REP e
   EI_EXPOSE_REP2).
2. `exception/BadRequestException.java:3`, `ConflictException.java:3` e
   `NotFoundException.java:3`: adicionar
   `private static final long serialVersionUID = 1L;`.

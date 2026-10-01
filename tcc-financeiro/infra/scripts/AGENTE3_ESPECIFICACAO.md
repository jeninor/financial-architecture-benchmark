# Especificação para o Agente 3 (Refatoração)

> Use este documento como prompt/contexto para o agente de refatoração
> (Claude Code). Execute-o duas vezes, em chats separados, uma apontando
> para `multiagente/monolito` e outra para `multiagente/microservicos`,
> igual ao procedimento do Agente 1.
>
> Esta versão substitui a especificação anterior: a revisão por IA do
> Agente 2 (`metrics/agente2_ia_monolito_review.md` e
> `agente2_ia_microservicos_review.md`, sobre a execução `154328`, com os
> 5 rulesets completos de PMD) concluiu que **não há defeitos reais** em
> nenhuma das duas arquiteturas. Por isso o Agente 3 não corrige defeitos
> — aplica apenas os dois endurecimentos opcionais que a revisão
> recomendou, de forma simétrica nas duas arquiteturas.

## Papel do agente

Você é o Agente 3 (refatoração) no pipeline multiagente. A tarefa é
aplicar **exatamente** dois endurecimentos opcionais, iguais nas duas
arquiteturas, sem alterar nenhum outro comportamento, sem alterar os 12
cenários funcionais (T01-T12) e sem introduzir novas dependências.

## Endurecimento 1 — cópia defensiva em `PortfolioResponse`

**Origem:** SpotBugs `EI_EXPOSE_REP`/`EI_EXPOSE_REP2` (CWE-374),
classificados como falso positivo pela revisão (o único ponto de
construção em cada arquitetura já passa uma lista imutável, criada com
`.toList()`). Ainda assim, é um endurecimento de baixo custo que elimina
o achado na raiz, em vez de apenas documentar.

**Arquivos** (mesma classe, duplicada nas duas arquiteturas):

- `multiagente/monolito/src/main/java/com/tcc/finance/web/dto/PortfolioResponse.java`
- `multiagente/microservicos/trade-service/src/main/java/com/tcc/finance/trade/web/dto/PortfolioResponse.java`

**Mudança** (idêntica nos dois arquivos):

1. Construtor compacto do record, normalizando `positions`:
   `positions = positions == null ? List.of() : List.copyOf(positions);`
2. Sobrescrever o accessor `positions()` para devolver sempre uma cópia
   (`List.copyOf(positions)`), em vez do campo diretamente.

Não altere a assinatura pública do record (nomes/ordem dos componentes —
use os nomes reais do arquivo).

## Endurecimento 2 — `serialVersionUID` nas exceções de domínio

**Origem:** PMD `MissingSerialVersionUID` (categoria `errorprone`),
classificado como estilo/baixa prioridade (as exceções nunca passam por
serialização Java — são convertidas em JSON pelo `GlobalExceptionHandler`
de cada módulo). Endurecimento trivial, sem risco.

**Arquivos** (10 no total — adicionar a mesma linha em cada um):

Monólito (3):
- `multiagente/monolito/src/main/java/com/tcc/finance/exception/BadRequestException.java`
- `multiagente/monolito/src/main/java/com/tcc/finance/exception/ConflictException.java`
- `multiagente/monolito/src/main/java/com/tcc/finance/exception/NotFoundException.java`

Microsserviços (7):
- `multiagente/microservicos/market-service/src/main/java/com/tcc/finance/market/exception/NotFoundException.java`
- `multiagente/microservicos/trade-service/src/main/java/com/tcc/finance/trade/exception/BadRequestException.java`
- `multiagente/microservicos/trade-service/src/main/java/com/tcc/finance/trade/exception/ConflictException.java`
- `multiagente/microservicos/trade-service/src/main/java/com/tcc/finance/trade/exception/NotFoundException.java`
- `multiagente/microservicos/user-service/src/main/java/com/tcc/finance/user/exception/BadRequestException.java`
- `multiagente/microservicos/user-service/src/main/java/com/tcc/finance/user/exception/ConflictException.java`
- `multiagente/microservicos/user-service/src/main/java/com/tcc/finance/user/exception/NotFoundException.java`

**Mudança** (idêntica em cada um): adicionar dentro da classe
`private static final long serialVersionUID = 1L;`

## O que NÃO alterar

Todo o resto dos achados (todos classificados como falso positivo ou
estilo/baixa prioridade pela revisão do Agente 2) permanece como está —
não é correção de defeito e não deve ser tocado:
`UseUtilityClass` (classes `*Application`), `DataClass`
(`TradeTransaction`), `UseConcurrentHashMap` (`GlobalExceptionHandler`),
`ImplicitFunctionalInterface` (`MarketClient`), `GuardLogStatement`
(`TradeEventPublisher`), `LawOfDemeter` (`getSaldo`,
`tx.getUser().getUsername()`), e os `EI_EXPOSE_REP2` de
`FeignErrorDecoder`/`TradeEventPublisher` (beans Spring injetados, não
dados mutáveis de domínio). Não adicione comentários de supressão nesses
pontos — ficam documentados nos relatórios do Agente 2, não no código.

## Critérios de aceitação

- Os 12 cenários (T01-T12) continuam passando nas duas arquiteturas
  (rodar os testes JUnit existentes, não criar novos).
- Build continua verde (`mvn clean package`) nos dois lados.
- Resultado esperado ao rodar o Agente 2 de novo:
  - SpotBugs: `PortfolioResponse` não aparece mais em nenhum relatório.
    No monólito, o total de SpotBugs cai de 2 para 0. Nos
    microsserviços, cai de 4 para 2 (os 2 de `FeignErrorDecoder` e
    `TradeEventPublisher` continuam — são falsos positivos documentados,
    **não é regressão**).
  - PMD: `MissingSerialVersionUID` não aparece mais. No monólito, o total
    cai de 9 para 6. Nos microsserviços, cai de 18 para 11.

## Registro do processo (para as métricas do TCC)

Salvar resumo em `metrics/agente3_<arquitetura>_log.md` com:
- arquivos alterados (10 + 2 = 12 por arquitetura, considerando os dois
  endurecimentos);
- diff aplicado em cada um;
- confirmação de que T01-T12 continuam passando;
- contagem de PMD/SpotBugs antes e depois (comparar com os números desta
  especificação).

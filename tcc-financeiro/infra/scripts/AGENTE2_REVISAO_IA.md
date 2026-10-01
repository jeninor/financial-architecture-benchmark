# Camada de Revisão por IA do Agente 2 (pós-análise estática)

> Use este documento como prompt/contexto para o agente de revisão
> (Claude Code). Execute-o **duas vezes**, uma apontando para os relatórios
> de `multiagente/monolito` e outra para os de `multiagente/microservicos`,
> igual ao procedimento dos Agentes 1 e 3.
>
> Esta camada roda **depois** de `run_agente2.sh` (PMD + SpotBugs), nunca
> antes e nunca em paralelo: ela lê os relatórios já gerados, não o código
> diretamente, para manter a classificação ancorada em dados objetivos das
> ferramentas, não em uma leitura livre do agente.

## Papel do agente

Você é a camada de revisão por IA do Agente 2 (revisão estática) no
pipeline multiagente. As ferramentas determinísticas (PMD, SpotBugs) já
rodaram e geraram relatórios brutos em `metrics/`. Seu trabalho é **ler
esses relatórios + o código-fonte apontado por eles** e produzir uma
análise qualitativa que as ferramentas sozinhas não entregam: classificar
cada achado, justificar a classificação e priorizar o que vale a pena
corrigir no Agente 3.

Você NÃO re-executa PMD/SpotBugs, NÃO inventa novos achados além dos que
estão nos relatórios, e NÃO analisa arquivos que as ferramentas não
apontaram.

## Entradas

Os relatórios estão em `metrics/`, nomeados como
`agente2_<modulo>_pmd_<timestamp>.xml` e
`agente2_<modulo>_spotbugs_<timestamp>.xml`, mais um
`agente2_resumo_<timestamp>.md` consolidado. O agente deve **descobrir
sozinho** quais arquivos pertencem à arquitetura pedida, sem que isso seja
passado manualmente:

- Arquitetura `monolito` → módulo `monolito` (um único módulo).
- Arquitetura `microservicos` → módulos `api-gateway`, `eureka-server`,
  `market-service`, `trade-service`, `user-service`.

Procedimento de descoberta: liste `metrics/agente2_*_pmd_*.xml` e
`metrics/agente2_*_spotbugs_*.xml`, filtre pelos módulos da arquitetura
pedida e, se houver mais de um timestamp para o mesmo módulo (reexecuções
do Agente 2), use sempre o **mais recente** (maior timestamp no nome do
arquivo) — ignore relatórios antigos sem perguntar, mas liste no início do
relatório de saída quais arquivos (com timestamp) foram efetivamente
usados, para rastreabilidade.

Além dos relatórios, o agente também lê o código-fonte do módulo
correspondente, mas **apenas os arquivos/linhas citados nos relatórios**
(não é uma revisão de código livre).

## Tarefa

Para cada achado reportado (PMD ou SpotBugs) em cada módulo da arquitetura:

1. **Classifique** como:
   - `defeito real` — o padrão apontado pode causar um problema de
     corretude, segurança ou manutenibilidade genuíno, dado o contexto real
     do uso da classe/método.
   - `falso positivo` — a regra disparou, mas o contexto (ex.: injeção de
     dependência gerenciada pelo Spring, campo efetivamente imutável,
     padrão idiomático do framework) torna o achado não-acionável.
   - `estilo/baixa prioridade` — tecnicamente correto, mas sem impacto
     relevante dentro do escopo do TCC (ex.: nomenclatura, formatação).

2. **Justifique** a classificação em 1-2 frases, citando o trecho de código
   relevante (não apenas repita a mensagem da ferramenta).

3. **Priorize** os achados classificados como `defeito real` em
   alta/média/baixa, considerando: (a) se o padrão se repete em mais de um
   módulo/arquitetura (indica decisão sistemática do Agente 1, não um erro
   isolado), (b) se afeta dados de domínio financeiro (saldo, posições,
   transações) vs. infraestrutura, (c) esforço estimado de correção.

## Saída esperada

Um arquivo `metrics/agente2_ia_<arquitetura>_review.md` com:

```markdown
# Revisão por IA - Agente 2 (<monolito|microservicos>)

## Resumo
- Total de achados analisados: N (M de PMD, K de SpotBugs)
- Defeitos reais: X | Falsos positivos: Y | Estilo/baixa prioridade: Z

## Achados

### [prioridade] <tipo da regra> - <arquivo:linha>
- **Classificação:** defeito real | falso positivo | estilo/baixa prioridade
- **Justificativa:** ...
- **Recomendação:** corrigir no Agente 3 | documentar como falso positivo | ignorar

(repetir por achado)

## Lista para o Agente 3
<lista ordenada só dos itens "corrigir no Agente 3", com arquivo + linha + mudança sugerida>
```

## Restrições metodológicas

- Não reclassifique um achado de forma diferente entre monólito e
  microsserviços quando o código e o contexto forem equivalentes (ex.: a
  mesma classe `PortfolioResponse` duplicada nos dois lados deve receber a
  mesma classificação nos dois relatórios) — isso é verificado manualmente
  na consolidação final, então inconsistências aqui vão aparecer.
- Não extrapole além do que os relatórios e o código mostram; se um achado
  for ambíguo, classifique como `estilo/baixa prioridade` e registre a
  ambiguidade, em vez de forçar uma classificação.
- Linguagem acadêmica em português brasileiro, consistente com o resto do
  TCC.

## Observação para o TCC (metodologia)

Diferente dos Agentes 1 e 3 (geração/refatoração de código) e das
ferramentas do Agente 2 (PMD/SpotBugs, determinísticas), esta camada usa um
LLM para **classificar e priorizar** achados já coletados objetivamente —
não para detectá-los do zero. Isso é uma escolha deliberada: a detecção
fica a cargo de ferramentas determinísticas (reprodutibilidade), e o
julgamento qualitativo (que exige entender contexto de negócio e padrões
de framework) fica a cargo do LLM. Registrar essa divisão de
responsabilidades explicitamente na metodologia do TCC.

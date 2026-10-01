# Especificação para o Agente 2 (Revisão Estática)

> Agente automatizado (não-LLM): roda ferramentas de análise estática via
> Maven + Docker, sem pedir julgamento de um agente de IA. Isso preserva a
> equivalência de procedimento entre monólito e microsserviços (mesmas
> regras, mesmos thresholds, aplicadas da mesma forma nas duas arquiteturas)
> e evita variância entre execuções que um agente LLM introduziria.

## Papel do agente

Avaliar qualidade de código estática nas duas arquiteturas geradas pelo
Agente 1, usando duas ferramentas complementares:

- **PMD** — code smells, boas práticas, design, complexidade, código morto.
- **SpotBugs** — defeitos potenciais em bytecode (null pointers, resource
  leaks, concorrência, etc.) que PMD não cobre por analisar só o código-fonte.

## Ferramentas e versões

| Ferramenta | Plugin Maven | Versão | Goal |
|---|---|---|---|
| PMD | `org.apache.maven.plugins:maven-pmd-plugin` | 3.21.2 | `pmd` |
| SpotBugs | `com.github.spotbugs:spotbugs-maven-plugin` | 4.8.6.0 | `spotbugs` (requer `compile` antes) |

Rodados via `docker run maven:3.9-eclipse-temurin-21` (mesma imagem já usada
pelo Agente 4), reaproveitando o cache `~/.m2` já populado. Não é necessário
declarar os plugins no `pom.xml` de cada módulo — são invocados direto na
linha de comando (`mvn groupId:artifactId:versão:goal`), sem alterar os
artefatos gerados pelo Agente 1.

> Se a resolução de uma dessas versões falhar (plugin não encontrado no
> Maven Central), remova o `:versão` da invocação para deixar o Maven
> resolver a mais recente disponível — e registre no log a versão que
> efetivamente rodou, para reprodutibilidade.

## Conjunto de regras (PMD)

Para manter o relatório focado em achados relevantes (não em estilo/
formatação, que não é o objeto do TCC), usamos apenas categorias de
conteúdo técnico:

```
category/java/bestpractices.xml
category/java/errorprone.xml
category/java/design.xml
category/java/multithreading.xml
category/java/performance.xml
```

Ficam de fora `codestyle` e `documentation` (formatação/Javadoc), que não
dizem respeito a corretude, manutenibilidade ou performance.

## Configuração do SpotBugs

- `effort=Max` (análise mais profunda possível)
- `threshold=Low` (reporta até achados de baixa confiança, para não perder
  nada relevante — a triagem de severidade é feita na leitura do relatório,
  não na coleta)
- Saída em XML (para contagem automática) e HTML (para leitura humana)

## Escopo de execução

Mesma lista de módulos usada pelo Agente 4 (segurança), para preservar
equivalência de procedimento:

- `multiagente/monolito` (projeto único)
- `multiagente/microservicos/{api-gateway, eureka-server, market-service,
  trade-service, user-service}` (5 módulos independentes, sem pom
  agregador)

## Saída esperada

Por módulo, dois relatórios brutos salvos em `metrics/`:
`agente2_<modulo>_pmd_<timestamp>.xml` e
`agente2_<modulo>_spotbugs_<timestamp>.{xml,html}`.

Além disso, um resumo consolidado `agente2_resumo_<timestamp>.md` com a
contagem de violações/achados por módulo, para comparação rápida
monólito vs. microsserviços (contagem bruta — a classificação por
severidade/prioridade é uma leitura manual sobre os XML/HTML, não
automatizada neste passo).

## Registro do processo (para as métricas do TCC)

Ao final, documentar manualmente (ou com apoio do Agente 3):
- contagem de achados por ferramenta e por módulo;
- quais achados são comuns aos dois lados (indicando um padrão sistemático
  da geração via IA) vs. específicos de uma arquitetura (indicando
  overhead/risco introduzido pela distribuição em serviços, ex.: tratamento
  de exceção duplicado, chamadas remotas sem timeout, etc.);
- decisão de quais achados alimentam o prompt do Agente 3 (refatoração) —
  tipicamente prioridade alta/média de `errorprone` e `design`, e todos os
  achados de `correctness`/`multithreading` do SpotBugs com confiança
  média ou alta.

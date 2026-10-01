# Camada de Revisão por IA do Agente 4 (pós-Trivy)

> Use este documento como prompt/contexto para o agente de revisão
> (Claude Code). Diferente da camada do Agente 2, esta roda **uma única
> vez**, lendo os relatórios das DUAS arquiteturas na mesma execução —
> porque a tarefa é comparar os dois conjuntos de CVEs, não julgar cada
> lado de forma independente. Não há risco de "contaminação" aqui: a
> comparação de conjuntos de IDs de CVE é uma operação mecânica, não um
> julgamento subjetivo de qualidade de código.

## Papel do agente

O Trivy já rodou (`run_trivy.sh`) e gerou um relatório JSON por
arquitetura em `metrics/`. O volume é grande (dezenas a centenas de
vulnerabilidades por severidade) — não liste cada uma em prosa. Em vez
disso, **escreva e execute um script** (Python, com o modulo `json` da
stdlib, é suficiente) que processe os dois arquivos e produza os dados
abaixo. Só depois de ter os números, escreva a análise textual sobre eles.

## Entradas

- Monólito: o `metrics/trivy_monolito_<timestamp>.json` mais recente.
- Microsserviços: o `metrics/trivy_microservicos_<timestamp>.json` cujo
  `<timestamp>` **coincide** com o do monólito usado (mesma execução de
  `run_trivy.sh`). Se não houver par com o mesmo timestamp, use o par mais
  recente de cada lado e registre isso explicitamente na saída.
- Se houver mais de uma execução do monólito com o mesmo resultado
  (mesma contagem por severidade), isso é esperado — o scan é
  determinístico — e não precisa ser reexecutado; apenas confirme e
  registre.

## Tarefa (via script)

Para cada vulnerabilidade em `Results[].Vulnerabilities[]`, extraia:
`VulnerabilityID`, `PkgName`, `InstalledVersion`, `FixedVersion` (pode
estar vazio), `Severity`, `Title`.

Calcule e reporte:

1. **Contagem por severidade** em cada arquitetura (confirmar os números
   já vistos: monólito 7/31/32/14 (CRITICAL/HIGH/MEDIUM/LOW),
   microsserviços 40/192/207/74).
2. **CVEs distintos** (por `VulnerabilityID`) em cada arquitetura — o
   total bruto conta a mesma CVE várias vezes se ela afeta pacotes
   usados por múltiplos módulos dos microsserviços; o número de CVEs
   *distintos* é o que importa para comparar exposição real, não
   contagem de ocorrências.
3. **Interseção**: CVEs que aparecem nas duas arquiteturas (devem vir de
   dependências compartilhadas da baseline comum — Spring Boot
   3.3.4, Java 21, etc.). Confirme isso olhando o `PkgName` de uma
   amostra.
4. **Exclusivas de microsserviços**: CVEs que só aparecem lá. Agrupe por
   `PkgName` e identifique de qual dependência de infraestrutura
   distribuída elas vêm (candidatos prováveis: Netty/Reactor via
   `spring-cloud-starter-gateway`, cliente Eureka, OpenFeign, cliente
   RabbitMQ/`amqp-client`, Spring Cloud LoadBalancer). Isso quantifica o
   "custo de segurança" específico de distribuir o sistema.
5. **Exclusivas de monólito** (se houver): mesma lógica, caso exista
   algo que o monólito tenha e os microsserviços não.
6. Dentro de "exclusivas de microsserviços", separe as de severidade
   `CRITICAL` e `HIGH` com `FixedVersion` não vazio — essas são
   **acionáveis** (existe uma versão que corrige) e merecem destaque
   próprio na saída, com o nome do pacote, versão atual, versão corrigida
   e se atualizar essa dependência é viável sem quebrar a especificação
   do Agente 1 (ex.: trocar uma versão de patch do Spring Cloud
   normalmente é seguro; trocar a versão major do Spring Boot não é).

## Saída esperada

`metrics/agente4_ia_review.md`:

```markdown
# Revisão por IA - Agente 4 (Trivy)

## Rastreabilidade
<quais arquivos/timestamps foram usados>

## Resumo quantitativo
<tabela: severidade x arquitetura, total bruto e CVEs distintos>

## CVEs compartilhados (baseline comum)
<contagem + amostra de 3-5 exemplos com PkgName, para evidenciar que vêm
da baseline Spring Boot/Java comum às duas arquiteturas>

## CVEs exclusivos de microsserviços
<tabela agrupada por PkgName/dependência de origem, com contagem por
severidade>

## Acionáveis (CRITICAL/HIGH com correção disponível)
<lista: PkgName, versão atual -> versão corrigida, severidade, se é
seguro atualizar sem violar a especificação do Agente 1>

## Leitura para o TCC
<1-2 parágrafos: a diferença bruta de contagem é explicada por quê? É
proporcional ao número de dependências de infraestrutura adicionais, ou
há algo desproporcional que mereça destaque?>
```

## Restrições metodológicas

- Não liste todas as CVEs individualmente em prosa — o script já
  consolidou os números; a prosa interpreta os números, não os repete.
- Não recomende "atualizar tudo" genericamente. Só recomende uma
  atualização específica quando houver `FixedVersion` concreta e a
  mudança não contradizer a especificação de versões do Agente 1
  (Java 21, Spring Boot 3.3.4, Spring Cloud 2023.0.3).
- Não trate contagem bruta de CVEs como medida de "pior qualidade" sem
  contextualizar — várias CVEs em bibliotecas de terceiros amplamente
  usadas (ex.: Netty, Jackson) nem sempre são exploráveis no contexto
  específico desta aplicação (API interna, sem exposição direta à
  internet, sem processar entrada não confiável de determinados tipos).
  Essa ressalva deve constar na "Leitura para o TCC".

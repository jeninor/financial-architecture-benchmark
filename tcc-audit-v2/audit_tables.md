# Tabelas do Capítulo 4 geradas automaticamente

## Tabela 1 – Métricas estruturais da etapa ChatGPT direto

| Métrica | Monolito | Microsserviços | Diferença Micro vs. Mono |
|---|---:|---:|---:|
| Physical Java LOC | 1274 | 2206 | +73,2% |
| NLOC | 904 | 1520 | +68,1% |
| Arquivos Java | 26 | 47 | +80,8% |
| Funções/métodos | 18 | 35 | +94,4% |
| CC total | 29 | 50 | +72,4% |
| CC média | 1,61 | 1,43 | -11,2% |
| CC máxima | 5 | 5 | +0,0% |
| Testes aprovados | 12/12 | 12/12 | — |

Fonte: elaboração própria a partir dos artefatos auditados.

## Tabela 2 – Métricas estruturais das execuções R0003

| Métrica | Monolito | Microsserviços | Diferença Micro vs. Mono |
|---|---:|---:|---:|
| Physical Java LOC | 362 | 577 | +59,4% |
| NLOC | 315 | 474 | +50,5% |
| Arquivos Java | 9 | 14 | +55,6% |
| Funções/métodos | 10 | 24 | +140,0% |
| CC total | 26 | 48 | +84,6% |
| CC média | 2,6 | 2 | -23,1% |
| CC máxima | 9 | 10 | +11,1% |
| Iterações A1 | 3 | 3 | — |
| Findings de qualidade | 0 | 0 | — |

Fonte: elaboração própria a partir dos artefatos auditados.

## Tabela 3 – Resultados principais do protocolo v1.2

| Métrica | Claude Mono | Claude Micro | Codex Mono | Codex Micro |
|---|---:|---:|---:|---:|
| Acceptance A1 | 12/12 | 12/12 | 12/12 | 12/12 |
| Tempo total (s) | 159,716 | 860,847 | 308,624 | 549,838 |
| Tempo provider A1 (s) | 42,223 | 71,214 | 183,197 | 258,082 |
| Iterações A1 | 1 | 1 | 1 | 1 |
| Tool calls A1 | 4 | 7 | 15 | 23 |
| Tool calls com falha | 0 | 0 | 3 | 5 |
| Physical Java LOC final | 384 | 631 | 410 | 530 |
| NLOC final | 311 | 526 | 341 | 447 |
| Arquivos Java | 8 | 15 | 10 | 16 |
| Funções/métodos | 9 | 33 | 11 | 21 |
| CC total | 26 | 58 | 19 | 40 |
| CC média | 2,889 | 1,758 | 1,727 | 1,905 |
| CC máxima | 9 | 7 | 4 | 8 |
| Findings de qualidade antes de A3 | 0 | 1 | 0 | 0 |
| Findings finais | 0 | 0 | 0 | 0 |
| A3 | SKIPPED_NO_FINDINGS | SUCCESS | SKIPPED_NO_FINDINGS | SKIPPED_NO_FINDINGS |

Fonte: elaboração própria a partir dos artefatos auditados.

## Tabela 4 – Resultados da análise de segurança no protocolo v1.2

| Categoria | Claude Mono | Claude Micro | Codex Mono | Codex Micro |
|---|---:|---:|---:|---:|
| Vulnerabilidades | 0 | 0 | 0 | 0 |
| Secrets | 0 | 0 | 0 | 0 |
| Misconfigurations | 0 | 0 | 0 | 0 |
| HIGH | 0 | 0 | 0 | 0 |
| CRITICAL | 0 | 0 | 0 | 0 |

Fonte: elaboração própria a partir dos artefatos auditados.

## Tabela 5 – Diferença percentual entre microsserviços e monolito nas diferentes etapas

| Condição | Δ LOC Micro | Δ NLOC Micro | Δ arquivos | Δ funções | Δ CC total |
|---|---:|---:|---:|---:|---:|
| ChatGPT direto | +73,2% | +68,1% | +80,8% | +94,4% | +72,4% |
| Claude R0003 | +59,4% | +50,5% | +55,6% | +140,0% | +84,6% |
| Claude v1.2 | +64,3% | +69,1% | +87,5% | +266,7% | +123,1% |
| Codex v1.2 | +29,3% | +31,1% | +60,0% | +90,9% | +110,5% |

Fonte: elaboração própria a partir dos artefatos auditados.

## Tabela 6 – Complexidade ciclomática média por condição

| Condição | CC média Mono | CC média Micro |
|---|---:|---:|
| ChatGPT direto | 1,61 | 1,43 |
| Claude R0003 | 2,6 | 2 |
| Claude v1.2 | 2,889 | 1,758 |
| Codex v1.2 | 1,727 | 1,905 |

Fonte: elaboração própria a partir dos artefatos auditados.

## Tabela 7 – Evidências de falhas e esforço de correção

| Etapa/condição | Evidência registrada | Resultado final |
|---|---|---|
| ChatGPT direto — Monolito | Sem contagem sistemática equivalente | 12/12 |
| ChatGPT direto — Microsserviços | 3 problemas documentados | 12/12 |
| Claude R0003 — Monolito | 3 iterações A1 | Sucesso |
| Claude R0003 — Microsserviços | 3 iterações A1 | Sucesso |
| Claude v1.2 — Monolito | 1 iteração A1 | 12/12 |
| Claude v1.2 — Microsserviços | 1 iteração A1; falha transitória durante A3 | 12/12 final |
| Codex v1.2 — Monolito | 1 iteração A1 | 12/12 |
| Codex v1.2 — Microsserviços | 1 iteração A1 | 12/12 |

Fonte: elaboração própria a partir dos artefatos auditados.

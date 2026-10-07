# Tabelas geradas automaticamente

## Protocolo v1.2 — execuções elegíveis

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
| Findings A2 iniciais | 0 | 1 | 0 | 0 |
| Findings finais | 0 | 0 | 0 | 0 |
| A3 | SKIPPED_NO_FINDINGS | SUCCESS | SKIPPED_NO_FINDINGS | SKIPPED_NO_FINDINGS |

## Comparação transversal — diferenças percentuais

| Condição | Δ LOC Micro | Δ NLOC Micro | Δ arquivos | Δ funções | Δ CC total |
|---|---:|---:|---:|---:|---:|
| ChatGPT direto | +73,2% | +68,1% | +80,8% | +94,4% | +72,4% |
| Claude R0003 | +59,4% | +50,5% | +55,6% | +140,0% | +84,6% |
| Claude v1.2 | +64,3% | +69,1% | +87,5% | +266,7% | +123,1% |
| Codex v1.2 | +29,3% | +31,1% | +60,0% | +90,9% | +110,5% |

## Complexidade ciclomática média

| Condição | CC média Mono | CC média Micro |
|---|---:|---:|
| ChatGPT direto | 1,61 | 1,43 |
| Claude R0003 | 2,6 | 2 |
| Claude v1.2 | 2,889 | 1,758 |
| Codex v1.2 | 1,727 | 1,905 |

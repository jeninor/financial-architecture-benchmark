# Relatório de auditoria dos resultados do TCC

- PASS: 41
- WARN: 0
- FAIL: 0
- Git HEAD: `ec22476c0396b5bdd0f283d13afb36d374b0c83e`
- Git branch: `main`

## Checks

| Status | Verificação | Detalhe |
|---|---|---|
| PASS | Etapa 1 monolith functions vs Lizard CSV | confirmado: 18 (legacy-chatpt/experiment/results/monolith-lizard.csv) |
| PASS | Etapa 1 monolith cc_total vs Lizard CSV | confirmado: 29 (legacy-chatpt/experiment/results/monolith-lizard.csv) |
| PASS | Etapa 1 monolith cc_average vs Lizard CSV | confirmado: 1.6111111111111112 (legacy-chatpt/experiment/results/monolith-lizard.csv) |
| PASS | Etapa 1 monolith cc_max vs Lizard CSV | confirmado: 5 (legacy-chatpt/experiment/results/monolith-lizard.csv) |
| PASS | Etapa 1 microservices functions vs Lizard CSV | confirmado: 35 (legacy-chatpt/experiment/results/microservices-lizard.csv) |
| PASS | Etapa 1 microservices cc_total vs Lizard CSV | confirmado: 50 (legacy-chatpt/experiment/results/microservices-lizard.csv) |
| PASS | Etapa 1 microservices cc_average vs Lizard CSV | confirmado: 1.4285714285714286 (legacy-chatpt/experiment/results/microservices-lizard.csv) |
| PASS | Etapa 1 microservices cc_max vs Lizard CSV | confirmado: 5 (legacy-chatpt/experiment/results/microservices-lizard.csv) |
| PASS | Etapa 1 problemas documentados | 3 problemas confirmados em legacy-chatpt/experiment/metrics/development-errors.md |
| PASS | R0003_monolith protocolo | official_finance_architecture_experiment_v3 |
| PASS | R0003_monolith success | true |
| PASS | R0003_monolith acceptance final | 12/12 em multiagent-experiment/runs/R0003_monolith/a1_iteration_03/acceptance.json |
| PASS | R0003_microservices protocolo | official_finance_architecture_experiment_v3 |
| PASS | R0003_microservices success | true |
| PASS | R0003_microservices acceptance final | 12/12 em multiagent-experiment/runs/R0003_microservices/a1_iteration_03/acceptance.json |
| PASS | RH0003_claude_monolith protocolo | official_finance_architecture_provider_experiment_v1_2 |
| PASS | RH0003_claude_monolith status | VALID_SUCCESS |
| PASS | RH0003_claude_monolith elegibilidade | primary_analysis_eligible=true |
| PASS | RH0003_claude_monolith acceptance A1 | 12/12 em multiagent-provider-experiment/runs/RH0003_claude_monolith/a1_iteration_01/acceptance.json |
| PASS | RH0003_claude_microservices protocolo | official_finance_architecture_provider_experiment_v1_2 |
| PASS | RH0003_claude_microservices status | VALID_SUCCESS |
| PASS | RH0003_claude_microservices elegibilidade | primary_analysis_eligible=true |
| PASS | RH0003_claude_microservices acceptance A1 | 12/12 em multiagent-provider-experiment/runs/RH0003_claude_microservices/a1_iteration_01/acceptance.json |
| PASS | RC0003_codex_monolith protocolo | official_finance_architecture_provider_experiment_v1_2 |
| PASS | RC0003_codex_monolith status | VALID_SUCCESS |
| PASS | RC0003_codex_monolith elegibilidade | primary_analysis_eligible=true |
| PASS | RC0003_codex_monolith acceptance A1 | 12/12 em multiagent-provider-experiment/runs/RC0003_codex_monolith/a1_iteration_01/acceptance.json |
| PASS | RC0003_codex_microservices protocolo | official_finance_architecture_provider_experiment_v1_2 |
| PASS | RC0003_codex_microservices status | VALID_SUCCESS |
| PASS | RC0003_codex_microservices elegibilidade | primary_analysis_eligible=true |
| PASS | RC0003_codex_microservices acceptance A1 | 12/12 em multiagent-provider-experiment/runs/RC0003_codex_microservices/a1_iteration_01/acceptance.json |
| PASS | v1.2 protocol SHA comum | SHA único: 4c1e63998f96494edac71f4d657a56c0106f957b9c11c1df233e45b9cd4de936 |
| PASS | v1.2 environment lock SHA comum | SHA único: 95163c8a19e194be2a9f4599f204d3ee2454aaf5fdfa81c4b4c505333d77c6fc |
| PASS | Claude Micro A3 attempt 1 | 7/12 confirmado |
| PASS | Claude Micro A3 attempt 2 | 12/12 confirmado |
| PASS | Claude Micro CC max A3 | 12 -> 7 confirmado |
| PASS | Claude Micro findings A3 | 1 -> 0 confirmado |
| PASS | RH0003_claude_monolith segurança | todos os indicadores registrados = 0 |
| PASS | RH0003_claude_microservices segurança | todos os indicadores registrados = 0 |
| PASS | RC0003_codex_monolith segurança | todos os indicadores registrados = 0 |
| PASS | RC0003_codex_microservices segurança | todos os indicadores registrados = 0 |

## Regra de uso

Os números do capítulo de Resultados devem ser copiados de `audit_tables.md` ou de `audit_data.json`, e não digitados a partir de memória/conversa.

Se houver qualquer `FAIL`, o conjunto não deve ser considerado auditado até que a divergência seja explicada.

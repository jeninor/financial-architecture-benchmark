# Relatório de auditoria dos resultados do TCC — v2

- PASS: 64
- WARN: 1
- FAIL: 0
- Git HEAD: `ec22476c0396b5bdd0f283d13afb36d374b0c83e`
- Git branch: `main`
- Audit script SHA-256: `8658af16b1f3dedd6569576df4cf16da45a19be832a6dc876c9b36dd929ff370`

## Checks

| Status | Verificação | Detalhe |
|---|---|---|
| PASS | Etapa 1 monolith functions vs Lizard CSV | confirmado: 18 (legacy-chatpt/experiment/results/monolith-lizard.csv) |
| PASS | Etapa 1 monolith cc_total vs Lizard CSV | confirmado: 29.0 (legacy-chatpt/experiment/results/monolith-lizard.csv) |
| PASS | Etapa 1 monolith cc_average vs Lizard CSV | confirmado: 1.6111111111111112 (legacy-chatpt/experiment/results/monolith-lizard.csv) |
| PASS | Etapa 1 monolith cc_max vs Lizard CSV | confirmado: 5.0 (legacy-chatpt/experiment/results/monolith-lizard.csv) |
| PASS | Etapa 1 microservices functions vs Lizard CSV | confirmado: 35 (legacy-chatpt/experiment/results/microservices-lizard.csv) |
| PASS | Etapa 1 microservices cc_total vs Lizard CSV | confirmado: 50.0 (legacy-chatpt/experiment/results/microservices-lizard.csv) |
| PASS | Etapa 1 microservices cc_average vs Lizard CSV | confirmado: 1.4285714285714286 (legacy-chatpt/experiment/results/microservices-lizard.csv) |
| PASS | Etapa 1 microservices cc_max vs Lizard CSV | confirmado: 5.0 (legacy-chatpt/experiment/results/microservices-lizard.csv) |
| PASS | Etapa 1 monolith Java files vs baseline | 26 confirmado |
| PASS | Etapa 1 monolith LOC vs baseline | 1274 confirmado |
| PASS | Etapa 1 microservices Java files vs baseline | 47 confirmado |
| PASS | Etapa 1 microservices LOC vs baseline | 2206 confirmado |
| PASS | Etapa 1 monolith acceptance independente | 12/12 confirmado em legacy-chatpt/experiment/results/monolith-test-results.txt |
| PASS | Etapa 1 microservices acceptance independente | 12/12 confirmado em legacy-chatpt/experiment/results/microservices-e2e-test-results.txt |
| PASS | Etapa 1 problemas documentados | 3 problemas confirmados em legacy-chatpt/experiment/metrics/development-errors.md |
| PASS | R0003_monolith protocolo | official_finance_architecture_experiment_v3 |
| PASS | R0003_monolith success | true |
| PASS | R0003_monolith iterações A1 | 3 confirmado |
| PASS | R0003_monolith findings | 0 confirmado |
| PASS | R0003_monolith segurança | todos os indicadores registrados = 0 |
| PASS | R0003_monolith acceptance final | 12/12 em multiagent-experiment/runs/R0003_monolith/a1_iteration_03/acceptance.json |
| PASS | R0003_microservices protocolo | official_finance_architecture_experiment_v3 |
| PASS | R0003_microservices success | true |
| PASS | R0003_microservices iterações A1 | 3 confirmado |
| PASS | R0003_microservices findings | 0 confirmado |
| PASS | R0003_microservices segurança | todos os indicadores registrados = 0 |
| PASS | R0003_microservices acceptance final | 12/12 em multiagent-experiment/runs/R0003_microservices/a1_iteration_03/acceptance.json |
| PASS | RH0003_claude_monolith protocolo | official_finance_architecture_provider_experiment_v1_2 |
| PASS | RH0003_claude_monolith status | VALID_SUCCESS |
| PASS | RH0003_claude_monolith elegibilidade | primary_analysis_eligible=true |
| PASS | RH0003_claude_monolith acceptance A1 | 12/12 em multiagent-provider-experiment/runs/RH0003_claude_monolith/a1_iteration_01/acceptance.json |
| PASS | RH0003_claude_monolith segurança | todos os indicadores registrados = 0 |
| PASS | RH0003_claude_microservices protocolo | official_finance_architecture_provider_experiment_v1_2 |
| PASS | RH0003_claude_microservices status | VALID_SUCCESS |
| PASS | RH0003_claude_microservices elegibilidade | primary_analysis_eligible=true |
| PASS | RH0003_claude_microservices acceptance A1 | 12/12 em multiagent-provider-experiment/runs/RH0003_claude_microservices/a1_iteration_01/acceptance.json |
| PASS | RH0003_claude_microservices segurança | todos os indicadores registrados = 0 |
| PASS | RC0003_codex_monolith protocolo | official_finance_architecture_provider_experiment_v1_2 |
| PASS | RC0003_codex_monolith status | VALID_SUCCESS |
| PASS | RC0003_codex_monolith elegibilidade | primary_analysis_eligible=true |
| PASS | RC0003_codex_monolith acceptance A1 | 12/12 em multiagent-provider-experiment/runs/RC0003_codex_monolith/a1_iteration_01/acceptance.json |
| PASS | RC0003_codex_monolith segurança | todos os indicadores registrados = 0 |
| PASS | RC0003_codex_microservices protocolo | official_finance_architecture_provider_experiment_v1_2 |
| PASS | RC0003_codex_microservices status | VALID_SUCCESS |
| PASS | RC0003_codex_microservices elegibilidade | primary_analysis_eligible=true |
| PASS | RC0003_codex_microservices acceptance A1 | 12/12 em multiagent-provider-experiment/runs/RC0003_codex_microservices/a1_iteration_01/acceptance.json |
| PASS | RC0003_codex_microservices segurança | todos os indicadores registrados = 0 |
| PASS | v1.2 protocol SHA comum | SHA único: 4c1e63998f96494edac71f4d657a56c0106f957b9c11c1df233e45b9cd4de936 |
| PASS | v1.2 environment lock SHA comum | SHA único: 95163c8a19e194be2a9f4599f204d3ee2454aaf5fdfa81c4b4c505333d77c6fc |
| PASS | Claude Micro A3 attempt 1 | 7/12 confirmado |
| PASS | Claude Micro A3 attempt 2 | 12/12 confirmado |
| PASS | Claude Micro CC max A3 | 12 -> 7 confirmado |
| PASS | Claude Micro findings A3 | 1 -> 0 confirmado |
| WARN | Claude Micro A3 segunda invocação sem alteração | não foi encontrada evidência textual inequívoca; não afirmar no TCC sem revisar o trace |
| PASS | Antigravity Mono status | INVALID_PROVIDER_POLICY confirmado |
| PASS | Antigravity Mono elegibilidade | primary_analysis_eligible=false |
| PASS | Antigravity Mono policy violation | 1 invocação de violação confirmada |
| PASS | Antigravity Mono docker --version | /home/alunos/Documentos/Juan/usp/financial-architecture-benchmark/multiagent-provider-experiment/runs/RA0003_antigravity_monolith/FINAL_SUMMARY.json:          "policy_violations": [             "forbidden provider Docker command: docker --version"           ],           "final_response": "",           "native_usage": {             "input_tokens": 144263,           |
| PASS | Antigravity Micro status | ABORTED_PROVIDER_RUNTIME confirmado |
| PASS | Antigravity Micro elegibilidade | primary_analysis_eligible=false |
| PASS | Antigravity Micro iterações A1 | 2 confirmado |
| PASS | Antigravity Micro sem policy violation | 0 confirmado |
| PASS | Antigravity Micro acceptance iter1 | 7/12 confirmado |
| PASS | Antigravity Micro quota/runtime evidence | /home/alunos/Documentos/Juan/usp/financial-architecture-benchmark/multiagent-provider-experiment/runs/RA0003_antigravity_microservices/a1_iteration_01/acceptance_failure_evidence/compose_logs_all.txt: B \| 396/920 kB Progress (2): 3.0/9.0 MB \| 412/920 kB Progress (2): 3.0/9.0 MB \| 429/920 kB Progress (3): 3.0/9.0 MB \| 429/920 kB \| 0/1.5 MB Progress (3): 3.0/9.0 MB \| 429/920 kB \| 0/1.5 MB Progress (3):  |
| PASS | RA0003_pair adjudicação | presente: multiagent-provider-experiment/protocol/adjudications/RA0003_pair.json |

## Regra de uso

Os números do Capítulo 4 devem ser copiados de `audit_tables.md` ou de `audit_data.json`.
Nenhum valor quantitativo deve ser reconstruído a partir de memória ou conversa.

Se houver qualquer `FAIL`, o conjunto não deve ser considerado auditado.
WARN exige revisão antes de repetir a afirmação correspondente no manuscrito.

As execuções Antigravity são auditadas como evidência operacional, mas permanecem fora da análise quantitativa primária.

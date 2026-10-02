# A2 Quality + A4 Security report — v2

- Workspace: `/home/alunos/Documentos/Juan/usp/financial-architecture-benchmark/multiagent-experiment/runs/P0007_monolith/workspace`
- Docker context: `desktop-linux`
- Trivy image: `aquasec/trivy:0.74.0`
- Network during A4: **disabled**
- Trivy offline scan: **enabled**

## A2 — Quality

- Physical Java LOC: **315**
- Java files: **6**
- Lizard NLOC: **274**
- Functions/methods: **11**
- CC total: **30**
- CC average: **2.727273**
- CC max: **11**
- Quality findings: **1**

## A4 — Security

- Built JAR artifacts scanned: **1**
- Vulnerabilities: **0**
- Secrets: **0**
- Misconfigurations: **0**
- HIGH: **0**
- CRITICAL: **0**

### Artifacts

- `financial-monolith/target/financial-monolith-0.0.1-SNAPSHOT.jar` — `8c46c4086b9a1741eb2b7ada9899c68c41e5ba25ac4313b361d414cc2d702f8d` (56627064 bytes)

## Findings for A3

- Total: **1**

| Agent | Type | Severity | Target | Detail |
|---|---|---|---|---|
| A2 | QUALITY_HIGH_CCN | HIGH | financial-monolith/src/main/java/com/juanesteban/tcc/finance/FinanceService.java | FinanceService::trade( boolean buy , UUID userId , String symbol , Integer shares) |

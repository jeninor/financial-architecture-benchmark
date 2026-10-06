# A2 Quality + A4 Security report — v2

- Workspace: `/home/alunos/Documentos/Juan/usp/financial-architecture-benchmark/multiagent-provider-experiment/runs/RH0003_claude_microservices/a3/workspace`
- Docker context: `desktop-linux`
- Trivy image: `aquasec/trivy:0.74.0`
- Network during A4: **disabled**
- Trivy offline scan: **enabled**

## A2 — Quality

- Physical Java LOC: **631**
- Java files: **15**
- Lizard NLOC: **526**
- Functions/methods: **33**
- CC total: **58**
- CC average: **1.757576**
- CC max: **7**
- Quality findings: **0**

## A4 — Security

- Built JAR artifacts scanned: **5**
- Vulnerabilities: **0**
- Secrets: **0**
- Misconfigurations: **0**
- HIGH: **0**
- CRITICAL: **0**

### Artifacts

- `api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar` — `683a3cf3cf345b564c492f7b93c84e9624faae54145ac518430789b584e78a0b` (50761127 bytes)
- `discovery-server/target/discovery-server-0.0.1-SNAPSHOT.jar` — `4db482919571df906c2df30f2df4c04d2a6f183ba610e51549e367569b6aa514` (58184441 bytes)
- `market-service/target/market-service-0.0.1-SNAPSHOT.jar` — `3b6395369411d9aef709c35d36fed1db838a4e989d9329d6b67685f3bb3c2300` (46849317 bytes)
- `trade-service/target/trade-service-0.0.1-SNAPSHOT.jar` — `7c51e5d5bfe274df0ae96eddcf94df85fe03a7d28ea481cef33fd23e8e6bcd03` (83629483 bytes)
- `user-service/target/user-service-0.0.1-SNAPSHOT.jar` — `55c0cbd66e8822f46f816d9d7b2f4813a37b8c7d9ad222e56b9fb10ca59cb8e5` (82124250 bytes)

## Findings for A3

- Total: **0**

| Agent | Type | Severity | Target | Detail |
|---|---|---|---|---|

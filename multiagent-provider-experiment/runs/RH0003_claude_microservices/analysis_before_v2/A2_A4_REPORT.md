# A2 Quality + A4 Security report — v2

- Workspace: `/home/alunos/Documentos/Juan/usp/financial-architecture-benchmark/multiagent-provider-experiment/runs/RH0003_claude_microservices/workspace`
- Docker context: `desktop-linux`
- Trivy image: `aquasec/trivy:0.74.0`
- Network during A4: **disabled**
- Trivy offline scan: **enabled**

## A2 — Quality

- Physical Java LOC: **616**
- Java files: **15**
- Lizard NLOC: **515**
- Functions/methods: **29**
- CC total: **54**
- CC average: **1.862069**
- CC max: **12**
- Quality findings: **1**

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
- `trade-service/target/trade-service-0.0.1-SNAPSHOT.jar` — `1661a2ac27ef314ae5a68e29aef5a73441a1ebd1eedb2ee219708f7060c09e03` (83629248 bytes)
- `user-service/target/user-service-0.0.1-SNAPSHOT.jar` — `dcdda3139e8c996b85f3763dcf769e3395ebb743c3dd397fbc64ceb269976dc1` (82124250 bytes)

## Findings for A3

- Total: **1**

| Agent | Type | Severity | Target | Detail |
|---|---|---|---|---|
| A2 | QUALITY_HIGH_CCN | HIGH | trade-service/src/main/java/com/juanesteban/tcc/trade/TradeController.java | TradeController::execute( String type , TradeRequest req) |

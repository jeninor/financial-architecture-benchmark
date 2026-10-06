# A2 Quality + A4 Security report — v2

- Workspace: `/home/alunos/Documentos/Juan/usp/financial-architecture-benchmark/multiagent-experiment/runs/R0003_microservices/workspace`
- Docker context: `desktop-linux`
- Trivy image: `aquasec/trivy:0.74.0`
- Network during A4: **disabled**
- Trivy offline scan: **enabled**

## A2 — Quality

- Physical Java LOC: **577**
- Java files: **14**
- Lizard NLOC: **474**
- Functions/methods: **24**
- CC total: **48**
- CC average: **2.0**
- CC max: **10**
- Quality findings: **0**

## A4 — Security

- Built JAR artifacts scanned: **5**
- Vulnerabilities: **0**
- Secrets: **0**
- Misconfigurations: **0**
- HIGH: **0**
- CRITICAL: **0**

### Artifacts

- `api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar` — `e2aa0e039fec46b97483b31c2430fc48a26edc332dbc0ae5fea2f35fe8599ce6` (50761118 bytes)
- `discovery-server/target/discovery-server-0.0.1-SNAPSHOT.jar` — `615fc4d87a9852eda5aa0fbb52e5459044d77093c5c199c84a51133d4c6aac53` (58184441 bytes)
- `market-service/target/market-service-0.0.1-SNAPSHOT.jar` — `aaf8bdcc68ba6f6ee59a8c87ab2775fd8ec47509cfb0d81f9a3df6277288034e` (46849079 bytes)
- `trade-service/target/trade-service-0.0.1-SNAPSHOT.jar` — `e86e79279c83bae6ec17c9f10c22e2c9328c1633f7e68d2eab692a5ae5a875ce` (83626155 bytes)
- `user-service/target/user-service-0.0.1-SNAPSHOT.jar` — `a9f2774220ae68f79ece2cdbeb377156f36f9b58e81897e661c81d2874d6dd2a` (82121308 bytes)

## Findings for A3

- Total: **0**

| Agent | Type | Severity | Target | Detail |
|---|---|---|---|---|

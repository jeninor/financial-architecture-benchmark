# A2 Quality + A4 Security report — v2

- Workspace: `/home/alunos/Documentos/Juan/usp/financial-architecture-benchmark/multiagent-provider-experiment/runs/RC0003_codex_microservices/workspace`
- Docker context: `desktop-linux`
- Trivy image: `aquasec/trivy:0.74.0`
- Network during A4: **disabled**
- Trivy offline scan: **enabled**

## A2 — Quality

- Physical Java LOC: **530**
- Java files: **16**
- Lizard NLOC: **447**
- Functions/methods: **21**
- CC total: **40**
- CC average: **1.904762**
- CC max: **8**
- Quality findings: **0**

## A4 — Security

- Built JAR artifacts scanned: **5**
- Vulnerabilities: **0**
- Secrets: **0**
- Misconfigurations: **0**
- HIGH: **0**
- CRITICAL: **0**

### Artifacts

- `api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar` — `20ca588721f04531da0a6ace24cee9f4668679fca81361c6cac179555f199aeb` (50761128 bytes)
- `discovery-server/target/discovery-server-0.0.1-SNAPSHOT.jar` — `8ec4d290ffea614d1606e2595a91cc4ed13ec02313e21ef105778b108c4a542e` (58184441 bytes)
- `market-service/target/market-service-0.0.1-SNAPSHOT.jar` — `fd3279488c14bb6268c713e8418299ce84968b1eb592dbaa0a316bf1cc7eae60` (46850159 bytes)
- `trade-service/target/trade-service-0.0.1-SNAPSHOT.jar` — `32a418bf67990225e472cd55ba5e361eda46356c70ff856ec923e19bd6588d0c` (83631069 bytes)
- `user-service/target/user-service-0.0.1-SNAPSHOT.jar` — `e5d32503d91508363e55bfb60f6c4aecf674c16312f490915d17bb6f3adf5a7b` (83632301 bytes)

## Findings for A3

- Total: **0**

| Agent | Type | Severity | Target | Detail |
|---|---|---|---|---|

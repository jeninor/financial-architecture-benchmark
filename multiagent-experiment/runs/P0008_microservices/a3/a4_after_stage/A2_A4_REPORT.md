# A2 Quality + A4 Security report — v2

- Workspace: `/home/alunos/Documentos/Juan/usp/financial-architecture-benchmark/multiagent-experiment/runs/P0008_microservices/a3/workspace`
- Docker context: `desktop-linux`
- Trivy image: `aquasec/trivy:0.74.0`
- Network during A4: **disabled**
- Trivy offline scan: **enabled**

## A2 — Quality

- Physical Java LOC: **678**
- Java files: **16**
- Lizard NLOC: **543**
- Functions/methods: **23**
- CC total: **47**
- CC average: **2.043478**
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

- `api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar` — `64d9799ccb71bd595b0eee7acf6fd7c6086693d84728d13b288f032edbc1bd6e` (50761194 bytes)
- `discovery-server/target/discovery-server-0.0.1-SNAPSHOT.jar` — `1630f6866f7620fa673c4e2c2489035ddd406ee3d41ad10f6646fe6cedbcc818` (58184441 bytes)
- `market-service/target/market-service-0.0.1-SNAPSHOT.jar` — `bdcb04a218ffa82650517a9be7b6de1a1c684d478391fc6e1553c76792186a72` (46850232 bytes)
- `trade-service/target/trade-service-0.0.1-SNAPSHOT.jar` — `7191e5ad82b18ff44971a0da71da288d40e9956fafb1e14001ea95963ca6cc33` (83638723 bytes)
- `user-service/target/user-service-0.0.1-SNAPSHOT.jar` — `d22bd469bf93587140fe408fe19e475c584101c74e30af4c4d80bc877038bd97` (82125676 bytes)

## Findings for A3

- Total: **0**

| Agent | Type | Severity | Target | Detail |
|---|---|---|---|---|

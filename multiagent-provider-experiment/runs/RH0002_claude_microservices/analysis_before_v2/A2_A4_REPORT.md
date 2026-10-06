# A2 Quality + A4 Security report — v2

- Workspace: `/home/alunos/Documentos/Juan/usp/financial-architecture-benchmark/multiagent-provider-experiment/runs/RH0002_claude_microservices/workspace`
- Docker context: `desktop-linux`
- Trivy image: `aquasec/trivy:0.74.0`
- Network during A4: **disabled**
- Trivy offline scan: **enabled**

## A2 — Quality

- Physical Java LOC: **599**
- Java files: **16**
- Lizard NLOC: **513**
- Functions/methods: **26**
- CC total: **70**
- CC average: **2.692308**
- CC max: **13**
- Quality findings: **2**

## A4 — Security

- Built JAR artifacts scanned: **5**
- Vulnerabilities: **0**
- Secrets: **0**
- Misconfigurations: **0**
- HIGH: **0**
- CRITICAL: **0**

### Artifacts

- `api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar` — `5f24a13312d4d526b35e078d0d0f4ab0244783844f2bff4ac0322c16898f1b6e` (50761126 bytes)
- `discovery-server/target/discovery-server-0.0.1-SNAPSHOT.jar` — `882406958da3cbe323e85faac8b2eae081a6f40d0c38b716518724c326af2d61` (58184441 bytes)
- `market-service/target/market-service-0.0.1-SNAPSHOT.jar` — `152d338c602b96424b6dab2038220783458b58a6dc8f09a7ba1d2725eca5e527` (46849149 bytes)
- `trade-service/target/trade-service-0.0.1-SNAPSHOT.jar` — `985b3dd7fa7af79aad341bcaa2bbe01cca235940e02eecb77edf2202b61b7d03` (83629587 bytes)
- `user-service/target/user-service-0.0.1-SNAPSHOT.jar` — `2c0ca5f7445997ce153f3195a44de7f84427c6556931e5a281a43ae6c3843835` (82121616 bytes)

## Findings for A3

- Total: **2**

| Agent | Type | Severity | Target | Detail |
|---|---|---|---|---|
| A2 | QUALITY_HIGH_CCN | HIGH | trade-service/src/main/java/com/juanesteban/tcc/trade/TradeController.java | TradeController::trade( String type , TradeRequest r) |
| A2 | QUALITY_HIGH_CCN | HIGH | user-service/src/main/java/com/juanesteban/tcc/user/UserController.java | UserController::apply( @ PathVariable String id , @ RequestBody Apply req) |

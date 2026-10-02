# Revisão por IA - Agente 4 (Trivy)

## Rastreabilidade

| Item | Valor |
|---|---|
| Monólito | `metrics/trivy_monolito_20261001_192804.json` (`CreatedAt` 2026-10-01T22:29:29Z) |
| Microsserviços | `metrics/trivy_microservicos_20261001_192804.json` (`CreatedAt` 2026-10-01T22:30:25Z) |
| Pareamento | **mesmo timestamp** (`192804`, mesma execução de `infra/scripts/run_trivy.sh`) |
| Trivy | 0.75.0, `trivy fs --format json`, analisador `pom` sobre `~/.m2` pré-populado (sem 429 nesta execução) |
| Script de consolidação | `infra/scripts/agente4_analise_trivy.py` (somente stdlib: `json`, `glob`, `re`, `collections`) |
| Dados completos gerados pelo script | `metrics/agente4_analise_20261001_192804.json` |

Alvos analisados: no monólito, 1 `pom.xml`; nos microsserviços, 5 `pom.xml`
(`api-gateway`, `eureka-server`, `market-service`, `trade-service`, `user-service`). A
atribuição de cada pacote vulnerável à dependência direta de origem foi feita pelo script,
percorrendo o grafo `Packages[].DependsOn` que o próprio Trivy exporta (raiz → `direct` →
`indirect`), e não por inferência.

**Determinismo e diferença em relação às execuções anteriores.** As três execuções
anteriores do monólito (`120623`, `124850`, `132208`) têm contagem idêntica (7/31/32/14).
A execução atual traz 7/**33**/32/14. O script identificou a diferença: duas CVEs novas
(CVE-2026-89425 e CVE-2026-89407, ambas HIGH em `jackson-core` 2.17.2, negação de
serviço). Elas também aparecem nos 5 módulos dos microsserviços (HIGH: 192 → 202 = +2 × 5).

Os `pom.xml` não mudaram de conteúdo entre as execuções. A data de modificação deles é a
da restauração feita por `run_agente2.sh`. A variação vem, portanto, da **atualização da
base de vulnerabilidades do Trivy**, e não do código. O scan continua determinístico para
uma mesma base. Ao comparar execuções em datas diferentes, deve-se registrar a data da
base, e não só a do scan.

## Resumo quantitativo

| Severidade | Monólito (bruto) | Microsserviços (bruto) | Monólito (CVEs distintos) | Microsserviços (CVEs distintos) |
|---|---|---|---|---|
| CRITICAL | 7 | 40 | 7 | 11 |
| HIGH | 33 | 202 | 33 | 66 |
| MEDIUM | 32 | 207 | 31 | 67 |
| LOW | 14 | 74 | 14 | 18 |
| **Total** | **86** | **523** | **85** | **162** |

Ocorrências brutas por módulo nos microsserviços: api-gateway 107, eureka-server 95,
market-service 99, trade-service 111, user-service 111.

Leitura: a razão bruta é de **6,1×** (523/86), mas a razão em CVEs distintos é de **1,9×**
(162/85). A maior parte da diferença bruta é a mesma CVE contada uma vez por módulo, porque
os cinco serviços repetem a mesma baseline Spring Boot. No monólito, bruto e distinto
praticamente coincidem: a única diferença é uma CVE MEDIUM que afeta dois pacotes.

Conjuntos por `VulnerabilityID`:

| Conjunto | CVEs distintos | CRITICAL | HIGH | MEDIUM | LOW |
|---|---|---|---|---|---|
| Interseção (ambas) | 85 | 7 | 33 | 31 | 14 |
| Exclusivos de microsserviços | 77 | 4 | 33 | 36 | 4 |
| Exclusivos do monólito | **0** | 0 | 0 | 0 | 0 |

## CVEs compartilhados (baseline comum)

**Todas as 85 CVEs do monólito também ocorrem nos microsserviços.** O monólito é um
subconjunto estrito. Isso é coerente com o grafo de dependências: as três dependências
diretas do monólito (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`,
`postgresql`) reaparecem em market-service, trade-service e user-service, nas mesmas
versões geridas pelo BOM do Spring Boot 3.3.4.

Pacotes que concentram a interseção (contagem de CVEs distintos por pacote):
`tomcat-embed-core` 10.1.30 (33), `spring-webmvc` 6.1.13 (13), `jackson-databind` 2.17.2
(11), `logback-core` 1.5.8 (6), `jackson-core` 2.17.2 (4), `spring-data-commons`,
`spring-expression` e `postgresql` 42.7.4 (3 cada).

Amostra (mesma versão instalada nos dois lados):

| CVE | Severidade | Pacote | Versão (mono = micro) | Origem |
|---|---|---|---|---|
| CVE-2025-24813 | CRITICAL | `org.apache.tomcat.embed:tomcat-embed-core` | 10.1.30 | `spring-boot-starter-web` |
| CVE-2026-43512 | CRITICAL | `org.apache.tomcat.embed:tomcat-embed-core` | 10.1.30 | `spring-boot-starter-web` |
| CVE-2026-41293 | CRITICAL | `org.apache.tomcat.embed:tomcat-embed-core` | 10.1.30 | `spring-boot-starter-web` |
| CVE-2026-89425 | HIGH | `com.fasterxml.jackson.core:jackson-core` | 2.17.2 | `spring-boot-starter-web` (Jackson) |
| CVE-2024-12798 | MEDIUM | `ch.qos.logback:logback-core` | 1.5.8 | `spring-boot-starter` (logging) |

A amostra confirma que a interseção vem do servidor embarcado, do Spring MVC, do Jackson e
do logging, ou seja, da baseline comum às duas arquiteturas, e não do código gerado.

## CVEs exclusivos de microsserviços

77 CVEs distintos (4 CRITICAL, 33 HIGH, 36 MEDIUM, 4 LOW). Nenhum pacote abaixo existe no
grafo do monólito. Agrupando cada CVE pela dependência direta que a introduz (sem dupla
contagem):

| Dependência de infraestrutura distribuída (origem) | Módulos | CVEs distintos | CRITICAL | HIGH | MEDIUM | LOW |
|---|---|---|---|---|---|---|
| `spring-cloud-starter-gateway` (Netty, Reactor Netty, WebFlux, Gateway Server) | api-gateway | 49 | 1 | 20 | 26 | 2 |
| `spring-cloud-starter-netflix-eureka-client` / `-server` | todos | 11 | 1 | 5 | 4 | 1 |
| `spring-boot-starter-amqp` (cliente RabbitMQ, Spring AMQP, Spring Retry) | trade-service, user-service | 10 | 0 | 4 | 5 | 1 |
| Vários starters Spring Cloud (`bcprov-jdk18on`, `spring-security-crypto`) | todos | 5 | 2 | 2 | 1 | 0 |
| `spring-cloud-starter-openfeign` (`commons-fileupload`, `commons-io`) | trade-service, user-service | 2 | 0 | 2 | 0 | 0 |
| **Total** | | **77** | **4** | **33** | **36** | **4** |

Detalhe por pacote (CVEs distintos; C/H/M/L):

| Pacote | Versão | CVEs | C/H/M/L | Origem |
|---|---|---|---|---|
| `io.netty:netty-codec-http` | 4.1.113.Final | 18 | 0/6/11/1 | gateway |
| `io.netty:netty-codec-http2` | 4.1.113.Final | 8 | 0/4/4/0 | gateway |
| `com.rabbitmq:amqp-client` | 5.21.0 | 7 | 0/4/2/1 | amqp |
| `io.netty:netty-handler` | 4.1.113.Final | 6 | 1/4/1/0 | gateway |
| `org.bouncycastle:bcprov-jdk18on` | 1.78 | 4 | 2/1/1/0 | gateway, eureka, openfeign |
| `io.netty:netty-codec`, `netty-resolver-dns` | 4.1.113.Final | 3 cada | 0/2/1/0 | gateway |
| `org.springframework:spring-webflux` | 6.1.13 | 3 | 0/0/3/0 | gateway |
| `io.netty:netty-codec-dns`, `netty-common` | 4.1.113.Final | 2 cada | — | gateway |
| `org.springframework.cloud:spring-cloud-gateway-server` | 4.1.5 | 2 | 0/2/0/0 | gateway |
| `org.springframework.amqp:spring-amqp` | 3.1.7 | 2 | 0/0/2/0 | amqp |
| `org.freemarker:freemarker` | 2.3.33 | 1 | 1/0/0/0 | eureka-server |
| `com.thoughtworks.xstream:xstream` | 1.4.20 | 1 | 0/1/0/0 | eureka |
| `org.apache.httpcomponents.core5:httpcore5`, `httpcore5-h2` | 5.2.5 | 1 cada | 0/1/0/0 | eureka |
| `io.micrometer:micrometer-core` | 1.13.4 | 1 | 0/1/0/0 | eureka-server |
| `org.springframework.boot:spring-boot-starter-actuator` | 3.3.4 | 1 | 0/1/0/0 | eureka-server |
| `org.springframework.security:spring-security-crypto` | 6.3.3 | 1 | 0/1/0/0 | vários |
| `commons-fileupload`, `commons-io` | 1.5 / 2.11.0 | 1 cada | 0/1/0/0 | openfeign |
| demais (`woodstox-core`, `commons-lang`, `commons-configuration`, `httpclient` 4.5.3, `httpclient5`, `reactor-netty-http`, `spring-retry`, `netty-handler-proxy`, `netty-transport-native-epoll`) | — | 1 cada | MEDIUM/LOW | eureka, gateway, amqp |

**Exclusivos do monólito:** nenhum. Não há dependência no monólito que os microsserviços
não tenham.

**Observação de higiene de dependências.** O `user-service` declara
`spring-boot-starter-amqp` e `spring-cloud-starter-openfeign` (com `@EnableFeignClients`),
mas não usa nenhum dos dois: não há `RabbitTemplate`, `@RabbitListener` nem
`@FeignClient` no código. Essas dependências vêm do esqueleto e expõem o serviço às 12 CVEs
de AMQP e OpenFeign sem nenhum benefício funcional. Elas não alteram a contagem de CVEs
distintos da arquitetura, porque o trade-service usa ambas, mas ampliam a superfície de um
serviço que lida com saldo. A seção "Leitura para o TCC" explica por que isso é tratado
como limitação do esqueleto experimental, e não como defeito a corrigir.

## Acionáveis (CRITICAL/HIGH com correção disponível)

Dentro dos exclusivos de microsserviços, há **18 pacotes** com CVE CRITICAL/HIGH e
`FixedVersion` concreta. A versão-alvo é a menor que corrige **todas** as CVEs
CRITICAL/HIGH do pacote, priorizando a mesma linha da versão instalada. A viabilidade foi
avaliada contra a especificação do Agente 1: Java 21, Spring Boot 3.3.4 e Spring Cloud
2023.0.3, mantendo o parent e o release train.

| Pacote | Atual → corrigida | Sev. (nº CVEs) | Módulos | Viável sem violar a especificação? |
|---|---|---|---|---|
| `io.netty:netty-handler` | 4.1.113 → 4.1.137.Final | CRITICAL+HIGH (5) | api-gateway | **Sim.** Patch na linha 4.1, aplicado com uma única propriedade `netty.version=4.1.137.Final` no `api-gateway/pom.xml`. Essa propriedade é gerida pelo BOM do Boot e mantém o parent 3.3.4. |
| `io.netty:netty-codec-http` | 4.1.113 → 4.1.136.Final | HIGH (6) | api-gateway | **Sim.** Coberto pela mesma propriedade `netty.version` acima. |
| `io.netty:netty-codec-http2` | 4.1.113 → 4.1.136.Final | HIGH (4) | api-gateway | **Sim.** Mesma propriedade. |
| `io.netty:netty-codec` | 4.1.113 → 4.1.136.Final | HIGH (2) | api-gateway | **Sim.** Mesma propriedade. |
| `io.netty:netty-resolver-dns` | 4.1.113 → 4.1.135.Final | HIGH (2) | api-gateway | **Sim.** Mesma propriedade. |
| `io.netty:netty-codec-dns` | 4.1.113 → 4.1.133.Final | HIGH (1) | api-gateway | **Sim.** Mesma propriedade. |
| `org.freemarker:freemarker` | 2.3.33 → 2.3.35 | CRITICAL (1) | eureka-server | **Sim.** Patch, via propriedade `freemarker.version` gerida pelo BOM do Boot. |
| `org.springframework.security:spring-security-crypto` | 6.3.3 → 6.3.8 | HIGH (1) | todos | **Sim.** Patch na linha 6.3, a mesma que o Boot 3.3.x gerencia, via `spring-security.version`. |
| `com.thoughtworks.xstream:xstream` | 1.4.20 → 1.4.21 | HIGH (1) | todos | **Sim.** Patch. O artefato não é gerido pelo BOM do Boot, então a versão é fixada em `<dependencyManagement>`. |
| `com.rabbitmq:amqp-client` | 5.21.0 → 5.34.0 | HIGH (4) | trade, user | **Viável com teste.** Minor dentro da 5.x, via propriedade `rabbit-amqp-client.version`. O Spring AMQP 3.1.x declara compatibilidade com o cliente 5.x. No user-service, a dependência não é usada (ver limitação do esqueleto em "Leitura para o TCC"). |
| `commons-io:commons-io` | 2.11.0 → 2.14.0 | HIGH (1) | trade, user | **Viável com teste.** Minor, retrocompatível, fixado em `<dependencyManagement>`. |
| `commons-fileupload:commons-fileupload` | 1.5 → 1.6.0 | HIGH (1) | trade, user | **Viável com teste.** Minor, retrocompatível, fixado em `<dependencyManagement>`. Vem do suporte a formulários do OpenFeign, que o projeto não usa. |
| `org.bouncycastle:bcprov-jdk18on` | 1.78 → 1.85 | CRITICAL+HIGH (3) | todos | **Viável com teste.** A Bouncy Castle versiona por releases sequenciais com API estável, e o artefato não é gerido pelo BOM. A correção mínima para todas as CVEs é 1.85; as linhas 1.80.2, 1.81.1 e 1.84 corrigem apenas parte delas. |
| `org.apache.httpcomponents.core5:httpcore5` e `httpcore5-h2` | 5.2.5 → 5.4.3 | HIGH (1 cada) | todos | **Requer avaliação.** Salto de duas minors num artefato gerido pelo Boot, que também exige alinhar o `httpclient5` (5.3.1). Não viola a especificação formalmente, mas é a mudança de maior risco entre as viáveis. |
| `io.micrometer:micrometer-core` | 1.13.4 → 1.15.12 | HIGH (1) | eureka-server | **Não.** Só há correção nas linhas 1.15 e 1.16, que pertencem ao Boot 3.5 e posteriores. Forçar essa versão sob o Boot 3.3.4 descasa a autoconfiguração do Actuator. |
| `org.springframework.boot:spring-boot-starter-actuator` | 3.3.4 → 3.5.12 | HIGH (1) | eureka-server | **Não.** Exige mudar a versão do Spring Boot, o que viola a especificação. Mitigação possível sem upgrade: restringir a exposição de endpoints do Actuator no eureka-server. |
| `org.springframework.cloud:spring-cloud-gateway-server` | 4.1.5 → 4.2.6 | HIGH (2) | api-gateway | **Não (parcial).** Uma das CVEs tem correção em 4.1.8, mas fora do que o release train 2023.0.3 declara. A outra só é corrigida a partir de 4.2.x, que pertence ao Spring Cloud 2024.0 e exige Boot 3.4. As duas violam a especificação. |

Síntese: dos 18 pacotes acionáveis, **9 podem ser corrigidos com segurança** sem violar a
especificação: 6 da Netty (com uma única propriedade), freemarker, spring-security-crypto
e xstream. Esses 9 eliminam 22 das 37 CVEs CRITICAL/HIGH exclusivas, inclusive 2 das 4
CRITICAL. Outros **6 são viáveis com teste de regressão** (+11 CVEs): amqp-client,
commons-io, commons-fileupload, bcprov e os dois httpcore5. Os **3 restantes não são
corrigíveis** dentro da especificação (4 CVEs): micrometer, actuator e gateway-server.
Essas três ficam como risco residual documentado. Todas as 37 CVEs CRITICAL/HIGH
exclusivas têm alguma `FixedVersion` publicada: o limite à correção vem da especificação
de versões, e não da falta de correção.

Para referência (fora do escopo "exclusivo", mas relevante para a simetria do Agente 3),
as CVEs compartilhadas têm 10 pacotes acionáveis:
- **Seguros em ambas as arquiteturas:** `tomcat-embed-core` 10.1.30 → 10.1.58 (patch, via
  `tomcat.version`, 21 CVEs CRITICAL/HIGH) e `postgresql` 42.7.4 → 42.7.12 (patch).
- **Exigem avaliação ou violam a especificação:** Jackson 2.17 → 2.18 (minor),
  `spring-core`, `spring-webmvc`, `spring-webflux` e `spring-expression` → 6.2.x (linha do
  Boot 3.4), e `spring-boot`/`spring-data-commons` → 3.5.x.

Se aplicadas, as correções compartilhadas precisam ir para os dois lados, para não
distorcer a comparação.

## Leitura para o TCC

A diferença bruta (523 contra 86 ocorrências) superestima a diferença real de exposição.
Em CVEs distintos, a razão cai de 6,1× para 1,9× (162 contra 85), porque cinco módulos
repetem a mesma baseline Spring Boot e o Trivy conta cada ocorrência por `pom.xml`. O
monólito é um subconjunto estrito dos microsserviços: as 85 CVEs dele estão todas do outro
lado, e não há nenhuma exclusiva sua. O acréscimo de 77 CVEs distintos é, portanto, o
**custo de segurança da distribuição** propriamente dito, e é atribuível
deterministicamente, pelo grafo de dependências, a cinco starters de infraestrutura que o
monólito não precisa (gateway, Eureka client e server, OpenFeign e AMQP).

O acréscimo não é distribuído de forma proporcional entre eles. Sozinho, o
`spring-cloud-starter-gateway` responde por 49 das 77 CVEs (64%), por trazer uma pilha
HTTP inteira (Netty, Reactor Netty e WebFlux) paralela ao Tomcat. Esse é o ponto
desproporcional que merece destaque, porque o gateway é também o único componente que
recebe tráfego de borda. O Eureka acrescenta 11 CVEs, o AMQP 10 e o OpenFeign 2, montantes
compatíveis com o porte dessas bibliotecas.

A contagem também não deve ser lida como medida direta de "pior qualidade" do código
gerado. Nenhuma dessas CVEs está no código das aplicações: todas vêm de bibliotecas de
terceiros escolhidas pelo esqueleto e pela especificação de versões. Além disso, muitas
não são exploráveis no contexto deste sistema, que é uma API interna, sem exposição direta
à internet e sem processar entrada não confiável de certos tipos. Exemplos, a confirmar por
teste de exploração, que esta revisão não realizou:
- as CRITICAL do Tomcat dependem de PUT parcial com servlet padrão gravável, de
  autenticação *digest* ou de HTTP/2, e nada disso está habilitado;
- o FreeMarker do eureka-server só renderiza templates internos do painel;
- o XStream desserializa respostas do próprio servidor Eureka, numa rede interna
  confiável;
- o Bouncy Castle não é usado diretamente pela aplicação.

O que a análise sustenta é uma afirmação mais estreita: a arquitetura distribuída
**multiplica a superfície de dependências**. Isso aumenta tanto a exposição potencial
quanto o esforço de manutenção de segurança, já que há mais artefatos a acompanhar e mais
`pom.xml` a atualizar de forma consistente, mesmo quando o risco efetivo de cada CVE
isolada é baixo.

**Nota — limitação do esqueleto experimental (dependências não usadas no user-service).**
O `user-service` declara `spring-boot-starter-amqp` e `spring-cloud-starter-openfeign`
no `pom.xml`, mas o código de negócio não usa nenhum dos dois: não há `RabbitTemplate`,
`@RabbitListener` nem `@FeignClient`, e o `@EnableFeignClients` da classe principal não
tem clientes a registrar. Com isso, o serviço fica exposto a **12 CVEs** (10 de AMQP e 2
de OpenFeign) sem nenhum benefício funcional.

Essas dependências **não foram removidas** porque pertencem ao esqueleto de
infraestrutura (`pom.xml`). O `AGENTE1_ESPECIFICACAO.md` (linhas 14-15) descreve esse
esqueleto como "já existente e já validado (build, rede entre containers e healthchecks
funcionando)". Ele está, portanto, fora do escopo de geração (Agente 1) e de refatoração
(Agente 3), que atuam sobre a lógica de negócio. Alterar o `pom.xml` mudaria a baseline
comum às execuções e comprometeria a comparabilidade entre as arquiteturas.

O achado é registrado como **limitação do esqueleto experimental, e não como defeito a
corrigir**. Ele também não afeta a contagem de CVEs distintos da arquitetura de
microsserviços, porque o trade-service usa legitimamente as duas dependências e já
contribui com as mesmas 12 CVEs. O efeito se limita à superfície de ataque do próprio
user-service.

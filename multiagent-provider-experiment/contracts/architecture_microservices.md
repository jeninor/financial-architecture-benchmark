# Architecture contract — Microservices

This file defines the architecture constraint for the MICROservices arm of the
controlled experiment. The baseline `docker-compose.yml` is authoritative and
its service topology must not be changed by A1 or A3.

## Architectural style

The application must be implemented as multiple independently running Spring
Boot services behind a single API Gateway.

The prepared baseline contains the architectural roles below. Exact Compose
service names, mounts, images, environment variables, ports and dependency
wiring in the baseline take precedence and must be preserved.

Expected application roles:

- `discovery-server` — Eureka service registry, public/admin port 8761.
- `api-gateway` — single public application entry point on port 8080.
- `user-service` — user, cash balance and portfolio/position ownership,
  internal service port 8081.
- `market-service` — fixed quote catalog, internal service port 8082.
- `trade-service` — buy/sell orchestration and trade history,
  internal service port 8083.

Expected infrastructure roles include separate persistence for user and trade
state and RabbitMQ. The prepared Compose topology is the canonical source for
the exact infrastructure service names.

The baseline may also contain an `e2e-tests` service. It is part of the frozen
Compose topology but the experiment's immutable black-box acceptance runner is
owned by the orchestrator and is not editable by the agent.

## Service ownership

### market-service

Owns the fixed market quote catalog:

- AAPL = 200.00
- MSFT = 400.00
- GOOGL = 170.00
- AMZN = 190.00
- NVDA = 120.00

It must expose the quote capability used by the public
`GET /api/quotes/{symbol}` route.

### user-service

Owns user/account state and portfolio positions.

At minimum it is responsible for:

- username uniqueness;
- initial cash balance of 10000.00;
- cash balance updates;
- position/holding state;
- portfolio calculation;
- unknown-user semantics.

User/account/position state must use the user-side database defined by the
baseline. Do not store trade history in this database.

### trade-service

Owns trade records/history in its own trade-side database.

It coordinates buy/sell behavior with user-service and market-service using
service-to-service communication already supported by the baseline
(Spring Cloud / OpenFeign / Eureka).

A successful trade must preserve the public contract and record the trade in
trade-service. Rejected trades must not produce a successful trade record.

There is no shared application database between user-service and trade-service.

### api-gateway

Port 8080 is the only public application endpoint used by the immutable
acceptance runner. The gateway must route the public API to the appropriate
service while preserving the public paths and status codes.

### discovery-server

Provides service registration/discovery. Application services that are
configured as Eureka clients must register with it rather than replacing
discovery with hard-coded host networking.

## Messaging

RabbitMQ is part of the fixed microservices architecture.

For the trade-completed integration/audit path, use the prepared messaging
dependencies/configuration and the following stable names when that path is
implemented:

- exchange: `financial.exchange`
- routing/event key: `trade.completed`
- queue: `trade.audit.queue`

Messaging supplements the synchronous business flow; it must not replace the
public HTTP contract.

## Transaction boundary

Do not introduce a shared database, distributed JPA transaction, Saga
framework, or 2PC coordinator.

Each service may use local transactions over the state it owns. Cross-service
coordination is therefore not represented as one ACID transaction spanning
multiple databases.

## Frozen topology

A1 and A3 MUST NOT:

- edit `docker-compose.yml`;
- add or remove Compose services;
- merge the business services into one deployable application;
- create a shared application database;
- bypass the API Gateway for the public contract;
- remove Eureka/discovery when it is present in the baseline;
- remove RabbitMQ from the prepared topology;
- replace the microservices architecture with a modular monolith.

A1/A3 MAY edit Java source, Maven POMs and service configuration files inside
the existing modules when required to implement or repair the application.

## Functional equivalence

The public behavior is defined only by `public_api_v1.md` and the immutable
external acceptance suite. The microservices implementation must satisfy the
same public behavior as the monolithic arm through `http://localhost:8080`.

The historical implementation in `legacy-chatpt` is NOT available to the agent
during a run and must not be used as implementation source code.

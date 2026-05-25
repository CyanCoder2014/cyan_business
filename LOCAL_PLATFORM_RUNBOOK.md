# Local Platform Runbook

This file is for both humans and agents. It explains how to start the Cyan business platform locally, including infrastructure, service order, panel usage, and the new integration-test workspace.

## What This Repo Contains

The repository is a Gradle multi-project microservice platform with:

- ingress and discovery: `api-gateway`, `discovery-server`
- identity: `sso-auth-service`, `sso-user-service`, `sso-session-service`, `sso-otp-service`, `sso-captcha-service`, `sso-fido-service`
- dynamic business services: `content-service`, `catalog-service`, `crm-service`, `commerce-service`, `finance-service`, `inventory-service`, `report-service`, `processor-service`
- experience and revenue services: `storefront-service`, `media-service`, `cart-service`, `checkout-service`, `pricing-promotion-service`, `payment-orchestrator-service`, `payment-service`, `search-index-service`, `notification-service`
- orchestration and automation: `bpm-service`, `event-service`, `automation-orchestrator-service`, `crm-automation-service`, `finance-automation-service`, `inventory-automation-service`, `report-automation-service`, `ai-orchestrator-service`, `bot-adapter-service`
- legacy services: `buyer-service`, `client-service`, `factor-service`, `product-service`, `tax-pay-sys`
- panel frontend: `panel-web`

## Prerequisites

Install locally:

- Java 17
- Node.js 18+ and npm
- Docker and Docker Compose

Recommended:

- `curl`
- `jq`
- `rg`

## Infrastructure Dependencies

### Required for most platform work

Start databases:

```bash
cd docker/databases
docker compose up -d
```

This brings up:

- PostgreSQL on `localhost:5432`
- MongoDB on `localhost:27017`

### Required for event fan-out and automation consumers

Start Kafka:

```bash
cd docker/kafka
docker compose up -d
```

Kafka is exposed on:

- `localhost:9092`

### Optional or scenario-specific

Axon server:

```bash
cd docker/axon
docker compose up -d
```

Ports:

- `8024`
- `8124`
- `8224`

Keycloak:

```bash
cd docker/keycloak
docker compose up -d
```

Important caveat:

- the provided Keycloak compose file references `demo-db` and an old MariaDB-based setup
- treat it as a starting point, not a guaranteed one-command local environment
- if you need Keycloak for SSO flows, review and adjust `docker/keycloak/docker-compose.yml` before relying on it

## Local Database Initialization

PostgreSQL databases are created by:

- [docker/databases/init/01-create-databases.sql](/Users/farid/Projects/naviya/old-cyan/cyan_business/docker/databases/init/01-create-databases.sql:1)

Mongo databases are created lazily by services on first use.

## Startup Order

Use this order:

1. infrastructure: PostgreSQL, MongoDB, Kafka
2. discovery: `discovery-server`
3. auth foundation if needed: `sso-auth-service` and related SSO services
4. gateway: `api-gateway`
5. shared orchestration services: `event-service`, `processor-service`, `automation-orchestrator-service`, `bpm-service`, `ai-orchestrator-service`
6. business domain services: content, catalog, CRM, commerce, finance, inventory, report, storefront, media, cart, checkout, pricing, payment, search, notification
7. automation consumers: CRM/finance/inventory/report automation services
8. channel integrations: `bot-adapter-service`
9. frontend: `panel-web`

## How To Run Any Backend Service

From repo root:

```bash
bash ./gradlew :SERVICE_NAME:bootRun
```

Example:

```bash
bash ./gradlew :discovery-server:bootRun
bash ./gradlew :api-gateway:bootRun
bash ./gradlew :content-service:bootRun
```

### Localdemo profile

Many dynamic services provide `application-localdemo.properties` and can use embedded H2 instead of PostgreSQL for their relational side.

Run with localdemo where available:

```bash
bash ./gradlew :content-service:bootRun --args='--spring.profiles.active=localdemo'
```

Use `localdemo` when:

- you want faster local startup
- you do not need PostgreSQL-specific behavior
- the service has a matching `application-localdemo.properties`

Do not assume every module has a fully equivalent localdemo mode.

## Service Inventory

### Core ingress and orchestration

| Service | Port | Storage |
|---|---:|---|
| `discovery-server` | `8761` | in-memory / service registry |
| `api-gateway` | `8001` | none |
| `ai-orchestrator-service` | `9121` | Mongo |
| `automation-orchestrator-service` | `9120` | Mongo |
| `bpm-service` | `9119` | Mongo + H2 |
| `event-service` | `9109` | H2 |
| `bot-adapter-service` | `9126` | Mongo |

### Dynamic business services

| Service | Port | Storage |
|---|---:|---|
| `content-service` | `9101` | PostgreSQL + Mongo |
| `catalog-service` | `9102` | PostgreSQL + Mongo |
| `crm-service` | `9103` | PostgreSQL + Mongo |
| `commerce-service` | `9104` | PostgreSQL + Mongo |
| `finance-service` | `9105` | PostgreSQL + Mongo |
| `inventory-service` | `9106` | PostgreSQL + Mongo |
| `report-service` | `9107` | PostgreSQL + Mongo |
| `processor-service` | `9108` | H2 |

### Experience and revenue services

| Service | Port | Storage |
|---|---:|---|
| `storefront-service` | `9115` | PostgreSQL + Mongo |
| `media-service` | `9116` | PostgreSQL + Mongo |
| `cart-service` | `9117` | PostgreSQL + Mongo |
| `checkout-service` | `9118` | PostgreSQL + Mongo |
| `payment-service` | `9114` | PostgreSQL |
| `payment-orchestrator-service` | `9123` | PostgreSQL + Mongo |
| `pricing-promotion-service` | `9124` | PostgreSQL + Mongo |
| `search-index-service` | `9125` | PostgreSQL + Mongo |
| `notification-service` | `9122` | PostgreSQL + Mongo |

### Automation consumers

| Service | Port | Storage |
|---|---:|---|
| `crm-automation-service` | `9110` | H2 |
| `finance-automation-service` | `9111` | H2 |
| `inventory-automation-service` | `9112` | H2 |
| `report-automation-service` | `9113` | H2 |

### Identity services

| Service | Port | Storage |
|---|---:|---|
| `sso-auth-service` | `9001` | H2 |
| `sso-user-service` | `9002` | H2 |
| `sso-captcha-service` | `9003` | H2 or in-memory |
| `sso-otp-service` | `9004` | H2 |
| `sso-session-service` | `9005` | H2 |
| `sso-fido-service` | `9006` | service-local |

### Legacy services

| Service | Port | Storage |
|---|---:|---|
| `tax-pay-sys` | `8002` | PostgreSQL + Mongo |
| `factor-service` | `8003` | PostgreSQL + Mongo |
| `buyer-service` | `8004` | PostgreSQL + Mongo |
| `product-service` | `8005` | PostgreSQL + Mongo |
| `client-service` | `8010` | PostgreSQL + Mongo |

## Minimal Platform Set For Current Panel Work

If you only want the panel features added in this session, start at least:

```bash
bash ./gradlew :discovery-server:bootRun
bash ./gradlew :api-gateway:bootRun
bash ./gradlew :content-service:bootRun --args='--spring.profiles.active=localdemo'
bash ./gradlew :storefront-service:bootRun --args='--spring.profiles.active=localdemo'
bash ./gradlew :bpm-service:bootRun --args='--spring.profiles.active=localdemo'
bash ./gradlew :ai-orchestrator-service:bootRun --args='--spring.profiles.active=localdemo'
bash ./gradlew :bot-adapter-service:bootRun --args='--spring.profiles.active=localdemo'
```

This is enough for:

- panel AI draft listing/generation contracts
- website builder publish and storefront preview
- BPM flow builder and managed-object testing
- bot integration save, webhook registration, outbound test message, and delivery log

## Panel Frontend

Run the panel:

```bash
cd panel-web
npm install
npm run dev
```

Default frontend URL:

- `http://localhost:3000`

Expected API base:

- `NEXT_PUBLIC_PLATFORM_API_BASE_URL=http://localhost:8001`

## Panel Routes You Should Use

### Builder routes

- `/projects/new` for AI Studio
- `/maker` for schema builder
- `/data` for record manager
- `/site-builder` for storefront builder
- `/flows` for BPM and automation builder
- `/integrations` for Telegram/Bale and mini app integration setup
- `/qa` for panel integration smoke testing

### What `/qa` does

The QA workspace runs smoke checks against:

- AI draft registry
- storefront resolve
- storefront render
- BPM flow list
- BPM metadata
- BPM managed objects
- bot integrations
- bot outbound delivery history

Use it after you bring core services up to verify that the panel and gateway contracts are working.

## Bot Token Secret Resolution

`bot-adapter-service` now resolves bot tokens through `tokenSecretRef` first.

Supported formats:

- `env://VAR_NAME`
- `file:///absolute/path/to/token.txt`
- `vault://some/logical/key`

### How `vault://...` resolves locally

Resolution order:

1. inline Spring properties map: `ai-orchestrator.bot-secret-values.*`
2. environment variable derived from the ref
3. optional properties file at `ai-orchestrator.bot-secrets-file`
4. legacy fallback to `managedBotToken` only if no `tokenSecretRef` is set

Example ref:

- `vault://bots/retail-demo`

Derived environment variable with default prefix:

- `CYAN_BOT_SECRET_VAULT_BOTS_RETAIL_DEMO`

Example startup:

```bash
export CYAN_BOT_SECRET_VAULT_BOTS_RETAIL_DEMO='123456:telegram-token'
bash ./gradlew :bot-adapter-service:bootRun --args='--spring.profiles.active=localdemo'
```

Optional file-based map:

```properties
vault://bots/retail-demo=123456:telegram-token
vault://bots/support=999999:bale-token
```

Run with:

```bash
bash ./gradlew :bot-adapter-service:bootRun --args='--spring.profiles.active=localdemo --ai-orchestrator.bot-secrets-file=/absolute/path/bot-secrets.properties'
```

## Common Smoke Checks

### Discovery

```bash
curl -I http://localhost:8761
```

### Gateway

```bash
curl -I http://localhost:8001
```

### Storefront render

```bash
curl -s 'http://localhost:8001/public/storefront/render?path=/' \
  -H 'X-Tenant-Key: tenant-demo' \
  -H 'X-Site-Key: site-commerce'
```

### BPM metadata

```bash
curl -s 'http://localhost:8001/endpoint/bpm/metadata/state-actions' \
  -H 'X-Tenant-Key: tenant-demo' \
  -H 'X-Site-Key: site-commerce'
```

### Bot integrations

```bash
curl -s 'http://localhost:8001/endpoint/bot-adapter/integrations?tenantKey=tenant-demo&siteKey=site-commerce'
```

## Known Caveats

- Not every service has a complete `localdemo` mode.
- Some legacy services may expect additional env vars or older infrastructure assumptions.
- Keycloak compose is not fully turnkey in the current repo.
- The panel can verify API contracts, but it does not replace full end-to-end business validation across all modules.
- The bot adapter now supports secret references and outbound delivery logs, but a real external secret manager integration is still not implemented.

## Suggested Daily Workflow

1. Start Docker databases and Kafka.
2. Start `discovery-server`.
3. Start `api-gateway`.
4. Start only the services needed for the feature you are working on.
5. Start `panel-web`.
6. Open `/qa` and run the smoke harness.
7. Use the feature-specific workspace such as `/site-builder`, `/flows`, or `/integrations`.

## For Agents

- Do not assume all services must be started for every task.
- Prefer the minimal service set required by the feature under test.
- Preserve `X-Tenant-Key` and `X-Site-Key` in manual smoke checks where relevant.
- Use the panel `/qa` route for quick contract verification before deeper debugging.
- When changing channel integrations, prefer `tokenSecretRef` over storing token values directly.

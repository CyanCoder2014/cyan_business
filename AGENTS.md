# Platform Agent Guide

## Purpose
This repository is a modular business platform built as Spring Boot microservices around dynamic entity definitions, workflow orchestration, commerce operations, identity, automation, and public experience delivery.

The platform aim is to let a tenant start from zero and grow to a fully operated business stack:

1. bootstrap a tenant, site, and identity boundary
2. define structured entities from service-owned templates
3. capture content, catalog, CRM, cart, checkout, order, invoice, finance, and inventory records
4. route those records through BPM and automation flows
5. expose public storefront, media, search, payment, and notification capabilities
6. aggregate data into reporting and downstream projections

## Platform Shape
The codebase mixes three layers:

- edge and infrastructure: `api-gateway`, `discovery-server`, SSO services
- dynamic business domain services: content, catalog, CRM, commerce, finance, inventory, reporting, storefront, media, cart, checkout, pricing, payment orchestration, search, notification, BPM, AI orchestration
- legacy domain services: `buyer-service`, `client-service`, `factor-service`, `product-service`, `tax-pay-sys`

Shared platform modules that influence behavior:

- `dynamic-entity-core`: structured definition storage, validation, record persistence, endpoint/internal API split
- `sso-common`: shared auth support for SSO services
- `generic`: legacy shared command/query payloads

## Core Architectural Ideas
### 1. Structured Dynamic Runtime
Most new services are not hardcoded CRUD apps. They host entity definitions and records through `dynamic-entity-core`.

Important behavior:

- definitions are stored in PostgreSQL
- records are stored in MongoDB
- every service has its own `dynamic.runtime.service-key`
- templates are published by each service and instantiated into definitions
- requests are strictly validated for missing and extra fields
- `/endpoint/**` is bearer-token facing
- `/internal/**` is basic-auth service-to-service facing

### 2. Tenant and Site Isolation
Several flows are scoped by `X-Tenant-Key` and `X-Site-Key`, especially storefront-style scenarios. Treat tenant/site as first-class routing and persistence dimensions whenever a service already supports them.

### 3. Evented Downstream Automation
Business services write local data and local outbox state, then hand integration events to `event-service`, which republishes to Kafka. Automation consumers build side effects and projections independently.

### 4. BPM as the Business Control Plane
`bpm-service` manages stateful workflows around dynamic or static submissions. It can render the current active form, lock and route managed objects, and call target services for submit actions.

### 5. AI-Assisted Provisioning
`ai-orchestrator-service` can turn prompts or blueprints into platform definitions and records by calling template-backed internal APIs across services.

## Main End-to-End Flow
### Provisioning flow
1. User or AI chooses a business scenario.
2. `ai-orchestrator-service` selects blueprints and service templates.
3. It creates definitions in services such as `content-service`, `catalog-service`, `storefront-service`, `cart-service`, `checkout-service`, `notification-service`, and `bpm-service`.
4. Those definitions become the controlled schema for later records.

### Operational data flow
1. Users create records through endpoint APIs or public experience layers.
2. Services validate payloads using `dynamic-entity-core`.
3. Records are stored in Mongo and definitions remain in PostgreSQL.
4. Services call internal collaborators where orchestration is required, for example pricing, payment, notification, or search sync.

### Business process flow
1. `bpm-service` opens a managed object for a workflow target.
2. The active state references a dynamic entity or a static submit URL.
3. Submit actions persist records and may trigger actions such as notifications or API calls.
4. Workflow state transitions move the object toward terminal status.

### Event and automation flow
1. Source service writes its business record and outbox row.
2. Event is pushed to `event-service`.
3. `event-service` stores the event idempotently and publishes Kafka envelopes.
4. Automation services consume with separate groups.
5. Reporting and projection services build their own read models.

### Public experience flow
1. `storefront-service` resolves tenant/site routes.
2. It fetches target content or product data from referenced services.
3. `media-service` serves public asset metadata and variants.
4. `search-index-service` supports discovery.
5. `cart-service`, `checkout-service`, `pricing-promotion-service`, `payment-orchestrator-service`, and `payment-service` complete purchase flows.

## Domain Map
### Identity and access
- `sso-auth-service`
- `sso-user-service`
- `sso-session-service`
- `sso-otp-service`
- `sso-captcha-service`
- `sso-fido-service`

### Dynamic content and commerce core
- `content-service`
- `catalog-service`
- `crm-service`
- `commerce-service`
- `finance-service`
- `inventory-service`
- `report-service`
- `processor-service`

### Experience and revenue operations
- `storefront-service`
- `media-service`
- `cart-service`
- `checkout-service`
- `pricing-promotion-service`
- `payment-orchestrator-service`
- `payment-service`
- `search-index-service`
- `notification-service`

### Orchestration and intelligence
- `bpm-service`
- `event-service`
- `automation-orchestrator-service`
- `crm-automation-service`
- `finance-automation-service`
- `inventory-automation-service`
- `report-automation-service`
- `ai-orchestrator-service`

### Legacy or specialized business domains
- `buyer-service`
- `client-service`
- `factor-service`
- `product-service`
- `tax-pay-sys`

## Infrastructure Expectations
- service discovery uses Eureka through `discovery-server`
- ingress and route aggregation use `api-gateway`
- most new dynamic services use PostgreSQL plus MongoDB
- SSO services mostly use H2 locally
- legacy services default to PostgreSQL now and may be switched back to MySQL through datasource configuration; some also use MongoDB
- Kafka is used for fan-out automation
- local Docker compose files exist for databases, Kafka, Axon, and Keycloak-related setup

## Working Rules For Agents
- Prefer existing templates and internal APIs over inventing new schemas.
- Preserve the endpoint/internal auth split.
- For new cross-service flows, document the owning service and the call direction.
- Do not bypass `event-service` when the pattern is event fan-out.
- Do not weaken strict validation in dynamic services unless there is a clear platform-level reason.
- Treat tenant/site headers as part of correctness where storefront or scoped definitions are involved.
- Keep new docs and code aligned with actual routes in `api-gateway`.
- Every microservice must keep a unique fixed local `server.port`; do not reuse ports across modules.

## Suggested Reading Order
1. `STRUCTURED_DYNAMIC_PLATFORM.md`
2. `BUSINESS_PLATFORM_SERVICES.md`
3. `BPM_SERVICE_ARCHITECTURE.md`
4. `KAFKA_AUTOMATION_ARCHITECTURE.md`
5. the `AGENTS.md` file inside the target microservice you are changing

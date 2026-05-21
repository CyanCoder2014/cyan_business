# Business Platform Services

## Overview

This repository now includes a separate set of business microservices for building:

- dynamic website
- blog/pages
- shop
- CRM
- orders/invoices
- finance transactions
- dynamic reports
- and future manufacturing/inventory workflows

These services do not modify `Cyan-core` or `Cyan-bpm`.
They live only in this repository and are intended to complement later BPM integration.

There is also a shared runtime submission engine:

- `processor-service`

And a shared integration event hub:

- `event-service`

And a public bot adapter for Telegram/Bale channels:

- `bot-adapter-service`

And Kafka-driven automation consumers:

- `crm-automation-service`
- `finance-automation-service`
- `inventory-automation-service`
- `report-automation-service`

It provides reusable validators and operators for business submissions before persistence.

## Services

### `content-service`

Purpose:

- dynamic website content
- blog posts
- pages
- page/article metadata

Main API:

- `POST /api/content-service/content`
- `GET /api/content-service/content`
- `GET /api/content-service/content/{key}`
- `GET /api/content-service/content/slug/{slug}`

Entity:

- `ContentEntry`

Storage:

- H2 file database `./data/content-db`

### `catalog-service`

Purpose:

- product catalog
- service catalog
- categories and price defaults

Main API:

- `POST /api/catalog-service/items`
- `GET /api/catalog-service/items`
- `GET /api/catalog-service/items/{itemKey}`

Entity:

- `CatalogItem`

Storage:

- H2 file database `./data/catalog-db`

### `crm-service`

Purpose:

- lead records
- contact records
- account/customer-style records

Main API:

- `POST /api/crm-service/records`
- `GET /api/crm-service/records`
- `GET /api/crm-service/records/{recordKey}`

Entity:

- `CrmRecord`

Record type examples:

- `LEAD`
- `CONTACT`
- `ACCOUNT`

Storage:

- H2 file database `./data/crm-db`

### `commerce-service`

Purpose:

- orders
- invoices
- item and total storage

Main API:

- `POST /api/commerce-service/documents`
- `GET /api/commerce-service/documents`
- `GET /api/commerce-service/documents/{documentKey}`

Entity:

- `CommerceDocument`

Document type examples:

- `ORDER`
- `INVOICE`

Storage:

- H2 file database `./data/commerce-db`

### `finance-service`

Purpose:

- payment-like records
- financial transactions
- references to invoices/orders/accounts

Main API:

- `POST /api/finance-service/transactions`
- `GET /api/finance-service/transactions`
- `GET /api/finance-service/transactions/{transactionKey}`

Entity:

- `FinanceTransaction`

Storage:

- H2 file database `./data/finance-db`

### `inventory-service`

Purpose:

- stock records
- warehouse quantities
- future bridge for manufacturing/inventory BPM workflows

Main API:

- `POST /api/inventory-service/items`
- `GET /api/inventory-service/items`
- `GET /api/inventory-service/items/{itemKey}`

Entity:

- `InventoryItem`

Storage:

- H2 file database `./data/inventory-db`

### `report-service`

Purpose:

- dynamic reports
- dynamic filter execution
- cross-service report queries

Main API:

- `POST /api/report-service/reports`
- `GET /api/report-service/reports`
- `GET /api/report-service/reports/{reportKey}`
- `POST /api/report-service/reports/{reportKey}/run`

Entity:

- `ReportDefinition`

Capabilities:

- fetches data from:
  - `content-service`
  - `catalog-service`
  - `crm-service`
  - `commerce-service`
  - `finance-service`
  - `inventory-service`
- supports filter operators:
  - `EQ`
  - `CONTAINS`
  - `GT`
  - `GTE`
  - `LT`
  - `LTE`
- supports sum and group-by in report execution

Storage:

- H2 file database `./data/report-db`

### `processor-service`

Purpose:

- shared submission validation
- shared field normalization
- shared computed-field operators
- reusable processing rules for content, catalog, CRM, commerce, finance, and inventory submissions

Main API:

- `POST /api/processor-service/processors`
- `GET /api/processor-service/processors`
- `GET /api/processor-service/processors/{processorKey}`
- `PUT /api/processor-service/processors/{processorKey}`
- `DELETE /api/processor-service/processors/{processorKey}`
- `POST /api/processor-service/processors/{processorKey}/run`

Entity:

- `ProcessorDefinition`

Storage:

- H2 file database `./data/processor-db`

Rule model:

- `validatorsJson`
- `operatorsJson`

Supported validator types:

- `REQUIRED`
- `MIN_LENGTH`
- `MAX_LENGTH`
- `REGEX`
- `ENUM`
- `DECIMAL_MIN`
- `DECIMAL_MAX`

Supported operator types:

- `SET_FIELD`
- `COPY_FIELD`
- `TRIM`
- `UPPERCASE`
- `LOWERCASE`
- `CONCAT_FIELDS`
- `SUM_FIELDS`
- `MULTIPLY_FIELDS`

### `event-service`

Purpose:

- persistent business event intake
- cross-service integration history
- foundation for automation and BPM consumers
- one stable event seam for content, shop, CRM, finance, and inventory actions
- Kafka event fan-out producer

Main API:

- `POST /api/event-service/events`
- `GET /api/event-service/events`
- `GET /api/event-service/events/{eventKey}`

Entity:

- `BusinessEvent`

Storage:

- H2 file database `./data/event-db`

Kafka:

- publishes to topic `business-events`
- default bootstrap server `localhost:9092`

Event fields:

- `eventKey`
- `sourceService`
- `entityType`
- `entityKey`
- `actionType`
- `title`
- `payloadJson`
- `occurredAt`

### `crm-automation-service`

Purpose:

- consume Kafka business events relevant to CRM
- store lead/customer activity automation actions

Main API:

- `GET /api/crm-automation-service/actions`

### `finance-automation-service`

Purpose:

- consume Kafka business events relevant to finance
- store settlement/accounting automation actions

Main API:

- `GET /api/finance-automation-service/actions`

### `inventory-automation-service`

Purpose:

- consume Kafka business events relevant to inventory
- store stock side-effect automation actions

Main API:

- `GET /api/inventory-automation-service/actions`

### `report-automation-service`

Purpose:

- consume all Kafka business events
- store report/projection feed records for later analytics or denormalized views

Main API:

- `GET /api/report-automation-service/records`

## Reporting Source Types

`report-service` supports these `sourceType` values:

- `CONTENT`
- `CATALOG`
- `CRM`
- `COMMERCE`
- `FINANCE`
- `INVENTORY`

## Gateway Routes

The API gateway now routes:

- `/api/content-service/**`
- `/api/catalog-service/**`
- `/api/crm-service/**`
- `/api/commerce-service/**`
- `/api/finance-service/**`
- `/api/inventory-service/**`
- `/api/report-service/**`
- `/api/processor-service/**`
- `/api/event-service/**`
- `/api/crm-automation-service/**`
- `/api/finance-automation-service/**`
- `/api/inventory-automation-service/**`
- `/api/report-automation-service/**`

## Submission Processing

These services now accept an optional `processorKey` query parameter on create and update operations:

- `content-service`
- `catalog-service`
- `crm-service`
- `commerce-service`
- `finance-service`
- `inventory-service`

If `processorKey` is provided:

- the request payload is sent to `processor-service`
- operators run first
- validators run after operators
- invalid payloads are rejected with `400`
- valid payloads are transformed and then persisted

## Business Events

These services now publish integration events after create, update, and delete operations:

- `content-service`
- `catalog-service`
- `crm-service`
- `commerce-service`
- `finance-service`
- `inventory-service`

Published event shape:

- `sourceService`
- `entityType`
- `entityKey`
- `actionType`
- `title`
- `payload`

Kafka flow:

- business service persists entity and local outbox record in one transaction
- business service outbox dispatcher posts to `event-service`
- `event-service` persists event row idempotently
- `event-service` Kafka outbox dispatcher publishes JSON envelope to topic `business-events`
- automation services consume with separate consumer groups

Action types currently used:

- `CREATE`
- `UPDATE`
- `DELETE`

Example event queries:

- `GET /api/event-service/events?sourceService=commerce-service`
- `GET /api/event-service/events?entityType=CRM`
- `GET /api/event-service/events?actionType=DELETE`

Example:

- `POST /api/commerce-service/documents?processorKey=invoice-defaults`
- `PUT /api/crm-service/records/lead-100?processorKey=lead-normalizer`

## Future BPM Integration

These services are designed so later `Cyan-bpm` can orchestrate them by business process without changing this repository structure.

Recommended future BPM usage:

- `content-service`: content approval/publishing workflows
- `crm-service`: lead qualification and approval workflows
- `commerce-service`: order approval and invoice workflows
- `finance-service`: payment approval and settlement workflows
- `inventory-service`: stock reservation, manufacturing, warehouse workflows
- `processor-service`: externalized rule execution for BPM-driven submissions
- `event-service`: BPM and automation consumers can poll or bridge from this stream
- `crm-automation-service`: CRM side effects and customer activity consumers
- `finance-automation-service`: settlement and accounting consumers
- `inventory-automation-service`: stock reservation and inventory consumers
- `report-automation-service`: reporting/projection consumers

## Important Limitations

- these services are intentionally lightweight
- they do not reuse `Cyan-core`
- they do not include renderer abstractions yet
- persistence is local H2 file-based storage, not production-grade distributed storage
- `report-service` performs aggregation in service memory after fetching source rows
- processor execution is synchronous and HTTP-based
- processor definitions currently store rule JSON directly rather than normalized relational rule tables
- event publishing is synchronous HTTP after persistence and is not a transactional outbox yet
- business services now use local outbox delivery to `event-service`
- `event-service` now uses its own Kafka outbox delivery to `business-events`
- cross-service delivery is retryable, but still split across multiple databases and processes

## Next Logical Step

If this platform is kept and expanded inside this repo, the next useful addition would be:

- event publishing between business services
- stronger inventory/finance document relations
- content/page rendering composition beyond raw content entities
- normalized processor/rule authoring UI
- BPM bridge endpoints for external orchestration
- transactional outbox and retry handling for event publication

# Event Service Flow

## Overview

`event-service` is the shared integration event hub for the business microservices in this repository.

It stores business events emitted by:

- `content-service`
- `catalog-service`
- `crm-service`
- `commerce-service`
- `finance-service`
- `inventory-service`

## Event Lifecycle

1. A business service accepts a create, update, or delete request.
2. Optional submission processing runs through `processor-service`.
3. The business entity is persisted in the local service database.
4. The service publishes an event to `event-service`.
5. `event-service` stores the event in `business_events`.

## Event Contract

Endpoint:

`POST /api/event-service/events`

Request example:

```json
{
  "eventKey": "1b6c62fd-4638-4f0d-9d30-c40c33aa1f52",
  "sourceService": "commerce-service",
  "entityType": "COMMERCE",
  "entityKey": "inv-2025-0001",
  "actionType": "CREATE",
  "title": "commerce document created",
  "occurredAt": "2025-01-01T10:00:00Z",
  "payload": {
    "documentKey": "inv-2025-0001",
    "documentType": "INVOICE",
    "customerKey": "cust-100",
    "currency": "IRR",
    "documentStatus": "DRAFT",
    "subtotal": 1000000,
    "taxTotal": 90000,
    "grandTotal": 1090000
  }
}
```

## Query Examples

- `GET /api/event-service/events`
- `GET /api/event-service/events?sourceService=crm-service`
- `GET /api/event-service/events?entityType=INVENTORY`
- `GET /api/event-service/events?entityKey=inv-2025-0001`
- `GET /api/event-service/events?actionType=UPDATE`
- `GET /api/event-service/events/{eventKey}`

## Current Publishers

### `content-service`

Publishes:

- `CONTENT` `CREATE`
- `CONTENT` `UPDATE`
- `CONTENT` `DELETE`

### `catalog-service`

Publishes:

- `CATALOG` `CREATE`
- `CATALOG` `UPDATE`
- `CATALOG` `DELETE`

### `crm-service`

Publishes:

- `CRM` `CREATE`
- `CRM` `UPDATE`
- `CRM` `DELETE`

### `commerce-service`

Publishes:

- `COMMERCE` `CREATE`
- `COMMERCE` `UPDATE`
- `COMMERCE` `DELETE`

### `finance-service`

Publishes:

- `FINANCE` `CREATE`
- `FINANCE` `UPDATE`
- `FINANCE` `DELETE`

### `inventory-service`

Publishes:

- `INVENTORY` `CREATE`
- `INVENTORY` `UPDATE`
- `INVENTORY` `DELETE`

## Intended Consumers

This event stream is intended to support later:

- rule-driven automation
- report snapshots and denormalized read models
- BPM bridge listeners
- inventory and finance side effects
- notifications and audit feeds

## Current Limitation

This is not an outbox implementation yet.

The current pattern is:

- save entity first
- publish event over HTTP second

That means event publication is visible and centralized, but not transactionally atomic with the originating service database write.

# Kafka Automation Architecture

## Overview

This repository now uses Kafka for downstream automation consumption.

Current event path:

1. business service saves local entity
2. business service posts event to `event-service`
3. `event-service` stores event in its local database
4. `event-service` publishes a JSON envelope to Kafka topic `business-events`
5. automation microservices consume independently with separate consumer groups

## Kafka Topic

- topic: `business-events`
- bootstrap server default: `localhost:9092`

## Producer

Producer service:

- `event-service`

Producer payload:

```json
{
  "eventKey": "uuid",
  "sourceService": "commerce-service",
  "entityType": "COMMERCE",
  "entityKey": "inv-2025-0001",
  "actionType": "CREATE",
  "title": "commerce document created",
  "occurredAt": "2025-01-01T10:00:00Z",
  "payload": {
    "documentKey": "inv-2025-0001",
    "documentType": "INVOICE",
    "customerKey": "cust-100"
  }
}
```

## Consumers

### `crm-automation-service`

Consumer group:

- `crm-automation-service`

Consumes:

- `CRM`
- `COMMERCE`

Purpose:

- lead pipeline updates
- customer activity sync

### `finance-automation-service`

Consumer group:

- `finance-automation-service`

Consumes:

- `FINANCE`
- `COMMERCE`

Purpose:

- settlement sync
- invoice/payment downstream actions

### `inventory-automation-service`

Consumer group:

- `inventory-automation-service`

Consumes:

- `INVENTORY`
- `COMMERCE`
- `CATALOG`

Purpose:

- stock side effects
- future reservation/manufacturing triggers

### `report-automation-service`

Consumer group:

- `report-automation-service`

Consumes:

- all business events

Purpose:

- event-fed projection store
- analytics input
- later report snapshots or denormalized read models

## Query APIs

- `GET /api/crm-automation-service/actions`
- `GET /api/finance-automation-service/actions`
- `GET /api/inventory-automation-service/actions`
- `GET /api/report-automation-service/records`

## Current Limitation

This is Kafka-based fan-out, but it is still not a full outbox architecture.

The current write order is:

1. origin service writes its entity
2. origin service calls `event-service`
3. `event-service` writes event row
4. `event-service` publishes to Kafka

So the durable integration seam is stronger than direct polling, but the originating entity write and emitted Kafka message are still not transactionally atomic.

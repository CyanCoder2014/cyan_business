# Kafka Automation Architecture

## Overview

This repository now uses Kafka for downstream automation consumption.

Current event path:

1. business service saves local entity and local outbox row in one transaction
2. business service outbox dispatcher retries post to `event-service`
3. `event-service` stores event in its local database idempotently
4. `event-service` Kafka dispatcher retries publish of JSON envelope to topic `business-events`
5. automation microservices consume independently with separate consumer groups

## Kafka Topic

- topic: `business-events`
- bootstrap server default: `localhost:9092`
- local Docker compose: [docker/kafka/docker-compose.yml](/Users/farid/Projects/Cyan/old-cyan/cyan_business/docker/kafka/docker-compose.yml:1)

## Local Startup

Start Kafka:

```bash
docker compose -f docker/kafka/docker-compose.yml up -d
```

Then start:

- `event-service`
- `crm-automation-service`
- `finance-automation-service`
- `inventory-automation-service`
- `report-automation-service`

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

This is now Kafka-based fan-out with layered outbox delivery.

The current durable write order is:

1. origin service writes its entity
2. origin service writes local outbox row
3. origin service outbox delivers to `event-service`
4. `event-service` writes event row
5. `event-service` Kafka outbox publishes to Kafka

So the integration seam is retryable at both boundaries, but the full chain is still not a single atomic distributed transaction.

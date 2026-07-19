# Structured Dynamic Platform

## Main Idea

This project is now oriented around the same core concept as `Cyan-core`:

- a service stores dynamic entity definitions
- each definition declares nested field structure
- each definition declares validators
- each definition declares operators
- each service exposes dynamic CRUD over those definitions
- requests are rejected if they contain missing required nested fields or extra fields

The aim is to keep every dynamic service structured, so AI-generated entities remain controlled JSON structures instead of unbounded ad hoc payloads.

## Shared Runtime

Shared module:

- [dynamic-entity-core](/Users/farid/Projects/Cyan/old-cyan/cyan_business/dynamic-entity-core:1)

It provides:

- `FieldDefinition`
- `ValidationRule`
- `OperationRule`
- `EntityDefinitionModel`
- recursive nested validation
- strict extra-field detection
- strict missing-field detection
- shared operators
- endpoint/internal security split
- PostgreSQL entity-definition storage
- Mongo record storage

## Definition Storage

Definitions are stored in PostgreSQL:

- table: `dynamic_entity_definitions`

Each definition stores:

- `serviceKey`
- `entityKey`
- `entityType`
- `title`
- `definitionJson`

`definitionJson` is the structured source of truth.

## Record Storage

Submitted entity records are stored in MongoDB:

- collection: `dynamic_entity_records`

Each record stores:

- `serviceKey`
- `entityKey`
- `recordKey`
- `data`
- `relations`
- `status`

This allows nested maps/lists to stay flexible while remaining schema-controlled by the stored definition.

## API Split

Every dynamic service now exposes two API surfaces:

- `/endpoint/...`
  - intended for end users and AI orchestrator
  - bearer token resource server

- `/internal/...`
  - intended for service-to-service calls
  - basic auth

Shared controllers are exposed by `dynamic-entity-core`:

- `POST /endpoint/entities/definitions`
- `PUT /endpoint/entities/definitions/{entityKey}`
- `GET /endpoint/entities/definitions`
- `GET /endpoint/entities/definitions/{entityKey}`
- `GET /endpoint/entities/templates`
- `GET /endpoint/entities/templates/{templateKey}`
- `POST /endpoint/entities/templates/{templateKey}/definitions`
- `POST /endpoint/entities/records/{entityKey}/validate`
- `POST /endpoint/entities/records/{entityKey}`
- `PATCH /endpoint/entities/records/{entityKey}/{recordKey}`
- `GET /endpoint/entities/records/{entityKey}`
- `GET /endpoint/entities/records/{entityKey}/{recordKey}`

And matching internal surfaces:

- `GET /internal/entities/definitions`
- `GET /internal/entities/definitions/{entityKey}`
- `GET /internal/entities/templates`
- `GET /internal/entities/templates/{templateKey}`
- `POST /internal/entities/templates/{templateKey}/definitions`
- `POST /internal/entities/records/{entityKey}/validate`
- `POST /internal/entities/records/{entityKey}`
- `GET /internal/entities/records/{entityKey}`
- `GET /internal/entities/records/{entityKey}/{recordKey}`

`POST .../templates/{templateKey}/definitions` lets the AI orchestrator instantiate a controlled entity definition from a service-owned blueprint instead of inventing a schema from scratch.

Definition create and update requests should send the definition as a structured JSON object:

```json
{
  "entityKey": "leave-request-form",
  "definition": {
    "entityType": "BPM_FORM",
    "title": "Leave Request",
    "fields": {}
  }
}
```

The runtime assigns the owning `serviceKey` and outer `entityKey`, then serializes `definition` internally for the existing `definitionJson` persistence column. The legacy request field `definitionJson` remains accepted during migration. If both fields are supplied, the structured `definition` field takes precedence. Stored definition responses continue to expose `definitionJson` for compatibility with existing consumers.

## Validation Behavior

Recursive validation behavior is modeled after `DynamicValidationServiceImp` from `Cyan-core`.

Current shared validation engine:

- validates top-level definition rules
- validates field rules
- walks nested `object` fields recursively
- walks nested `list` items recursively
- rejects unexpected fields
- rejects missing fields

Supported validators in shared runtime:

- `REQUIRED`
- `REGEX`
- `MIN_LENGTH`
- `MAX_LENGTH`
- `ENUM`
- `DECIMAL_MIN`
- `DECIMAL_MAX`
- `AntlrExpression`

### `AntlrExpression` Validator

The shared runtime now includes an expression validator modeled after the `Cyan-core` `AntlrExpressionValidator` idea.

It uses a dedicated lexer/parser-style pipeline and supports:

- field identifiers like `subtotal`
- nested identifiers like `customer.type`
- string literals
- number literals
- boolean literals
- `null`
- `==`
- `!=`
- `>`
- `>=`
- `<`
- `<=`
- `&&`
- `||`
- `!`
- parentheses

Example validation rule:

```json
{
  "order": 10,
  "validation": "AntlrExpression",
  "validationParams": {
    "expression": "documentType == \"INVOICE\" && subtotal >= 0 && customer.type != null",
    "isRoot": true
  },
  "validationMessage": "invoice expression validation failed"
}
```

`isRoot=true` means the expression is evaluated against the full submitted document.

## Operator Behavior

Supported shared operators:

- `SET_FIELD`
- `COPY_FIELD`
- `SUM_FIELDS`

These are intended for structured submission processing.

## Business Services

These services now share the same dynamic entity runtime:

- `content-service`
- `catalog-service`
- `crm-service`
- `commerce-service`
- `finance-service`
- `inventory-service`
- `report-service`

Each service has its own:

- PostgreSQL database
- Mongo database
- `dynamic.runtime.service-key`
- internal basic auth credentials

## Service Templates

Each service now publishes structured templates for AI/bootstrap use:

- `content-service`
  - `blog-page`
  - `landing-page`
- `catalog-service`
  - `catalog-product`
  - `catalog-service-offer`
- `crm-service`
  - `crm-lead`
  - `crm-contact`
- `commerce-service`
  - `sales-order`
  - `sales-invoice`
- `finance-service`
  - `finance-transaction`
- `inventory-service`
  - `stock-item`
  - `work-order`
- `report-service`
  - `dynamic-report`
- `buyer-service`
  - `buyer-profile`
- `client-service`
  - `company-profile`
  - `client-profile`
- `factor-service`
  - `factor-document`
- `product-service`
  - `legacy-product`
- `tax-pay-sys`
  - `tax-submission`

## Example AI-Orchestrator Flow

1. AI decides the user wants an ecommerce site.
2. AI creates entity definitions for:
   - `product`
   - `category`
   - `order`
   - `invoice`
   - `content-page`
3. AI posts those definitions to the target services via `/endpoint/entities/definitions`.
4. Users submit records through `/endpoint/entities/records/{entityKey}`.
5. Services enforce the declared structure and validators.
6. Post-save event flow moves through local outbox -> `event-service` -> Kafka -> automation services.

## Example Definition JSON

```json
{
  "serviceKey": "commerce-service",
  "entityKey": "invoice",
  "entityType": "INVOICE",
  "title": "Invoice",
  "defaultValues": {
    "status": "DRAFT",
    "currency": "IRR"
  },
  "fields": {
    "customerKey": {
      "type": "input",
      "validations": [
        {
          "order": 1,
          "validation": "REQUIRED",
          "validationParams": {},
          "validationMessage": "customerKey is required"
        }
      ]
    },
    "subtotal": {
      "type": "input",
      "validations": [
        {
          "order": 1,
          "validation": "DECIMAL_MIN",
          "validationParams": {
            "min": "0"
          },
          "validationMessage": "subtotal must be zero or positive"
        }
      ]
    },
    "items": {
      "type": "list",
      "itemValidations": {
        "sku": {
          "type": "input",
          "validations": [
            {
              "order": 1,
              "validation": "REQUIRED",
              "validationParams": {},
              "validationMessage": "sku is required"
            }
          ]
        },
        "price": {
          "type": "input",
          "validations": [
            {
              "order": 1,
              "validation": "DECIMAL_MIN",
              "validationParams": {
                "min": "0"
              },
              "validationMessage": "price must be zero or positive"
            }
          ]
        }
      }
    }
  },
  "operations": [
    {
      "order": 1,
      "operation": "SET_FIELD",
      "operationParams": {
        "field": "status",
        "value": "DRAFT"
      }
    }
  ]
}
```

## Local Runtime

Start PostgreSQL and MongoDB:

```bash
docker compose -f docker/databases/docker-compose.yml up -d
```

Start Kafka if automation/event streaming is needed:

```bash
docker compose -f docker/kafka/docker-compose.yml up -d
```

## Current Limitation

This is now much closer to the intended platform model, but a few things still remain simplified:

- validator set is smaller than `Cyan-core`
- operators are simpler than the full `Cyan-core` behavior
- report-service still keeps its earlier custom report code alongside the new shared dynamic runtime
- existing fixed controllers from earlier work still exist in some services, even though the new preferred surface is `/endpoint/...` and `/internal/...`

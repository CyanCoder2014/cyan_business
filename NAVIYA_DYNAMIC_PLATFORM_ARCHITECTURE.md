# Naviya Dynamic Platform Architecture

## Purpose

This document defines a concrete architecture for evolving the current `naviya-core` and `naviya-bpm` model into a broader dynamic platform that supports:

- dynamic forms
- dynamic websites and content pages
- shop/catalog/product/service use cases
- CRM objects such as lead, contact, order, invoice, payment
- dynamic reports and filters
- later BPM orchestration for manufacturing, inventory, and business processes

This is not a generic brainstorm.
It is anchored to the actual current classes and responsibilities in:

- `naviya-core`
- `naviya-bpm`

## Current Baseline

### `naviya-core` already provides

Current key entities:

- [FormRenderer](/Users/farid/Projects/naviya/naviya-core/src/main/java/com/vasl/formFlow/entity/FormRenderer.java:1)
- [FormFlow](/Users/farid/Projects/naviya/naviya-core/src/main/java/com/vasl/formFlow/entity/FormFlow.java:1)
- [Form](/Users/farid/Projects/naviya/naviya-core/src/main/java/com/vasl/formFlow/entity/Form.java:1)

Current practical meaning:

- `FormRenderer` = UI schema/layout definition
- `FormFlow` = backend processing definition with validations and operations
- `Form` = saved submission record in `registerForm`

Current reporting presence:

- dynamic report/filter capabilities already exist in `com.vasl.formFlow.report`
- report access already exists in controllers like [PanelReportController](/Users/farid/Projects/naviya/naviya-core/src/main/java/com/vasl/formFlow/controller/PanelReportController.java:1)

### `naviya-bpm` already provides

Current key workflow entities:

- [ManagedObject](/Users/farid/Projects/naviya/naviya-bpm/src/main/java/com/vasl/bpm/dynamicflow/domain/ManagedObject.java:1)
- [FlowState](/Users/farid/Projects/naviya/naviya-bpm/src/main/java/com/vasl/bpm/dynamicflow/domain/FlowState.java:1)
- [ObjectFlowService](/Users/farid/Projects/naviya/naviya-bpm/src/main/java/com/vasl/bpm/dynamicflow/service/ObjectFlowService.java:1)

Current practical meaning:

- BPM owns workflow state and state transitions
- each state can already point to:
  - `formKey`
  - `processorKey`
- BPM already treats `naviya-core` as the submission backend

This means the foundation is already right.
The missing piece is generalizing the data model beyond "form submission only".

## Main Architectural Decision

Do not continue modeling everything as a bigger `FormFlow`.

Instead, make `naviya-core` evolve into a **dynamic entity platform** with four primary concepts:

1. `RendererDefinition`
2. `ProcessorDefinition`
3. `EntityDefinition`
4. `EntityRecord`

This is the core design shift.

## New Core Concepts

## 1. RendererDefinition

Purpose:

- UI schema only
- layout, sections, widgets, fields, order
- reusable across business entities

It is the generalized form of `FormRenderer`.

Recommended class:

`com.vasl.dynamic.schema.entity.RendererDefinition`

Suggested fields:

```java
public class RendererDefinition extends Entity {
    private String key;
    private String type;
    private String title;
    private String description;
    private String submitLabel;
    private Integer canvasColumns;
    private List<String> topLevelOrder;
    private List<Map<String, Object>> sections;
    private List<Map<String, Object>> fields;
    private List<String> tags;
    private boolean active;
    private Integer version;
}
```

Migration note:

- `FormRenderer.name` becomes `RendererDefinition.key`
- `FormRenderer` can initially stay as-is and be treated as the first implementation of `RendererDefinition`

Mongo collection:

- `renderer_definition`

## 2. ProcessorDefinition

Purpose:

- backend submission processor
- validators
- operators
- transformations
- computed fields
- pre-save hooks
- post-save hooks

This is the generalized replacement for `FormFlow`.

Recommended class:

`com.vasl.dynamic.processor.entity.ProcessorDefinition`

Suggested fields:

```java
public class ProcessorDefinition extends Entity {
    private String key;
    private String type;
    private String entityType;
    private String description;
    private List<ValidationRule> validations;
    private List<OperationRule> operations;
    private Map<String, ProcessorFieldConfig> fieldConfigs;
    private List<ProcessorHookConfig> prePersistHooks;
    private List<ProcessorHookConfig> postPersistHooks;
    private boolean active;
    private Integer version;
}
```

Supporting classes:

```java
public class ProcessorFieldConfig {
    private String fieldKey;
    private String type;
    private Integer order;
    private List<ValidationRule> validations;
    private List<OperationRule> operations;
    private Map<String, Object> config;
}

public class ProcessorHookConfig {
    private String key;
    private String handler;
    private Map<String, Object> config;
    private boolean async;
}
```

Migration note:

- `FormFlow.name` becomes `ProcessorDefinition.key`
- `FormFlow.validations` and `FormFlow.operations` move directly
- `FormFlow.formData` should be flattened into `fieldConfigs`

Mongo collection:

- `processor_definition`

## 3. EntityDefinition

Purpose:

- defines what kind of business object exists in the platform
- links renderer and processor
- defines default storage and report behavior
- gives first-class identity to things like `ORDER`, `PRODUCT`, `CONTENT`, `LEAD`

Recommended class:

`com.vasl.dynamic.entitydef.entity.EntityDefinition`

Suggested fields:

```java
public class EntityDefinition extends Entity {
    private String key;
    private EntityType type;
    private String title;
    private String description;
    private String rendererKey;
    private String processorKey;
    private String defaultState;
    private StoragePolicy storagePolicy;
    private List<String> tags;
    private Map<String, Object> defaults;
    private List<RelationDefinition> relations;
    private List<ReportBinding> reportBindings;
    private boolean active;
    private Integer version;
}
```

Enums:

```java
public enum EntityType {
    FORM,
    CONTENT,
    PRODUCT,
    SERVICE,
    ORDER,
    INVOICE,
    LEAD,
    CONTACT,
    PAYMENT,
    REPORT,
    TASK,
    GENERIC
}

public enum StoragePolicy {
    MONGO_DOCUMENT,
    SQL_TABLE,
    HYBRID
}
```

Mongo collection:

- `entity_definition`

## 4. EntityRecord

Purpose:

- actual stored business object instance
- generalizes `Form`
- can represent a content page, invoice, lead, order, product, or generic submission

Recommended class:

`com.vasl.dynamic.record.entity.EntityRecord`

Suggested fields:

```java
public class EntityRecord extends Entity {
    private String entityKey;
    private EntityType entityType;
    private String rendererKey;
    private String processorKey;
    private String tenantId;
    private String submittedBy;
    private String ownerId;
    private String state;
    private String status;
    private Map<String, Object> data;
    private Map<String, Object> baseData;
    private Map<String, Object> computedData;
    private Map<String, Object> totals;
    private Map<String, Object> relations;
    private List<String> validationMessages;
    private String validationState;
    private Instant submittedAt;
    private Instant verifiedAt;
    private Instant publishedAt;
    private Boolean archived;
}
```

Migration note:

- current `Form` becomes a specialized first-generation `EntityRecord`
- `Form.flow` maps to `entityKey` or `processorKey` depending on context
- `Form.stepName` should move into `data` or `stateContext`
- `Form.formData` becomes `EntityRecord.data`

Mongo collection:

- `entity_record`

## Specialized Domain Extensions

Not every entity type should be a completely separate schema from day one.
But some need typed extensions.

## Order / Invoice

Recommended typed extension:

`com.vasl.dynamic.commerce.entity.OrderRecord`

```java
public class OrderRecord extends EntityRecord {
    private String orderNumber;
    private String currency;
    private List<OrderItem> items;
    private BigDecimal subtotal;
    private BigDecimal discountTotal;
    private BigDecimal taxTotal;
    private BigDecimal grandTotal;
    private String paymentStatus;
    private String fulfillmentStatus;
}

public class OrderItem {
    private String itemId;
    private String productKey;
    private String name;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal lineTotal;
    private Map<String, Object> attributes;
}
```

Collection:

- `order_record`

## Product / Service

Recommended class:

`com.vasl.dynamic.catalog.entity.CatalogItem`

```java
public class CatalogItem extends Entity {
    private String key;
    private String type;
    private String name;
    private String sku;
    private String categoryKey;
    private String unit;
    private BigDecimal defaultPrice;
    private String currency;
    private Boolean active;
    private Map<String, Object> attributes;
    private Map<String, Object> inventoryInfo;
}
```

Collection:

- `catalog_item`

## Content

Recommended class:

`com.vasl.dynamic.content.entity.ContentRecord`

```java
public class ContentRecord extends Entity {
    private String key;
    private String type;
    private String slug;
    private String title;
    private String summary;
    private String seoTitle;
    private String seoDescription;
    private String publicationStatus;
    private List<Map<String, Object>> blocks;
    private Map<String, Object> metadata;
    private Instant publishedAt;
}
```

Collection:

- `content_record`

## Report Definition

Recommended class:

`com.vasl.dynamic.report.entity.ReportDefinition`

```java
public class ReportDefinition extends Entity {
    private String key;
    private String title;
    private String entityKey;
    private List<ReportColumnDefinition> columns;
    private List<ReportFilterDefinition> filters;
    private List<AggregationDefinition> aggregations;
    private Map<String, Object> defaultSort;
    private Map<String, Object> defaultQuery;
    private boolean exportable;
    private boolean dashboardEnabled;
}
```

Collections:

- `report_definition`
- optional `report_execution`

## Microservice Split

This is the recommended service split if you want to split "as far as useful", but not into nonsense.

## 1. `naviya-schema-service`

Responsibilities:

- manage `RendererDefinition`
- manage field/widget layout metadata
- frontend-builder support
- dynamic page schema and layout schema

Owns:

- `renderer_definition`

APIs:

- `POST /api/schema/renderers`
- `GET /api/schema/renderers/{key}`
- `PUT /api/schema/renderers/{key}`
- `POST /api/schema/renderers/query`

## 2. `naviya-processor-service`

Responsibilities:

- manage `ProcessorDefinition`
- execute validators
- execute operators
- transform incoming submission payloads
- expose validator/operator registry

Owns:

- `processor_definition`
- optional execution logs

APIs:

- `POST /api/processors`
- `GET /api/processors/{key}`
- `POST /api/processors/{key}/validate`
- `POST /api/processors/{key}/execute`
- `GET /api/processors/validators`
- `GET /api/processors/operators`

Important:

- this service becomes the replacement for "FormFlow as the center"

## 3. `naviya-entity-service`

Responsibilities:

- manage `EntityDefinition`
- manage generic `EntityRecord`
- generic CRUD/query for dynamic entities
- submit via processor

Owns:

- `entity_definition`
- `entity_record`

APIs:

- `POST /api/entities/definitions`
- `GET /api/entities/definitions/{key}`
- `POST /api/entities/records`
- `GET /api/entities/records/{id}`
- `POST /api/entities/records/query`
- `POST /api/entities/records/{id}/submit`

## 4. `naviya-catalog-service`

Responsibilities:

- product/service catalog
- price defaults
- category hierarchy
- reusable product/service metadata

Owns:

- `catalog_item`
- `catalog_category`

APIs:

- `POST /api/catalog/items`
- `GET /api/catalog/items/{key}`
- `POST /api/catalog/items/query`
- `POST /api/catalog/categories`

## 5. `naviya-order-service`

Responsibilities:

- orders
- invoices
- order items
- financial totals
- link to catalog
- link to customer/contact

Owns:

- `order_record`
- `invoice_record`

APIs:

- `POST /api/orders`
- `GET /api/orders/{id}`
- `POST /api/orders/{id}/recalculate`
- `POST /api/invoices`
- `GET /api/invoices/{id}`

Important:

- processor rules can still be used for totals, approval logic, and side effects

## 6. `naviya-content-service`

Responsibilities:

- blog posts
- pages
- landing pages
- menus
- reusable content blocks

Owns:

- `content_record`
- `menu_definition`

APIs:

- `POST /api/content`
- `GET /api/content/{slug}`
- `POST /api/content/query`
- `POST /api/menus`

## 7. `naviya-report-service`

Responsibilities:

- dynamic report definitions
- dynamic filters
- aggregation pipelines
- export and dashboards

Owns:

- `report_definition`
- `report_execution`

APIs:

- `POST /api/reports`
- `GET /api/reports/{key}`
- `POST /api/reports/{key}/run`
- `POST /api/reports/{key}/export`

## 8. `naviya-bpm`

Responsibilities:

- workflow state
- transitions
- approval/review process
- assignment and access
- integration with dynamic entity platform

BPM should not own:

- validation rule definitions
- entity submission rules
- UI schema
- generic report definitions

## Updated BPM Integration Model

Current `FlowState` has:

- `formKey`
- `processorKey`

Recommended evolution:

```java
public record FlowState(
    String id,
    String displayName,
    boolean terminal,
    String entityKey,
    String rendererKey,
    String processorKey,
    boolean reviewCommentRequired,
    Set<String> candidateGroups,
    List<FlowActionConfig> onEnterActions,
    FlowAccessRule accessRule
) {}
```

Why:

- BPM state should know what business object it is acting on
- renderer and processor should not be overloaded as business-object identity

## Updated ManagedObject Payload

Current `ManagedObject` is already close enough.
Do not remove top-level `state`.

Recommended payload shape:

```json
{
  "entityKey": "sales-order",
  "rendererKey": "sales-order-editor",
  "processorKey": "sales-order-submit",
  "recordId": "6658a4df6f6f8f12ac11fe10",
  "recordType": "ORDER",
  "currentValues": {
    "customerId": "c-100",
    "total": 1200000,
    "currency": "IRR"
  },
  "stateSubmissions": {
    "draft": {
      "recordId": "6658a4df6f6f8f12ac11fe10",
      "rendererKey": "sales-order-editor",
      "processorKey": "sales-order-submit",
      "submittedBy": "user-77",
      "submittedAt": "2026-04-25T16:30:00Z"
    }
  }
}
```

## API Contracts

Below is the recommended API surface between services.

## Schema service

```http
POST /api/schema/renderers
GET /api/schema/renderers/{key}
POST /api/schema/renderers/query
```

Example create:

```json
{
  "key": "order-editor",
  "type": "FORM",
  "title": "Order Editor",
  "sections": [],
  "fields": []
}
```

## Processor service

```http
POST /api/processors
GET /api/processors/{key}
POST /api/processors/{key}/validate
POST /api/processors/{key}/execute
```

Example validate request:

```json
{
  "entityKey": "sales-order",
  "recordId": null,
  "data": {
    "customerId": "c-100",
    "items": [
      { "productKey": "p-1", "quantity": 2, "unitPrice": 1000 }
    ]
  },
  "context": {
    "actorUserId": "u-1"
  }
}
```

Example validate response:

```json
{
  "valid": true,
  "messages": [],
  "computedData": {
    "subtotal": 2000,
    "grandTotal": 2000
  }
}
```

## Entity service

```http
POST /api/entities/records
GET /api/entities/records/{id}
POST /api/entities/records/query
POST /api/entities/records/{id}/submit
```

Example create:

```json
{
  "entityKey": "lead-capture",
  "rendererKey": "lead-capture-form",
  "processorKey": "lead-capture-submit",
  "data": {
    "fullName": "John Smith",
    "mobile": "09120000000"
  }
}
```

## Report service

```http
POST /api/reports
GET /api/reports/{key}
POST /api/reports/{key}/run
```

Example report definition:

```json
{
  "key": "orders-by-date",
  "entityKey": "sales-order",
  "columns": [
    { "key": "customerId", "label": "Customer" },
    { "key": "grandTotal", "label": "Total" }
  ],
  "filters": [
    { "key": "fromDate", "type": "DATE" },
    { "key": "toDate", "type": "DATE" }
  ],
  "aggregations": [
    { "type": "SUM", "field": "grandTotal", "as": "totalSales" }
  ]
}
```

## Storage Model

The default recommendation is:

- MongoDB for metadata-heavy dynamic definitions and records
- SQL only where operational joins or accounting guarantees demand it

Recommended collections:

- `renderer_definition`
- `processor_definition`
- `entity_definition`
- `entity_record`
- `catalog_item`
- `catalog_category`
- `order_record`
- `invoice_record`
- `content_record`
- `report_definition`
- `report_execution`
- BPM:
  - existing `managed_objects`
  - existing flow-definition collections

## Refactor Path from Current Code

## Phase 1. Keep backward compatibility

Do not break current behavior immediately.

Actions:

1. keep `FormRenderer`, `FormFlow`, and `Form`
2. add aliases/adapters:
   - `FormRenderer` -> `RendererDefinition`
   - `FormFlow` -> `ProcessorDefinition`
   - `Form` -> `EntityRecord`
3. introduce new APIs alongside old ones

## Phase 2. Introduce EntityDefinition

Actions:

1. create `EntityDefinition`
2. update admin UI/config to define:
   - `entityKey`
   - `rendererKey`
   - `processorKey`
3. allow current form flows to register themselves as `EntityType.FORM`

## Phase 3. Introduce EntityRecord

Actions:

1. create `EntityRecord`
2. modify submission adapters so they can persist either:
   - old `Form`
   - new `EntityRecord`
3. migrate reports to read from `EntityRecord` where possible

## Phase 4. Split processor execution

Actions:

1. extract validators/operators execution into `naviya-processor-service`
2. move `DynamicFormService` responsibilities into:
   - validation
   - record persistence
   - operation execution

## Phase 5. Specialize catalog/content/order/report

Actions:

1. add `catalog-service`
2. add `content-service`
3. add `order-service`
4. add `report-service`

## Phase 6. Expand BPM contract

Actions:

1. add `entityKey` and `rendererKey` to BPM `FlowState`
2. keep `processorKey`
3. update `ObjectFlowService` integration payloads

## Example Use Cases

## Dynamic Website

Use:

- `content-service` for pages/blog/menu
- `schema-service` for page block editor schemas
- `processor-service` for contact forms, newsletter, comments
- `report-service` for site analytics summaries

## Shop

Use:

- `catalog-service` for products/services
- `order-service` for cart/order/invoice
- `schema-service` for checkout forms
- `processor-service` for order validation and pricing operators
- `report-service` for sales dashboards

## CRM

Use:

- `entity-service` for lead/contact/case
- `processor-service` for lead scoring, assignment, dedupe
- `report-service` for funnel and pipeline dashboards
- `bpm` for approval/escalation journeys

## Mini ERP / Business App

Use:

- `entity-service` for custom master/transaction entities
- `order-service` for invoice/order
- `catalog-service` for product/service
- `report-service` for operational reports
- `bpm` for process control

## What Should Stay In `naviya-core`

If you do not want to split immediately, `naviya-core` can temporarily host:

- renderer management
- processor management
- entity definitions
- entity records
- reporting

Then you can split services later without changing the conceptual model.

This is the safest path.

## Final Recommendation

The platform should be reframed as:

- **Schema**: renderer definitions
- **Processor**: validators/operators/transforms
- **Entity**: business object definitions
- **Record**: stored instances
- **Report**: dynamic query and analytics
- **BPM**: orchestration and state machine

That gives you one platform that can support:

- forms
- website pages
- blog/content
- products/services
- orders/invoices
- leads/CRM
- finance transitions
- dynamic reports
- and later full BPM-driven manufacturing/inventory processes

This is the correct direction for Naviya.

# Importer Credit and Delivery: BPM + Automation + Batch Reference Scenario

This scenario processes roughly 100,000 importer orders every morning, calculates
customer credit from behavioral aggregates, persists credit reports, opens BPM
reviews for exceptional customers, and dispatches due loadings to a delivery API.

It is deliberately more complex than a simple scheduled HTTP call. It exercises:

- strict dynamic entity definitions
- reusable processor validation and normalization
- six-field cron schedules with an explicit timezone
- durable, chunked, restartable batch ingestion
- source pagination and checkpoint restart
- retry, skip, quarantine, and run retry
- receiver-enforced idempotency
- customer projection/aggregation
- Zen/GoRules JDM credit decisions
- automation-to-BPM exception routing
- BPM dynamic form submission
- a second durable batch for delivery dispatch

The canonical JSON payloads are under
[`docs/examples/importer-credit-delivery`](examples/importer-credit-delivery).

## 0. What is implemented and what is a solution extension

There are two different source modes:

1. **Platform-owned orders.** Orders are dynamic records in `commerce-service`.
   Use `order-projection-batch-platform.json`. The batch reads the real internal
   dynamic-record API with bounded pagination and Basic authentication, then
   upserts normalized projection-event records in `report-service`.
2. **External client orders.** Use `order-projection-batch.json`. The batch reads
   the client's paginated API and sends to the client's projection API with
   bearer tokens.

The generic platform currently provides paging, batch durability, idempotent
dynamic-record keys, automation, BPM, and basic processor rules. It does **not**
yet provide the domain-specific atomic 90-day credit aggregator described below.
That component must be implemented as a versioned report/analytics processor (or
a custom report-service projection endpoint) before `/finalize` is a real route.
The earlier `projection.example` URL is therefore an integration contract, not an
existing platform microservice.

For the platform-owned path, the concrete calls are:

```text
automation
  -> batch-worker-service /internal/batch/.../runs
batch worker
  -> commerce-service /internal/entities/records/importer-order?page=N&size=500
  -> report-service /internal/entities/records/customer-credit-order-event
future projection processor
  -> reads customer-credit-order-event pages
  -> creates/upserts customer-credit-projection records
  -> invokes processor-service customer-credit-score-v1
  -> stores customer-credit-report and optionally starts BPM review
```

The source page envelope is:

```json
{
  "content": [],
  "page": 0,
  "size": 500,
  "totalElements": 100000,
  "totalPages": 200
}
```

Calls that omit `page`, `size`, and `sort` still receive the legacy array response.
This preserves existing internal consumers while allowing large batch reads.

## 1. Why the work is split across services

Do not fetch 100,000 orders into one automation execution. Automation execution
documents and normal Java/Mono memory are not durable ETL storage.

The safe ownership split is:

| Concern | Owner | Reason |
|---|---|---|
| 08:00 and 08:30 schedules | automation orchestrator | Cron is orchestration state |
| Paginated 100k-order read | batch worker | Bounded pages, chunks, checkpoints |
| Per-order retries and quarantine | batch worker | Restartable Spring Batch semantics |
| Customer clustering and metrics | versioned projection processor | Stateful, explainable aggregation by customer |
| Credit policy | versioned processor or Zen/JDM node | Deterministic rules with optional AI/ML adapters |
| Credit report schema | report-service dynamic entity | Strict controlled report record |
| High-risk/manual decisions | BPM | Human task, access rules, audit trail |
| Due-loading dispatch | second batch definition | Large remote side-effect set |
| Rejected delivery investigation | batch quarantine + BPM | Preserve failed items and assign operations |

The projection receiver is an API owned by the importer solution. It must update
customer aggregates transactionally and idempotently. A typical database model is:

- `projection_event(event_key unique, customer_key, occurred_at, amount, ...)`
- `customer_credit_projection(customer_key primary key, order_count_90d, total_amount_90d, ...)`

For one incoming order event, the receiver inserts `event_key` and updates the
customer projection in the same database transaction. A duplicate `event_key`
returns the original success response without incrementing the projection twice.

Redis may accelerate locks or counters, but it is not required when PostgreSQL can
enforce the unique event key and transactional aggregate update. Redis alone should
not be the system of record for important credit data.

## 2. End-to-end sequence

```text
08:00 automation schedule (platform-owned source)
  -> start importer-order-projection-v1 batch with runKey=scheduledAt
  -> batch reads commerce-service dynamic order records?page=N&size=500
  -> maps data.* fields to a report-service projection-event record
  -> report-service deduplicates by the scoped recordKey unique index
  -> projection processor calculates per-customer 90-day metrics
  -> projection processor starts one idempotent customer-credit-score-v1 execution per customer
  -> Zen JDM calculates limit/risk/manualReview
  -> automation upserts strict customer-credit-report
  -> high-risk customer opens customer-credit-manual-review-v1 BPM object

08:30 automation schedule
  -> start importer-due-loading-dispatch-v1 batch
  -> batch reads due READY loadings in bounded pages
  -> delivery API receives stable Idempotency-Key per loading
  -> HTTP 429/5xx is retried
  -> permanent HTTP 4xx is quarantined and counted as skipped
  -> if skipCount > 0, automation opens one delivery batch BPM exception
```

## 3. Platform and external API contracts

### 3.0 Platform dynamic-record source with Basic authentication

The platform batch definition stores only environment-variable names, never the
password:

```json
{
  "url": "http://commerce-service:9104/internal/entities/records/importer-order",
  "itemsPath": "content",
  "pageSize": 500,
  "headers": {
    "X-Tenant-Key": "importer-demo",
    "X-Site-Key": "main-site"
  },
  "authentication": {
    "type": "BASIC",
    "username": "commerce_internal",
    "secretEnvironmentVariable": "COMMERCE_SERVICE_INTERNAL_PASSWORD"
  }
}
```

`authentication.type` supports `BASIC` and `BEARER`. For Basic auth, either
`username` or `usernameEnvironmentVariable` is required. The password/token is
always loaded from `secretEnvironmentVariable` inside the worker pod. The legacy
`bearerTokenEnvironmentVariable` remains supported, but it cannot be combined
with `authentication`.

Authentication ownership elsewhere is:

- automation `CALL_API` supports `BASIC`, `BEARER`, and `API_KEY` connector
  credentials; a Basic credential secret is `username:password` (or its Base64
  representation)
- BPM `DYNAMIC` submissions and service-key/path actions use the platform's
  internal Basic service credentials automatically
- an arbitrary external client call with its own Basic credential should be an
  automation `CALL_API` node referenced by BPM, because BPM does not own connector
  secrets

### 3.1 Paginated order source

Request:

```http
GET /v1/orders?status=CONFIRMED&page=0&size=500
Authorization: Bearer <IMPORTER_API_TOKEN>
```

Response:

```json
{
  "content": [
    {
      "orderId": "order-10001",
      "customer": { "id": "customer-77" },
      "createdAt": "2026-07-23T10:20:00Z",
      "totalAmount": 420000000,
      "currency": "IRR",
      "payment": { "maximumOverdueDays": 12 },
      "loading": {
        "id": "loading-50001",
        "promisedDeliveryAt": "2026-07-24T09:30:00Z",
        "status": "READY"
      }
    }
  ]
}
```

The source must return an empty array or a final page smaller than `size`.

### 3.2 Projection event receiver

The order batch sends:

```http
POST /v1/credit-projection/order-events
Idempotency-Key: batch:<sha256>
```

```json
{
  "eventKey": "order-10001",
  "orderKey": "order-10001",
  "customerKey": "customer-77",
  "createdAt": "2026-07-23T10:20:00Z",
  "totalAmount": 420000000,
  "currency": "IRR",
  "maximumOverdueDays": 12,
  "loadingKey": "loading-50001",
  "promisedDeliveryAt": "2026-07-24T09:30:00Z",
  "deliveryStatus": "READY"
}
```

Required receiver behavior:

- same `Idempotency-Key` or `eventKey` always produces the same effect
- never increment customer totals twice
- return `2xx` for an already-applied duplicate
- return `429` or `5xx` for retryable infrastructure failures
- return permanent `4xx` only for data that should be quarantined

### 3.3 Projection finalization and scoring fan-out

After the order batch completes, automation calls:

```json
{
  "sourceRunKey": "2026-07-24T04:30:00Z",
  "batchRunId": "43dc...",
  "expectedWriteCount": 99998,
  "scoreFlowKey": "customer-credit-score-v1"
}
```

The projection service closes the projection window and starts one internal
automation execution per customer. The execution key must be stable:

```bash
curl -fSs -u automation_orchestrator_internal:automation_orchestrator_secret \
  -H 'Content-Type: application/json' \
  -H 'X-Tenant-Key: importer-demo' \
  -H 'X-Site-Key: main-site' \
  --data '{
    "flowKey":"customer-credit-score-v1",
    "automationFlowKey":"customer-credit-score-v1",
    "executionMode":"ASYNC",
    "failurePolicy":"MARK_FAILED",
    "idempotencyKey":"credit:assessment-2026-07-24-customer-77",
    "tenantKey":"importer-demo",
    "siteKey":"main-site",
    "input":{
      "assessmentKey":"assessment-2026-07-24-customer-77",
      "customerKey":"customer-77",
      "assessedAt":"2026-07-24T05:00:00Z",
      "currency":"IRR",
      "sourceRunKey":"2026-07-24T04:30:00Z",
      "metrics":{
        "orderCount90d":42,
        "activeDays90d":26,
        "orderFrequencyPerActiveDay":1.615,
        "totalAmount90d":2400000000,
        "averageOrderAmount90d":57142857,
        "maximumOverdueDays":12,
        "failedDeliveryCount90d":6
      }
    }
  }' \
  http://automation-orchestrator-service:9120/internal/automation-orchestrator/executions/start
```

Calling this twice does not create a second non-failed execution because the
automation execution idempotency key is tenant/site scoped.

### 3.4 Due-loading source and delivery receiver

The batch reads:

```http
GET /v1/loadings/due?window=MORNING&status=READY&page=0&size=250
```

It sends one mapped dispatch body per loading:

```json
{
  "dispatchKey": "loading-50001",
  "loadingKey": "loading-50001",
  "customerKey": "customer-77",
  "recipient": {
    "name": "Customer Warehouse",
    "mobile": "09120000000"
  },
  "address": {
    "city": "Tehran",
    "postalCode": "1234567890"
  },
  "promisedDeliveryAt": "2026-07-24T09:30:00Z",
  "packages": [
    { "packageKey": "package-1", "weight": 12.5 }
  ]
}
```

The delivery API must enforce the supplied `Idempotency-Key`. This is what makes
batch restart safe after a timeout where the remote write may have succeeded but
the local checkpoint was not committed.

## 4. Provision the scenario

Run these commands from the repository root or copy the example directory into an
administration pod. These examples call Kubernetes Services directly; no platform
gateway or discovery server is required.

```bash
export TENANT_KEY=importer-demo
export SITE_KEY=main-site
export TOKEN='<builder bearer token>'
export EXAMPLE_DIR='docs/examples/importer-credit-delivery'

export REPORT_URL='http://report-service:9107'
export BPM_URL='http://bpm-service:9119'
export PROCESSOR_URL='http://processor-service:9108'
export AUTOMATION_URL='http://automation-orchestrator-service:9120'
export BATCH_URL='http://batch-worker-service:9127'
```

Do not store real bearer tokens or connector secrets in the JSON files.

### 4.1 Create the projection-event and strict credit-report entities

```bash
curl -fSs \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -H "X-Tenant-Key: $TENANT_KEY" \
  -H "X-Site-Key: $SITE_KEY" \
  --data-binary "@$EXAMPLE_DIR/customer-credit-order-event-definition.json" \
  "$REPORT_URL/endpoint/entities/definitions"

curl -fSs \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -H "X-Tenant-Key: $TENANT_KEY" \
  -H "X-Site-Key: $SITE_KEY" \
  --data-binary "@$EXAMPLE_DIR/customer-credit-report-definition.json" \
  "$REPORT_URL/endpoint/entities/definitions"
```

### 4.2 Create BPM form entities

```bash
curl -fSs \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -H "X-Tenant-Key: $TENANT_KEY" \
  -H "X-Site-Key: $SITE_KEY" \
  --data-binary "@$EXAMPLE_DIR/customer-credit-review-definition.json" \
  "$BPM_URL/endpoint/entities/definitions"

curl -fSs \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -H "X-Tenant-Key: $TENANT_KEY" \
  -H "X-Site-Key: $SITE_KEY" \
  --data-binary "@$EXAMPLE_DIR/delivery-exception-definition.json" \
  "$BPM_URL/endpoint/entities/definitions"
```

These definitions intentionally use nested `object` fields and strict field
declarations. Extra undeclared fields are rejected.

### 4.3 Create the reusable processor

```bash
curl -fSs \
  -H 'Content-Type: application/json' \
  --data-binary "@$EXAMPLE_DIR/credit-review-processor.json" \
  "$PROCESSOR_URL/api/processor-service/processors"
```

The BPM submission sequence is:

1. run `credit-review-normalizer`
2. stop when processor validation fails
3. validate the processed payload against `customer-credit-review`
4. persist only after both stages succeed

Test it independently:

```bash
curl -fSs \
  -H 'Content-Type: application/json' \
  --data '{
    "targetType":"BPM_FORM",
    "payload":{
      "assessmentKey":"assessment-2026-07-24-customer-77",
      "customerKey":" customer-77 ",
      "recommendedLimit":300000000,
      "approvedLimit":250000000,
      "riskBand":"HIGH",
      "reviewerDecision":"reduce",
      "reviewerComment":"  approved after collateral review  "
    }
  }' \
  "$PROCESSOR_URL/api/processor-service/processors/credit-review-normalizer/run"
```

Expected: `valid=true`, trimmed strings, and `reviewerDecision=REDUCE`.

### 4.4 Create the batch definitions

```bash
curl -fSs -u batch_worker_internal:batch_worker_secret \
  -H 'Content-Type: application/json' \
  -H "X-Tenant-Key: $TENANT_KEY" \
  -H "X-Site-Key: $SITE_KEY" \
  --data-binary "@$EXAMPLE_DIR/order-projection-batch-platform.json" \
  "$BATCH_URL/internal/batch/definitions"

curl -fSs -u batch_worker_internal:batch_worker_secret \
  -H 'Content-Type: application/json' \
  -H "X-Tenant-Key: $TENANT_KEY" \
  -H "X-Site-Key: $SITE_KEY" \
  --data-binary "@$EXAMPLE_DIR/due-loading-dispatch-batch.json" \
  "$BATCH_URL/internal/batch/definitions"
```

For the platform-owned source, these referenced secrets must exist only in the
batch-worker pod:

```text
COMMERCE_SERVICE_INTERNAL_PASSWORD
REPORT_SERVICE_INTERNAL_PASSWORD
DELIVERY_API_TOKEN
```

To test the external-client variant instead, save
`order-projection-batch.json`. That variant references:

```text
IMPORTER_API_TOKEN
PROJECTION_API_TOKEN
```

### 4.5 Create and activate BPM flows

```bash
curl -fSs -u bpm_internal:bpm_secret \
  -H 'Content-Type: application/json' \
  -H "X-Tenant-Key: $TENANT_KEY" \
  -H "X-Site-Key: $SITE_KEY" \
  --data-binary "@$EXAMPLE_DIR/customer-credit-review-bpm-flow.json" \
  "$BPM_URL/internal/bpm/flows"

curl -fSs -u bpm_internal:bpm_secret \
  -H 'Content-Type: application/json' \
  -H "X-Tenant-Key: $TENANT_KEY" \
  -H "X-Site-Key: $SITE_KEY" \
  --data-binary "@$EXAMPLE_DIR/delivery-exception-bpm-flow.json" \
  "$BPM_URL/internal/bpm/flows"

curl -fSs -u bpm_internal:bpm_secret -X POST \
  -H "X-Tenant-Key: $TENANT_KEY" \
  -H "X-Site-Key: $SITE_KEY" \
  "$BPM_URL/internal/bpm/flows/customer-credit-manual-review-v1/activate/1"

curl -fSs -u bpm_internal:bpm_secret -X POST \
  -H "X-Tenant-Key: $TENANT_KEY" \
  -H "X-Site-Key: $SITE_KEY" \
  "$BPM_URL/internal/bpm/flows/delivery-batch-exception-review-v1/activate/1"
```

### 4.6 Create, approve, and activate automation flows

```bash
for file in \
  morning-credit-master-flow.json \
  customer-credit-score-flow.json \
  morning-delivery-flow.json
do
  curl -fSs -u automation_orchestrator_internal:automation_orchestrator_secret \
    -H 'Content-Type: application/json' \
    -H "X-Tenant-Key: $TENANT_KEY" \
    -H "X-Site-Key: $SITE_KEY" \
    --data-binary "@$EXAMPLE_DIR/$file" \
    "$AUTOMATION_URL/internal/automation-flows"
done
```

Use lifecycle actions for each flow:

```bash
for flow in \
  morning-importer-credit-master-v1 \
  customer-credit-score-v1 \
  morning-importer-delivery-v1
do
  for action in SUBMIT APPROVE ACTIVATE
  do
    curl -fSs -u automation_orchestrator_internal:automation_orchestrator_secret \
      -X POST \
      -H "X-Tenant-Key: $TENANT_KEY" \
      -H "X-Site-Key: $SITE_KEY" \
      "$AUTOMATION_URL/internal/automation-flows/$flow/versions/1/$action"
  done
done
```

The schedules are:

```text
0 0 8 * * *   Asia/Tehran   credit ingestion
0 30 8 * * *  Asia/Tehran   delivery dispatch
```

The six fields are second, minute, hour, day-of-month, month, day-of-week.

## 5. Run and inspect each step

### 5.1 Run the order batch directly

Use a stable run key for one logical occurrence:

```bash
ORDER_RUN=$(
  curl -fSs -u batch_worker_internal:batch_worker_secret \
    -H 'Content-Type: application/json' \
    -H "X-Tenant-Key: $TENANT_KEY" \
    -H "X-Site-Key: $SITE_KEY" \
    --data '{"runKey":"2026-07-24T04:30:00Z"}' \
    "$BATCH_URL/internal/batch/definitions/importer-order-projection-v1/runs" |
  jq -r '.id'
)
```

Poll:

```bash
curl -fSs -u batch_worker_internal:batch_worker_secret \
  -H "X-Tenant-Key: $TENANT_KEY" \
  -H "X-Site-Key: $SITE_KEY" \
  "$BATCH_URL/internal/batch/runs/$ORDER_RUN" | jq
```

Expected terminal response:

```json
{
  "status": "COMPLETED",
  "readCount": 100000,
  "writeCount": 99998,
  "skipCount": 2
}
```

Inspect quarantined permanent failures:

```bash
curl -fSs -u batch_worker_internal:batch_worker_secret \
  -H "X-Tenant-Key: $TENANT_KEY" \
  -H "X-Site-Key: $SITE_KEY" \
  "$BATCH_URL/internal/batch/runs/$ORDER_RUN/rejected-items" | jq
```

Retry a failed run:

```bash
curl -fSs -u batch_worker_internal:batch_worker_secret -X POST \
  -H "X-Tenant-Key: $TENANT_KEY" \
  -H "X-Site-Key: $SITE_KEY" \
  "$BATCH_URL/internal/batch/runs/$ORDER_RUN/retry" | jq
```

Remote duplicates remain safe only if the projection receiver enforces the
idempotency key.

### 5.2 Run the master credit automation manually

```bash
CREDIT_EXECUTION=$(
  curl -fSs \
    -H "Authorization: Bearer $TOKEN" \
    -H 'Content-Type: application/json' \
    -H "X-Tenant-Key: $TENANT_KEY" \
    -H "X-Site-Key: $SITE_KEY" \
    --data '{
      "environment":"production",
      "async":true,
      "input":{
        "scheduledAt":"2026-07-24T04:30:00Z",
        "triggeredAt":"2026-07-24T04:30:01Z"
      }
    }' \
    "$AUTOMATION_URL/endpoint/automation-orchestrator/flows/morning-importer-credit-master-v1/manual-run" |
  jq -r '.executionId'
)
```

Inspect the execution and node steps:

```bash
curl -fSs \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-Key: $TENANT_KEY" \
  -H "X-Site-Key: $SITE_KEY" \
  "$AUTOMATION_URL/endpoint/automation-orchestrator/executions/$CREDIT_EXECUTION" | jq

curl -fSs \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-Key: $TENANT_KEY" \
  -H "X-Site-Key: $SITE_KEY" \
  "$AUTOMATION_URL/endpoint/automation-orchestrator/executions/$CREDIT_EXECUTION/steps" | jq
```

While the batch is active, the automation status is `WAITING`, with a durable
resume time and batch run ID in execution context. A pod restart does not start a
new batch because the execution checkpoint remembers that ID.

### 5.3 Test one real Zen credit decision

```bash
curl -fSs \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -H "X-Tenant-Key: $TENANT_KEY" \
  -H "X-Site-Key: $SITE_KEY" \
  --data '{
    "environment":"production",
    "async":false,
    "input":{
      "assessmentKey":"assessment-2026-07-24-customer-77",
      "customerKey":"customer-77",
      "assessedAt":"2026-07-24T05:00:00Z",
      "currency":"IRR",
      "sourceRunKey":"2026-07-24T04:30:00Z",
      "metrics":{
        "orderCount90d":42,
        "activeDays90d":26,
        "orderFrequencyPerActiveDay":1.615,
        "totalAmount90d":2400000000,
        "averageOrderAmount90d":57142857,
        "maximumOverdueDays":12,
        "failedDeliveryCount90d":6
      }
    }
  }' \
  "$AUTOMATION_URL/endpoint/automation-orchestrator/flows/customer-credit-score-v1/manual-run" | jq
```

Because failed deliveries are at least five, the expected decision is:

```json
{
  "riskBand": "HIGH",
  "manualReview": true,
  "creditLimit": 120000000,
  "reason": "Repeated failed deliveries"
}
```

The flow then upserts the report record and creates a BPM object in
`finance-review`.

### 5.4 Read and submit the BPM review

List finance work:

```bash
curl -fSs \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-Key: $TENANT_KEY" \
  -H "X-Site-Key: $SITE_KEY" \
  "$BPM_URL/endpoint/bpm/managed-objects/assigned-to-me" | jq
```

Assuming the object ID is in `BPM_OBJECT_ID`:

```bash
curl -fSs \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-Key: $TENANT_KEY" \
  -H "X-Site-Key: $SITE_KEY" \
  "$BPM_URL/endpoint/bpm/managed-objects/$BPM_OBJECT_ID/active-form" | jq
```

Submit the active dynamic form:

```bash
curl -fSs \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -H "X-Tenant-Key: $TENANT_KEY" \
  -H "X-Site-Key: $SITE_KEY" \
  --data '{
    "formData":{
      "assessmentKey":"assessment-2026-07-24-customer-77",
      "customerKey":" customer-77 ",
      "recommendedLimit":120000000,
      "approvedLimit":100000000,
      "riskBand":"HIGH",
      "reviewerDecision":"reduce",
      "reviewerComment":"  approved with reduced exposure  "
    },
    "nextState":"approved",
    "context":{"reviewTicket":"FIN-2026-441"}
  }' \
  "$BPM_URL/endpoint/bpm/managed-objects/$BPM_OBJECT_ID/active-form/submissions" | jq
```

This call demonstrates the full processor -> strict validation -> persistence ->
state transition sequence.

### 5.5 Run delivery and inspect exceptions

```bash
DELIVERY_RUN=$(
  curl -fSs -u batch_worker_internal:batch_worker_secret \
    -H 'Content-Type: application/json' \
    -H "X-Tenant-Key: $TENANT_KEY" \
    -H "X-Site-Key: $SITE_KEY" \
    --data '{"runKey":"2026-07-24T05:00:00Z"}' \
    "$BATCH_URL/internal/batch/definitions/importer-due-loading-dispatch-v1/runs" |
  jq -r '.id'
)

curl -fSs -u batch_worker_internal:batch_worker_secret \
  -H "X-Tenant-Key: $TENANT_KEY" \
  -H "X-Site-Key: $SITE_KEY" \
  "$BATCH_URL/internal/batch/runs/$DELIVERY_RUN" | jq

curl -fSs -u batch_worker_internal:batch_worker_secret \
  -H "X-Tenant-Key: $TENANT_KEY" \
  -H "X-Site-Key: $SITE_KEY" \
  "$BATCH_URL/internal/batch/runs/$DELIVERY_RUN/rejected-items" | jq
```

The scheduled delivery automation performs the same run and opens one BPM
`DELIVERY_BATCH_EXCEPTION` when `skipCount > 0`.

## 6. Failure tests

Use a staging receiver or a controllable mock.

### Source pagination failure

Return `503` on page 37 twice, then `200`.

Expected:

- the page is retried
- earlier committed chunks are not reread after restart
- the run completes without duplicate receiver effects

### Destination timeout after commit

Persist the projection event, then close the connection before returning a response.

Expected:

- batch retries the item
- receiver sees the same `Idempotency-Key`
- aggregate increments once

### Permanent bad order

Return `422` for one malformed order projection event.

Expected:

- item is skipped up to `skipLimit`
- rejected payload and reason appear under `/rejected-items`
- other chunks continue

### Too many bad records

Return `422` for more than `skipLimit`.

Expected:

- the run fails
- automation records the batch failure
- operations can correct the receiver/data and retry the run

### Pod termination

Terminate the batch-worker pod after several committed chunks.

Expected:

- a replica reclaims the stale run
- Spring Batch resumes from the persisted page/index checkpoint
- stable receiver idempotency prevents repeated remote effects

### Duplicate schedule delivery

Trigger the same scheduled timestamp twice.

Expected:

- automation schedule idempotency reuses the logical execution
- the same batch `runKey` resolves to the same logical run
- destination effects remain unique per loading

### Credit-rule exception

Send a customer with `maximumOverdueDays=120`.

Expected:

- `riskBand=BLOCKED`
- `creditLimit=0`
- `manualReview=true`
- BPM finance review is created

## 7. Automated repository tests

Run all scenario tests:

```bash
bash ./gradlew \
  :processor-service:test \
  :batch-worker-service:test \
  :automation-orchestrator-service:test \
  :bpm-service:test
```

The scenario-specific tests verify:

- processor normalization and validation failure
- paginated order ingestion
- field mappings
- per-item stable idempotency keys
- customer clustering receiver contract (mock contract, not a platform implementation)
- paginated due-loading dispatch
- real Zen/JDM evaluation
- batch WAIT/resume behavior in automation
- report persistence call
- BPM review creation
- delivery skip-count routing
- BPM form/entity/processor wiring
- tenant/site scoping
- rejected-item endpoint scope checking

## 8. Important guarantees and limits

- Batch checkpoints make local processing restartable.
- Stable run keys make one schedule occurrence identifiable.
- Automation and batch records are tenant/site scoped.
- Remote exactly-once behavior exists only when the destination enforces the
  `Idempotency-Key`.
- A database transaction cannot include an arbitrary remote HTTP API.
- `Mono` or reactive execution does not replace durable batch metadata.
- Large raw order sets belong in batch/projection storage, not automation variables
  or BPM payloads.
- BPM should store the decision context and references needed for review, not all
  100,000 orders.
- Connector credentials belong in Kubernetes Secrets/environment variables and
  connector credential records, never in flow JSON.

The inline credit policy uses GoRules JSON Decision Model, following the official
[JDM node and decision-table format](https://docs.gorules.io/developers/jdm/node-types).

## 9. Professional processor and operator toolkit

Complex finance and risk logic must not be embedded in batch readers, BPM state
transitions, or entity persistence code. Add it through a versioned processor
toolkit with these extension categories:

| Extension | Good examples | State |
|---|---|---|
| Entity operator | normalize IBAN, round money, derive a field | Stateless and deterministic |
| Validator | exposure limit, date consistency, accounting balance | Stateless and deterministic |
| Rules adapter | Zen/GoRules JDM, Drools decision table | Versioned rule artifact |
| Analytics processor | 90-day metrics, RFM, clustering, anomaly detection | May read bounded datasets and write projections |
| Model processor | default probability, forecast, classification | Versioned model with explainability |
| AI processor | document extraction or advisory classification | Optional, policy-controlled, never the only control for critical finance writes |

The platform tooling should provide:

- a Java SPI/SDK for typed validators, operators, and processors
- input/output JSON Schema and entity-definition compatibility checks
- a registry containing `processorKey`, immutable version, implementation type,
  checksum, capabilities, and lifecycle (`DRAFT`, `TESTED`, `APPROVED`, `RETIRED`)
- a simulation API and web-panel test bench with fixtures and expected results
- deterministic replay using the exact rule/model version and parameters
- timeout, input-size, memory, concurrency, and tenant allow-list controls
- structured explanation output: decisions, contributing features, thresholds,
  warnings, and rule/model version
- audit records without secrets or sensitive raw prompt leakage
- adapters for local Java, Zen/JDM, Drools, ONNX/PMML, remote model APIs, and AI
- approval gates for finance/risk processors before production activation

A flow should reference a stable logical key and version policy:

```json
{
  "processorKey": "customer-credit-score",
  "version": "1.3.0",
  "executionMode": "DETERMINISTIC",
  "inputEntityKey": "customer-credit-projection",
  "outputEntityKey": "customer-credit-report",
  "idempotencyKey": "credit:{{assessmentKey}}",
  "explanationRequired": true
}
```

AI-assisted provisioning may select or configure an approved processor, but the
same processor must also be runnable without AI. AI must not generate and activate
unreviewed financial code or rules in one step.

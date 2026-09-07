# Automation and Batch Services: Apache NiFi Feature Comparison

Assessment date: 2026-09-07.

This document records the repository implementation reviewed for the automation,
batch, processor, and business-event services. It is a source-code assessment,
not a production certification or a throughput benchmark. A capability listed
as supported means the described behavior exists in code; it does not imply
complete compatibility with the corresponding NiFi feature.

## Current platform shape

The platform combines workflow automation, Spring Batch jobs, and domain Kafka
consumers. It supports REST/JSON ETL and business orchestration, but does not yet
provide the full connector, queue, content repository, and provenance model of
Apache NiFi.

| Component | Responsibility |
| --- | --- |
| `automation-orchestrator-service` | Flow definitions, schedules, graph/item execution, API calls, waits, callbacks, subflows, and execution history |
| `batch-worker-service` | Durable paginated API extraction, field mapping, chunk processing, HTTP delivery, checkpoints, and rejected items |
| `processor-service` | Field transformations and validation through a service API |
| `event-service` | Idempotent business-event storage and Kafka publication |
| CRM, finance, inventory, and report automation services | Fixed business-event consumers that persist domain action records or projections |

Scheduling belongs to automation. For durable ETL, automation calls the batch
worker using `RUN_BATCH_JOB`, then polls the run until it completes. Batch owns
its source and destination HTTP calls. Platform business-event fan-out follows
the source outbox → `event-service` → Kafka → domain consumer path.

## Capability matrix

- **Supported:** Implemented within the scope described in the last column.
- **Partial:** A narrower equivalent exists, or an external service is required.
- **Not supported:** No native implementation was found in the reviewed services.
  Calling an adapter through HTTP does not make its underlying protocol native.

### Extraction, transformation, and loading

| Capability | Status | Current implementation and limits |
| --- | --- | --- |
| HTTP/API extraction | Supported | Internal service calls and external HTTP requests; the batch reader uses paginated HTTP GET |
| Paginated JSON extraction | Supported | Batch uses page/size query parameters and an `itemsPath`; automation also has `PAGINATED_CALL_API` |
| Field mapping | Supported | Nested source paths and nested destination fields; batch transformation is limited to this mapping |
| JSON transformation | Supported | Automation mappings, templates, and expressions |
| Filtering and conditional routing | Supported | Automation `IF`, `SWITCH`, and item-mode `FILTER` |
| Split and fan-out | Supported | Array splitting and multiple outgoing item-mode edges; this is not distributed parallel processing |
| Iteration | Supported | Item-mode loops with feedback edges; variable-mode `FOR_EACH` maps an existing array |
| Merge and join | Partial | Item-mode append, combine by position, combine by field, and branch selection; no general relational join engine |
| Aggregation | Partial | Grouped collect/list, first, last, count, sum, min, and max; no built-in `AVG` implementation |
| Sorting and limiting | Partial | Item-mode sort and limit exist; standalone `SORT` compares string representations |
| Deduplication | Partial | Deduplicates an in-memory collection; no general persistent cross-run duplicate cache |
| Custom code | Partial | Variable-mode SpEL and item-mode expressions; JavaScript/Python code requires an external script runner |
| Data validation | Partial | Automation input/output schema validation and processor-service field rules; no generic batch record validation stage or schema registry |
| Lookup/enrichment | Partial | Can call another service and map its result; no shared lookup/cache service framework |
| REST-to-REST ETL | Supported | Paginated JSON reader → field mapping → per-item HTTP writer |
| Chunk processing | Supported | Configurable Spring Batch chunks with persisted job/step metadata |
| Database ETL | Not supported | No configurable JDBC source, SQL query processor, or database destination writer; internal PostgreSQL persistence is not an ETL connector |
| Incremental extraction and CDC | Not supported | No native high-water-mark source, database log CDC, or generic cursor/token pagination |
| File/SFTP/FTP ingestion | Not supported | No native list, fetch, watch, tail, or write connector |
| Binary/file processing | Partial | File references and metadata can pass through automation; `FILE_METADATA` does not fetch or transform file contents |
| Object-store connectors | Not supported | No native S3, GCS, Azure Blob, or HDFS ETL nodes |
| Record-format conversion | Not supported | No generic CSV, XML, Avro, Parquet, or Excel record reader/writer framework |
| Configurable Kafka source/sink | Not supported | Domain Kafka consumers exist separately; automation flows cannot configure arbitrary Kafka ingestion or publication nodes |
| Other messaging protocols | Not supported | No native AMQP, JMS, or MQTT flow nodes |

Implementation references: [batch definition contract][batch-spec],
[batch reader][batch-reader], [batch writer][batch-writer],
[batch job factory][batch-job], [graph runtime][graph-runtime],
[item-stream runtime][item-runtime], and [processor implementation][processor].
NiFi's corresponding connector and record-processing options are documented in
its [component catalog](https://nifi.apache.org/components/), including
[incremental database queries](https://nifi.apache.org/components/org.apache.nifi.processors.standard.QueryDatabaseTableRecord/)
and [lookup services](https://nifi.apache.org/components/org.apache.nifi.processors.standard.LookupRecord/).

### Orchestration, reliability, and operations

| Capability | Status | Current implementation and limits |
| --- | --- | --- |
| Visual flow design | Supported | Node/edge definitions and a React Flow builder; UI presence alone does not establish runtime support |
| Manual and webhook triggers | Supported | Manual execution and public webhook entry points |
| Scheduling | Supported | Cron with timezone or fixed intervals, owned by automation |
| Error workflow | Supported | Error workflow references and `ERROR_TRIGGER` |
| Wait and callback resume | Supported | Persisted timed waits and callback waits |
| Reusable subflows | Supported | Child executions and parent wait/resume; runtime restrictions apply |
| Rule and AI operations | Supported | JDM decision evaluation in variable mode and calls to AI orchestration; AI operations depend on provider configuration |
| Flow lifecycle | Supported | Versioned definitions, approval, activation, and environment promotion |
| Credential references | Supported | Automation Basic/Bearer/API-key credentials; batch Basic/Bearer credentials resolved from environment variables |
| Tenant/site scope | Supported | Automation and batch definitions/runs carry scope; outbound batch headers are configured on definitions |
| Checkpoint and restart | Supported | Automation checkpoints and worker leases; batch job metadata and source page/index checkpoints |
| Retry and failure routing | Supported | Automation node retries/backoff/fallback; batch configures retries for retryable API exceptions |
| Rejected items | Supported | Batch destination 4xx responses, except 429, can be skipped and persisted under the skip limit |
| Dead letters and requeue | Supported | Automation execution dead letters and requeue APIs; separate from batch rejected items |
| Stable HTTP delivery keys | Supported | Batch derives an idempotency key from run scope and item identity |
| Exactly-once remote effects | Not guaranteed | Remote HTTP writes are outside the local database transaction; destination idempotency behavior determines duplicate suppression |
| Execution history and metrics | Supported | Execution/run history, step snapshots, errors, counters, and automation metrics |
| Retry/cancel controls | Partial | Automation exposes retry and cancel; batch exposes run retry but no public stop/cancel endpoint in its controller |
| Data provenance | Partial | Execution snapshots and item pairing exist; no independent searchable object-lineage repository across transformations and services |
| Historical content replay | Not supported | Failure retry/requeue does not provide replay of arbitrary archived provenance content |
| Persistent FlowFile/content repository | Not supported | Execution state and batch metadata do not provide NiFi's separate content/FlowFile storage model |
| Durable per-connection queues | Not supported | Item work queues are part of execution state; edges have no independent queue repository |
| Backpressure | Not supported | No configurable per-edge byte/item thresholds that stop upstream processing |
| Queue priority and expiration | Not supported | No per-edge prioritizers, age-off, or queue inspection/management API |
| Clustered execution | Partial | Worker claims and leases support distributing separate runs; no partitioning of one batch job or NiFi-style connection load balancing |
| Controller Services | Not supported | No generic reusable connection-pool, schema-reader/writer, or lookup-service framework |
| Extension SDK | Not supported | New native nodes/connectors require code changes and deployment |
| NiFi Site-to-Site | Not supported | No remote process groups or Site-to-Site protocol |
| NiFi flow/Registry compatibility | Not supported | Internal versions and a subset of n8n import/export exist; NiFi flows and extensions cannot be imported or executed |

Implementation references: [scheduling][schedule], [flow lifecycle][flow-service],
[execution management][execution-service], [checkpoints][checkpoints],
[batch dispatcher][batch-dispatcher], [batch APIs][batch-controller], and
[visual builder][builder]. NiFi's persistent delivery, queue backpressure,
provenance, and extension model are described in its official
[overview](https://nifi.apache.org/docs/nifi-docs/html/overview).

## Runtime boundaries that affect ETL design

The two automation runtime modes expose different behavior:

| Runtime | ETL-relevant behavior |
| --- | --- |
| `VARIABLES` | Shared variables, mappings, API calls, pagination, JDM decisions, and durable batch orchestration through `RUN_BATCH_JOB` |
| `N8N_ITEMS` | Item arrays, filtering, splitting, aggregation, sorting, merging, looping, and per-item API calls |

`RUN_BATCH_JOB`, `PAGINATED_CALL_API`, and `JDM_DECISION` are not implemented in
`N8N_ITEMS`. In `VARIABLES`, `MERGE` is a pass-through node, and `FOR_EACH` maps
an already-loaded array rather than running a downstream branch once per item.
Item-mode child workflow execution currently requires an item-mode child.

The name "item stream" does not imply bounded continuous streaming. Item arrays,
loop state, and node outputs are retained in execution state. Variable-mode
pagination also accumulates responses and items. Large datasets should not be
loaded into automation state merely because a loop has a chunk-size setting.

The durable batch worker instead reads pages and processes chunks. Its built-in
processor only maps fields: the richer automation transformations do not
automatically become streaming Spring Batch processors. Large joins, global
sorts, and aggregations require a suitable external service or additional worker
implementation.

The runtime implementation also takes precedence over node metadata. For
example, `AVG` is advertised in aggregation metadata but absent from the grouped
aggregation switch; ungrouped aggregation collects values. See the
[item-stream implementation][item-runtime].

## Durable REST ETL behavior

```text
Automation schedule / manual start
  → RUN_BATCH_JOB (VARIABLES)
  → batch-worker-service
      → paginated HTTP GET
      → JSON array extraction using itemsPath
      → nested field mapping
      → chunk processing and checkpointing
      → per-item HTTP destination call
          → retryable failure: network / HTTP 429 / HTTP 5xx
          → skippable rejection: other destination HTTP 4xx
  → automation polls run status and continues or handles failure
```

The source reader starts at page zero, requests configurable page/size parameters,
and stops on an empty or short page. Checkpoints persist the page and item index.
Correct restart therefore depends on a source whose pagination remains stable
enough to read the same logical records after a restart. This is not a source
snapshot, a CDC offset, or a cross-run incremental watermark.

The writer sends one HTTP request per item, including within a chunk. If some
requests succeed before a chunk fails, those remote effects cannot be rolled
back by the local database transaction. A retry can repeat them.

The writer supplies a stable `Idempotency-Key` based on tenant, site, definition,
run key, and item identity. If no configured item key resolves, it hashes the
serialized payload. Duplicate suppression depends on the destination honoring
that key. A different run key produces a different delivery scope.

Batch rejected-item inspection and run retry are available. A dedicated API to
replay only selected rejected items is not present. The worker dispatcher permits
one active run per worker instance; the job factory does not configure partitioning
or parallel chunk workers.

## Business-event automation is a separate capability

The event path is:

```text
Business record + local outbox
  → event-service storage
  → Kafka publication
  → independent CRM / finance / inventory / report consumer groups
```

The reviewed domain consumers filter relevant entity types, check event keys,
and persist action records or projection records. Labels such as
`STOCK_SIDE_EFFECT` or `FINANCE_SETTLEMENT_SYNC` do not themselves implement an
inventory mutation or settlement API call. These consumers are not configurable
general-purpose Kafka ETL flows.

See [Kafka automation architecture][kafka-doc] and the
[CRM][crm-consumer], [finance][finance-consumer],
[inventory][inventory-consumer], and [report][report-consumer] consumers.
The outbox boundaries are retryable, but the full chain is not a single atomic
distributed transaction.

## Practical coverage and remaining work

Current code covers scheduled API imports, REST record synchronization, nested
field mapping, failure quarantine, and workflows coordinating batch jobs with
rules or service calls. Smaller datasets can use richer item-mode transforms.

Database-to-database ETL, file imports, CDC, Kafka-to-database pipelines, large
binary transfers, and format conversion require additional native support or
HTTP adapters. Direct database access should remain appropriate to the owning
service's contract; platform business writes should continue through service APIs
where they enforce validation, isolation, and events.

Potential development priorities, not implemented features:

1. Add a connector interface and selected source/destination implementations.
2. Add record readers/writers, schema handling, validation, and streaming transforms.
3. Add cursor/watermark state and source consistency contracts for incremental ETL.
4. Add independent durable queues with backpressure if continuous dataflow is required.
5. Add searchable record lineage and retained-content replay.
6. Add job partitioning, rate controls, and batch cancellation where needed.

Related repository example:
[Importer credit delivery batch/BPM scenario](IMPORTER_CREDIT_DELIVERY_BATCH_BPM_SCENARIO.md).

[batch-spec]: ../batch-worker-service/src/main/java/com/cyancoder/batchworker/api/BatchDefinitionSpec.java
[batch-reader]: ../batch-worker-service/src/main/java/com/cyancoder/batchworker/service/ApiBatchReader.java
[batch-writer]: ../batch-worker-service/src/main/java/com/cyancoder/batchworker/service/ApiBatchWriter.java
[batch-job]: ../batch-worker-service/src/main/java/com/cyancoder/batchworker/service/BatchJobFactory.java
[batch-dispatcher]: ../batch-worker-service/src/main/java/com/cyancoder/batchworker/service/BatchDispatcher.java
[batch-controller]: ../batch-worker-service/src/main/java/com/cyancoder/batchworker/controller/BatchController.java
[graph-runtime]: ../automation-orchestrator-service/src/main/java/com/cyancoder/automationorchestrator/service/GraphAutomationRuntime.java
[item-runtime]: ../automation-orchestrator-service/src/main/java/com/cyancoder/automationorchestrator/service/ItemStreamAutomationRuntime.java
[schedule]: ../automation-orchestrator-service/src/main/java/com/cyancoder/automationorchestrator/service/AutomationScheduleService.java
[flow-service]: ../automation-orchestrator-service/src/main/java/com/cyancoder/automationorchestrator/service/AutomationFlowDefinitionService.java
[execution-service]: ../automation-orchestrator-service/src/main/java/com/cyancoder/automationorchestrator/service/AutomationExecutionService.java
[checkpoints]: ../automation-orchestrator-service/src/main/java/com/cyancoder/automationorchestrator/service/AutomationExecutionCheckpointService.java
[builder]: ../panel-web/components/automation/automation-builder.tsx
[processor]: ../processor-service/src/main/java/com/cyancoder/processor/service/ProcessorExecutionService.java
[kafka-doc]: ../KAFKA_AUTOMATION_ARCHITECTURE.md
[crm-consumer]: ../crm-automation-service/src/main/java/com/cyancoder/crmautomation/service/CrmAutomationConsumer.java
[finance-consumer]: ../finance-automation-service/src/main/java/com/cyancoder/financeautomation/service/FinanceAutomationConsumer.java
[inventory-consumer]: ../inventory-automation-service/src/main/java/com/cyancoder/inventoryautomation/service/InventoryAutomationConsumer.java
[report-consumer]: ../report-automation-service/src/main/java/com/cyancoder/reportautomation/service/ReportAutomationConsumer.java

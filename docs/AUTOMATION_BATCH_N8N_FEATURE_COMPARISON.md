# Automation and Batch Services: n8n Feature Comparison

Assessment date: 2026-09-12.

This document captures the source review of Cyan's automation and batch services.
It compares implemented capabilities with n8n and distinguishes native platform
behavior from compatibility with imported n8n workflows. Cross-engine execution
tests and production benchmarks were not performed for this assessment.

## Current coverage

Cyan supports a subset of n8n's core workflow operations and adds durable REST/JSON
ETL through Spring Batch. The largest gaps are native application connectors,
complete expressions and item linking, webhook behavior, AI agent nodes, and
faithful import/export.

An operation implemented in Cyan does not necessarily behave identically after
importing an n8n workflow. In particular, the current analyzer's `compatible: true`
means that every node type matched its mapping. It does not certify equivalent
execution, node-version compatibility, expressions, credentials, or all options.

## Workflow capability matrix

- **Supported:** The described native behavior exists in source code.
- **Supported subset:** A restricted set of operations or options is implemented.
- **Partial:** Significant behavior differs, or another service is required.
- **Not supported:** No native equivalent was found in the reviewed services.

| n8n feature | Our support | Implemented behavior and limits |
| --- | --- | --- |
| Visual workflow editor | Supported | Canvas, connections, node configuration, save, and run |
| Manual trigger | Supported | Native manual executions |
| Schedule trigger | Supported subset | Cron, timezone, and interval scheduling; not a guarantee that all imported schedule configurations translate |
| Webhook trigger | Partial | POST with a JSON object; no equivalent coverage of custom paths, request methods, or response modes |
| Multiple workflow triggers | Partial | Import selects the first recognized trigger as the entry point |
| HTTP Request | Partial | Methods, headers, query parameters, JSON body, credentials, and timeouts; advanced options and output semantics differ |
| Edit Fields / Set | Supported subset | Assign fields using values/expressions and retain or drop existing fields |
| IF / Filter | Partial | Native comparisons; import retains only the first condition |
| Switch | Partial | Basic routing; imported rules become equality cases |
| Split Out | Supported subset | Split an array into separate items |
| Loop Over Items | Supported subset | Batch size, feedback loop, and done output |
| Merge | Partial | Append, combine by position, combine by one field, and choose branch; not a full relational join engine |
| Aggregate / Summarize | Partial | Native collect/group/count/sum/min/max/first/last; no built-in AVG, and the n8n Summarize node is not recognized |
| Sort | Partial | Single-field standalone sorting compares string representations |
| Limit | Supported subset | Keep a bounded number of items; native option values must be checked after import |
| Remove Duplicates | Partial | Within the supplied collection; no persistent cross-execution duplicate cache |
| Wait | Partial | Native timed and callback waits exist; imported n8n Wait modes are not fully translated |
| Execute Sub-workflow | Partial | Native child workflows work; imported IDs require matching platform flow keys and compatible child definitions |
| Stop And Error | Supported | Explicit execution failure |
| Error workflow | Supported natively | Uses platform errorWorkflowKey; import does not translate n8n's error-workflow setting into it |
| Retry / continue on failure | Supported natively | Retry, backoff, and fallback policies exist; n8n node-level policies are not mapped during import |
| Execution history | Supported | Runs, step inputs/outputs, errors, retry, and cancellation APIs |
| Data pinning | Supported in backend | Item-mode manual runs can substitute pinned node outputs |
| Partial execution | Partial | Can start at a selected node with supplied input; not full editor/debugger equivalence |
| Execution Data | Partial | Custom execution data is stored; search and filtering are narrower |
| Flow governance | Supported natively | Versioned definitions, approval, activation, and environment promotion |
| Credentials | Supported subset | Automation Basic, Bearer, and API-key references; batch environment-based Basic/Bearer authentication |
| Tenant/site scope | Supported natively | Automation and batch definitions/runs carry platform scope; not an implementation of n8n project/workflow sharing semantics |

References: [item runtime][item-runtime], [execution service][execution-service],
[schedule service][schedule], [flow lifecycle][flow-service],
[visual builder][builder], and [credential service][credentials].
For comparison, n8n's [execution management][n8n-executions] includes loading
previous execution data into the editor and choosing the original or currently
saved workflow for a retry.

## Compatibility details that affect imported workflows

| Feature | Current implementation limit |
| --- | --- |
| Expression language | A small interpreter implements selected reference forms, operators, and string methods; it is not the full JavaScript/n8n expression environment |
| Common references | Selected forms of $json, $input, $binary, $vars, $execution, $workflow, $itemIndex, and previous-node references are implemented |
| Variables | Item-mode $vars resolves against execution input; it is not an instance-level n8n variable-management service |
| Item linking | Some pairedItem metadata is recorded, but previous-node lookup chooses an item by position rather than following full lineage |
| JavaScript/Python Code | Requires an external HTTP script runner; automation itself does not execute these languages |
| Respond to Webhook | Records proposed status/headers/body in execution output; the public controller returns AutomationStartResponse rather than applying that HTTP response |
| Form Trigger | Imported as a webhook; no generated n8n form is served |
| HTTP output shape | Keeps the input item and stores the response under response by default; downstream expressions may require adjustment |
| HTTP pagination | Native variable-mode pagination and batch page/size reads exist; imported HTTP Request pagination options are not fully implemented |
| Callback wait import | n8n Wait maps to WAIT, not WAIT_FOR_CALLBACK; native callback support does not establish imported callback-wait compatibility |
| Child workflow import | Execute Sub-workflow is recognized, but Execute Sub-workflow Trigger is absent from the import mapping |
| Node settings | Imported nodes receive no mapped retry, timeout, error, or concurrency policies; preserving workflow settings does not mean executing all of them |
| Export | Normalized internal parameters are emitted without comprehensive reverse translation into n8n schemas; credential bindings are not exported |

For example, `$('Lookup').item.json.status` is recognized, but the interpreter
selects a stored item by the current item index. After filtering, splitting, or
merging, this can select a different record from n8n's linked-item lookup. The
same lookup implementation also handles `$('Lookup').first()` without a separate
first-item selection rule.

Similarly, the presence of `RESPOND_TO_WEBHOOK` does not mean a workflow can
currently serve its chosen HTTP response to the original caller. The runtime
stores the response as output data, while the controller returns an execution
response object.

These limits are visible in the [expression interpreter][expressions],
[public webhook controller][webhooks], and [import/export implementation][compatibility].
n8n's [item-linking documentation][n8n-item-linking] describes the lineage behavior
that a fully compatible implementation would need to preserve.

## Features without a native equivalent

| Feature family | Examples missing in automation/batch |
| --- | --- |
| Application nodes and triggers | Slack, Gmail, Google Sheets, Notion, Salesforce, Shopify, and Telegram connectors |
| Database nodes | Configurable Postgres, MySQL, or MongoDB query/write operations |
| Messaging nodes | Configurable Kafka, RabbitMQ, and MQTT sources/destinations |
| Files and formats | Read/write files, FTP/SFTP, CSV/Excel conversion, XML, compression, and image editing |
| Connector OAuth | Consent, token exchange/refresh, and provider-specific connector authentication flows |
| Community/custom packages | Installing and executing n8n node packages |
| Complete expression libraries | Luxon/date helpers, JMESPath, and the broader n8n helper API |
| Persistent workflow data | n8n-compatible workflow static-data APIs and Data Tables |
| AI agent graphs | Agent/model/tool/memory connections, vector-store/RAG nodes, and evaluation nodes |
| Specialized triggers | Native n8n-compatible Chat, Form, and MCP trigger behavior |
| Source-control integration | n8n-style Git push/pull and workflow diff integration |
| n8n queue-mode compatibility | Executing n8n worker jobs; Cyan instead has its own database claims and worker leases |

Other Cyan services can expose related functionality through HTTP. For example,
platform business-event consumers use Kafka, and other services own forms,
records, notifications, and AI operations. These are not native equivalents of
the corresponding n8n nodes and do not make those nodes importable.

`AI_OPERATION` calls AI orchestration for data transformation, content generation,
or DSL generation. It does not implement n8n's agent/tool/memory graph model,
described in n8n's [agent-pattern reference][n8n-agents]. Likewise, the native
`N8N_WORKFLOW` node calls an external webhook: invoking a separately running n8n
instance does not add its execution engine or connector packages to Cyan.

## Batch ETL and the two automation runtimes

| Runtime/service | Role |
| --- | --- |
| VARIABLES | Shared variables, API calls, mappings, pagination, JDM decisions, and RUN_BATCH_JOB |
| N8N_ITEMS | Imported n8n graphs, item arrays, branching, filtering, merging, loops, and per-item calls |
| batch-worker-service | Paginated REST extraction, nested mapping, chunk checkpoints, remote HTTP writes, retries, and rejected items |

Automation owns scheduling and starts batch work through the batch worker's
internal API. It polls the run and resumes when the job reaches a terminal state.
The batch worker owns its source and destination calls.

```text
Automation schedule / manual start
  → VARIABLES flow: RUN_BATCH_JOB
  → batch-worker-service
      → paginated REST/JSON extraction
      → nested field mapping
      → per-item HTTP delivery within chunk processing
      → checkpoints / retry / rejected-item quarantine
  → automation reads completion or failure and continues
```

`RUN_BATCH_JOB`, `PAGINATED_CALL_API`, and `JDM_DECISION` are unavailable in
`N8N_ITEMS`. Importing `Loop Over Items` does not create a Spring Batch job.
Item-mode arrays, loop state, and node outputs remain in automation execution
state; chunk size on an item loop does not guarantee bounded streaming memory.

The batch worker's processor only performs field mapping. Rich item-mode
transformations do not automatically become streaming batch processors. Its
writer sends stable idempotency keys, but remote effects may repeat after a
failure unless the destination honors those keys. Batch exposes run retry and
rejected-item inspection, but its controller has no stop/cancel endpoint.

References: [graph runtime][graph-runtime], [batch definition][batch-spec],
[batch job factory][batch-job], [batch reader][batch-reader],
[batch writer][batch-writer], and [batch controller][batch-controller].

## Import/export assessment

The analyzer should currently be understood as a node-type recognition check.
Import subsequently applies platform validation, but that does not validate
behavioral parity with n8n. Recognized nodes can lose settings or change semantics.
Unrecognized nodes are rejected by the import API.

The converter preserves selected structure, names, IDs, positions, disabled
flags, settings, pin data, and credential references. It normalizes some node
parameters and imports main connections. This does not provide lossless
round-trip conversion, complete version support, or AI sub-node connections.

The existing compatibility test checks a small Manual Trigger → Set workflow
and rejection of a Slack node. Expression and runtime tests cover selected native
scenarios. They do not establish parity against an actual n8n engine. See the
[compatibility tests][compatibility-tests], [expression tests][expression-tests],
and [item-runtime tests][item-tests].

Workflows authored for Cyan can use its native orchestration and ETL facilities.
Existing n8n workflows require review of expressions, credentials, item shapes,
node options, and triggers before use. Drop-in n8n compatibility is not established.

## Potential implementation priorities

These are proposed follow-up areas, not implemented features:

1. Make import analysis report unsupported versions, options, expressions, and semantic changes.
2. Correct webhook responses, callback-wait conversion, child triggers, and policy imports.
3. Preserve item lineage and implement or explicitly reject unsupported expression forms.
4. Add selected native connectors and OAuth credential lifecycles.
5. Validate imported/exported workflows using shared fixtures against both engines.
6. Connect durable batch execution to item workflows with an explicit runtime contract.

Related documents:

- [NiFi feature comparison](AUTOMATION_BATCH_NIFI_FEATURE_COMPARISON.md)
- [Importer credit delivery batch/BPM scenario](IMPORTER_CREDIT_DELIVERY_BATCH_BPM_SCENARIO.md)
- [Kafka automation architecture](../KAFKA_AUTOMATION_ARCHITECTURE.md)

[compatibility]: ../automation-orchestrator-service/src/main/java/com/cyancoder/automationorchestrator/service/N8nWorkflowCompatibilityService.java
[expressions]: ../automation-orchestrator-service/src/main/java/com/cyancoder/automationorchestrator/service/N8nExpressionService.java
[item-runtime]: ../automation-orchestrator-service/src/main/java/com/cyancoder/automationorchestrator/service/ItemStreamAutomationRuntime.java
[graph-runtime]: ../automation-orchestrator-service/src/main/java/com/cyancoder/automationorchestrator/service/GraphAutomationRuntime.java
[execution-service]: ../automation-orchestrator-service/src/main/java/com/cyancoder/automationorchestrator/service/AutomationExecutionService.java
[schedule]: ../automation-orchestrator-service/src/main/java/com/cyancoder/automationorchestrator/service/AutomationScheduleService.java
[flow-service]: ../automation-orchestrator-service/src/main/java/com/cyancoder/automationorchestrator/service/AutomationFlowDefinitionService.java
[webhooks]: ../automation-orchestrator-service/src/main/java/com/cyancoder/automationorchestrator/controller/PublicAutomationTriggerController.java
[credentials]: ../automation-orchestrator-service/src/main/java/com/cyancoder/automationorchestrator/service/ConnectorCredentialService.java
[builder]: ../panel-web/components/automation/automation-builder.tsx
[batch-spec]: ../batch-worker-service/src/main/java/com/cyancoder/batchworker/api/BatchDefinitionSpec.java
[batch-job]: ../batch-worker-service/src/main/java/com/cyancoder/batchworker/service/BatchJobFactory.java
[batch-reader]: ../batch-worker-service/src/main/java/com/cyancoder/batchworker/service/ApiBatchReader.java
[batch-writer]: ../batch-worker-service/src/main/java/com/cyancoder/batchworker/service/ApiBatchWriter.java
[batch-controller]: ../batch-worker-service/src/main/java/com/cyancoder/batchworker/controller/BatchController.java
[compatibility-tests]: ../automation-orchestrator-service/src/test/java/com/cyancoder/automationorchestrator/service/N8nWorkflowCompatibilityServiceTest.java
[expression-tests]: ../automation-orchestrator-service/src/test/java/com/cyancoder/automationorchestrator/service/N8nExpressionServiceTest.java
[item-tests]: ../automation-orchestrator-service/src/test/java/com/cyancoder/automationorchestrator/service/ItemStreamAutomationRuntimeTest.java
[n8n-executions]: https://docs.n8n.io/workflows/executions/all-executions/
[n8n-item-linking]: https://docs.n8n.io/data/data-mapping/data-item-linking/item-linking-node-building/
[n8n-agents]: https://blog.n8n.io/production-ai-playbook-complex-agent-patterns/

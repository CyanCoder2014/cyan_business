# Automation Reference Guide

`automation-orchestrator-service` owns tenant/site-scoped automation definitions and execution records. BPM invokes it through `RUN_AUTOMATION_BLOCK`.

## Flow definition

```json
{
  "flowKey": "customer-screening",
  "version": 1,
  "name": "Customer Screening",
  "active": true,
  "entryNodeId": "trigger",
  "runtimeMode": "N8N_ITEMS",
  "nodes": [],
  "edges": [],
  "inputsSchema": {},
  "outputsSchema": {},
  "labels": ["screening"],
  "environment": "default",
  "lifecycleStatus": "ACTIVE",
  "requiredRoles": ["ROLE_ADMIN"],
  "settings": {},
  "pinData": {},
  "errorWorkflowKey": "automation-error-handler"
}
```

Definitions support draft, approval, activation, and promotion lifecycle actions. Promotion creates a separate approved version in the target environment; activation retires the previous active version only in that environment. Edge and required node configuration contracts are validated before persistence.

`runtimeMode` selects one of two intentionally separate data models:

- `VARIABLES` preserves the original shared-map runtime and existing definitions.
- `N8N_ITEMS` runs the native item-stream runtime. Data between nodes is an array of items shaped as `{ "json": {}, "binary": {}, "pairedItem": {} }`.

`N8N_ITEMS` flows can start with `WEBHOOK_TRIGGER`, `MANUAL_TRIGGER`, `SCHEDULE_TRIGGER`, or `ERROR_TRIGGER`. `VARIABLES` flows continue to require `WEBHOOK_TRIGGER`.

## Nodes

Native graph nodes are:

- `WEBHOOK_TRIGGER`, `WAIT`, `WAIT_FOR_CALLBACK`
- `CALL_API`, `PAGINATED_CALL_API`, `N8N_WORKFLOW`
- `IF`, `SWITCH`, `MERGE`, `FOR_EACH`, `SUBFLOW`
- `JDM_DECISION`, `MAP_FIELDS`, `JSON_TRANSFORM`
- `FILE_METADATA`, `DEDUP_BY_KEY`, `CODE`, `END`

The `N8N_ITEMS` runtime additionally provides:

- triggers: `MANUAL_TRIGGER`, `SCHEDULE_TRIGGER`, `ERROR_TRIGGER`
- transport and flow: `HTTP_REQUEST`, `LOOP_OVER_ITEMS`, `EXECUTE_WORKFLOW`
- item operations: `EDIT_FIELDS`, `FILTER`, `SPLIT_OUT`, `AGGREGATE`, `SORT`, `LIMIT`, `REMOVE_DUPLICATES`
- execution control: `EXECUTION_DATA`, `RESPOND_TO_WEBHOOK`, `STOP_AND_ERROR`, `NO_OP`

It supports per-item expressions such as `={{ $json.customerId }}`, input and prior-node references, item links, binary metadata, named branch outputs, multi-input merge modes, batch-loop feedback, persisted time/callback waits, node retries, error policies, child workflows, pinned manual data, partial runs, execution history, retry, scheduled production runs, and error workflows.

Every node accepts `credentialRef`, `retryPolicy`, `timeoutPolicy`, `errorPolicy`, and `concurrencyPolicy`. Executions preserve node attempts, input/output snapshots, errors, waits, callbacks, and dead letters.

`CALL_API` accepts either an external `url` or an internal `serviceKey` plus absolute `path`. Internal calls receive service credentials and tenant/site headers automatically.

`N8N_WORKFLOW` invokes an n8n production webhook and therefore delegates connector-specific work to any nodes installed in that n8n instance:

```json
{
  "id": "run-n8n-enrichment",
  "type": "N8N_WORKFLOW",
  "credentialRef": "n8n-bearer",
  "config": {
    "webhookUrl": "https://n8n.example.com/webhook/customer-enrichment",
    "method": "POST",
    "body": { "customerId": "{{customerId}}" },
    "storeResponseAt": "n8n.enrichment"
  }
}
```

This remains available for external interoperability. It is not the native n8n-compatible runtime; use `runtimeMode: N8N_ITEMS` for locally executed graphs.

## n8n workflow compatibility

The platform can analyze, import, and export n8n workflow JSON for the natively implemented core node set:

- `POST /endpoint/automation-flows/n8n/analyze`
- `POST /endpoint/automation-flows/n8n/import?flowKey=customer-sync`
- `GET /endpoint/automation-flows/{flowKey}/versions/{version}/n8n-export`

Import preserves node identity, display name, position, parameters, item output/input indices, settings, pin data, and credential references. Credential secrets are deliberately never imported.

n8n has a large and independently evolving catalog of built-in, app-specific, and community connectors. The compatibility endpoint rejects a workflow containing a node that has no native implementation and reports every unsupported node; it never silently changes that node to a no-op. Connector-specific credentials and behaviors must be implemented and tested in the platform before that connector becomes importable.

JavaScript and Python `CODE` nodes use the item runtime but send untrusted source to the configured isolated runner at `automation.script-runner.url`. They are rejected when no runner is configured. Expression-only `CODE` nodes run locally without that runner.

## BPM bridge

`RUN_AUTOMATION_BLOCK` runs synchronously by default. Set `async: true` for `WAIT` or `WAIT_FOR_CALLBACK` flows.

It supports saved `flowKey` definitions or a complete graph under `inlineFlow`, templated `variables` and `context`, idempotency, output mappings, execution stores, failure policies, and signed completion callbacks. BPM also tracks async runs under `payload.asyncActions.<actionKey>`.

Set `context.environment` when BPM should resolve an active flow outside the `default` environment. Public webhooks use the `X-Automation-Environment` header for the same selection.

## APIs

- `POST/GET /endpoint/automation-flows`
- `GET /endpoint/automation-flows/{flowKey}/active?environment=default`
- `POST /endpoint/automation-flows/{flowKey}/versions/{version}/{submit|approve|activate|promote}`
- `POST /endpoint/automation-orchestrator/executions/start`
- `GET /endpoint/automation-orchestrator/executions?flowKey=&status=`
- `GET /endpoint/automation-orchestrator/executions/{executionId}`
- `POST /endpoint/automation-orchestrator/executions/{executionId}/retry?fromFailedNode=true`
- `POST /endpoint/automation-orchestrator/flows/{flowKey}/manual-run?version=1`
- `GET /endpoint/automation-orchestrator/executions/{executionId}/steps`
- `GET /endpoint/automation-orchestrator/executions/{executionId}/dead-letters`
- `GET /endpoint/automation-orchestrator/metrics`
- `POST/GET /endpoint/automation-orchestrator/credentials`
- `PATCH /endpoint/automation-orchestrator/credentials/{id}/rotate`
- `POST /public/automation-orchestrator/webhooks/{flowKey}`
- `POST /public/automation-orchestrator/executions/{executionId}/nodes/{nodeId}/callback`
- `GET /public/automation-flows/node-structures`
- `GET /public/automation-flows/edge-structures`

The same definition and execution APIs have `/internal/**` variants protected by service Basic authentication.

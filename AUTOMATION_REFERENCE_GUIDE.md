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
  "nodes": [],
  "edges": [],
  "inputsSchema": {},
  "outputsSchema": {},
  "labels": ["screening"],
  "environment": "default",
  "lifecycleStatus": "ACTIVE",
  "requiredRoles": ["ROLE_ADMIN"]
}
```

Definitions support draft, approval, activation, and promotion lifecycle actions. Promotion creates a separate approved version in the target environment; activation retires the previous active version only in that environment. The entry node must be `WEBHOOK_TRIGGER`. Edge and required node configuration contracts are validated before persistence.

## Nodes

Native graph nodes are:

- `WEBHOOK_TRIGGER`, `WAIT`, `WAIT_FOR_CALLBACK`
- `CALL_API`, `PAGINATED_CALL_API`, `N8N_WORKFLOW`
- `IF`, `SWITCH`, `MERGE`, `FOR_EACH`, `SUBFLOW`
- `JDM_DECISION`, `MAP_FIELDS`, `JSON_TRANSFORM`
- `FILE_METADATA`, `DEDUP_BY_KEY`, `CODE`, `END`

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

This provides interoperability with n8n connectors without embedding n8n's separate UI, package manager, or third-party node runtime inside the Java service.

## BPM bridge

`RUN_AUTOMATION_BLOCK` runs synchronously by default. Set `async: true` for `WAIT` or `WAIT_FOR_CALLBACK` flows.

It supports saved `flowKey` definitions or a complete graph under `inlineFlow`, templated `variables` and `context`, idempotency, output mappings, execution stores, failure policies, and signed completion callbacks. BPM also tracks async runs under `payload.asyncActions.<actionKey>`.

Set `context.environment` when BPM should resolve an active flow outside the `default` environment. Public webhooks use the `X-Automation-Environment` header for the same selection.

## APIs

- `POST/GET /endpoint/automation-flows`
- `GET /endpoint/automation-flows/{flowKey}/active?environment=default`
- `POST /endpoint/automation-flows/{flowKey}/versions/{version}/{submit|approve|activate|promote}`
- `POST /endpoint/automation-orchestrator/executions/start`
- `GET /endpoint/automation-orchestrator/executions/{executionId}`
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

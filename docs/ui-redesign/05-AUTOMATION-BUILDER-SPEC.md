# Automation Builder Specification

## 1. Goal

Replace raw JSON-first automation editing with an n8n-style graph editor backed by the existing automation-orchestrator lifecycle and execution APIs.

The UI must expose the native runtime honestly. It must not imply support for connectors or node types the backend cannot execute.

## 2. Route structure

- `/automations` — definitions, statuses, schedules, recent executions
- `/automations/new` — create
- `/automations/[flowKey]` — editor
- `/automations/[flowKey]/executions` — run history
- `/automations/executions/[executionId]` — execution detail

## 3. Desktop editor layout

```text
┌ Header: name, version, environment, lifecycle, save/test/activate ┐
├ Node palette 280 ┬ Graph canvas flex ┬ Inspector 380             ┤
│ search/categories│ pan/zoom/minimap  │ node/edge/workflow tabs   │
├──────────────────┴───────────────────┴───────────────────────────┤
│ Collapsible execution/log drawer                                 │
└──────────────────────────────────────────────────────────────────┘
```

## 4. Mobile editor

- Full-screen canvas with pan/zoom.
- Floating add-node button opens node palette bottom sheet.
- Selecting a node opens a full-height inspector sheet.
- Workflow settings are a separate screen.
- Execution logs open as a sheet.
- Landscape mode is supported but not required.
- All graph actions have non-drag alternatives.

## 5. Node catalog

Native graph nodes documented by the platform include:

### Triggers
- `WEBHOOK_TRIGGER`
- `MANUAL_TRIGGER`
- `SCHEDULE_TRIGGER`
- `ERROR_TRIGGER`

### Wait
- `WAIT`
- `WAIT_FOR_CALLBACK`

### Transport and workflow
- `CALL_API`
- `PAGINATED_CALL_API`
- `HTTP_REQUEST`
- `N8N_WORKFLOW`
- `EXECUTE_WORKFLOW`
- `SUBFLOW`

### Logic and flow
- `IF`
- `SWITCH`
- `MERGE`
- `FOR_EACH`
- `LOOP_OVER_ITEMS`
- `END`

### Transform and item operations
- `JDM_DECISION`
- `MAP_FIELDS`
- `JSON_TRANSFORM`
- `EDIT_FIELDS`
- `FILTER`
- `SPLIT_OUT`
- `AGGREGATE`
- `SORT`
- `LIMIT`
- `DEDUP_BY_KEY`
- `REMOVE_DUPLICATES`
- `FILE_METADATA`

### Execution control
- `EXECUTION_DATA`
- `RESPOND_TO_WEBHOOK`
- `STOP_AND_ERROR`
- `NO_OP`
- `CODE`

The palette must be generated from a versioned node metadata registry, preferably returned by the backend.

## 6. Required metadata endpoint

The UI needs a backend contract equivalent to:

```text
GET /endpoint/automation-flows/metadata/nodes
```

Response should include:
- node type
- localized display label key
- category
- description
- icon key
- runtime modes
- number/names of inputs and outputs
- configuration JSON schema
- UI schema
- credential types
- expression-enabled fields
- validation rules
- deprecated flag

Until this exists, a versioned frontend registry may be used only for UI metadata; execution truth remains backend validation.

## 7. Graph model

Use `@xyflow/react`.

Node view model:

```ts
type AutomationNodeView = {
  id: string;
  type: string;
  position: { x: number; y: number };
  data: {
    displayName: string;
    config: Record<string, unknown>;
    credentialRef?: string;
    retryPolicy?: unknown;
    timeoutPolicy?: unknown;
    errorPolicy?: unknown;
    concurrencyPolicy?: unknown;
    disabled?: boolean;
  };
};
```

Preserve backend node identity and positions during import/export.

## 8. Node inspector

Tabs:
- Parameters
- Input
- Output
- Settings
- Error handling
- Notes

Common settings:
- credential reference selector
- retry policy
- timeout
- error policy
- concurrency
- disabled
- continue-on-fail only when supported

Expressions:
- expression/code toggle
- syntax highlighting
- autocomplete for `$json`, prior nodes, input items, environment, and execution data
- preview using pinned or latest execution data
- never execute untrusted code in the browser

## 9. Edge editor

- output branch selection
- target input index
- label
- condition when applicable
- invalid loop/connection feedback
- keyboard connect action
- delete confirmation when it changes executable path

## 10. Workflow settings

- flow key
- name
- version
- labels
- environment
- runtime mode: `VARIABLES` or `N8N_ITEMS`
- input/output schema
- error workflow
- required roles
- settings
- pin data
- schedule configuration

Changing runtime mode requires validation and confirmation.

## 11. Lifecycle

States:
- Draft
- Submitted
- Approved
- Active
- Retired

Buttons appear from backend-allowed lifecycle actions.

Before activation:
- validate graph
- show diff from active version
- confirm environment
- confirm schedule/webhook impact
- report unsupported or missing credentials

## 12. Execution UX

### Start/test
- manual input editor generated from input schema
- choose full run or partial run from selected node
- use pinned data when enabled

### Execution detail
- overall status and duration
- node attempt timeline
- input/output snapshot
- errors
- waits/callbacks
- dead letters
- retry action
- cancel action
- child workflows
- download sanitized diagnostic bundle

Do not poll aggressively; use WebSocket/SSE when available, otherwise backoff polling.

## 13. n8n import/export

UI:
- analyze before import
- display supported and unsupported nodes
- require explicit confirmation
- never import credential secrets
- show mapping summary
- export exact supported graph format

Existing endpoints:
- analyze
- import
- n8n export

Unsupported nodes must block native import, not become no-ops.

## 14. API integration

Use existing automation definition, lifecycle, execution, batch, import/export APIs from the repository. Add typed clients instead of inline fetches.

Every mutation includes tenant/site scope and an idempotency key where supported.

## 15. Acceptance scenarios

1. Create a manual-trigger flow with `EDIT_FIELDS` and `HTTP_REQUEST`.
2. Configure expression using `$json`.
3. Validate and save draft.
4. Test with input items.
5. Inspect node-level output.
6. Submit, approve, and activate.
7. Refresh page and preserve graph positions.
8. Import a supported n8n flow.
9. Reject unsupported nodes with a complete report.
10. Use the editor on a 390px mobile viewport.

# BPM Service Architecture

This repo now includes [bpm-service](/Users/farid/Projects/Cyan/old-cyan/cyan_business/bpm-service:1), an additive BPM module modeled on `Cyan-bpm` but integrated with the microservices in this project instead of `Cyan-core`.

## Goal

`bpm-service` manages workflow state for:

- dynamic entities exposed by the shared `dynamic-entity-core` runtime
- static entity APIs exposed by older microservices

It keeps the `Cyan-bpm` shape:

- `DynamicFlowDefinition`
- `FlowState`
- `FlowTransition`
- `ManagedObject`
- `TransitionHistoryEntry`
- active-form fetch
- active-form submit
- transition execution

## Main Difference From `Cyan-bpm`

Instead of submitting to `Cyan-core` form APIs, each BPM state can target:

1. a dynamic entity in another service
2. a static submit URL in another service

## State Model

`FlowState` supports the original fields:

- `formKey`
- `processorKey`
- `reviewCommentRequired`
- `candidateGroups`
- `onEnterActions`
- `accessRule`

and adds the integration fields:

- `entityService`
- `entityKey`
- `rendererService`
- `rendererKey`
- `submitMode`
- `submitUrl`

`submitMode`:

- `DYNAMIC`
  BPM fetches entity definition through `/internal/entities/definitions/{entityKey}` and submits through `/internal/entities/records/{entityKey}`.
- `STATIC`
  BPM posts to the configured `submitUrl` on the target service.

## Managed Object Model

`ManagedObject` is stored in Mongo and tracks:

- `tenantKey`
- `siteKey`
- `objectType`
- `objectRef`
- `flowKey`
- `state`
- `assignee`
- `payload`
- `accessRule`
- `locked`
- `auditLog`
- `transitionHistory`

`objectRef` links the workflow object to a platform entity:

- `service`
- `entityKey`
- `recordKey`

## API Surfaces

Bearer-token API:

- `/endpoint/bpm/flows`
- `/endpoint/bpm/managed-objects`

Internal basic-auth API:

- `/internal/bpm/flows`
- `/internal/bpm/managed-objects`

Gateway route:

- `/api/bpm-service/**`
- `/endpoint/bpm/**`
- `/internal/bpm/**`

## Active Form / Renderer Behavior

There is no direct `Cyan-core` `FormRenderer` dependency here.

Instead, BPM resolves the active renderer from the target service definition:

- `rendererService` + `rendererKey`
- or fallback to `entityService` + `entityKey`

The active-form response includes `rendererDefinition`, which contains:

- `serviceKey`
- `entityKey`
- `entityType`
- `title`
- `definition`

That lets frontend or orchestration clients render the current structured entity form the same way they would use a form definition from `Cyan-core`.

## On-Enter Actions

Supported action types:

- `ADD_AUDIT_ENTRY`
- `SET_ASSIGNEE`
- `SET_ACCESS_RULE`
- `UPDATE_OBJECT_FIELDS`
- `COPY_FIELDS`
- `REMOVE_FIELDS`
- `CALL_API`
- `CALL_OPERATOR`
- `NOTIFY_OWNER`

`CALL_API`, `CALL_OPERATOR`, and `NOTIFY_OWNER` are routed through the same internal service HTTP bridge used elsewhere in this repo.

## Example State

```json
{
  "id": "draft-order",
  "displayName": "Draft Order",
  "terminal": false,
  "formKey": "shop-order",
  "processorKey": "order-review",
  "entityService": "commerce-service",
  "entityKey": "shop-order",
  "rendererService": "commerce-service",
  "rendererKey": "shop-order",
  "submitMode": "DYNAMIC",
  "reviewCommentRequired": false,
  "candidateGroups": ["ROLE_USER"]
}
```

## Example Static State

```json
{
  "id": "tax-submit",
  "displayName": "Submit Tax",
  "terminal": false,
  "entityService": "tax-pay-sys",
  "submitMode": "STATIC",
  "submitUrl": "/internal/tax/submit",
  "candidateGroups": ["ROLE_FINANCE"]
}
```

## Current Scope

Implemented:

- flow definition storage
- managed object lifecycle
- transition evaluation
- active-form resolution
- dynamic submit integration
- static submit integration
- endpoint/internal auth split
- tenant/site scoping

Not yet implemented from full `Cyan-bpm`:

- Flowable engine orchestration
- async callback registry
- full ANTLR transition expression engine
- BPMN export/builder
- deployment metadata helpers

This keeps the service usable now while staying aligned with the `Cyan-bpm` model.

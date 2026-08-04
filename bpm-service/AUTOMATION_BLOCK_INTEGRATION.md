# BPM Automation Block Integration

## Summary
`START_AUTOMATION_FLOW` has been replaced by `RUN_AUTOMATION_BLOCK`.

The action now accepts either an active saved automation `flowKey` or a complete graph under `inlineFlow`. Execution is synchronous by default; flows containing `WAIT` or `WAIT_FOR_CALLBACK` must set `async: true`.

The new model makes automation a first-class BPM concept instead of a generic async payload entry.

## Main Features
- execution mode: `SYNC` or `ASYNC`
- first-class managed object persistence through `automationBlockRegistry`
- state-level wait semantics via `FlowState.waitForAutomation`
- failure policies:
  - `FAIL_FAST`
  - `MARK_FAILED`
  - `CONTINUE`
  - `RETRY`
- start-response mappings
- final callback/output mappings
- full response storage paths
- retry metadata
- timeout metadata
- cancel metadata
- optional inline automation fragment payload

## BPM Side
Key classes:

- `ActionType.RUN_AUTOMATION_BLOCK`
- `AutomationBlockExecution`
- `AutomationExecutionMode`
- `AutomationFailurePolicy`
- `FlowActionExecutor`
- `ObjectFlowService`

Managed object persistence:

- `ManagedObject.automationBlockRegistry`

## Automation Orchestrator Side
Key classes:

- `AutomationStartRequest`
- `AutomationStartResponse`
- `AutomationExecution`
- `AutomationExecutionService`

Supported execution behaviors:

- synchronous completion
- asynchronous completion via background task
- signed BPM callback
- cancellation
- timeout evaluation
- inline fragments:
  - `HYBRID_SCREENING`
  - `MAP_OUTPUT`
  - `FAIL`

## Callback Flow
1. BPM starts automation block.
2. Automation service stores execution and either completes immediately or asynchronously.
3. Automation service signs and sends callback to BPM.
4. BPM maps final output into managed object payload.
5. BPM updates block state and may retry or transition automatically.

## Current Limits
- retry is implemented from BPM callback handling, not from a separate scheduler
- timeout is evaluated when execution is processed, not by a periodic sweeper
- inline fragments support a small built-in set, not a full embedded automation DSL

# bpm-service Agent Guide

## Purpose
`bpm-service` is the workflow control plane for dynamic and static submissions across the platform.

## Owns
- flow definitions
- managed objects
- active state resolution
- transition execution

## Main APIs
- `/endpoint/bpm/flows`
- `/endpoint/bpm/managed-objects`
- matching `/internal/bpm/**` APIs

## Key Concepts
- `submitMode=DYNAMIC`: submit through target service dynamic entity APIs
- `submitMode=STATIC`: submit to a configured static URL
- state can reference renderer definitions from another service

## Dependencies
- target domain services
- `processor-service`
- `automation-orchestrator-service` for callback-style actions when used

## Change Rules
- Preserve tenant/site and access-rule semantics.
- Do not collapse workflow state into source service business logic.

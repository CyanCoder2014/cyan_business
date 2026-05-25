# processor-service Agent Guide

## Purpose
`processor-service` is the reusable validation and operator engine for submission processing.

## Owns
- processor definitions
- validator and operator configuration

## Main APIs
- processor CRUD
- `POST /api/processor-service/processors/{processorKey}/run`

## Flow Role
1. Receive payload.
2. Apply configured validators.
3. Apply normalization or computed operators.
4. Return processed output for caller-controlled persistence.

## Change Rules
- Keep processor logic generic; business ownership stays in domain services or BPM flows.

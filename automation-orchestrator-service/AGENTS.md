# automation-orchestrator-service Agent Guide

## Purpose
`automation-orchestrator-service` manages automation execution runs and callback-aware async orchestration.

## Owns
- automation execution records
- internal start API
- callback secret and signature validation settings

## Main APIs
- `POST /internal/automation-orchestrator/executions/start`

## Dependencies
- used by BPM or other services that need async automation execution

## Change Rules
- Preserve callback signing and timestamp semantics.
- Keep execution tracking separate from source business records.

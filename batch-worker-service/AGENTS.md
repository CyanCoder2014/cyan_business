# batch-worker-service Agent Guide

## Purpose
`batch-worker-service` executes durable, restartable, chunk-oriented ETL jobs started by automation or internal platform services.

## Owns
- Spring Batch job and step metadata
- ETL job definitions and run requests
- per-item retry, skip, quarantine, and checkpoint state
- idempotent delivery keys for remote writers

## Main APIs
- `POST /internal/batch/definitions`
- `POST /internal/batch/definitions/{definitionKey}/runs`
- `GET /internal/batch/runs/{runId}`
- `GET /internal/batch/runs/{runId}/rejected-items`
- `POST /internal/batch/runs/{runId}/retry`
- matching bearer-token endpoint APIs under `/endpoint/batch`

## Change Rules
- Keep scheduling in `automation-orchestrator-service`; this service executes batch work.
- Never put connector secrets in Spring Batch job parameters.
- Remote writes must carry stable idempotency keys.
- Preserve tenant/site scope on definitions, executions, and downstream calls.
- Use bounded pages and chunks; never load a complete large dataset into memory.

# Durable Batch Worker

`batch-worker-service` owns large, restartable API-to-API ETL jobs. It complements
`automation-orchestrator-service`; it does not replace BPM or normal automation nodes.

## Ownership and call direction

1. An automation `SCHEDULE_TRIGGER` creates one idempotent automation execution for a
   scheduled occurrence.
2. `RUN_BATCH_JOB` calls `batch-worker-service` through its internal Basic-auth API.
3. The batch worker leases the run to one pod and launches a Spring Batch job.
4. The reader fetches bounded pages. Spring Batch commits each chunk and stores its
   page/index checkpoint in PostgreSQL.
5. The writer sends each item with a deterministic `Idempotency-Key`.
6. Automation polls the run and waits without holding a worker thread until the batch
   reaches `COMPLETED` or `FAILED`.

Definitions and executions are tenant/site scoped. Spring Batch metadata and batch
run leases are in PostgreSQL. Normal automation execution leases and node checkpoints
remain in MongoDB.

## Delivery guarantees

- The unique `(tenant, site, definition, runKey)` constraint prevents duplicate jobs.
- PostgreSQL row locks and leases allow only one pod to own a run.
- A new pod recovers an expired run from the last committed Spring Batch checkpoint.
- HTTP `429` and `5xx` failures are retried.
- Destination `4xx` item failures are skipped up to `skipLimit` and copied to
  `batch_rejected_items`.
- A destination write repeated after a crash has the same `Idempotency-Key`.

Exactly-once effects require the destination API to store and enforce that key, or to
implement an equivalent idempotent upsert. Without receiver cooperation, an HTTP
success followed by a network failure is indistinguishable from a failed request.

The source API must provide stable pagination for the duration of a run, preferably
using an immutable cursor or a deterministic sort and snapshot filter.

## Create a definition

```bash
curl --request POST \
  --url http://localhost:8001/endpoint/batch/definitions \
  --header 'Authorization: Bearer <token>' \
  --header 'Content-Type: application/json' \
  --header 'X-Tenant-Key: demo-tenant' \
  --header 'X-Site-Key: main-site' \
  --data '{
    "definitionKey": "morning-customer-sync",
    "title": "Morning customer sync",
    "active": true,
    "spec": {
      "source": {
        "url": "https://source.example/api/customers",
        "itemsPath": "content",
        "pageParameter": "page",
        "sizeParameter": "size",
        "pageSize": 200,
        "bearerTokenEnvironmentVariable": "SOURCE_API_TOKEN"
      },
      "destination": {
        "url": "https://target.example/api/customers/upsert",
        "method": "POST",
        "itemKeyPath": "externalId",
        "bearerTokenEnvironmentVariable": "TARGET_API_TOKEN"
      },
      "fieldMappings": {
        "externalId": "id",
        "name": "name"
      },
      "chunkSize": 200,
      "retryLimit": 5,
      "skipLimit": 100
    }
  }'
```

Secrets are referenced by environment-variable name and are never stored in the
definition or Spring Batch job parameters.

## Start or inspect a run

```bash
curl --request POST \
  --url http://localhost:8001/endpoint/batch/definitions/morning-customer-sync/runs \
  --header 'Authorization: Bearer <token>' \
  --header 'Content-Type: application/json' \
  --header 'X-Tenant-Key: demo-tenant' \
  --header 'X-Site-Key: main-site' \
  --data '{"runKey":"2026-07-23"}'
```

Submitting the same run key again returns the existing run. History is available at
`GET /endpoint/batch/runs`, and a failed run can be restarted with
`POST /endpoint/batch/runs/{id}/retry`.

## Production setup

- Create a dedicated PostgreSQL database and set the `BATCH_WORKER_DATASOURCE_*`
  variables.
- Initialize the Spring Batch 6 PostgreSQL schema once during deployment. The server
  profile defaults `BATCH_WORKER_INITIALIZE_SCHEMA` to `never` to avoid schema races
  between replicas; use `always` only for a controlled first initialization.
- Supply `SOURCE_API_TOKEN`, `TARGET_API_TOKEN`, or the environment variables named
  by each definition.
- Run at least two replicas. No Redis deployment is required: PostgreSQL provides
  transactional batch metadata and run leasing, while MongoDB provides automation
  execution leasing.
- Alert on expired leases, `FAILED` runs, quarantine growth, and a non-zero skip count.

The panel Automation page can save definitions, activate a six-field cron schedule,
run a batch immediately, and inspect execution counters.

# Platform orchestration design rules

The service inventory supplied with each AI request is authoritative. Generate only
resources owned by services in `availableServiceKeys`. When a required service is not
available, preserve the user's intent in `manualActions` and name the missing service.

## Keys and BPM submission

- `entityKey` selects a structured definition and its record schema in the owning
  dynamic service.
- `formKey` selects the BPM state form/renderer. For simple forms it may equal the
  `entityKey`; explicit `rendererService` and `rendererKey` are preferred.
- `processorKey` selects optional reusable pre-submit validation/normalization. It is
  not a persistence key and is not mandatory for every form.
- Create entity definitions and processor definitions before the BPM flow that
  references them.
- BPM executes a processor before target validation and save. Processor failure stops
  persistence. Successful processed data still passes strict entity validation.
- `submitMode=DYNAMIC` requires `entityService` and `entityKey`.
- `submitMode=STATIC` requires `submitUrl`; it does not persist a dynamic entity unless
  that static target explicitly does so.

## Automation and rules

BPM is the stateful control plane. It may invoke automation with
`RUN_AUTOMATION_BLOCK`. Automation handles integration, transformations, scheduled
work, and fan-out without becoming the owner of human workflow state.

Use deterministic JDM/Zen decisions for rule-engine behavior. Schema validation,
processor validation/normalization, and business decisions are separate stages.

Do not generate direct database connectors unless metadata exposes a supported
connector. API calls are the default integration boundary.

## Scheduling and large ETL

Spring cron has six fields: second, minute, hour, day-of-month, month, day-of-week.
Always include an IANA timezone.

Small scheduled work can run as an automation flow. Large or important ETL requires
`batch-worker-service`: bounded pages, chunks, PostgreSQL Spring Batch checkpoints,
retry, skip/quarantine, and multi-pod leases. Schedule it with:

`SCHEDULE_TRIGGER -> RUN_BATCH_JOB -> END`

Use `{{scheduledAt}}` as the stable `runKey`. Remote HTTP writes are exactly-once only
when the receiver enforces the deterministic `Idempotency-Key` or an equivalent
upsert. Never put secrets in flow definitions or batch job parameters.

MongoDB atomic leases coordinate normal automation executions. PostgreSQL and Spring
Batch coordinate durable ETL. Redis and reactive `Mono` are not substitutes for
transactional checkpoints and crash recovery.

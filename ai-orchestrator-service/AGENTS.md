# ai-orchestrator-service Agent Guide

## Purpose
`ai-orchestrator-service` turns prompts, blueprints, drafts, and conversations into platform provisioning actions across dynamic services.

## Owns
- blueprint catalog
- draft generation and storage
- provisioning runs
- conversation sessions
- LLM provider routing and RAG bootstrap

## Main APIs
- `/endpoint/ai-orchestrator/generate/app`
- `/endpoint/ai-orchestrator/blueprints/**`
- `/endpoint/ai-orchestrator/drafts/**`
- `/endpoint/ai-orchestrator/sessions/**`
- matching `/internal/ai-orchestrator/**`

## Service-Aware Request Contract
- Prompt-bearing create/update/message bodies accept `availableServiceKeys`.
- Treat an explicitly supplied list as authoritative for that request and persist it
  with the draft, session, and generated app.
- Normalize common aliases such as `AI`, `automation`, `bpm`, `ssh-user`, and
  `processoor` to canonical service keys.
- In Kubernetes, when the list is omitted, use the configured deployment inventory.
  Local profiles may use discovery before the configured fallback.
- Never provision resources owned by an unavailable service. Preserve unsupported
  user intent as a precise `manualActions` item naming the missing service.

The panel's normal lightweight deployment inventory is:
`ai-orchestrator-service`, `notification-service`, `bpm-service`,
`automation-orchestrator-service`, `report-service`, `sso-auth-service`,
`sso-user-service`, `sso-captcha-service`, `media-service`, and
`processor-service`. Deployments with durable high-volume ETL must additionally
advertise `batch-worker-service`.

## Generated Orchestration Resources
- `PROCESSOR_DEFINITION` is owned by `processor-service`.
- `AUTOMATION_FLOW` is owned by `automation-orchestrator-service`.
- `BATCH_DEFINITION` is owned by `batch-worker-service`.
- Provision entity and processor definitions before BPM flows that reference them.
- Use automation for ordinary schedules and API orchestration. Use the batch worker
  for important/high-volume ETL that requires leases, chunk checkpoints,
  retry/skip/quarantine, and restart recovery.
- Require a stable run key per schedule occurrence and destination-enforced
  idempotency for remote writes.

## Dependencies
- internal template APIs across many domain services
- optional LLM providers and local heuristic fallback

## Runtime Routing
- Production runs on Kubernetes and does not depend on `discovery-server` or
  `api-gateway`.
- Production service-to-service calls resolve Kubernetes Service DNS routes.
- `AVAILABLE_SERVICE_KEYS` is the production deployment inventory fallback when an
  AI request does not explicitly include `availableServiceKeys`.
- Eureka discovery remains a local-development fallback only.

## Flow Role
1. Interpret user intent.
2. Pick blueprints/templates.
3. Create definitions and optionally records in target services.
4. Track draft and provisioning run state.

## Change Rules
- Prefer template-backed provisioning over freeform schema invention.
- Keep service-key inventory aligned with actual platform services.

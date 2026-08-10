# Phase 11 Backend Gap Closure Report

Status: **COMPLETE**

Completed: 2026-08-10

## Scope

This approved follow-on closes GAP-081 through GAP-093 without starting a new
visual product phase. Runtime contracts remain service-owned, tenant/site
scoped, authenticated, permission checked, persisted, and truthful when an
external provider is unavailable.

## Delivered contracts

- **AI Orchestrator / Media:** tenant/site custom-provider profiles use bounded
  HTTPS endpoints, declared model/modalities, and environment or Kubernetes
  secret references. Multimodal inputs are read through Media's internal byte
  contract with scope, MIME, and size checks. Persisted artifact jobs expose
  queued/running/succeeded/failed/cancelled states and register generated Media
  ownership; Kubernetes resolution remains `NOT_CONFIGURED` until an operator
  supplies the integration. Direct profile/job reads and mutations revalidate
  Tenant's persisted `ai.read` or `ai.execute` permission and effective AI
  capability instead of trusting browser role claims.
- **BPM / Tenant:** authoritative USER and ROLE targets come from Tenant;
  flow-owned GROUP keys remain exact. BPM validates assignment and exposes
  paged assigned, visible, role, group, unassigned, and completed cartable
  views with filters and totals.
- **Storefront:** the authenticated site work portal checks site membership and
  forwards the authenticated actor and tenant/site scope to BPM. Public routes
  cannot expose cartable data.
- **Tenant / SSO / Notification:** invitations are persisted and tokenized,
  acceptance provisions bounded membership, last-owner safety is preserved,
  ownership can be transferred, and client capabilities can be changed after
  provisioning. Notification delivery reports `NOT_CONFIGURED` when no real
  provider exists.
- **Reporting / Media:** report exports are idempotent persisted CSV/JSON jobs;
  bytes are ingested into Media and registered as references. Platform-health
  runs and check details are persisted by tenant/site. Media deletion is
  rejected while authoritative references exist.
- **Platform / Panel:** the shared error DTO includes validation fields,
  correlation identity, and retryability. Panel BFF routes use one bounded
  proxy policy with request-header allowlisting, response streaming, and safe
  errors. Seventeen checked-in API contracts are statically verified and can
  also be checked against live OpenAPI documents.

## PostgreSQL-only test boundary

All repository H2 runtime dependencies, `jdbc:h2` URLs, and tracked `.mv.db`
files were removed. Local service defaults and explicit Spring context tests
use service-specific PostgreSQL databases. Dynamic record stores that already
use MongoDB remain MongoDB by architecture; this change does not move them into
the wrong persistence owner.

## Panel and UX integration

The panel now exposes real provider profiles and generation jobs, report
exports, durable health history, media usage and safe deletion, the complete
cartable filter set, authoritative assignment search, authenticated site work,
client capability updates, tenant invitations, and ownership transfer.
Mutation controls use pending labels, `disabled`, `aria-disabled`, and
`aria-busy` semantics so repeated clicks cannot issue duplicate requests.
Layout hardening adds logical spacing, safe-area clearance, bounded dense
content, and mobile action stacking without embedding sample production data.

## Verification

Final commands:

```text
./gradlew :platform-error-handling:test :tenant-service:test :bpm-service:test :storefront-service:test :media-service:test :report-service:test :ai-orchestrator-service:test :automation-orchestrator-service:test :batch-worker-service:test :processor-service:test --continue
npm run lint
npm run build
npm run contracts:check
npm run test:e2e
```

- Backend compilation and affected test suites pass against PostgreSQL-backed
  contexts.
- Panel lint passes with recorded non-fatal legacy hook and raw-image warnings.
- The production build passes and includes 46 generated routes.
- Contract validation passes for 17 checked-in contracts.
- Production-server Playwright result: **37 passed, 8 skipped, 0 failed** across
  45 tests. Skips are explicitly gated visual-capture suites and the
  credential-dependent live-admin journey.
- New deterministic coverage verifies persisted contract rendering and
  one-request mutation behavior for invitation/ownership, custom AI jobs,
  report/health/cartable/site portal surfaces, and protected media deletion.

## Remaining prerequisites

Only external operator prerequisites remain in the gap register: payment
merchant approval, OAuth keys, Telegram/Bale webhook credentials and HTTPS
ingress, DNS/certificate ownership, and Notification provider credentials.
Their UI and APIs continue to report `NOT_CONFIGURED` rather than success.

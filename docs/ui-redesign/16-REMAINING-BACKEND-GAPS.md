# Remaining Backend and External Gaps

Updated: 2026-08-10

This is the post-Phase-11 gap register. These items are not represented as
working UI success. `NOT_CONFIGURED`, unavailable, permission-denied, empty, or
read-only states remain truthful until the owning contract exists.

| ID | Priority | Remaining contract | Correct owner and direction | Current truthful state |
|---|---|---|---|---|
| EXT-001 | External | payment-provider merchant approval and credentials | Billing/Payment owners | external paid checkout/billing is `NOT_CONFIGURED` |
| EXT-002 | External | OAuth application keys and approved redirect URIs | SSO Auth | unavailable providers remain disabled |
| EXT-003 | External | Telegram/Bale token and webhook secret references plus reachable HTTPS ingress | Bot Adapter/operator | configuration state is truthful; no credential is embedded |
| EXT-004 | External | DNS ownership and certificate/hosting provider | Domain/Storefront operator | DNS can be verified; certificate state remains provider-dependent |
| EXT-005 | External | SMTP/SMS/push/MQTT credentials | Notification owner/operator | provider state is `NOT_CONFIGURED` until supplied |

## Closed after Phase 11

- GAP-080 — effective user/capability authorization for existing Automation AI
  calls, including persisted execution provenance.
- GAP-081 — tenant/site custom AI provider profiles with bounded HTTPS URLs,
  models, modalities, and secret references; raw secrets are never returned.
- GAP-082 — scoped multimodal Media input retrieval with ownership, MIME, and
  byte-size enforcement before AI execution.
- GAP-083 — persisted asynchronous generated-media jobs with cancellation,
  truthful failure states, Media ingestion, and reference registration.
- GAP-084 — authoritative tenant user/role assignment-target lookup and BPM
  assignment validation.
- GAP-085 — pageable server cartable views for assigned, visible, role, group,
  unassigned, and completed work.
- GAP-086 — authenticated site portal composition that preserves the actor and
  tenant/site boundary when calling BPM.
- GAP-087 — persisted tenant invitations, acceptance, ownership transfer, and
  post-provision client capability changes. Delivery stays `NOT_CONFIGURED`
  when Notification has no provider.
- GAP-088 — idempotent report export records with CSV/JSON artifacts owned by
  Media.
- GAP-089 — authoritative Media reference registry and reference-protected
  deletion.
- GAP-090 — durable tenant/site-scoped platform-health run history in Reporting.
- GAP-091 — checked-in panel contract manifest and CI-capable OpenAPI
  conformance checker.
- GAP-092 — shared platform error response with field errors, correlation ID,
  and retryability.
- GAP-093 — shared BFF proxy policy for bounded timeouts, header allowlisting,
  streaming responses, and safe upstream errors.
- Frontend-only PWA/offline/update, shared focus, pending-mutation, safe-area,
  RTL/theme, compatibility cleanup, and graph lazy-loading gaps.

No remaining gap should be solved by a frontend-only endpoint, hardcoded
response, local production registry, in-memory authoritative storage, or fake
provider completion.

# Remaining Backend and External Gaps

Updated: 2026-08-09

This is the post-Phase-11 gap register. These items are not represented as
working UI success. `NOT_CONFIGURED`, unavailable, permission-denied, empty, or
read-only states remain truthful until the owning contract exists.

| ID | Priority | Remaining contract | Correct owner and direction | Current truthful state |
|---|---|---|---|---|
| GAP-081 | P1 | tenant/site provider profiles for custom AI APIs, bounded URL/model/modalities and secret-manager references | `ai-orchestrator-service`; Automation references a profile key | no custom-profile selector or raw API-key field |
| GAP-082 | P1 | multimodal AI input for scoped image/audio/video/file assets, ownership and size/type checks | AI Orchestrator → Media internal byte contract | existing AI node handles its current typed operation only |
| GAP-083 | P1 | asynchronous generated image/audio/video artifact lifecycle, usage, cancellation and retention | AI Orchestrator status + Media artifact ownership | no fake generated URL or success |
| GAP-084 | P1 | BPM state candidate users/roles and an authoritative assignable-target search | `bpm-service` with Tenant identity/role validation | candidate groups and free-text exact USER/ROLE/GROUP assignment only |
| GAP-085 | P1 | pageable server cartable views for role/group, unassigned and completed work with totals/SLA filters | `bpm-service` | assigned-to-me and visible-to-me only |
| GAP-086 | P2 | authenticated site block/portal for “my work” | Storefront composition authenticates SSO then calls BPM as that user | cartable stays in `/work`; public Storefront never leaks work |
| GAP-087 | P2 | client invitation delivery, ownership transfer, and post-provision capability changes | Tenant + SSO + Notification + Billing owners | direct head-user provisioning and bounded team management work |
| GAP-088 | P1 | report export job/file contract | `report-service` with Media for large output | run/results work; export action is absent |
| GAP-089 | P1 | authoritative cross-service media reference registry for delete | Media plus every referring service | upload/read/usage work; unsafe delete is unavailable |
| GAP-090 | P2 | durable platform health history and alert ownership | platform health/reporting projection | current checks are live/session-oriented only |
| GAP-091 | Foundation | generated or schema-conformance-checked TypeScript clients | API Docs/CI + Panel | typed handwritten facades remain the source in the panel |
| GAP-092 | Foundation | one platform error DTO including field errors, correlation ID and retryability | platform-wide | panel safely normalizes heterogeneous responses where possible |
| GAP-093 | Foundation | complete BFF timeout/conditional-header/safe-response policy | Panel BFF | auth/scope/idempotency/correlation forwarding is route-dependent |
| EXT-001 | External | payment-provider merchant approval and credentials | Billing/Payment owners | external paid checkout/billing is `NOT_CONFIGURED` |
| EXT-002 | External | OAuth application keys and approved redirect URIs | SSO Auth | unavailable providers remain disabled |
| EXT-003 | External | Telegram/Bale token and webhook secret references plus reachable HTTPS ingress | Bot Adapter/operator | configuration state is truthful; no credential is embedded |
| EXT-004 | External | DNS ownership and certificate/hosting provider | Domain/Storefront operator | DNS can be verified; certificate state remains provider-dependent |
| EXT-005 | External | SMTP/SMS/push/MQTT credentials | Notification owner/operator | provider state is `NOT_CONFIGURED` until supplied |

## Closed in Phase 11

- GAP-080 — effective user/capability authorization for existing Automation AI
  calls, including persisted execution provenance.
- Frontend-only PWA/offline/update, shared focus, pending-mutation, safe-area,
  RTL/theme, compatibility cleanup, and graph lazy-loading gaps.

No remaining gap should be solved by a frontend-only endpoint, hardcoded
response, local production registry, in-memory authoritative storage, or fake
provider completion.

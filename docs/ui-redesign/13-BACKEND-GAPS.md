# Backend Gap Register

Phase 0 gap register for the contracts required by `docs/ui-redesign/`.

## Status legend

- **P0 blocker**: required before a truthful shared shell/access/scope foundation.
- **Phase blocker**: blocks a primary job in the named redesign phase.
- **Partial**: a related API exists, but it cannot support the promised UI contract.
- **Frontend-only**: no backend work is required; listed here only to prevent misclassification.

Exact paths below are proposed contracts unless marked existing. Backend owners should confirm naming and authorization before implementation. The frontend must show an unavailable/disabled state until a confirmed contract exists.

## 1. Foundation, tenancy, access, plans, and OAuth

| ID | Priority | Missing contract | Minimum DTO / behavior | Suggested owner | Existing related contract |
|---|---|---|---|---|---|
| GAP-001 | P0 blocker | `GET /api/panel/bootstrap` | user, tenants, activeTenant, sites, activeSite, plan, capabilities, permissions, featureFlags, locale, theme; stable IDs and capability health | panel BFF plus IAM/tenant/plan services | separate user and IAM access reads only |
| GAP-002 | P0 blocker | tenant/workspace registry | list/create/read/update tenants accessible to current user; tenant status; membership; default/last active tenant | new tenant/workspace owner or expanded IAM | IAM realms are identity realms, not documented tenants |
| GAP-003 | P0 blocker | active tenant selection | persist/read active tenant for current user/session; reject inaccessible tenant; return canonical tenant key | tenant/session owner | only local display string |
| GAP-004 | P0 blocker | site registry and tenant-site membership | list/create/read/update sites for tenant; status, display name, default locale/time zone; default/active site | storefront/tenant owner | `site-route` records are pages/routes, not sites |
| GAP-005 | P0 blocker | active site selection | persist/read active site; verify it belongs to active tenant; optional site-less mode | tenant/session owner | only local display string |
| GAP-006 | P0 blocker | capability resolution | `EffectiveCapability[]` with key, enabled, source, AVAILABLE/DEGRADED/UNAVAILABLE, limits and optional reason; tenant/site scoped | platform capability/plan owner | environment service inventory and backend `@PreAuthorize` checks |
| GAP-007 | P0 blocker | plan catalog/current entitlement | plan ID/status/renewal, features, limits, usage, limited/no-plan state, comparison catalog | new plan/billing owner | static Pro card only |
| GAP-008 | P0 blocker | subscription billing | invoices, subscription changes/cancel, billing payment methods, failed payment state, redirect/confirmation | new billing owner | commerce `payment-service` is not subscription billing |
| GAP-009 | P0 blocker | feature flags | server-backed flags with scope and rollout source, included in bootstrap | platform config owner | none |
| GAP-010 | P0 blocker | panel permission vocabulary mapping | versioned mapping from current colon permissions (`builder:*`) to route/action permissions required by redesign, or one canonical vocabulary | IAM/platform authorization | IAM effective access exists |
| GAP-011 | Phase 2 blocker | invitations | list, create, accept, expire, revoke, resend; tenant/client boundary and intended roles | tenant/IAM owner | memberships can be upserted by privileged callers; no invite lifecycle |
| GAP-012 | Phase 2 blocker | Google OAuth | start, callback, state/PKCE, error codes, redirect allowlist, account link/unlink | SSO Auth/User | none |
| GAP-013 | Phase 2 blocker | GitHub OAuth | same as GAP-012 | SSO Auth/User | none |
| GAP-014 | Phase 2 blocker | LinkedIn OAuth | same as GAP-012 | SSO Auth/User | none |
| GAP-015 | Phase 2 blocker | explicit MFA login challenge response | login response/error must identify OTP-required state and challenge metadata without relying on raw error text | SSO Auth | login request accepts `otpCode`; OTP send/verify endpoints exist |
| GAP-016 | Phase 8 blocker | role removal/replacement | revoke realm/client role, replace assignment set, list assignable roles bounded by caller privileges | SSO User/IAM | assign-only endpoints |
| GAP-017 | Phase 8 blocker | user invitation/suspension/removal/resend | tenant-scoped user lifecycle with privilege-escalation prevention | SSO User/IAM | platform-admin create/list and managed provisioning only |
| GAP-018 | Phase 8 blocker | self-service profile/security | update profile/password, enable/disable MFA, linked identities, notification preferences | SSO User/Auth | get user only |
| GAP-019 | Phase 8 blocker | sessions/devices list and revoke | list current user's sessions/devices, revoke one/all; last activity and device metadata | SSO Session/Auth | get/revoke by known session ID only |
| GAP-020 | P0 blocker | self identity/access endpoint | resolve current JWT subject and effective access without a username path supplied from local storage | SSO User or bootstrap BFF | `/users/{username}/access` exists |

### Required bootstrap semantics

The bootstrap must distinguish:

- unauthenticated (`401`);
- authenticated with no tenant;
- tenant selected with no plan;
- permission denied;
- plan locked;
- capability disabled;
- capability degraded/unavailable.

An empty array is valid data and must not be conflated with unavailable data. Backend authorization remains authoritative for every mutation.

### Proposed foundation contracts

These paths are recommendations, not discovered APIs. The owning backend teams may rename them, but the semantics and fields are required before Phase 1 can present functional selectors or access gates.

```text
GET  /api/panel/bootstrap
GET  /endpoint/tenants
POST /endpoint/tenants
GET  /endpoint/tenants/{tenantKey}
GET  /endpoint/tenants/{tenantKey}/sites
POST /endpoint/tenants/{tenantKey}/sites
GET  /endpoint/tenants/{tenantKey}/invitations
POST /endpoint/tenants/{tenantKey}/invitations
POST /endpoint/tenant-invitations/{invitationToken}/accept
GET  /api/panel/scope
PUT  /api/panel/scope

GET  /endpoint/plans
GET  /endpoint/tenants/{tenantKey}/subscription
GET  /endpoint/tenants/{tenantKey}/usage
GET  /endpoint/tenants/{tenantKey}/billing/invoices
GET  /endpoint/tenants/{tenantKey}/billing/payment-methods
POST /endpoint/tenants/{tenantKey}/subscription/change
POST /endpoint/tenants/{tenantKey}/subscription/cancel
```

Recommended bootstrap response:

```ts
type PanelBootstrap = {
  user: {
    username: string;
    displayName?: string;
    email?: string;
  };
  tenants: Array<{
    tenantKey: string;
    displayName: string;
    status: "ACTIVE" | "SUSPENDED" | "PENDING";
  }>;
  activeTenant: { tenantKey: string; displayName: string } | null;
  sites: Array<{
    siteKey: string;
    tenantKey: string;
    displayName: string;
    status: "ACTIVE" | "INACTIVE" | "PENDING";
  }>;
  activeSite: { siteKey: string; tenantKey: string; displayName: string } | null;
  plan: {
    planKey: string;
    status: "NONE" | "TRIAL" | "ACTIVE" | "PAST_DUE" | "CANCELLED";
    renewsAt?: string;
    features: string[];
    limits: Record<string, number | string | boolean>;
  };
  capabilities: Array<{
    key: string;
    enabled: boolean;
    source: "PLAN" | "TENANT_OVERRIDE" | "PLATFORM";
    status: "AVAILABLE" | "DEGRADED" | "UNAVAILABLE";
    limits?: Record<string, number | string | boolean>;
    reason?: string;
  }>;
  roles: string[];
  permissions: string[];
  featureFlags: Record<string, boolean | string | number>;
  locale: "en" | "fa";
  theme: "light" | "dark" | "system";
};
```

Recommended active-scope mutation:

```ts
type SetPanelScopeRequest = {
  tenantKey: string;
  siteKey?: string | null;
};

type SetPanelScopeResponse = {
  activeTenant: PanelBootstrap["activeTenant"];
  activeSite: PanelBootstrap["activeSite"];
  capabilities: PanelBootstrap["capabilities"];
  permissions: string[];
  plan: PanelBootstrap["plan"];
};
```

The scope mutation must verify membership and return `403` for an inaccessible tenant/site. It must not accept display names as identifiers.

Recommended social OAuth surface, repeated for `google`, `github`, and `linkedin`:

```text
GET /api/sso/oauth/{provider}/start?returnTo={same-origin-path}
GET /api/sso/oauth/{provider}/callback?code=...&state=...
POST /api/sso/oauth/{provider}/link
DELETE /api/sso/oauth/{provider}/link
```

The start response may be an HTTP redirect or `{ authorizationUrl }`, but it must define state lifetime, PKCE verifier storage, allowed return URLs, cancellation/provider error codes, existing-account linking, and the final token/session response. A provider without this contract remains disabled.

## 2. AI, projects, uploads, provisioning, and releases

| ID | Priority | Missing or partial contract | Required behavior | Suggested owner | Existing related contract |
|---|---|---|---|---|---|
| GAP-021 | Phase 4 blocker | release lifecycle | publish draft, list/get releases, active release, rollback with confirmation/idempotency | AI Orchestrator | no release controllers/domain in current implementation |
| GAP-022 | Phase 4 blocker | draft resolve/recompute | `POST /drafts/{id}/resolve`, validation/readiness result, revision/conflict behavior | AI Orchestrator | create/get/patch drafts |
| GAP-023 | Phase 4 blocker | session close | `POST /sessions/{id}/close` plus allowed transition/status | AI Orchestrator | create/list/get/message only |
| GAP-024 | Partial | provisioning PLAN/APPLY | request DTO with mode, idempotency key, dry-run steps, retry eligibility, partial-success semantics | AI Orchestrator | current `POST /drafts/{id}/provision` with empty body and run reads |
| GAP-025 | Partial | provisioning realtime/retry | SSE/WebSocket or documented backoff status; retry eligible step/run; cancellation if supported | AI Orchestrator | run list/get only |
| GAP-026 | Phase 4 blocker | backend readiness/validation | authoritative readiness state, blocking errors, warnings, and source revision | AI Orchestrator | panel currently calculates readiness |
| GAP-027 | Phase 4 blocker | byte upload flow | bearer-facing prepare contract returning upload target; byte upload; completion/finalization; progress-compatible semantics | Media | current internal prepare writes metadata and fabricates CDN URL |
| GAP-028 | Phase 4 blocker | attach asset to AI session/draft | asset reference DTO and mutation, with authorization and cleanup rules | AI Orchestrator + Media | panel appends metadata to prompt |
| GAP-029 | Partial | AI realtime contract | documented authenticated WebSocket/SSE endpoint and event envelope; avoid access token in URL | AI Orchestrator | frontend-only URL setting, no matching documented controller |
| GAP-030 | Phase 4 blocker | real preview/publish target | returned preview URL tied to a release/draft and tenant/site; expiry and authorization | AI Orchestrator/Storefront | panel fabricates `preview.cyan.app` URL |

## 3. Definitions and data

| ID | Priority | Missing or partial contract | Required behavior | Suggested owner | Existing related contract |
|---|---|---|---|---|---|
| GAP-031 | Partial | definition lifecycle/version/diff | versions, status, validate-before-save, publish/activate where applicable, server diff | Dynamic Entity Core | CRUD and generated OpenAPI exist |
| GAP-032 | Partial | server grid query | stable pagination, multi-sort, filter groups, search, total counts on records | Dynamic Entity Core | page/size/sort is described in generated OpenAPI; current client expects arrays and lacks filter schema |
| GAP-033 | Partial | optimistic concurrency | revision/ETag and `409` DTO for stale definition/record updates | Dynamic Entity Core | timestamps only in panel types |
| GAP-034 | Partial | relation lookup metadata/query | relation target, display fields, scoped search/pagination | Dynamic Entity Core | definitions can carry relation-like data but no dedicated UI query contract |
| GAP-035 | Phase 5 blocker | import/export and bulk mutation | explicit capability endpoint and job/result DTO; omit controls until present | owning dynamic service/core | no generic panel-safe contract found |

Strict nested validation, templates, definition CRUD, record CRUD/validate, and definition-generated OpenAPI already exist and should be reused.

## 4. Automation and batch

| ID | Priority | Missing or partial contract | Required behavior | Suggested owner | Existing related contract |
|---|---|---|---|---|---|
| GAP-036 | Phase 6 blocker | rich node metadata | bearer endpoint returning localized label key, category, ports, JSON Schema, UI schema, credential types, expression fields, validation/deprecation/runtime modes | Automation Orchestrator | public node structures expose only common/config field-name lists |
| GAP-037 | Partial | allowed lifecycle actions/diff | per-version allowed actions and diff against active version, including schedule/webhook impact | Automation Orchestrator | generic lifecycle mutation accepts path action |
| GAP-038 | Partial | credential catalog metadata | credential types/schemas and safe references; responses must never expose secret value | Automation Orchestrator | credential CRUD/rotate exists |
| GAP-039 | Partial | partial run contract | selected start node, pinned input, validation, result provenance | Automation Orchestrator | manual run and runtime support exist, but UI-facing DTO is not documented |
| GAP-040 | Partial | execution realtime | SSE/WebSocket events for execution/node attempts/waits | Automation Orchestrator | history/detail/steps/dead letters/retry exist |
| GAP-041 | Phase 6 blocker | sanitized diagnostic bundle | downloadable redacted bundle with permissions and content contract | Automation Orchestrator | none |
| GAP-042 | Partial | schedule management | list/update/disable schedule metadata without reconstructing and auto-activating a fixed graph | Automation Orchestrator | schedule trigger node and lifecycle exist |
| GAP-043 | Partial | batch definition reads/updates | list/get/update definitions and safe validation metadata for editor | Batch Worker | save, run, retry/history are documented; panel only wraps a subset |

The existing n8n analyze/import/export contract correctly rejects unsupported native nodes and must remain authoritative.

## 5. BPM and Processor

| ID | Priority | Missing or partial contract | Required behavior | Suggested owner | Existing related contract |
|---|---|---|---|---|---|
| GAP-044 | Partial | BPM lifecycle/version/diff | draft versions, validate, allowed actions, activation diff/history | BPM | save/get/list/activate only |
| GAP-045 | Partial | graph layout persistence | state/node positions and viewport metadata, or an explicit frontend layout-storage contract | BPM | flow DTO has no audited position fields |
| GAP-046 | Phase 7 blocker | assignment/lock mutations | reassign with allowed targets; lock/unlock with conflict and ownership DTOs | BPM | fields exist on managed object; no endpoints found |
| GAP-047 | Partial | queue pagination/filter/search | flow/state/assignee/date/priority/scope filters and totals | BPM | assigned/visible list endpoints return lists |
| GAP-048 | Partial | transition option identity | stable transition ID, comment requirement, condition/permission explanation | BPM | current option type exposes next state/label and limited roles/groups |
| GAP-049 | Phase 7 blocker | processor discovery through panel BFF | bearer-facing processor catalog with purpose/input/output/failure metadata; add service to BFF | Processor | CRUD/run uses `/api/processor-service/processors`; generic proxy omits service |
| GAP-050 | Partial | work activity timeline | normalized audit/transition/comment/attachment history with pagination | BPM | audit/transition arrays plus comment/attachment lists exist separately |

Active form loading/submission, transitions, comments, attachments, action metadata, and condition metadata already exist.

## 6. Notifications

| ID | Priority | Missing contract | Required behavior | Suggested owner | Existing related contract |
|---|---|---|---|---|---|
| GAP-051 | Phase 3/9 blocker | notification inbox | current-user list/unread count, mark one/all read, deep link, pagination | Notification | send and get-one-message only |
| GAP-052 | Phase 9 blocker | message/delivery history | scoped list/filter/status, attempts, retry eligibility, retry mutation | Notification | dynamic message records exist but no operator history endpoint |
| GAP-053 | Phase 9 blocker | provider catalog/health | channel/provider IDs, configured/unconfigured/degraded, safe secret refs, capabilities | Notification | free-text provider in send request |
| GAP-054 | Phase 9 blocker | template render/validate preview | render from template/model without dispatch; validation errors and sanitized output | Notification | dynamic templates and send exist |
| GAP-055 | Partial | async delivery status/realtime | status endpoint/events and correlation/idempotency contract | Notification | send-async exists; no UI-facing run stream found |

## 7. Reports, media, and search

| ID | Priority | Missing or partial contract | Required behavior | Suggested owner | Existing related contract |
|---|---|---|---|---|---|
| GAP-056 | Phase 10 blocker | report catalog path/auth alignment | one bearer-facing catalog/run contract accessible through typed BFF; define static vs dynamic report ownership | Report | `/api/report-service/reports` and internal dynamic run both exist |
| GAP-057 | Phase 10 blocker | report run history/status/export | async run ID/status/result schema/history/export formats | Report | synchronous-looking run endpoint only |
| GAP-058 | Phase 10 blocker | media asset operator API | bearer list/search/page/filter, metadata update, folder/tag operations, delete | Media | dynamic CRUD can expose raw records; no cohesive operator contract |
| GAP-059 | Phase 10 blocker | real media storage | upload bytes, signed URLs, completion, variants/processing status, cancel/retry | Media | metadata-only internal prepare and public metadata retrieval |
| GAP-060 | Phase 10 blocker | media usage/delete protection | usage references and conflict response when referenced | Media plus referring services | none |
| GAP-061 | Phase 10 blocker | search sync runs | persisted run ID, progress/status/history/errors by source, retry | Search Index | immediate sync call only |
| GAP-062 | Phase 10 blocker | indexed counts and index health | document counts, last sync, source errors, analyzer/index health | Search Index | query/suggest only |

## 8. Bots, sites, commerce, domains, and hosting

| ID | Priority | Missing or partial contract | Required behavior | Suggested owner | Existing related contract |
|---|---|---|---|---|---|
| GAP-063 | Phase 9 blocker | bot session mapping/inbound operations | list/edit chat-to-AI-session mappings, inbound history, mapping health | Bot Adapter | domain models exist; no controller endpoints found |
| GAP-064 | Partial | bot provider/webhook health | registered URL/status/last check/provider availability without returning secrets | Bot Adapter | register mutation returns URL; no health read |
| GAP-065 | Security blocker | secret-safe bot response | integration response must omit `webhookSecret` and any token material; return configured/fingerprint/reference state only | Bot Adapter | current domain response includes secret-capable fields |
| GAP-066 | Phase 9 blocker | site registry | list/create/update sites, environment, locale, active release | Storefront/tenant owner | route/theme dynamic records only |
| GAP-067 | Phase 9 blocker | page/block builder contract | block metadata/schema, page composition, validation, draft revision, preview token | Storefront/Content | raw `site-route` records and public render |
| GAP-068 | Phase 9 blocker | site publish/release | publish with validation, immutable release, rollback, returned public/preview URL | Storefront | record publication string only |
| GAP-069 | Phase 9 blocker | domain registry | ownership, verification challenge, DNS instructions/status/history, redirects | new domain/hosting owner | none |
| GAP-070 | Phase 9 blocker | certificate/hosting status | certificate lifecycle, environment, deployment/publish status, polling/events | new domain/hosting owner | none |
| GAP-071 | Partial | commerce operator APIs | order/cart/checkout lists, details, lifecycle actions, refund/capture/reconciliation where supported | Commerce/Checkout/Payment | dynamic records and payment transactions exist, but current UI is seeding-oriented |

## 9. Dashboard and operational aggregation

| ID | Priority | Missing or partial contract | Required behavior | Suggested owner | Existing related contract |
|---|---|---|---|---|---|
| GAP-072 | Phase 3 blocker | usage/limit metrics | source, updated time, limit/window and current usage per plan feature | plan/billing owner | none |
| GAP-073 | Partial | recent activity feed | stable event IDs, actor, tenant/site, resource link, timestamp, pagination and capability filtering | reporting/event projection | panel currently synthesizes activity from unrelated lists |
| GAP-074 | Partial | capability health | service status/degradation with updated time and reason | capability/health owner | `/qa` performs ad hoc reads; Eureka is not a user entitlement contract |

Dashboard widgets can otherwise compose existing AI, BPM, Automation, Bot, Notification, and Storefront APIs. Partial failures must remain isolated.

## 10. OpenAPI and transport gaps

| ID | Priority | Missing or partial contract | Required behavior | Suggested owner |
|---|---|---|---|---|
| GAP-075 | P0 blocker | live export completeness verification | run controller-derived export and compare all UI-facing services; checked-in static specs are stale | API Docs/CI |
| GAP-076 | Foundation | generated/validated TypeScript contracts | generate clients or add schema conformance tests; preserve handwritten domain facade | Panel/API Docs |
| GAP-077 | Foundation | BFF forwarding policy | correlation/idempotency/conditional headers, safe response headers, timeouts, normalized upstream-unavailable error | Panel BFF |
| GAP-078 | Security blocker | internal endpoint mediation | browser must not call `/internal/**` with bearer; add a bearer endpoint or server-held Basic mediation with strict allowlist | Media/Panel BFF |
| GAP-079 | Foundation | consistent error DTO | code, message, field errors, correlation ID, retryability, status across services | platform-wide |

## 11. Frontend-only gaps (not backend blockers)

These require implementation in later approved phases but no new backend contract by themselves:

- design tokens, local Roboto assets, unified shell, theme pre-hydration, system theme;
- locale catalogs, RTL mirroring, logical CSS conversion;
- responsive composition and mobile bottom sheets;
- service worker, offline shell, safe areas, stale/offline UI;
- normalized frontend error adapter;
- query cancellation/deduplication/cache invalidation;
- accessible primitives, focus management, reduced motion, keyboard graph alternatives;
- compatibility redirects in Next.js;
- visual regression and viewport coverage.

## 12. Recommended backend delivery order

1. GAP-001 through GAP-010 and GAP-020: bootstrap, tenant/site, plan/capability, canonical permissions.
2. GAP-011 through GAP-019: onboarding, invitations, OAuth, identity/security lifecycle.
3. GAP-027/GAP-028/GAP-059: real media upload, because AI, BPM attachments, and Media all depend on it.
4. Phase-specific blockers in order: AI releases/readiness, definition query/versioning, automation metadata, BPM assignment/processor, notification inbox/history, site/domain/hosting, reports/media/search.
5. GAP-075 through GAP-079 continuously, so frontend contracts remain generated or verifiably aligned.

No frontend phase should replace any unresolved item with fixtures, local-only authoritative state, or simulated success.

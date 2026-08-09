# Phase 11 Pre-Implementation Report

Status: **AWAITING APPROVAL**

Scope: Phase 11 only — system hardening, PWA, accessibility, performance,
testing, visual regression, and migration cleanup

Prepared: 2026-08-09

No Phase 11 runtime code has been changed. This report is the required approval
gate from `11-PHASE-BY-PHASE-CODEX-PROMPTS.md`.

## 0. Scope decision and user jobs

### Personas and jobs

| Persona | Job to be done | Primary success condition |
|---|---|---|
| Tenant operator | Use every enabled product route without scope, layout, loading, or duplicate-mutation failures | A complete task can be performed with real scoped data on desktop or mobile |
| Builder | Design, save, validate, run, and inspect definitions, Automation, BPM, bots, and sites | Backend-authorized mutations execute once and remain inspectable |
| Platform administrator | Create a client tenant, head user, plan, and selected service access | The new head user can sign in and administer only that tenant |
| Client head user | Add users and manage bounded roles/permissions | No user or role can receive privileges outside the head user's effective boundary |
| Assigned worker | Find role/group/user-visible work and complete its active BPM form | The cartable reflects backend access and refreshes after transitions |
| Public site visitor | View a published site without panel credentials | Storefront content is real, published, responsive, and safely rendered |

### Phase boundary

Phase 11 explicitly “should not introduce major new product features.” Therefore:

- repository-wide UI, PWA, access, reliability, performance, migration, and test
  hardening are in scope;
- authoritative permission enforcement for the already shipped Automation AI
  node is security hardening and is in scope;
- expanding that node to custom multimodal providers, file/video analysis, image
  or video generation, and durable generated artifacts is a material new product
  capability. Its complete backend contract is proposed in section 8, but it is
  recorded for a separately approved follow-on phase rather than silently added
  to Phase 11;
- exposing already persisted BPM role/group/access fields and fully testing the
  existing cartable is in scope;
- adding new state-level candidate-user/candidate-role fields or embedding an
  authenticated cartable into a public site is a backend/product extension and
  is recorded as a follow-on gap;
- the existing client/head-user capability is in scope for hardening and a real
  end-to-end proof. No second client hierarchy will be invented.

## 1. UX diagnosis

The six Phase 11 references define a calm, consistent shell baseline: compact
scope selectors, 24px desktop gutters, clear card hierarchy, deep-navy dark
surfaces, mirrored Farsi structure, generous but efficient mobile spacing, and
a safe-area-aware bottom navigation. Their names, counts, metrics, providers,
and statuses are illustrative and will not enter production code.

The current implementation has a strong shared Phase 1 foundation, but the
repository-wide audit found accumulated drift:

- 52 page components include canonical pages, compatibility redirects, old
  detail routes, and platform/developer surfaces. Compatibility behavior is not
  consistently identifier-aware (`/flows` always goes to `/bpm`, `/site-builder`
  always goes to `/sites`, and `/iam` always goes to `/settings`).
- `AppShell` remains in use by `/bot/[sessionId]`, producing an English-only
  compatibility presentation. `WorkspaceControls` and `ProjectCards` are
  unused. Old project/bot BFF registries remain reachable even though canonical
  clients no longer use them.
- `panel-fixtures.ts`, `draft-store.ts`, `workspace-roadmap.ts`, and static
  product-roadmap content remain production-adjacent. The first is currently
  unused, but Phase 11 must remove dead fixture-bearing code rather than leave a
  future accidental data source.
- many pages are minified into very large one-line components. This obscures
  focus handling, pending-state consistency, error mapping, responsive behavior,
  and maintainability.
- visible Farsi support is incomplete. Numerous field labels, status labels,
  confirmations, section headings, empty/error states, and developer surfaces
  remain English-only even when the document is RTL.
- route components frequently surface `Error.message` or `String(error)`
  directly. The shared transport normalizes some failures, but raw upstream text
  can still become user-visible and correlation IDs are not consistently shown.
- async protection is good in Phases 6–10, but not universal. Record save/delete,
  definition-template creation, role/user state changes, site preview promises,
  and some legacy/detail actions do not share one mutation guard.
- dialogs and drawer-like panels are not uniformly focus-trapped/restored.
  Several are hand-authored overlays instead of shared primitives. Some icon
  buttons use only `×`, `＋`, arrows, or ellipses without localized accessible
  names.
- CSS has safe-area variables and reduced-motion support, but route-specific
  rules need a systematic 1440/1024/834/390/360 inspection. Dense panels and
  sticky actions can still approach viewport edges, especially with the mobile
  bottom navigation.
- Playwright is configured as one desktop Chromium project. Viewport changes are
  embedded in capture tests, so mobile/touch, tablet, RTL, PWA, offline, reduced
  motion, and keyboard journeys are not first-class projects.
- the PWA has a manifest and static-only service worker, but only one SVG icon,
  no install/update UI, no offline document fallback, and no stale-data contract.
  Service-worker registration errors are swallowed.
- Next.js has no bundle analysis or route-level performance budget. Large
  XYFlow editors are client-loaded with their pages, large JSON renders are not
  bounded, and list/log virtualization is absent.
- analytics and external error monitoring are not configured. Phase 11 must not
  add an external vendor without credentials/approval; it can add redacted local
  diagnostics and structured correlation-aware reporting.

### Page-by-page hardening inventory

| Route/group | Current finding | Phase 11 correction |
|---|---|---|
| `/auth` | Large duplicated desktop/mobile tree; some provider/legal controls are unavailable; raw/localized state coverage is uneven | unify state/action logic, preserve truthful not-configured controls, verify captcha/MFA/returnTo/keyboard/mobile |
| `/dashboard` | Real independent widgets; visual hierarchy needs full breakpoint and overflow review | align card gutters/density to references; verify partial, empty, limited-plan, dark, and RTL states |
| `/ai` | Persistent real session/upload flow; dense one-line implementation and several local confirmations/errors | normalize errors, pending and focus states; lazy-load heavy summary/code; verify file failure and resumed session |
| `/projects`, `/projects/[id]` | Real drafts/runs/releases; some headings/status copy remains English; old helper panel exists | complete locale keys, async guards, responsive tab overflow, and run/release confirmation/access tests |
| `/definitions/**` | Real visual editor; JSON parse catches are silent and inspector labels are English | preserve invalid JSON with inline errors, unify pending/dirty protection, keyboard/focus, RTL and mobile inspector spacing |
| `/data/**` | Real definition-driven CRUD; row action is a nested clickable span inside a row button; save/delete lack global pending guard | remove nested interaction, add guarded mutations, dialog focus, localized fields/errors, responsive grid/card behavior |
| `/automations/**` | Real XYFlow and AI node; editor is visually dense and AI operation authorization/provider semantics are incomplete | harden permission checks, diagnostics, keyboard graph alternative, loading/pending/offline behavior, inspector sheets, and lazy loading |
| `/bpm/**` | Real XYFlow; state candidate groups and transition roles/groups exist but the inspector exposes a partial model | expose supported fields with metadata-driven inputs, verify validation/access conflicts, keyboard alternative and sheet spacing |
| `/work/**` | Real assigned/visible queues and work item; only two queue views; client-side search; assignment target is free text | harden supported role/group/user assignment UI, localized history, pending states and truthful unsupported queue views; no invented endpoints |
| `/team/users`, `/team/roles` | Real bounded tenant access; search is visual-only and drawer focus is hand-authored | make client-side search real, focus-trap dialogs, per-row pending states, conflict/revision handling, full head-user E2E |
| `/clients` | Real platform-admin provisioning, but E2E stops before submit; search is visual-only | complete filtering, focus/pending/error/access states and live admin-create → head-login → bounded-delegation test |
| `/profile`, `/billing`, `/settings` | Real and truthful unavailable states; scattered English labels and route-local async handling | complete localization, focus, pending/error consistency, scope reload, and not-configured accessibility |
| `/bots/**` | Real Telegram/Bale, Automation/BPM bindings and secret refs | regression test provider states, secret redaction, pending de-duplication, responsive tabs and bot-to-process flows |
| `/sites/**` | Real registry/definition-backed builder and sandboxed preview | verify blank/create/publish/preview, focus, safe iframe, asset URL inputs, mobile/RTL padding; keep public site separate |
| public storefront endpoints | Backend render exists; panel has no dedicated customer-facing route | test actual published render through storefront; do not create a fake panel “site view” |
| `/domains` | Real DNS verification and truthful certificate state | verify poll/retry, destructive confirmation, LTR hostname/DNS fields, mobile status layout |
| `/notifications` | Real inbox/providers/history/preview/retry | normalize pending/error/correlation states, bell consistency, tabs/sheets and provider not-configured flow |
| `/commerce` | Real read-only records after seeding UI removal | access classification, structured empty/error states, mobile tables, no developer mutations |
| `/reports/**` | Real scoped catalog/runs/results; no export | harden large result rendering, cancellation/stale query handling, partial result semantics and responsive tables |
| `/media` | Real upload/list/content; deletion truthfully protected | harden preview security, progress/cancel/retry, list performance, localized metadata and mobile detail behavior |
| `/search` | Real scoped definitions/sync/stats/query; no load mutation | cancel stale search, debounce with visible refresh, run history/access states, structured large result performance |
| `/platform/health` | Real current-session checks | protect route authoritatively, redact output, add correlation and keyboard/accessibility; no fake durable history |
| `/platform/roadmap` | Static runtime product-planning data | remove from tenant runtime/navigation or retain only as explicitly protected developer documentation based on usage audit |
| `/api-docs` | Live catalog but old large English-only page | classify under platform/build access, migrate shell/localization/error/pagination, or retain with explicit audience |
| compatibility routes | Basic redirects exist; old bot session detail and BFF adapters remain | preserve identifiers/query/returnTo, remove only after zero-caller/test audit, and update stale tests |

## 2. Current route/API map

Phase 11 will not replace typed clients or change service ownership. The primary
functional boundaries are:

| Area | Browser/BFF surface | Owner | Scope/access truth |
|---|---|---|---|
| Bootstrap/scope | `/api/panel/bootstrap`, `/api/panel/scope` | panel BFF + SSO Session/Tenant/Site/Billing | JWT subject, session ownership, membership, site ownership |
| Identity/team/client | `/api/sso/**`, `/endpoint/tenants/**`, `/endpoint/clients/**` | SSO services + `tenant-service` | platform admin for clients; tenant effective permissions for users/roles |
| AI/projects | `/endpoint/ai-orchestrator/**` | `ai-orchestrator-service` | `builder:use` plus tenant/site resources |
| Automation | `/endpoint/automation-flows/**`, `/endpoint/automation-orchestrator/**` | `automation-orchestrator-service` | operations capability, flow roles, tenant/site |
| Automation AI call | `/internal/ai-orchestrator/operations` | `ai-orchestrator-service` called by Automation | Basic service auth exists; originating tenant-user AI permission is incomplete |
| BPM/cartable | `/endpoint/bpm/**` | `bpm-service` | tenant/site, actor user/roles/groups, assignee/access/transition rules |
| Media | `/endpoint/media/**`, `/public/media/content/**` | `media-service` | tenant membership for private bytes; public completed assets only |
| Bots | `/endpoint/bot-adapter/**` | `bot-adapter-service` | membership, secret refs, target validation, idempotent inbound dispatch |
| Sites/domains | `/endpoint/sites/**`, dynamic storefront definitions, `/endpoint/domains/**`, `/public/storefront/**` | `storefront-service` | tenant/site management vs public published render |
| Reports/search/notifications | respective `/endpoint/**` surfaces | owning service | scoped reads/mutations, persisted run/delivery state |

### Existing client/head-user contract

`POST /endpoint/clients` already atomically provisions:

- a real tenant;
- a real SSO identity for `headUser`;
- a `TENANT_OWNER` membership;
- selected known capability overrides;
- a real FREE subscription through billing;
- idempotent retry behavior.

The head user subsequently uses `/endpoint/tenants/{tenantKey}/users`, roles,
permission catalog, and effective access. `TenantTeamService` prevents granting
permissions the caller does not hold and prevents demoting/suspending the final
owner. Phase 11 needs proof and UI hardening, not another microservice.

### Existing BPM access contract

The backend currently supports:

- state `candidateGroups` and `accessRule.canRead/canEdit/canApprove`;
- transition `allowedGroups` and `allowedRoles`;
- managed-object assignee type `USER`, `ROLE`, or `GROUP`;
- actor roles/groups from authenticated authorities or trusted internal headers;
- assigned and visible queue reads;
- server-side edit/transition checks.

It does not currently model state `candidateUsers` or `candidateRoles`, and the
queue does not expose all requested server-side filtered/paginated views.

## 3. Proposed information architecture and migration cleanup

Canonical product groups remain Home, Build, Operate, Manage, and protected
Platform. Navigation continues to derive from bootstrap capabilities and
permissions.

Phase 11 migration decisions:

1. keep canonical routes as the only navigation destinations;
2. retain compatibility redirects for one verified release, preserving query
   identifiers (`flowKey`, `objectId`, `serviceKey`, `entityKey`, section) rather
   than blindly dropping them;
3. retire `AppShell` after migrating `/bot/[sessionId]` to a canonical AI session
   destination or an explicit gone/unavailable state;
4. remove unused fixture/helper components after import and BFF-call audits;
5. remove old `/api/projects/**` and `/api/bot-sessions/**` only when repo-wide
   search and updated tests prove no clients remain;
6. keep `/platform/health` protected; remove runtime Roadmap navigation/static
   tenant implication; classify API Docs explicitly;
7. keep public storefront outside the authenticated panel shell;
8. keep `/work` as the authenticated cartable. A site-embedded work portal is
   not inferred from public storefront APIs.

## 4. Desktop layout hardening

- Use 24px ordinary-page gutters, 16px builder gutters, and consistent 16–24px
  card padding; no action touches a card or viewport edge.
- Keep title scale at 26/34 and compact status/actions on one aligned header row.
- Make list/detail, editor/canvas/inspector, and runtime/timeline layouts use the
  shared patterns instead of route-specific shell variants.
- Preserve 340–400px inspectors without squeezing the working canvas below a
  usable width; at 1024px convert inspectors to drawers.
- Align toolbar controls to a common 44px block size and baseline. Long Farsi
  labels wrap within controls rather than pushing actions outside the container.
- Apply surface/border/text tokens to all charts, graphs, code views, overlays,
  sticky bars, and scrollbars in light and dark themes.
- Use semantic tables only where the viewport permits; prioritize columns at
  tablet sizes and expose remaining data in a detail view.

## 5. Mobile and PWA layout hardening

- Validate 390×844 and 360×800 with safe-area top/bottom insets.
- Reserve bottom-nav clearance for all page endings, sticky actions, sheets,
  upload trays, toasts, and confirmation dialogs.
- Use full-width primary actions with at least 16px inline gutters and 44px
  targets; no button sticks to the top or bottom edge.
- Builders retain a full-screen canvas, add-node sheet, inspector sheet, state
  list/graph keyboard alternative, and execution/detail sheet.
- Long tab sets scroll with visible focus and edge affordance rather than clip.
- Dense grids become cards/detail sheets; desktop tables are not scaled down.
- Add install readiness and an update-available prompt backed by actual service
  worker lifecycle events.
- Cache only the offline document shell and versioned static assets. API reads
  are not silently served as fresh; stale cached application data is shown only
  if an explicit versioned read cache is implemented and labeled. Mutations fail
  offline and never show success without a queue.

## 6. State matrix

| State | Required global behavior | Mutation behavior |
|---|---|---|
| Initial loading | stable skeleton matching final geometry | action unavailable |
| Refresh loading | retain current content with visible refreshing label | prevent conflicting mutation only |
| Empty | real zero-data explanation and permitted primary action | no sample records |
| Partial | keep successful regions; identify failed dependency | only dependent action disabled |
| Validation | field/path-specific message; preserve input | no request until locally valid where possible |
| Request error | normalized safe message, status, retryability, correlation ID | button re-enabled after response |
| Success | authoritative refreshed state and accessible toast/status | exactly one request per activation |
| Permission denied | identify missing action permission without leaking resource | no request from disabled control; backend remains authoritative |
| Plan/capability locked | distinguish locked, disabled, degraded, unavailable | no fake local completion |
| Offline | global banner plus route-local impact | mutation fails truthfully; no queued-success claim |
| Stale | timestamp/source and explicit refresh | do not mutate against stale revision without confirmation/conflict handling |
| Conflict | preserve draft and show authoritative revision/action | reload, compare, or retry with new idempotency key as contract allows |
| Destructive/high impact | accessible confirmation with resource identity | pessimistic pending state, one request |

Every primary and secondary async button will use a shared pending contract:
`disabled`, `aria-disabled`, `aria-busy`, stable width where practical, a clear
progress label, and duplicate invocation protection in the handler as well as
the UI.

## 7. Reusable component and infrastructure plan

### Keep and harden

- `PanelShell`, `ScopeAccessProvider`, `PanelProvider`, `PwaRuntime`;
- `Button`/`AsyncButton`, `Field`, `Select`, `Tabs`, `Dialog`, `Drawer`,
  `BottomSheet`, `Toast`, `ConfirmDialog`;
- `Skeleton`, `EmptyState`, `ErrorState`, `PermissionState`, `PlanGate`,
  `StatusBadge`, `CodeViewer`, `DataGrid`, `ResponsiveInspector`;
- typed domain API clients and normalized `UiError`.

### Add or consolidate

- `MutationBoundary`/`useGuardedMutation` for one-request pending, normalized
  error, correlation, confirmation, invalidation, and focus return;
- `RouteState` for consistent loading/empty/denied/unavailable/offline rendering;
- `FocusDialog` and `ResponsiveSheet` as the only overlay implementations;
- `LocalizedText`/message catalog helpers so Farsi is not embedded ad hoc in
  every JSX branch;
- `VirtualCollection` for large tables, logs, report rows, media grids, and
  execution timelines after measured thresholds;
- `LazyBuilder` boundaries for Automation/BPM/definition editors;
- `GraphKeyboardNavigator` and list alternative shared by XYFlow builders;
- `DiagnosticError` that redacts secrets and exposes a correlation ID/copy action;
- `PwaUpdatePrompt` and install-status helper based on real browser events;
- a route/capability registry shared by navigation, route guard tests, and the
  final matrix document.

### Remove after confirmed zero usage

- `components/app-shell.tsx`;
- `components/project-cards.tsx`;
- `components/workspace-controls.tsx`;
- `lib/panel-fixtures.ts`;
- `lib/draft-store.ts` and `lib/workspace-roadmap.ts`;
- legacy project/bot registry and BFF routes if final caller tests are empty.

## 8. API and backend gaps

### 8.1 Phase 11 security hardening: authorize existing Automation AI calls

Current behavior: an endpoint caller with Automation `operations:*` can start a
published flow containing `AI_OPERATION`. Automation then calls the AI internal
endpoint with service Basic authentication. Tenant/site are forwarded in the
request context, but the AI call does not receive or independently validate a
tenant-level AI permission/capability decision for the originating actor.

Proposed in-scope hardening:

- add canonical tenant permissions `ai.read` and `ai.execute` to the tenant
  permission catalog and system-role reconciliation;
- require `automation.manage` plus `ai.execute` when saving/activating a flow
  that contains `AI_OPERATION`;
- require `automation.execute` and re-check AI capability/permission when a human
  starts such a flow;
- persist immutable execution provenance: initiating user/service, authorization
  mode, flow/version, tenant/site, and correlation ID;
- for endpoint human execution, resolve current effective tenant access through
  the existing tenant-service internal API. Do not trust a role list supplied by
  the browser;
- for trusted BPM/bot service starts, require the internal service principal and
  an already activated flow whose AI-node authorization was validated. Preserve
  the external actor only as audited provenance, not as an authorization grant;
- require the effective `ai-orchestrator` capability to be enabled and available;
- return distinct `AI_PERMISSION_DENIED`, `AI_CAPABILITY_DISABLED`, and
  `AI_PROVIDER_NOT_CONFIGURED` errors; never downgrade them to a generic node
  success;
- ensure step snapshots and diagnostic exports redact prompts/outputs according
  to configured data policy and always redact credentials/tokens.

Owning direction remains:

```text
User/BPM/Bot -> automation-orchestrator-service
automation-orchestrator-service -> tenant-service (effective access/capability)
automation-orchestrator-service -> ai-orchestrator-service (internal operation)
```

### 8.2 Follow-on gap: platform/custom multimodal AI operations

This is required by the product request but is not Phase 11 hardening. Proposed
contract for separate approval:

```text
GET    /endpoint/ai-orchestrator/provider-profiles
POST   /endpoint/ai-orchestrator/provider-profiles
PUT    /endpoint/ai-orchestrator/provider-profiles/{profileKey}
POST   /endpoint/ai-orchestrator/operations/validate
POST   /internal/ai-orchestrator/operations
GET    /endpoint/ai-orchestrator/operations/{operationId}
```

Provider routing:

- `providerMode=PLATFORM` uses the configured AI Orchestrator routing policy;
- `providerMode=CUSTOM_PROFILE` references an AI-Orchestrator-owned provider
  profile, never a raw API key or arbitrary runtime URL;
- custom profiles store only a Vault/secret-manager reference, bounded base URL,
  provider type, allowed models/modalities, timeout/size limits, tenant/site
  scope, allowed roles, and configuration state;
- endpoint validation blocks private/link-local metadata addresses, redirect
  escape, unsupported TLS, unbounded responses, and provider/model mismatches;
- unavailable credentials expose `NOT_CONFIGURED`, never fake success.

Typed operation request:

```text
operation: ANALYZE_DATA | ANALYZE_MEDIA | GENERATE_CONTENT | GENERATE_DSL |
           GENERATE_IMAGE | GENERATE_VIDEO
providerMode: PLATFORM | CUSTOM_PROFILE
providerProfileKey?: string
instructions: string
inputs: [
  { kind: TEXT | JSON, value: ... },
  { kind: MEDIA_ASSET, assetKey: ..., mediaType: IMAGE | AUDIO | VIDEO | FILE }
]
outputSchema?: object
outputMode: INLINE | MEDIA_ASSET
locale?: en | fa
```

Architecture and persistence:

- `ai-orchestrator-service` owns provider adapters, provider profiles,
  multimodal request normalization, operation status, usage metadata, and model
  capability checks;
- `media-service` owns input bytes and generated binary artifacts. AI reads
  scoped assets through an internal byte contract and writes generated image,
  audio, or video through an authenticated internal upload/finalize contract;
- `automation-orchestrator-service` owns node configuration, mappings,
  idempotent execution, retry, and the reference to the AI operation/result;
- generated text/JSON/DSL returns as typed output. A later explicit Automation
  node may persist it to Content or another dynamic service; AI execution must
  not write arbitrary business records implicitly;
- large/long video work is asynchronous with `QUEUED/RUNNING/SUCCEEDED/FAILED/
  NOT_CONFIGURED`, polling/backoff, cancellation where provider-supported,
  idempotency, bounded retention, and persisted provider correlation IDs;
- all operations enforce tenant/site ownership, `ai.execute`, provider-profile
  access, asset access, size/type limits, and audit/redaction.

Record this as new gaps `GAP-080` (AI execution authorization), `GAP-081`
(provider profiles), `GAP-082` (multimodal media input), and `GAP-083`
(generated binary artifact lifecycle). Only GAP-080 is proposed for Phase 11.

### 8.3 BPM cartable and site-view gaps

In-scope Phase 11 work uses the existing contract to expose and verify:

- state candidate groups and access rules;
- transition roles/groups;
- object assignment type USER/ROLE/GROUP;
- assigned and visible cartable tabs;
- active form, comments, attachments, locks, transitions, and audit history.

Follow-on contracts, not to be invented in Phase 11:

- `candidateUsers` and `candidateRoles` on a BPM state, with authoritative
  validation against tenant identities/roles;
- pageable server views for My roles/groups, Unassigned, and Completed with
  totals, filters, SLA/priority, and permission-gated visibility;
- an assignable-target endpoint returning only users/roles/groups the caller may
  select, replacing free-text assignment;
- a storefront-authenticated portal/block contract if a tenant wants a site page
  to embed “my work.” It must authenticate through SSO, resolve the same active
  tenant/site, call BPM as the user, and never expose work through public render.

The public site viewer remains Storefront-owned. The cartable remains BPM-owned.
Embedding one in the other must be an explicit composition contract, not a
frontend shortcut.

### 8.4 Client/head-user gaps

No new backend owner is required for the requested core flow. Phase 11 will add
the missing proof:

1. platform admin creates a tenant/head user with selected capabilities;
2. creation is idempotent and returns the real FREE subscription;
3. admin logs out;
4. head user logs in and selects the new tenant/site scope;
5. navigation exposes only selected effective capabilities;
6. head user creates a bounded custom role and adds another user;
7. attempts to grant an unavailable/unheld permission or cross-tenant role fail;
8. the new member logs in and sees only effective access.

Potential follow-on improvements are invitation delivery, ownership transfer,
capability changes after provisioning, and client list detail APIs. They are not
required to prove the currently implemented direct-provisioning contract.

## 9. Exact implementation file plan

The Phase 11 change will be split into logical commits. Exact additions may be
refined after compilation, but service ownership will not change.

### Panel shell, PWA, migration, and shared UI

- `panel-web/package.json`, `package-lock.json`, `next.config.mjs`,
  `playwright.config.ts`;
- `panel-web/app/layout.tsx`, `globals.css`, `phase1-shell.css`;
- `panel-web/public/manifest.json`, `sw.js`, existing repository icon assets and
  any generated raster icon sizes derived from the Cyan repository logo;
- `panel-web/components/pwa-runtime.tsx`, `app-providers.tsx`,
  `panel-provider.tsx`, `scope-access-provider.tsx`, `panel-shell.tsx`,
  `ui/primitives.tsx`, `access-gates.tsx`;
- new shared localization, guarded-mutation, virtual-collection, diagnostic,
  graph-keyboard, lazy-builder, and PWA update helpers under
  `panel-web/components/**` and `panel-web/lib/**`;
- canonical and compatibility page files under `panel-web/app/**` where the
  audit identifies route, access, pending, focus, locale, responsive, or error
  drift;
- remove the zero-use compatibility/fixture files listed in section 7 only after
  caller verification.

### Automation AI authorization hardening

- `tenant-service/src/main/java/com/cyancoder/tenant/service/TenantTeamService.java`
  and its tests for canonical AI permissions/system-role reconciliation;
- `automation-orchestrator-service` execution provenance/domain DTOs,
  endpoint/internal controllers, flow validation/lifecycle service,
  `GraphAutomationRuntime`, `ItemStreamAutomationRuntime`, internal tenant client,
  structured errors, configuration/OpenAPI, and tests;
- `ai-orchestrator-service` internal operation controller/service error mapping,
  tenant/site context DTO validation, OpenAPI, and tests only as required for
  GAP-080; no custom-provider or multimodal implementation in Phase 11;
- `panel-web/lib/automation-api.ts` and
  `components/automation/automation-builder.tsx` for permission/capability state,
  normalized errors, and truthful provider availability.

### BPM/cartable hardening

- `panel-web/lib/bpm-api.ts`;
- `panel-web/components/bpm/bpm-designer.tsx`;
- `panel-web/app/work/page.tsx` and `app/work/[objectId]/page.tsx`;
- BPM tests only if the hardening exposes a defect in existing access/assignment
  behavior. No new state candidate-user/role persistence is included.

### Client/head-user hardening

- `panel-web/components/iam/client-console.tsx`, `team-console.tsx`;
- `panel-web/app/clients/page.tsx`, `team/users/page.tsx`, `team/roles/page.tsx`;
- tenant/SSO/billing code only if the live E2E exposes an actual contract defect;
  otherwise changes are limited to tests/UI hardening.

### Tests and final documents

- migrate existing specs under `panel-web/tests/**`;
- add Phase 11 route, accessibility, PWA, compatibility, pending-mutation,
  delegated-client, Automation-AI-auth, BPM/cartable, and visual-regression specs;
- backend unit/integration tests in every changed owning service;
- `docs/ui-redesign/15-FINAL-IMPLEMENTATION-STATUS.md`;
- `docs/ui-redesign/16-REMAINING-BACKEND-GAPS.md`;
- `docs/ui-redesign/17-ROUTE-AND-CAPABILITY-MATRIX-FINAL.md`;
- `docs/ui-redesign/completion/phase-11/PHASE-11-COMPLETION-REPORT.md`;
- `docs/ui-redesign/completion/phase-11/SCREENSHOT-MANIFEST.md` and screenshots.

## 10. Test and screenshot plan

### Static, unit, integration, and build

```text
npm run lint
npm run build
npm run test:e2e
```

Run targeted backend tests for every changed service, at minimum Tenant,
Automation Orchestrator, AI Orchestrator, and any BPM defect fixed during the
audit. Verify OpenAPI generation for changed endpoint/internal contracts.

Add bundle analysis and record route chunk sizes for dashboard, AI, definition,
Automation, BPM, reports, media, and search. Define budgets from the measured
baseline rather than claiming an arbitrary pass.

### Deterministic browser E2E

- every canonical route loads under allowed, denied, capability-disabled,
  unavailable, empty, error, and representative success states;
- every compatibility route preserves identifiers/query and reaches the correct
  canonical job;
- click every visible navigation destination, tab, menu, drawer, primary action,
  retry, confirmation, and close control in Phases 1–11;
- for every mutation class, delay the response, assert disabled + `aria-busy`,
  click/keyboard-activate repeatedly, and assert exactly one request;
- keyboard-only shell, auth, dialogs, drawers, definition editor, Automation
  graph/list alternative, BPM graph/list alternative, cartable, and active form;
- axe-equivalent automated WCAG checks plus manual focus order, names, contrast,
  reduced motion, zoom/text expansion, and 44px targets;
- service worker install/activate/update, offline shell, online recovery, and
  proof that offline POST/PUT/PATCH/DELETE does not report success;
- English/Farsi, LTR/RTL, light/dark/system, date/number formatting, LTR IDs/code;
- stale request cancellation during scope, filter, route, and query changes;
- secret and token redaction in errors, logs, diagnostics, bot/provider forms,
  Automation steps, and iframe content.

### Live end-to-end flows with minimum owning services

1. Sign in with captcha/MFA and recover persisted session scope.
2. Platform admin creates a client, head user, selected services, and FREE plan.
3. Head user signs in, creates a bounded role and member, and is blocked from
   cross-tenant or excessive grants.
4. Admin/head builder creates a CRM + Automation project, provisions it, runs
   the Automation AI node through the platform AI path, creates a release, and
   publishes it.
5. Verify an actor without `ai.execute` cannot activate/start the AI-bearing flow.
6. Create/activate BPM with role/group access, start work, find it through the
   correct cartable view, submit the active form, attach real media, comment,
   transition, and inspect history.
7. Connect Telegram and Bale integrations using real configured secret
   references when prerequisites exist; verify bot-to-Automation and bot-to-BPM
   dispatch/idempotency. If external credentials are absent, verify only the
   truthful `NOT_CONFIGURED` branch plus internal contract tests.
8. Create/publish a real storefront page and render it through the public
   storefront endpoint at desktop/mobile sizes. Do not expose authenticated BPM
   work publicly.
9. Exercise notifications, commerce reads, reports, real media upload/content,
   search definition/sync/query, domain `NOT_CONFIGURED` or real DNS state, and
   protected platform health.

### Visual regression capture

Capture every Tier 1 screen and all canonical route groups at:

- 1440×1000 desktop;
- 1600×1000 builders;
- 1024 desktop/tablet transition;
- 834×1112 tablet;
- 390×844 mobile;
- 360×800 small mobile;
- English light and dark;
- Farsi light and dark RTL for Tier 1 screens.

Target state captures include loading, empty, partial error, validation,
permission denied, plan locked, capability unavailable, offline/stale,
destructive confirmation, pending mutation, success toast, and mobile sheet.

Visual review will compare shell geometry, baseline alignment, card/control
padding, content density, truncation, action gutters, safe-area clearance,
focus, light/dark contrast, and RTL mirroring against the supplied Phase 11
references. Sample reference content will not be copied.

## Approval checkpoint

Approval of this report authorizes Phase 11 hardening, including GAP-080
authorization for the existing Automation AI node. It does **not** authorize the
new custom-provider/multimodal/video feature set (GAP-081 through GAP-083), new
BPM state candidate-user/role persistence, or a site-embedded authenticated work
portal. Those remain fully specified, truthful follow-on gaps so Phase 11 can
finish without violating its “no major new product features” boundary.

Implementation has not started and will not start until this report is approved.

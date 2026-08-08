# Phase 10 Pre-Implementation Report

Status: **AWAITING APPROVAL**

Scope: Phase 10 only — Reports, Media, Search, operational tools, and related settings

Prepared: 2026-08-08

No Phase 10 runtime code has been changed. Phase 11 has not started.

## 1. UX diagnosis

The supplied Phase 10 references establish a compact operational product pattern: a summary/header layer, searchable lists or grids, a focused detail/editor surface, and clear progress/health states. They are visual direction only; none of their sample records, metrics, providers, or statuses will be copied into runtime data.

The current product does not yet meet that direction:

- `/reports` and `/media` do not exist.
- `/search` uses the obsolete shell, labels Search as “Search & media” in navigation, seeds fixed form values, creates definitions during initial load, swallows definition errors, triggers an unscoped sync, and renders response JSON rather than an operator-friendly result.
- `/qa` uses the obsolete shell, accepts editable scope inputs, runs a small set of real checks sequentially, and exposes raw payload JSON. It is not isolated as a platform-admin tool.
- `/roadmap` mixes static product-planning content with tenant runtime signals and is available as an ordinary panel route.
- settings do not describe Report, Media, or Search capability/health truthfully.
- existing shared Phase 1 primitives already cover loading, empty, denied, unavailable, error, status, dialog, tabs, async-button, theme, responsive shell, and English/Farsi RTL states. Phase 10 should extend these patterns rather than create a second design system.

The most important UX constraint is truthfulness. A report must not appear complete without a persisted successful run; an uploaded asset must not appear deliverable when its bytes cannot be retrieved; an index must not appear healthy when sync was unscoped or failed. Every mutation will have a pending state, disable repeat submission, retain contextual errors, and refresh authoritative data after success.

## 2. Current route and API map

| Product area | Current UI | Current backend/BFF | Audit result |
|---|---|---|---|
| Reports | No route | Report service has unprotected-looking legacy CRUD/run at `/api/report-service/reports`; dynamic report run is internal-only | No tenant/site-scoped bearer catalog, no membership check, no persisted run/status/history, no export contract |
| Media | Upload is used by AI/work-item pages; no library | `/endpoint/media/uploads/prepare`, byte `PUT`, cancel `DELETE`; raw dynamic records; public metadata reads | Bytes are persisted and progress is real, but asset metadata is written without scope; advertised `/public/media/content/{assetKey}` has no controller; no cohesive list/detail/update/usage/delete contract |
| Search | `/search` legacy builder/tester | Scoped query/suggest; unscoped immediate sync; dynamic `index-definition` and `search-document` | Initial-load mutation and fixed values are unsafe; sync drops scope and creates definitions/records unscoped; no run history/count/health |
| QA | `/qa` legacy operational checker | Real service calls through current clients | No platform-admin route boundary; no durable history API; raw payload-first UI |
| Roadmap | `/roadmap` tenant-facing static plan | No owning runtime API | Static planning content is not tenant operational data and must be moved behind the platform/developer boundary |
| Settings | `/settings` Phase 8 console | Bootstrap capability/access data | Report/Media/Search sections absent; no reason to add settings mutations unless an owning API exists |

Confirmed BFF routing facts:

- the dynamic proxy already permits `report-service`, `media-service`, and `search-index-service`;
- the generic service proxy permits `search-index-service`, but not the new Report/Media endpoint contracts;
- `panel-web/lib/media-api.ts` already implements a real `XMLHttpRequest` byte upload with progress;
- `panel-web/lib/service-api.ts` forwards scope for search/query and suggest, but `syncSearchIndex` has no scope argument;
- gateway routes cover legacy/public/internal paths but must be extended for any new bearer `/endpoint/**` paths if gateway parity is required.

## 3. Proposed information architecture

### Tenant operations

- `/reports` — report catalog and latest-run summaries.
- `/reports/[reportKey]` — definition summary, parameter form, run action, history, selected run result.
- `/media` — scoped asset library with list/grid switch, search, type/status filters, upload, and pagination.
- `/media/[assetKey]` — preview, safe metadata, scope, delivery state, and usage/delete state.
- `/search` — scoped index catalog and index health.
- `/search/[indexKey]` — definition editor, sync history/count, and query/suggest tester.

List and detail routes will preserve identifiers in the URL, respond to active tenant/site changes, and never create a definition or record during a read.

### Platform/developer operations

- `/platform/health` — canonical, platform-admin-only QA route.
- `/platform/roadmap` — canonical, platform-admin/developer-only planning route, visually and navigationally separated from tenant operations.
- `/qa` and `/roadmap` — temporary compatibility redirects after the canonical route guard is active.
- `/platform/api-docs` remains a Phase 10 classification option only if the existing API Docs audience and access check can be preserved; otherwise `/api-docs` remains unchanged until Phase 11 cleanup.

No Report, Media, Search, QA, or Roadmap route will be shown when its permission/capability gate is not satisfied. A direct visit must independently enforce the same access decision and render denied/unavailable states rather than rely on hidden navigation.

## 4. Desktop design

### Reports

- compact summary row from actual definition/run counts;
- searchable definition list with title, source, latest status, latest duration, and last-run time;
- detail uses a two-column working layout: parameter/run panel and history/result panel;
- result uses a semantic table; a small chart appears only when the response contains a compatible grouped numeric series;
- no export control until a real export response exists;
- partial source failures stay visible alongside any returned partial result.

### Media

- toolbar with search, type/status filters, list/grid switch, and upload action;
- cards use authenticated/public byte URLs only when allowed; document assets use a safe file summary rather than an unsafe embed;
- side detail panel/page contains preview, filename, content type, size, visibility, upload state, tags/folder only when supported, and usage protection;
- upload tray shows byte progress, current state, failure, cancel, and retry eligibility from the real upload contract;
- destructive actions require confirmation and are absent/disabled when usage cannot be proven.

### Search

- index list shows source, engine, indexed count, last sync, and health from the service;
- detail editor separates source, searchable/analyzer fields, filters/facets/sort/suggest configuration;
- sync card shows current/last run and history rather than optimistic “started” copy;
- tester renders structured result cards/table, facets, pagination, and suggestions without dumping JSON.

### Operational tools

- platform shell treatment with environment label, check groups, duration/status, retry-one, and current-session export where allowed;
- technical response details are progressive disclosure with secret redaction;
- Roadmap is visibly a planning/developer surface and does not imply tenant state.

## 5. Mobile design

- all routes remain operational at 390×844 and 360×800, not read-only;
- filters and secondary actions move into accessible bottom sheets; primary run/upload/sync actions remain reachable without sticky elements covering content;
- report results and histories use responsive cards or horizontally scrollable semantic tables with a visible affordance;
- media defaults to a one-column grid/list and opens detail as a full-height sheet/page;
- search definition sections become stacked accordions, with key/URL/source values kept LTR in Farsi;
- pending actions remain disabled and display a label/progress indicator; bottom safe-area padding prevents controls from touching viewport edges or the mobile navigation;
- keyboard focus is trapped/restored for dialogs/sheets and returns to the triggering control.

## 6. State matrix

| State | Reports | Media | Search | QA/Roadmap |
|---|---|---|---|---|
| Loading | catalog/detail/history skeletons | grid/detail skeletons and byte progress | index/detail/history skeletons | check skeleton/progress |
| Empty | no definitions / no runs | no assets for scope/filter | no index definitions / no results | no checks/history only when truthful |
| Error | catalog, definition, run, or selected-result error isolated | prepare/upload/list/preview/detail errors isolated | definition, sync, query, suggest errors isolated | failed check with status/duration and retry |
| Partial | successful rows plus source warnings | metadata available but preview unavailable | results available with degraded sync/health | some checks pass, others fail |
| Denied | route/action denied by report permission | route/upload/edit denied by media permission | route/manage denied by search permission | platform route denied unless platform admin/developer |
| Unavailable | capability/service unavailable | storage/delivery unavailable | service/index source unavailable | check target unavailable |
| Pending | run button disabled for same action | upload/cancel/edit/delete mutually guarded | save/sync disabled against duplicate submission | retry-one disabled while running |
| Success | persisted run status/result | persisted bytes plus scoped asset | persisted definition/run/count | real response only |
| Conflict | idempotency/revision conflict shown | duplicate asset/reference conflict | duplicate/revision/sync conflict | not applicable unless API returns it |
| Not configured | source integration unavailable | external storage/variant provider if applicable | configured engine/provider unavailable | target service not configured |

Empty arrays are valid data and will not be presented as service failure. `401`, `403`, `404`, `409`, validation, timeout, and upstream-unavailable errors will remain distinct.

## 7. Reusable component plan

Reuse:

- `PanelShell`, `useScopeAccess`, `usePanel`;
- `AsyncButton`, `Skeleton`, `EmptyState`, `ErrorState`, `StatusBadge`, `Dialog`, `Tabs`, `Field`, `Select`, and existing responsive sheets;
- shared API error normalization and scope-aware platform clients;
- existing real media byte-upload implementation.

Add only shared operational primitives justified across these pages:

- `OperationalToolbar` for search/filter/view/actions;
- `PaginatedCollection` controls with accessible counts;
- `RunStatusTimeline` for report/search runs;
- `StructuredResultTable` with optional compatible-series chart adapter;
- `AssetPreview` with image/document/fallback policies;
- `DefinitionSection` for report/search editors;
- `HealthCheckCard` with redacted expandable detail.

CSS will extend the Phase 1 token system and logical properties. It will not introduce physical left/right assumptions, button-to-edge layouts, nested interactive controls, or a page-specific shell.

## 8. Backend gaps and proposed contracts

Approval of this report authorizes implementation of the following service-owned gaps. Names may be refined to existing conventions during implementation, but the semantics and ownership must remain.

### 8.1 Report service — owner: `report-service`

Use `dynamic-report` as the canonical definition shape; retire the unscoped legacy JPA controller from panel use rather than duplicating two catalogs.

Proposed bearer surface:

```text
GET  /endpoint/reports?page=&size=&q=
GET  /endpoint/reports/{reportKey}
POST /endpoint/reports
PUT  /endpoint/reports/{reportKey}
POST /endpoint/reports/{reportKey}/runs
GET  /endpoint/reports/{reportKey}/runs?page=&size=
GET  /endpoint/reports/{reportKey}/runs/{runId}
```

Every request requires `X-Tenant-Key`; `X-Site-Key` is optional and becomes part of definition/run scope. Read/manage/run permissions are distinct. Tenant membership and site ownership are verified through the established tenant-service internal client pattern.

Persist `ReportRunEntity` in PostgreSQL with `runId`, report key/revision, tenant/site, requested parameters, status (`QUEUED`, `RUNNING`, `SUCCEEDED`, `PARTIAL`, `FAILED`), row/group counts, result payload or bounded result reference, warnings/error code, actor, idempotency key, and timestamps. A unique tenant/idempotency constraint prevents duplicate runs. The first implementation may execute synchronously but must persist transitions and return the authoritative run DTO; the UI must not simulate progress. Export is omitted because no current export contract exists.

Source calls must forward scope via the internal endpoint convention, validate service/entity and filter/aggregate fields against the referenced definition, apply bounded row/page limits, and return normalized validation/not-found/denied/upstream errors. The current internal dynamic-report route remains for service-to-service callers but must share the scoped execution service.

### 8.2 Media service — owner: `media-service`

Keep the current prepare/byte PUT/cancel flow and make the resulting asset real and scoped.

Proposed bearer/public surface:

```text
GET   /endpoint/media/assets?page=&size=&q=&type=&status=&folderKey=&tag=
GET   /endpoint/media/assets/{assetKey}
PATCH /endpoint/media/assets/{assetKey}
GET   /endpoint/media/assets/{assetKey}/content
GET   /endpoint/media/assets/{assetKey}/usage
DELETE /endpoint/media/assets/{assetKey}
GET   /public/media/content/{assetKey}
```

The upload entity remains the durable byte/storage record. Completion writes `media-asset` through `DynamicScope(tenantKey, siteKey)` and associates the upload/asset deterministically. The endpoint list is pageable and searchable from persisted upload metadata, enriched by scoped asset metadata. Update permits only safe metadata fields. Private content requires bearer authorization plus scope membership; public content is served only for completed `PUBLIC` assets with safe content type/disposition/cache headers and path containment checks.

Usage references do not currently have a platform-wide reverse-reference contract. Therefore Phase 10 will expose an explicit `UNKNOWN`/`UNAVAILABLE` usage state and keep delete disabled unless the service can prove `referenceCount == 0`. It will not claim that an asset is unused. A future reference registry remains a documented gap rather than implementing cross-service scans in Media. Folders/tags will be shown only if scoped `media-folder` definitions/records already exist or the user explicitly creates them through supported dynamic endpoints; page load will never instantiate them.

No external object-store prerequisite is required for local filesystem operation. If an external storage/variant provider is configured but unavailable, the API returns truthful `NOT_CONFIGURED`/`DEGRADED` status while local supported behavior remains explicit.

### 8.3 Search Index service — owner: `search-index-service`

Proposed bearer surface additions:

```text
POST /endpoint/search-index/sync/{sourceServiceKey}/{sourceEntityKey}
GET  /endpoint/search-index/sync-runs?page=&size=&indexKey=
GET  /endpoint/search-index/sync-runs/{runId}
GET  /endpoint/search-index/stats?indexKey=
```

The existing sync endpoint gains required tenant scope, optional site scope, actor, and `Idempotency-Key`. Persist `SearchSyncRunEntity` in PostgreSQL with scope, source/index identity, status, scanned/synced/failed counts, bounded errors, actor/idempotency, and timestamps. Scope must be forwarded to source internal-record reads and to `search-document` definition/record operations. The service must validate that a scoped index definition exists and matches the requested source; sync may not create definitions implicitly. Search-document template instantiation, if absent, is an explicit administrator/setup operation—not a GET or sync side effect.

Stats count only scoped indexed documents and include last run/health. Query/suggest retain their structured response and scope. Public search requires a resolvable tenant/site boundary rather than an unscoped global fallback. The panel editor loads and saves actual scoped `index-definition` records and derives source field choices from real service/entity definitions.

### 8.4 Access, transport, OpenAPI, and errors

- add Report/Media to the typed panel service proxy allowlist, forwarding only approved bearer, tenant/site, correlation, content, range, and idempotency headers;
- update gateway route predicates for new `/endpoint/reports/**`, `/endpoint/media/**`, and `/endpoint/search-index/**` parity;
- use existing platform authorization conventions plus tenant membership/site ownership checks; never trust the header alone;
- return consistent structured errors for invalid scope, validation, conflict, referenced media, run failure, and not configured/degraded state;
- add controller-derived OpenAPI descriptions and keep internal Basic routes separate from bearer endpoint routes;
- never return filesystem paths, secrets, Basic credentials, raw upstream auth errors, or unbounded source payloads.

### 8.5 QA/Roadmap and bot/BPM boundary

QA and Roadmap require frontend route/access classification, not a new fake health/history backend. QA will operate on real calls; durable history is omitted because no storage contract exists. Client-side export, if included, exports only the currently displayed redacted real results.

Bot-to-Automation/BPM bindings remain the service-backed Phase 9 capability and are important, but they are outside Phase 10’s authorized feature scope. Phase 10 will preserve and regression-test their navigation/access wiring; it will not redesign or duplicate bot/BPM contracts in Report, Media, or Search services.

## 9. Exact implementation file plan

The anticipated file set is intentionally explicit. Minor adjacent files may be required when compilation reveals an existing convention, but no unrelated service will own these capabilities.

### Report backend

- replace/retire panel use of `report-service/.../controller/ReportController.java`;
- add endpoint report controller, scoped report definition/run services, run entity/repository, request/response DTOs, validation/error handling, tenant-service membership client/config;
- update `DynamicReportQueryService.java`, `ReportQueryService.java`, `InternalDynamicReportController.java`, security config, application config, migrations, OpenAPI annotations/docs;
- add unit/controller/integration tests under `report-service/src/test/**`.

### Media backend

- update `EndpointMediaUploadController.java`, `MediaByteUploadService.java`, `MediaAssetService.java`, `MediaUploadEntity.java`, and repository;
- add endpoint asset controller, public content controller, list/detail/update/usage DTOs, pageable query/content/authorization services, membership client, migration indexes, OpenAPI docs;
- extend `MediaByteUploadServiceTest.java` and add controller/scope/content/security integration tests.

### Search backend

- update `EndpointSearchIndexController.java`, `SearchIndexSyncService.java`, `InternalServiceHttpSupport.java`, security/config;
- add sync-run entity/repository/DTOs, stats query, migration, membership validation, OpenAPI docs;
- add scope, idempotency, source-validation, history/stats, query/suggest isolation tests under `search-index-service/src/test/**`.

### Panel and BFF

- add `panel-web/app/reports/**`, `panel-web/app/media/**`, `panel-web/app/search/[indexKey]/**`, and protected `panel-web/app/platform/{health,roadmap}/**`;
- refactor `panel-web/app/search/page.tsx`, and replace old `/qa` and `/roadmap` pages with compatibility routes after canonical guards exist;
- add `panel-web/lib/report-api.ts`, extend `media-api.ts`, add/refine `search-api.ts` or the search section of `service-api.ts`, types, error mapping, and BFF allowlists;
- update `panel-web/components/panel-shell.tsx`, capability/access registry, settings UI, shared operational components, and `app/globals.css` using logical properties/tokens;
- add Phase 10 E2E tests and fixtures that intercept APIs only in isolated UI tests; production code receives no mock data.

### Documentation/configuration

- service migrations/config/OpenAPI for each owning microservice;
- gateway predicates where needed;
- Phase 10 completion report and screenshot manifest after implementation.

## 10. Verification, E2E, and screenshot plan

Backend verification:

- Report: create/read/update definition in tenant A, reject tenant B; validate fields/source; same idempotency key yields one run; persisted success/partial/failure history; internal Basic and endpoint bearer boundaries.
- Media: prepare/upload actual bytes with progress-compatible content length; retrieve correct bytes; private/public access; tenant/site isolation; path traversal/content headers; cancel/failure; scoped metadata; delete remains protected when usage is unknown.
- Search: save a real definition explicitly; scoped sync from a real source; tenant/site isolation; persisted run counts/history/errors; duplicate idempotency; structured query/suggest; verify sync performs no definition creation.

Panel E2E journeys:

- login, select real tenant/site, navigate every Phase 10 menu item;
- report definition → parameters → run → persisted history/result;
- media upload → progress → detail → real preview/download → protected delete state;
- search definition save → sync → history/count → query/suggest structured results;
- permission/capability denial, service unavailable, empty, partial, validation, conflict, and retry;
- duplicate clicks while a mutation is pending must produce exactly one request;
- platform admin can open Health/Roadmap; ordinary tenant user cannot;
- regression click-through for existing Phase 1–9 navigation, including Bots → Automation/BPM bindings.

Commands after approved implementation:

```text
mvn test in report-service
mvn test in media-service
mvn test in search-index-service
npm run lint
npm run build
npm run test:e2e -- --grep "Phase 10"
```

Then run the complete relevant panel E2E suite to catch Phase 1–9 regressions.

Screenshots will cover:

- desktop 1440×1000, tablet 834×1112, mobile 390×844;
- Reports list/detail/result, Media list/upload/detail, Search list/editor/tester, platform Health/Roadmap;
- English light and dark;
- Farsi RTL light and dark for the principal Report, Media, and Search screens;
- loading, empty, error/unavailable, and pending states through the shared state system where a dedicated reference does not exist.

Visual comparison will check density, page gutters, card/list alignment, button spacing from panel and viewport edges, sticky regions, logical RTL alignment, readable dark-theme states, touch targets, focus visibility, and safe-area clearance.

## Approval gate

Implementation will begin only after approval of this report. On approval, Phase 10 will be implemented end-to-end in the owning services and panel, tested, visually reviewed, documented, and committed in logical changes. Phase 11 will still wait for the Phase 10 completion report and its own pre-implementation approval gate.

# Phase 10 Completion Report

Completed: 2026-08-09

## Outcome

Phase 10 is implemented across the owning backend services and `panel-web`. No Phase 11 work was started, no production mock data was added, and no read operation creates definitions.

The primary user can now:

- create and browse scoped report definitions, run them once per idempotency key, inspect persisted run history, and render structured rows;
- upload real media bytes with progress, browse/search/filter scoped assets, preview public images or retrieve authorized private content, inspect metadata, and see truthful delete protection;
- explicitly create or update a scoped search definition, start a scoped persisted synchronization, see indexed counts and latest run health, and exercise structured query/suggest results;
- access QA health and product roadmap only through a platform-admin/developer area, with compatibility redirects from `/qa` and `/roadmap`;
- see Report, Media, and Search capability/service availability in Settings.

## Backend delivery

### Report service

- added bearer `/endpoint/reports/**` catalog, definition, run, history, and run-detail contracts;
- reused the service-owned `dynamic-report` template with tenant/site-scoped definitions and records;
- added persisted `ReportRunEntity` state with tenant/site, actor, request/result, timestamps, error state, and tenant/idempotency uniqueness;
- forwarded scope on internal source reads and on the internal dynamic-report path;
- added tenant membership enforcement and existing platform read/manage authorization.

Report export is intentionally absent because there is no export contract.

### Media service

- retained the real prepare → byte PUT → completion/cancel lifecycle and browser progress support;
- fixed completed asset metadata to use the upload's tenant/site scope;
- added scoped list/search/page/filter, detail, safe metadata update, usage-state, and authenticated content contracts;
- implemented the previously missing `/public/media/content/{assetKey}` byte controller for completed public assets;
- added filesystem path containment, visibility/status checks, safe content headers, and tenant membership enforcement;
- changed delivery URLs to real public or authenticated content paths instead of metadata URLs.

There is no platform reverse-reference registry yet. Usage returns `UNKNOWN` and deletion remains protected; the UI does not claim an asset is unused.

### Search Index service

- added explicit scoped index-definition list/get/save endpoints;
- removed implicit `search-document` definition creation from synchronization;
- added tenant/site and idempotency to sync requests and forwarded scope to source reads and search-document writes;
- persisted search sync run status/count/error/timestamps and exposed history and stats endpoints;
- validated that a matching saved index definition exists before sync;
- required tenant scope for public search and tenant membership for bearer query/suggest/admin operations.

### Platform boundaries

- added Report/Media endpoint routes to the panel BFF and preserved binary response bytes/headers;
- added gateway predicates for the new bearer endpoint paths;
- added `report.read/manage`, `media.read/manage`, and `search.read/manage` to the tenant permission catalog and system-role reconciliation;
- retained endpoint bearer versus internal Basic separation.

## Panel delivery

New canonical routes:

- `/reports`
- `/reports/[reportKey]`
- `/media`
- `/search`
- `/platform/health`
- `/platform/roadmap`

Compatibility routes:

- `/qa` → `/platform/health`
- `/roadmap` → `/platform/roadmap`

The Phase 1 shell now labels Reports, Media, and Search separately and shows the Platform group only to recognized platform-admin roles. Mobile “More” includes all accessible operational and platform destinations.

All new mutation controls use `AsyncButton` pending states. Report run, definition save, index sync/save, media upload, and operational retry actions disable duplicate interaction until their authoritative response completes.

## Responsive, locale, theme, and accessibility

- desktop uses compact operational toolbars, scoped lists/grids, focused report/search workspaces, and sticky contextual panels;
- tablet collapses multi-column layouts without shrinking desktop tables into unreadable columns;
- mobile stacks report parameters/results, uses one-column media/search layouts, preserves actions, and includes safe-area bottom padding;
- Farsi uses RTL layout while keys, paths, IDs, and service/entity names remain LTR;
- light and dark surfaces use the shared Phase 1 tokens;
- controls retain 44px touch targets, visible focus, semantic status text, and labeled inputs;
- structured results replace raw JSON as the primary UI.

## Visual QA

Thirty screenshots were captured under `docs/ui-redesign/completion/phase-10/screenshots/`:

- Reports list and report detail;
- Media library;
- Search index workspace;
- Platform health;
- 1440×1000 desktop light/dark;
- 834×1112 tablet light;
- 390×844 mobile light;
- Farsi RTL desktop light and mobile dark.

Reviewed issues and corrections:

- constrained media filter width and prevented upload-label wrapping;
- opened the first returned asset as contextual detail instead of leaving unused desktop space;
- kept page actions within card/viewport gutters;
- verified dark-theme contrast and RTL logical alignment;
- verified mobile bottom-nav clearance and stacked action sizing.

## Verification

Passed:

```text
bash ./gradlew :report-service:test
bash ./gradlew :report-service:test :media-service:test :search-index-service:test :tenant-service:test :api-gateway:test
npm run lint
npm run build
npx playwright test tests/phase10.e2e.spec.ts
CAPTURE_PHASE_10=1 npx playwright test tests/phase10.e2e.spec.ts --grep "capture Phase 10"
```

Phase 10 E2E result: 2 passed, 1 capture-only skipped in the normal run. Visual capture: 1 passed and generated 30 screenshots.

The complete historical `npm run test:e2e` suite was also run: 25 passed, 7 capture/live tests skipped, and 5 failed. Four failures are stale legacy expectations for screens intentionally migrated in completed earlier phases (`/site-builder`, `/integrations`, and `/iam`); one Phase 6/7 BPM assertion timed out in the full run. The dedicated Phase 6/7 test had passed earlier, and the Phase 8, Phase 9 bot-to-Automation/BPM, and Phase 10 targeted suites pass. These legacy-test migrations belong to the Phase 11 compatibility cleanup and have not been silently changed in Phase 10.

Lint completes with warnings already present across earlier phase files plus hook-dependency/image warnings in the new compact pages. Build and type checking pass.

## Remaining truthful gaps

- media delete remains unavailable until an authoritative cross-service usage/reference registry exists;
- report export remains absent until a backend export contract exists;
- external object storage, variants, or CDN processing beyond the supported local filesystem remain configuration-dependent;
- QA history is session-only because no durable platform health-history owner exists;
- the Search UI shows indexed count and the latest persisted run; the backend history endpoint is available for a later expanded history presentation;
- Phase 11 must update stale compatibility E2E expectations and perform the repository-wide hook/accessibility/performance cleanup.

## Bot, Automation, and BPM regression boundary

Phase 10 did not duplicate or move bot process ownership. The real Phase 9 Telegram/Bale process bindings to Automation and BPM remain in `bot-adapter-service`; the full suite confirms the Telegram automation-binding and Bale BPM-binding journeys pass.

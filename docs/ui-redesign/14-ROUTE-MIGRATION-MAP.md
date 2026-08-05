# Route Migration Map

Phase 0 route compatibility, migration risk, and branch/PR sequence.

## 1. Migration rules

1. Introduce canonical routes before removing old routes.
2. Preserve resource identifiers, query strings, and `returnTo` values.
3. Use temporary redirects during development and permanent redirects only after the canonical route is stable.
4. Do not redirect a user into a route they cannot access; canonical route loaders must resolve authentication, tenant/site, plan, permission, and capability independently.
5. Do not use redirects to hide unavailable backend contracts.
6. Keep old BFF URLs until all callers and tests have migrated; page-route migration and API-route migration are separate concerns.
7. Retain compatibility for at least one merged release cycle, then remove it in Phase 11 after telemetry/test confirmation.

## 2. Current-to-canonical route map

| Current route | Canonical route | Migration behavior | Identifier/query handling | Primary risk |
|---|---|---|---|---|
| `/` | `/dashboard` | redirect after `/dashboard` exists | preserve query/hash client-side where relevant | auth return loops if both routes guard/redirect differently |
| `/projects/new` | `/ai` | redirect | preserve query and draft/session hints | bookmarks and tests target old AI route |
| `/projects` | `/projects` | keep | n/a | page meaning changes from blueprint catalog to full project list |
| `/projects/[projectId]` | same | keep and evolve into project shell | preserve project ID and tab query | tabs depend on unavailable capability bootstrap |
| `/maker` | `/definitions` | redirect default maker entry | preserve selected service/entity as query until canonical detail URL exists | current route also edits BPM; silent data loss if unsaved local state exists |
| `/maker?serviceKey=S&entityKey=E` | `/definitions/S/E` | explicit redirect when identifiers exist | encode both keys | current Maker selection is mostly local state, so old URLs may lack identifiers |
| `/data` | `/data` | keep as entity catalog | preserve query | current route is a fixed-bucket manager, so saved UI state is not canonical |
| n/a | `/data/[serviceKey]/[entityKey]` | new | stable service/entity keys | definition and record scope must match active tenant/site |
| `/automation` | `/automations` | redirect | preserve query | navigation currently labels it Analytics; external links may use old label/path |
| n/a | `/automations/new` | new | optional template query | must not create a flow during load |
| n/a | `/automations/[flowKey]` | new | preserve flow key, optional version/environment | flow keys need URL encoding; active vs draft version ambiguity |
| n/a | `/automations/[flowKey]/executions` | new | flow key and filters | execution permissions differ from edit permissions |
| n/a | `/automations/executions/[executionId]` | new | execution ID | tenant/site isolation must be enforced server-side |
| `/flows` | `/bpm` or `/work` | use explicit compatibility landing, not heuristic silent routing | honor `view=work`/object ID if supplied; otherwise show choice or default `/bpm` | current page mixes definition and runtime; one blind redirect strands half the jobs |
| `/flows?objectId=O` | `/work/O` | explicit redirect | preserve object ID | object may be invisible in new active scope |
| `/flows?flowKey=F` | `/bpm/F` | explicit redirect | preserve flow key | current URLs may not include flow key |
| n/a | `/bpm`, `/bpm/new`, `/bpm/[flowKey]` | new | stable flow key/version | no graph position/lifecycle contract yet |
| n/a | `/work`, `/work/[objectId]` | new | stable object ID | assignment/lock operations are incomplete |
| `/integrations` | `/bots` | redirect | preserve selected integration key as query/detail route | current route also implies generic client apps |
| `/bot` | `/bots` | redirect | optional operations tab | overlaps Integrations and loses old read-only mental model |
| `/bot/[sessionId]` | `/ai?sessionId=...` or project AI tab | redirect only after session-resume UX exists | preserve session ID and linked draft ID | `/bots/[integrationKey]` would be semantically wrong for AI sessions |
| n/a | `/bots/[integrationKey]` | new | integration key and tab | integration keys need encoding; backend session mapping APIs missing |
| `/site-builder` | `/sites/[activeSite]/builder` | resolve active site, then redirect; otherwise `/sites` | never synthesize a site ID | no active-site registry exists, so Phase 1/9 must show unavailable state first |
| n/a | `/sites` | new | tenant scope | site registry missing |
| `/search` | `/search` | keep | n/a | fix label from Media to Search; remove initial-load writes |
| n/a | `/media` | new | n/a | current Media label points to Search; no operator media API/upload bytes |
| `/notifications` | same | keep and expose | n/a | route currently mutates definitions on load |
| n/a | `/reports` | new | report/run IDs | current report paths are not aligned to a typed panel client |
| `/iam` | `/profile` by default | redirect | use explicit `section` mapping where possible | current route mixes profile, access debug, and cosmetic settings |
| `/iam?section=settings` | `/settings` | explicit redirect | preserve recognized section | workspace/site fields are not real settings |
| n/a | `/team/users`, `/team/roles`, `/clients`, `/billing`, `/settings` | new | n/a | backend gaps for invitations, role removal, tenant/client semantics, billing |
| `/commerce` | protected platform/developer tools or future commerce operator routes | no automatic product redirect until classification is approved | preserve only in admin/dev area | current page writes demo data and is unsafe as tenant product UI |
| `/qa` | `/platform/health` or protected dev tool | defer until platform route/access exists | n/a | no platform-admin route guard today |
| `/roadmap` | protected product/developer tool or remove from runtime | defer | n/a | static product planning is mixed with tenant operations |
| `/api-docs` | `/platform/api-docs` | optional redirect after platform admin shell exists | preserve selected service query | API docs may also be useful to builders; audience decision required |
| `/auth` | `/auth` | keep | preserve validated same-origin `returnTo` | open-redirect validation should be explicit |
| n/a | `/onboarding`, `/domains` | new | invitation/domain tokens where applicable | backend contracts missing |

## 3. Navigation migration

### Current fixed navigation problems

- all authenticated users see the same menu;
- Search is labeled Media;
- Automation is labeled Analytics;
- notifications, commerce, QA, and roadmap are inconsistently hidden/linked;
- active-key mapping treats `/bot/[sessionId]` as Integrations;
- mobile navigation exposes only Dashboard, AI Studio, Data, Flows, and Integrations;
- plan and capability states do not affect visibility.

### Target navigation dependency

The target Home/Build/Operate/Manage/Platform registry must wait for the bootstrap contract. Until capabilities and permissions are real, Phase 1 may define the registry and render explicit unavailable states, but must not infer entitlements from route presence or `NEXT_PUBLIC_AVAILABLE_SERVICE_KEYS`.

## 4. BFF compatibility migration

| Current BFF | Decision | Required migration |
|---|---|---|
| `/api/sso/[...path]` | retain | add normalized error/timeout/header behavior later without changing auth endpoints blindly |
| `/api/platform/service/[serviceKey]/[...path]` | retain as transport | extend allowlist only for confirmed services; forward approved correlation/idempotency headers |
| `/api/platform/dynamic/[serviceKey]/[...path]` | retain for dynamic runtime | forbid browser-mediated internal Basic routes; use bearer endpoint or strictly mediated server handler |
| `/api/projects/**` | deprecate | primary AI clients already call AI Orchestrator; remove after no callers/tests and after preserving bearer/scope if temporarily retained |
| `/api/bot-sessions/**` | deprecate | replace with canonical AI session client; do not map missing scope to demo values |

No compatibility API should silently convert upstream authorization or validation failure into `null`, empty data, or 404.

## 5. Redirect implementation risks and controls

### Authentication and `returnTo`

- allow only same-origin route paths beginning with `/`;
- prevent `/auth` self-return loops;
- resolve canonical redirect after login once, then guard the target normally;
- preserve query parameters needed to open a resource or tab.

### Scope changes

- a resource URL does not prove that the active tenant/site owns the resource;
- canonical route loaders must include active scope and handle `403`/not-found distinctly;
- switching scope must invalidate route data; do not silently keep stale records.

### Unsaved editor state

- `/maker`, `/flows`, `/automation`, and `/site-builder` can hold unsaved client state;
- do not force automatic mid-session redirects during the compatibility release;
- redirect on navigation/full reload and add dirty-form protection before old edit routes become thin redirects.

### URL identity

- encode service, entity, flow, integration, project, execution, and object keys;
- do not redirect an edit URL to a generic list if its identifier is available;
- version/environment should be explicit for automation and BPM when ambiguity affects mutations.

### SEO and cache

- authenticated panel redirects should not be cached across users;
- use temporary status during rollout; promote to permanent only after the old route is retired;
- public storefront URLs are outside this panel route migration.

## 6. Proposed branch and PR sequence for Phases 1-11

The sequence assumes Phase 0 documents are accepted. Each branch starts from the merged predecessor. Backend-contract PRs may run ahead, but frontend PRs must consume only merged, documented APIs.

### Phase 1 — Foundation

Branch family: `feat/panel-p1-*`

1. `feat/panel-p1-tokens-fonts` — tokens, approved local fonts, theme/locale bootstrap.
2. `feat/panel-p1-primitives-states` — accessible primitives and truthful state components.
3. `feat/panel-p1-bootstrap-scope-access` — typed bootstrap, active scope, access resolver; depends on GAP-001 through GAP-010/GAP-020.
4. `feat/panel-p1-shell-navigation` — unified shell, capability navigation, mobile sheets.
5. `feat/panel-p1-pwa-shell` — manifest/installability, service worker shell, offline/stale indicator.
6. `test/panel-p1-foundation` — viewport, locale/RTL, theme, access/scope and PWA tests/screenshots.

Do not merge PR 3 with fake selectors if backend scope APIs are absent.

### Phase 2 — Authentication and commercial access

Branch family: `feat/panel-p2-*`

1. `feat/panel-p2-auth-states` — normalized password/captcha/OTP auth.
2. `feat/panel-p2-oauth` — only providers with confirmed contracts.
3. `feat/panel-p2-onboarding-tenancy` — invitations, tenant/site create/select.
4. `feat/panel-p2-plans-limited-access` — plan selection and limited mode.
5. `test/panel-p2-auth-onboarding` — desktop/mobile/RTL/error/access scenarios.

### Phase 3 — Dashboard and notification shell

Branch family: `feat/panel-p3-*`

1. `feat/panel-p3-dashboard-route` — `/dashboard` and `/` compatibility.
2. `feat/panel-p3-dashboard-widgets` — independently sourced capability widgets.
3. `feat/panel-p3-notification-inbox` — bell/drawer/full-page shell; depends on GAP-051.
4. `test/panel-p3-dashboard` — full/limited/partial/mobile screenshots and tests.

### Phase 4 — AI and projects

Branch family: `feat/panel-p4-*`

1. `feat/panel-p4-ai-sessions` — canonical `/ai`, persistent session UI, redirect from `/projects/new`.
2. `feat/panel-p4-project-catalog` — project list/filter and blueprint start.
3. `feat/panel-p4-project-workspace` — capability-filtered tabs.
4. `feat/panel-p4-media-upload` — real byte upload/attachment; depends on GAP-027/GAP-028.
5. `feat/panel-p4-provisioning-releases` — plan/apply, runs, publish/rollback; depends on GAP-021/GAP-024.
6. `test/panel-p4-ai-projects` — persistence, refresh, upload, mobile, screenshots.

### Phase 5 — Definitions and Data

Branch family: `feat/panel-p5-*`

1. `feat/panel-p5-definition-catalog` — `/definitions` and `/maker` compatibility.
2. `feat/panel-p5-definition-editor` — visual schema editor and versions/diff.
3. `feat/panel-p5-generated-record-form` — shared live-definition form renderer.
4. `feat/panel-p5-data-grid` — canonical entity routes and record CRUD.
5. `feat/panel-p5-mobile-editing` — full definition/record mobile parity.
6. `test/panel-p5-definitions-data` — strict nested validation and responsive coverage.

### Phase 6 — Automation

Branch family: `feat/panel-p6-*`

1. `feat/panel-p6-automation-routes` — catalog/editor/execution routes and old redirect.
2. `feat/panel-p6-automation-metadata-canvas` — XYFlow and metadata registry/endpoint.
3. `feat/panel-p6-automation-inspector` — schema-driven node/edge/workflow settings.
4. `feat/panel-p6-automation-lifecycle` — validation/diff/activation/schedules/credentials.
5. `feat/panel-p6-automation-executions` — runs, node attempts, retry/cancel/logs.
6. `feat/panel-p6-n8n` — analyze/import/export.
7. `test/panel-p6-automation` — acceptance scenarios, mobile and screenshots.

### Phase 7 — BPM and work

Branch family: `feat/panel-p7-*`

1. `feat/panel-p7-bpm-routes-catalog` — `/bpm`, `/work`, and `/flows` compatibility.
2. `feat/panel-p7-bpm-designer` — XYFlow states/transitions/layout.
3. `feat/panel-p7-bpm-inspectors` — bindings/processors/actions/conditions/automation bridge.
4. `feat/panel-p7-work-queue` — scoped queues and filters.
5. `feat/panel-p7-work-item` — active form, transition, comments, attachments, history.
6. `test/panel-p7-bpm-work` — desktop/mobile workflow scenarios and screenshots.

### Phase 8 — Team, profile, billing, settings

Branch family: `feat/panel-p8-*`

1. `feat/panel-p8-team-users`.
2. `feat/panel-p8-roles-permissions`.
3. `feat/panel-p8-clients`.
4. `feat/panel-p8-profile-security`.
5. `feat/panel-p8-billing`.
6. `feat/panel-p8-settings-iam-redirect`.
7. `test/panel-p8-access-management`.

Each PR is gated by its corresponding GAP-011 through GAP-019 contract.

### Phase 9 — Bots, sites, domains, notifications

Branch family: `feat/panel-p9-*`

1. `feat/panel-p9-bots-catalog-config`.
2. `feat/panel-p9-bot-sessions-deliveries`.
3. `feat/panel-p9-sites-catalog-builder`.
4. `feat/panel-p9-site-publish-preview`.
5. `feat/panel-p9-domains-hosting`.
6. `feat/panel-p9-notification-operations`.
7. `chore/panel-p9-commerce-classification` — protect/move developer seeding; implement only supported operator paths.
8. `test/panel-p9-channels-publishing`.

### Phase 10 — Reports, media, search, operations

Branch family: `feat/panel-p10-*`

1. `feat/panel-p10-reports`.
2. `feat/panel-p10-media`.
3. `feat/panel-p10-search`.
4. `refactor/panel-p10-admin-tools` — classify/protect QA, Roadmap, API Docs.
5. `feat/panel-p10-settings-completion`.
6. `test/panel-p10-operational-tools`.

### Phase 11 — Hardening

Branch family: `chore/panel-p11-*`

1. `refactor/panel-p11-remove-compatibility` — only after route/API usage audit.
2. `fix/panel-p11-access-scope-mock-audit`.
3. `fix/panel-p11-a11y-i18n-theme`.
4. `perf/panel-p11-builders-grids`.
5. `fix/panel-p11-pwa-security-reliability`.
6. `test/panel-p11-system-regression`.
7. `docs/panel-p11-final-status` — final status, gap register, final route/capability matrix, screenshot manifest.

## 7. Merge gates per PR

Every PR must include only its route/foundation concern plus clients/types/localization/tests/docs. Before merge:

- no runtime fixtures, fake successes, fabricated previews, or hardcoded active scope;
- visible controls work or explain the exact unavailable contract;
- backend authorization and same-origin BFF behavior are preserved;
- lint/build/relevant Playwright tests pass;
- required desktop/mobile/theme/locale screenshots are visually reviewed;
- redirects preserve identifiers and do not conceal access or scope failures.

Phase 0 stops here. No redirect or runtime route change has been implemented.

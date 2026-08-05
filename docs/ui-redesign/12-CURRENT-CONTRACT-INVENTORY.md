# Current Contract Inventory

Phase 0 repository and backend contract audit for `panel-web`.

- Audit date: 2026-08-05
- Runtime application code changed: no
- Mock or fixture data added: no
- Sources: current `panel-web` source, backend controllers and DTOs, architecture guides, checked-in OpenAPI snapshots, and the UI redesign package
- OpenAPI caveat: `docs/swagger/**` is a legacy offline snapshot. The controller-derived `/v3/api-docs` documents and `api-docs-service` are authoritative when services are running.

## 1. Executive contract status

The current panel can use real APIs for password/captcha authentication, token refresh, IAM realms/clients/roles/memberships, AI blueprints/drafts/sessions/provisioning runs, dynamic definitions and records, BPM definitions/runtime/forms/comments/attachments, automation definitions/executions/import/export, bot integrations/deliveries/mini-apps, storefront rendering, notification dispatch, search query/sync, commerce payment methods, and API documentation discovery.

The Phase 1 access and scope foundation cannot yet be truthful from existing APIs. There is no panel bootstrap, tenant/workspace registry, site registry, active-scope persistence, plan/billing contract, capability registry, or Google/GitHub/LinkedIn OAuth contract. IAM `realm` and `client` records are identity boundaries and OAuth clients; they are not documented as tenant or site resources and must not be relabeled as such.

## 2. Current route-to-component map

All routes are Next.js App Router pages. There is no route middleware.

| Route | Page component | Shell | Current responsibility | Navigation status |
|---|---|---|---|---|
| `/auth` | `panel-web/app/auth/page.tsx` | none | Password/captcha sign-in and public registration | public |
| `/` | `panel-web/app/page.tsx` | `PanelShell` | dashboard | visible as Dashboard |
| `/projects/new` | `panel-web/app/projects/new/page.tsx` | `PanelShell` | one-shot AI Studio | visible |
| `/projects` | `panel-web/app/projects/page.tsx` | `PanelShell` | blueprints and saved drafts | visible |
| `/projects/[projectId]` | `panel-web/app/projects/[projectId]/page.tsx` | `AppShell` -> `PanelShell` | draft detail, sessions, provisioning | reached from Projects |
| `/maker` | `panel-web/app/maker/page.tsx` | `PanelShell` | dynamic definitions plus BPM editing | visible |
| `/data` | `panel-web/app/data/page.tsx` | `PanelShell` | four fixed dynamic-entity buckets | visible |
| `/flows` | `panel-web/app/flows/page.tsx` | `PanelShell` | BPM catalog, static canvas, cartable | visible |
| `/integrations` | `panel-web/app/integrations/page.tsx` | `PanelShell` | bot integration, outbound test, mini-app build | visible |
| `/bot` | `panel-web/app/bot/page.tsx` | `PanelShell` | read-only bot delivery summary | visible |
| `/bot/[sessionId]` | `panel-web/app/bot/[sessionId]/page.tsx` | `AppShell` -> `PanelShell` | read-only AI conversation session | linked |
| `/site-builder` | `panel-web/app/site-builder/page.tsx` | `PanelShell` | storefront `site-route` record editor | visible |
| `/search` | `panel-web/app/search/page.tsx` | `AppShell` -> `PanelShell` | search definition/sync/query tool | mislabeled Media |
| `/automation` | `panel-web/app/automation/page.tsx` | `AppShell` -> `PanelShell` | raw automation and batch JSON tool | mislabeled Analytics |
| `/iam` | `panel-web/app/iam/page.tsx` | `PanelShell` | profile/access JSON and cosmetic scope labels | visible as Settings |
| `/notifications` | `panel-web/app/notifications/page.tsx` | `AppShell` -> `PanelShell` | template/test dispatch tool | hidden |
| `/commerce` | `panel-web/app/commerce/page.tsx` | `AppShell` -> `PanelShell` | developer seeding/payment tool | hidden |
| `/qa` | `panel-web/app/qa/page.tsx` | `AppShell` -> `PanelShell` | live read-only smoke harness | hidden |
| `/roadmap` | `panel-web/app/roadmap/page.tsx` | `AppShell` -> `PanelShell` | static roadmap plus live counts | linked from dashboard |
| `/api-docs` | `panel-web/app/api-docs/page.tsx` | `PanelShell` | live API catalog/browser | visible |

`AppShell` is a compatibility wrapper that adds a generic `panel-card` and maps old routes to `PanelShell` navigation keys. Authenticated pages therefore have two presentation paths even though both eventually use `PanelShell`.

## 3. Current route-to-API map

All browser service calls below normally pass through a same-origin BFF unless noted.

| Route | Load calls | Mutation calls | Scope behavior |
|---|---|---|---|
| `/auth` | `POST /api/sso/captcha/challenges?clientId=cyan-panel` | `POST /api/sso/users/register`, `POST /api/sso/auth/login` | no tenant/site |
| `/` | AI drafts, bot integrations/messages, BPM flows | none | fixed `tenant-demo` / `site-commerce` |
| `/projects/new` | AI blueprints and drafts | `POST /endpoint/ai-orchestrator/generate/app`; `POST /internal/media/assets/prepare-upload` | fixed scope; media call does not upload bytes |
| `/projects` | AI blueprints and drafts | create draft; automatically provision eligible created draft | created draft uses fixed scope |
| `/projects/[projectId]` | draft, linked sessions, provisioning runs | provision draft | draft ID only; backend owns returned scope |
| `/maker` | dynamic templates/definitions; BPM flows/action/condition metadata | create/update definition; generate AI draft; save BPM flow | fixed scope |
| `/data` | records for four fixed entity buckets | ensure definition, create fixed record, patch first record | fixed scope |
| `/flows` | BPM flows, metadata, assigned/visible objects, transition options | save/activate flow, create managed object, generate AI draft | fixed scope |
| `/integrations` | bot integrations/messages/mini-apps | fixed integration upsert, fixed test send, webhook registration, mini-app upsert/publish | fixed scope in query/body |
| `/bot` | bot integrations/messages/mini-apps | none | fixed scope |
| `/bot/[sessionId]` | AI session detail | none | session ID only |
| `/site-builder` | storefront dynamic route records plus public resolve/render | ensure `site-route` definition and submit a mostly generated route record | fixed scope |
| `/search` | dynamic index-definition records | creates definitions on load; save fixed index schema; start sync; search/suggest | editable fields default to fixed scope; sync omits scope headers in current client |
| `/automation` | none initially | execution start/get/cancel; batch save/run/list; automation flow save and three lifecycle transitions | editable fields default to fixed scope; execution start embeds scope in body rather than headers |
| `/iam` | current user and IAM effective access | logout; local-only workspace/site label save | username comes from local storage |
| `/notifications` | dynamic template records | creates definitions on load; save template; send notification; get message | editable fields default to fixed scope; dispatch does not send scope headers |
| `/commerce` | dynamic records and payment methods | creates four definitions on load; seeds fixed commerce records/method; initiates fixed payment | editable fields default to fixed scope; payment calls omit scope headers |
| `/qa` | AI drafts, storefront resolve/render, BPM flows/metadata/objects, bot integrations/messages | none | editable fields default to fixed scope |
| `/roadmap` | AI drafts, BPM flows, storefront/catalog definitions, bot integrations/mini-apps | none | fixed scope |
| `/api-docs` | API docs service list and selected OpenAPI document | refresh selected document | no tenant/site |

The page imports and typed clients are the source of truth for this table. The older README occasionally names gateway-direct URLs, while the current browser clients use `/api/platform/**` BFF paths.

## 4. Current BFF proxy map

### SSO proxy

`/api/sso/[...path]` maps the first segment to a service and then calls `/api/sso/{path}`.

| Segment | Backend default |
|---|---|
| `auth` | `http://localhost:9001` |
| `users` | `http://localhost:9002` |
| `captcha` | `http://localhost:9003` |
| `otp` | `http://localhost:9004` |
| `sessions` | `http://localhost:9005` |
| `fido` | `http://localhost:9006` |

It supports GET/POST/PUT/PATCH/DELETE, forwards `Authorization` only, forwards request/response text and content type, and disables caching.

### Generic service proxy

`/api/platform/service/[serviceKey]/[...path]` supports:

`sso-user-service`, `bot-adapter-service`, `ai-orchestrator-service`, `bpm-service`, `storefront-service`, `notification-service`, `search-index-service`, `automation-orchestrator-service`, `batch-worker-service`, `payment-service`, `payment-orchestrator-service`, and `api-docs-service`.

It supports GET/POST/PUT/PATCH/DELETE and forwards `Authorization`, `X-Tenant-Key`, `X-Site-Key`, content type, status, and response content type. It does not forward correlation, idempotency, locale, conditional request, or arbitrary response headers.

### Dynamic service proxy

`/api/platform/dynamic/[serviceKey]/[...path]` supports:

`content-service`, `catalog-service`, `crm-service`, `commerce-service`, `finance-service`, `inventory-service`, `report-service`, `storefront-service`, `media-service`, `cart-service`, `checkout-service`, `bpm-service`, `payment-service`, `pricing-promotion-service`, `notification-service`, and `search-index-service`.

Its forwarding behavior matches the generic proxy. Despite the name, it can proxy arbitrary paths on an allowed service. This is how the panel currently attempts an internal media call with a browser bearer token; `/internal/**` requires Basic authentication and the call is therefore not a valid production contract.

### Compatibility routes

| BFF route | Adapter | Important limitation |
|---|---|---|
| `/api/projects`, `/api/projects/[projectId]` | maps old `ProjectDraft` to AI drafts | server-side direct AI calls do not forward the browser bearer token or scope headers; errors are collapsed in parts of the registry |
| `/api/bot-sessions/**` | maps old bot session shapes to AI sessions | server-side direct AI calls do not forward bearer credentials; missing scope falls back to hardcoded demo values; several errors become `null`/404 |

Current primary pages generally bypass these compatibility adapters and call typed AI clients through the generic BFF.

## 5. Authentication and refresh behavior

The browser stores access token, refresh token, computed expiry, session ID, and username in local storage. `PanelShell` considers any non-empty access token authenticated. There is no server middleware, bootstrap validation, role-aware route loader, or capability-aware navigation.

`platformFetch`:

1. reads the access token;
2. refreshes when within 60 seconds of the stored expiry;
3. attaches `Authorization: Bearer ...`;
4. on `401`, refreshes once and retries once;
5. on failure, clears local auth state and redirects to `/auth?returnTo=...`.

Concurrent proactive refreshes share one promise. Login uses `clientId=cyan-panel`, password, captcha, optional OTP, and `deviceId=panel-web`. The login DTO supports OTP, but the UI has no OTP challenge flow. Logout revokes the stored session ID and clears storage in `finally`.

Risks:

- route access is based on local token presence, not a validated session;
- token and refresh token are exposed to browser JavaScript;
- API errors are commonly returned as raw response text;
- the optional AI WebSocket puts the access token in the URL query string;
- auth state contains no tenant, plan, capabilities, or normalized permissions;
- the current user lookup trusts a username copied into local storage;
- BFF compatibility registries omit bearer forwarding.

## 6. Tenant and site handling

The dynamic, BPM, storefront, generic service, and BFF layers can carry `X-Tenant-Key` and `X-Site-Key`. AI and bot list APIs also accept query/body scope. The missing piece is authoritative scope discovery and selection.

`PanelProvider` persists only display strings under `cyan.panel.workspace` and `cyan.panel.site`. It defaults them to `tenant-demo` and `site-commerce`. Header switchers are non-interactive `div` elements. `/iam` edits those strings but does not update operational API scope.

### Runtime hardcoded scope occurrences

| Location | Occurrence |
|---|---|
| `components/panel-provider.tsx` | default workspace and site labels |
| `app/page.tsx` | dashboard load scope |
| `app/projects/new/page.tsx` | `TENANT_KEY` and `SITE_KEY` constants used for load/generate/preview |
| `app/projects/page.tsx` | draft creation scope |
| `app/maker/page.tsx` | module-level scope |
| `app/data/page.tsx` | all record loads and mutations |
| `app/flows/page.tsx` | module-level BPM/AI scope |
| `app/integrations/page.tsx` | module-level bot scope |
| `app/bot/page.tsx` | load scope |
| `app/site-builder/page.tsx` | module-level storefront scope |
| `app/roadmap/page.tsx` | all live-count calls |
| `app/automation/page.tsx` | initial editable field values |
| `app/notifications/page.tsx` | initial editable field values |
| `app/search/page.tsx` | initial editable field values |
| `app/commerce/page.tsx` | initial editable field values |
| `app/qa/page.tsx` | initial editable field values |
| `app/iam/page.tsx` | fallback values when saving cosmetic labels |
| `app/bot/[sessionId]/page.tsx` | display fallbacks |
| `lib/bot-session-registry.ts` | compatibility mapping fallbacks |
| `lib/draft-store.ts` | unused seeded local drafts with demo scope |

Tests intentionally use demo scope and are not production occurrences, but they tightly encode the current behavior.

There are no tenant list/create/select, tenant invitation, site list/create/select, tenant-site membership, or persisted active-scope APIs in the repository. Existing IAM realms/clients cannot be assumed equivalent.

## 7. Roles, permissions, plans, features, and capabilities

### Implemented IAM contracts

The backend provides:

- realms: list/upsert;
- OAuth/OIDC clients: list/upsert;
- realm roles and permissions: list/upsert;
- client roles and permissions: list/upsert;
- user realm memberships: list/upsert;
- realm/client role assignment;
- managed user provisioning;
- effective access: `IamUserAccessSummary` with realm roles/permissions and per-client roles/permissions.

Seeded permission vocabulary uses colon-style capabilities such as `builder:*`, `builder:use`, `operations:*`, `commerce:*`, and `panel:*`. Backend services use `@PreAuthorize` with this vocabulary. It does not match the dot-style UI redesign permissions (`project.read`, `automation.execute`, and so on), so a mapping/versioning decision is required.

The panel already defines TypeScript shapes and client methods for IAM, but `/iam` uses only current user and effective-access reads. It renders access mainly as raw JSON.

### Missing access/commercial contracts

No DTO or API was found for:

- effective service capabilities by tenant/site;
- capability source, health, degradation, or limits;
- plan catalog, active plan, plan features, usage, or limits;
- subscription lifecycle, invoices, billing payment methods, or plan changes;
- feature flags;
- a combined panel bootstrap.

`NEXT_PUBLIC_AVAILABLE_SERVICE_KEYS` is an environment-provided AI prompt inventory, not a user entitlement or health contract. The shell's Pro card is static. `payment-service` manages commerce gateway methods and transactions; it is not panel subscription billing.

## 8. OAuth status

| Provider | UI | Backend start/callback | State/PKCE/account link | Status |
|---|---|---|---|---|
| Google | visible button | none found | none found | placeholder only |
| GitHub | visible button | none found | none found | placeholder only |
| LinkedIn | not implemented in current auth UI | none found | none found | unavailable |

The SSO stack exposes password/captcha, OTP, refresh, introspection, FIDO boundaries, JWKS, OpenID discovery, and realm token/introspection/logout endpoints. These make Cyan an OIDC-style issuer; they do not implement social-provider login.

## 9. Responsive, locale, RTL, and theme implementation

- Locale: `en`/`fa` in `PanelProvider`, persisted in local storage after hydration.
- Direction: document `lang` and `dir` are changed client-side; root HTML starts as English/LTR.
- Fonts: Vazir 300/400/500/700 is self-hosted. Roboto is named in CSS but no Roboto asset is present, so the browser falls back unless installed locally.
- Theme: light/dark only, persisted after hydration; there is no `system` mode or pre-hydration theme script, so theme/locale flash is possible.
- CSS: one 2,790-line global stylesheet with many route-specific selectors and literal colors.
- Breakpoints: 1360, 1100, 980 for API docs, and 820 pixels. The target 1024/834/390/360 behavior is not explicitly validated.
- Mobile: below 820 pixels, the sidebar and page intro disappear and a sticky five-link bottom navigation appears. Several pages swap separately authored `.desktop-only` and `.mobile-only` trees; mobile frequently has reduced capability.
- RTL: some logical properties and `text-align:start` exist, but layout mirroring is not systematic. Directional glyphs and many graph/layout assumptions remain.
- Accessibility: input focus exists, but there is no general `:focus-visible` system, reduced-motion rule, dialog focus management, or documented graph keyboard alternative. At least one nested-button pattern remains in Blueprints.

## 10. PWA status

Present:

- manifest link from root metadata;
- standalone display, start URL, scope, theme/background color;
- Apple web-app metadata;
- one SVG icon marked `any maskable`;
- viewport metadata.

Missing:

- service worker or Workbox integration;
- offline shell/data behavior;
- install/update prompt;
- online/offline indicator;
- stale-data labeling;
- safe-area CSS;
- multiple raster icon sizes/screenshots expected by stricter installability checks;
- mutation queue policy enforcement in code.

The current app is PWA-described, not an operational offline-capable PWA.

## 11. Shared and duplicated components

### Existing shared components

- `PanelProvider`: locale/theme/cosmetic workspace state;
- `PanelShell`: auth guard, desktop sidebar/header, account menu, mobile nav;
- `AppShell`: old wrapper around `PanelShell`;
- `ProjectProvisioningPanel`;
- `auth-icons`;
- small `ProjectCards` and `WorkspaceControls` components.

There is no shared Button, Field, Tabs, Dialog, Drawer, BottomSheet, Toast, ConfirmDialog, Skeleton, Empty/Error/Permission state, DataGrid, RecordForm, CodeViewer, or access/scope resolver.

### Duplication and dead compatibility code

- desktop/mobile business markup is duplicated in Auth, AI Studio, Projects, Maker, Data, Flows, Integrations, Site Builder, and other major pages;
- route-level toolbar, status pill, JSON panel, loading text, and error handling patterns are repeated;
- `PanelShell` and `AppShell` coexist;
- `WorkspaceControls` and `ProjectCards` are not used by pages;
- `bot-session-api`, `project-api`, `draft-store`, and `workspace-roadmap` are unused compatibility/local-data modules;
- `bot-session-registry` and `project-registry` remain behind BFF compatibility routes but are not primary UI paths.

## 12. Visible placeholder controls

| Area | Placeholder or misleading control |
|---|---|
| Shell | workspace/site switcher-looking containers, notification bell, Manage plan |
| Auth | Google, GitHub, magic link, Terms, Privacy, Docs, Changelog, Status; registration workspace is local-only |
| Dashboard | latest-draft overflow button; some capability cards are static catalog descriptions rather than entitlement-aware modules |
| Projects | search, category/tag/complexity filters, Preview, saved rows, mobile Generate action |
| AI Studio | prompt enhancement is local text manipulation; preview URL is fabricated; attachment progress is metadata preparation, not byte upload |
| Maker | search; raw JSON as primary editor; generic state-action params; back/overflow glyph buttons; mobile edit parity missing |
| Data | search, filters, import, export, preview, row selection, insight cards; first row is always active |
| Flows | static non-interactive canvas; transition options cannot execute; active forms/comments/attachments absent from page; some mobile glyph controls have no action |
| Integrations | Add opens no form and writes fixed data; chips that look like tabs are not tabs |
| Bot | summary is read-only and duplicates Integrations; session resume link does not resume |
| Site Builder | Add Page injects defaults; Content/SEO/Rendering chips are not editing tabs; preview is stylized rather than isolated returned HTML |
| Search | result/suggestion JSON is raw; displayed sync-start status has no progress contract |
| Automation | raw JSON, fixed schedule graph, manual refresh only; no graph editor/metadata form/confirmation |
| IAM | workspace/site fields are cosmetic; access shown as raw JSON |
| Notifications | free-text channel/provider, no render preview/provider health/history/retry |
| Commerce | developer seeding actions presented as product UI |

## 13. Hardcoded/demo backend mutation payloads

| Location | Mutation and fixed data |
|---|---|
| `app/data/page.tsx` | creates template definitions, then fixed Product/Landing/CRM/Inventory records; patches the first record with fixed transformations |
| `app/flows/page.tsx` | Save with no flow creates `purchase_order_approval`; Start object sends only `source` and timestamp |
| `app/maker/page.tsx` | create-from-template reuses template key as entity key; added actions get generic `{source,label}` params |
| `app/integrations/page.tsx` | creates `telegram-main`, `@cyan_assistant_bot`, Vault ref, preview URL; sends fixed order text to `@john_doe`; registers webhook opportunistically; fixed mini-app routes |
| `app/site-builder/page.tsx` | writes generated entity reference, navigation, SEO, theme/template, cache, indexing, and lifecycle values rather than editing them |
| `app/search/page.tsx` | writes fixed engine, analyzers, searchable/filter/sort/suggest fields and status |
| `app/notifications/page.tsx` | defaults a real dispatch recipient to an external example webhook and fixed welcome template/model |
| `app/commerce/page.tsx` | seeds fixed cart, guest, Tehran addresses, promotion, VAT, sandbox payment method, amount, currency, and external callback URLs |
| `app/automation/page.tsx` | fixed execution defaults and inline result; example source/destination ETL spec; fixed three-node schedule graph followed by automatic submit/approve/activate |
| `app/projects/page.tsx` | fixed tenant/site/client on draft creation and automatic provisioning attempt |
| `app/projects/new/page.tsx` | fixed tenant/site and fabricated preview URL; metadata-only media record creation |
| `lib/draft-store.ts` | unused production-adjacent local seed drafts |

## 14. Backend mutation during initial page load

| Route | Initial-load mutation |
|---|---|
| `/search` | attempts to create `index-definition` and `search-document` definitions |
| `/notifications` | attempts to create `notification-template` and `notification-message` definitions |
| `/commerce` | attempts to create shopping-cart, checkout-session, promotion-rule, and tax-rule definitions |

Each call swallows creation errors, then continues with reads. All other audited route effects are read-only; mutations on Projects, Data, Maker, Flows, Integrations, Site Builder, Automation, and API Docs require a user action.

## 15. OpenAPI and schema inventory

Implemented platform support:

- each Spring service exposes `/v3/api-docs`, `/v3/api-docs.yaml`, and Swagger UI with configurable docs authentication;
- `api-docs-service` exposes service list/detail/aggregate endpoint and internal variants;
- `/api-docs` consumes the endpoint catalog and permits per-service JSON download;
- every dynamic definition can generate strict tenant/site-specific OpenAPI at `/endpoint/entities/definitions/{entityKey}/openapi` and `/internal/...`;
- checked-in static snapshots cover the main services under `docs/swagger/services`;
- `docs/swagger/cyan-business-platform.openapi.json` is an aggregate offline artifact.

Limitations:

- static snapshots are explicitly non-authoritative and already omit current endpoints such as IAM, endpoint search routes, and aliases;
- no generated TypeScript client is used by `panel-web`;
- the API catalog proxy is allowlisted and must be updated for any new service;
- there is no checked-in environment example for panel variables;
- no contract test verifies that panel TypeScript types match live OpenAPI schemas.

## 16. Existing APIs usable by later phases

| Area | Usable now | Important boundary |
|---|---|---|
| Auth | captcha, register, password login, refresh, logout, OTP/FIDO service boundaries | no social OAuth; UI lacks OTP state machine |
| IAM | realm/client catalogs, roles/permissions, memberships, assignments, effective access, managed users | not tenant/site/plan/capability management; removal/invitation gaps |
| AI | blueprints, drafts, sessions/messages, provisioning runs, one-shot generation | release, close, resolve, rich plan/apply gaps |
| Dynamic entities | templates, definitions, strict schemas/OpenAPI, CRUD/validate | frontend lacks generated editor/grid; search/filter contracts are limited |
| Automation | definitions/lifecycle, execution history/details/steps/dead letters/retry, credentials, n8n analyze/import/export, public basic metadata | metadata is too shallow for target inspector; realtime/diagnostic gaps |
| BPM | flows, activate, metadata, managed objects, queues, transitions, active forms, comments, attachments | no full lifecycle/version UX, assignment/lock mutations, graph-position contract |
| Processor | CRUD/run under `/api/processor-service/processors` | generic BFF does not allow processor-service |
| Notification | send sync/async and get one message; templates/messages through dynamic runtime | no inbox/history/provider/render/retry APIs |
| Report | static report CRUD/run and dynamic definitions | path/auth/BFF alignment and run history/export are incomplete |
| Media | dynamic metadata and public retrieval/variants | internal prepare call fabricates CDN metadata and uploads no bytes |
| Search | endpoint/public search/suggest and immediate source sync | no persisted sync run/progress/history/counts |
| Bot Adapter | integration upsert/list, webhook registration, outbound list/send/retry, mini-app list/upsert/publish | no session mapping/inbound operations/provider health contract |
| Storefront | dynamic route/theme data; public resolve/render/page/sitemap/robots | no site registry, block editor, domains, hosting, certificates, or release contract |
| Payment | commerce payment methods/transactions and payment orchestration | not subscription billing |

## 17. Test inventory

- `auth.e2e.spec.ts`: redirect/login, register-then-login, proactive refresh.
- `panel-wiring.e2e.spec.ts`: project/session reads, AI generate follow-up answer, empty Data/Flows, Site publish, Integration mini-app publish, IAM/logout, API docs.
- Playwright uses one Chromium desktop project. No tablet/mobile, Farsi/RTL, dark mode, permission, plan, capability, offline/PWA, accessibility, or visual regression suite exists.
- There are no panel unit tests.

## 18. Phase 0 exit gate

- Features implementable from current APIs are identified above.
- Missing contracts are enumerated in `13-BACKEND-GAPS.md` with proposed DTOs and owners.
- Hardcoded scope and demo mutations are enumerated in this document.
- Route redirects and migration risks are in `14-ROUTE-MIGRATION-MAP.md`.

The repository is audited, but Phase 1 should not claim a real scope/access foundation until the P0 blockers in the backend gap register are resolved or explicitly accepted as unavailable states.

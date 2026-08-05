# Phase 1 Completion Report

## Outcome

Phase 1 is implemented without starting a Phase 2 page redesign. The authenticated shell now uses persisted backend tenant/site scope, self-derived identity/access, billing entitlements, service capability state, responsive navigation, locale direction, theme preferences, shared UI primitives, and a static-only PWA cache.

The existing feature-page and dashboard business content was deliberately left in place. Runtime code contains no new mock records, fake tenant/site values, fake access decisions, or simulated API success.

## Changed Areas

### Backend ownership and contracts

- Added `tenant-service` on unique port `9129`:
  - PostgreSQL/H2 persistence and Flyway schema for tenants, memberships, capability overrides, feature flags, and idempotency records.
  - bearer endpoint API and Basic-auth internal API.
  - tenant membership enforcement and tenant-owner creation semantics.
  - effective capability resolution from billing entitlements, tenant/site overrides, and service discovery, including `AVAILABLE`, `DEGRADED`, and `UNAVAILABLE`.
- Added `billing-service` on unique port `9130`:
  - plan, subscription, and idempotency persistence.
  - tenant membership enforcement.
  - truthful `NONE` / `NOT_CONFIGURED` state when no subscription or external billing provider exists.
  - FREE-plan activation support; external billing never reports fake success.
- Extended `storefront-service` with the service-owned site registry, tenant membership validation, Flyway migration, endpoint API, and Basic-auth internal ownership check.
- Extended `sso-session-service` with persisted active tenant/site scope, session ownership checks, tenant/site boundary validation, JWT protection, and migration.
- Extended `sso-user-service` with JWT-subject-derived `/me` identity and access endpoints.
- Added gateway routes for tenant, billing, and site registry endpoints.
- Updated the local platform runbook and panel service allowlist.

### Panel foundation

- Added composed BFF endpoints for bootstrap and session scope.
- Added typed bootstrap/scope/access contracts and normalized API error categories.
- Added active-scope request propagation and keyed query-boundary invalidation after scope changes.
- Replaced cosmetic local tenant/site shell state with API-backed selectors.
- Added capability- and permission-filtered desktop and mobile navigation.
- Added distinct auth-required, permission-denied, plan-locked, capability-disabled, and service-unavailable access resolution.
- Added light, dark, and system theme preference with pre-hydration application and reload persistence.
- Added English/Farsi locale persistence, document `lang`/`dir`, RTL mirroring, and LTR handling for identifiers/code.
- Added desktop sidebar, collapsed tablet sidebar, and five-destination mobile navigation with focus-trapped Build/More sheets.
- Added the Phase 1 primitive foundation: buttons, fields, selects/comboboxes, tabs, badges, cards, dialogs/drawers/sheets, toast, confirmation, skeleton, empty/error/permission states, plan gate, offline/stale indicator, page header, responsive inspector, code viewer, and DataGrid shell.
- Added visible focus, reduced-motion handling, safe-area variables, semantic labels, and 44px mobile targets.
- Added an installable manifest, offline banner, service-worker registration, and static-shell-only caching. API reads and mutations are never cached or queued.

## API Endpoints Used

Panel/BFF:

- `GET /api/panel/bootstrap`
- `GET /api/panel/scope`
- `PUT /api/panel/scope`

Identity/session:

- `GET /api/sso/users/me`
- `GET /api/sso/iam/me/access?clientId=cyan-panel`
- `GET /api/sso/sessions/{sessionId}/scope`
- `PUT /api/sso/sessions/{sessionId}/scope`

Tenant/site/billing:

- `GET|POST /endpoint/tenants`
- `GET /endpoint/tenants/{tenantKey}`
- `GET /endpoint/tenants/{tenantKey}/capabilities`
- `GET /endpoint/tenants/{tenantKey}/feature-flags`
- `GET|POST /endpoint/sites` with `X-Tenant-Key`
- `GET /endpoint/billing/plans`
- `GET /endpoint/billing/tenants/{tenantKey}/subscription`
- `POST /endpoint/billing/tenants/{tenantKey}/subscription/change`
- Basic-auth internal membership, site ownership, and billing entitlement APIs.

## Removed Phase 1 Placeholders

- Removed `tenant-demo` and `site-commerce` from shared scope infrastructure.
- Removed localStorage-backed cosmetic workspace/site selection from the shell.
- Removed the hardcoded Pro-plan card and replaced it with the backend subscription state.
- Replaced the interactive notification placeholder with an explicitly disabled unavailable control; Phase 3 owns the real inbox.
- Capability-disabled, permission-denied, plan-locked, and unavailable navigation entries no longer imply access.

Existing hardcoded scope values in feature routes not migrated by Phase 1 were not expanded into a feature-page redesign. Shared request infrastructure overrides their outgoing scope with the authenticated active scope, and the Phase 1 query boundary remounts consumers when scope changes. Those route-local literals remain scheduled for their owning feature phases.

## Access and Operational Behavior

- The backend remains authoritative. The panel never grants access based on UI state.
- Bootstrap derives identity from the JWT subject instead of a username supplied from localStorage.
- Scope mutation verifies session ownership, active session state, tenant membership, and site-to-tenant ownership.
- No tenant produces an empty selector and limited shell state; it does not invent a default tenant.
- No subscription produces `NONE` with provider state `NOT_CONFIGURED`.
- Missing service registration produces unavailable/degraded capability state and disables relevant navigation.
- Bootstrap dependency failures are shown as retryable operational errors; partial site, billing, or capability failures retain truthful per-service status.

## Responsive, RTL, and Theme Comparison

The supplied references were used as the visual source of truth. The implementation matches their restrained density, 248px desktop navigation rail, compact bordered scope controls, quiet surfaces, grouped hierarchy, deep-navy dark shell, fixed five-item mobile navigation, and logical RTL mirroring.

Intentional differences from the references:

- Reference dashboard metrics and populated cards were not copied because Phase 1 forbids mock data and Dashboard redesign belongs to Phase 3.
- The plan area shows the real `NONE` / `NOT_CONFIGURED` result used during capture instead of a fabricated paid plan.
- The notification control is disabled without a badge because a Phase 1 notification-inbox contract does not exist.
- Roboto font files are not present in the repository. English uses the documented Roboto-first system fallback; existing local Vazir files are used for Farsi.

## Tests and Results

- `npm run lint` — passed.
- `npm run build` — passed; all 25 routes generated/compiled.
- `npm run test:e2e -- tests/phase1-shell.e2e.spec.ts` — passed.
  - 1440, 1024, 834, 390, and 360 widths.
  - persisted Farsi RTL and dark/system preferences.
  - five-item mobile navigation and accessible Build sheet.
  - scope persistence and query invalidation.
  - distinct permission, plan, and service-unavailable decisions.
- `bash ./gradlew :tenant-service:test :billing-service:test :storefront-service:test :sso-session-service:test :sso-user-service:test :api-gateway:test` — passed.
  - tenant ownership/idempotency unit coverage.
  - truthful no-subscription billing coverage.
  - tenant-scoped site persistence coverage.
  - session-owner scope validation/persistence coverage.
  - `sso-user-service` currently has no pre-existing test sources.

## Screenshots

- `screenshots/desktop-en-light.png` — 1440×1000
- `screenshots/desktop-en-dark.png` — 1440×1000
- `screenshots/tablet-en-light.png` — 834×1112
- `screenshots/mobile-en-light.png` — 390×844
- `screenshots/mobile-en-dark.png` — 390×844
- `screenshots/desktop-fa-rtl-light.png` — 1440×1000
- `screenshots/mobile-fa-rtl-light.png` — 390×844

The screenshot harness uses network interception only as a Playwright test fixture. It is not included in runtime data paths or production state.

## Remaining External or Later-Phase Gaps

- External subscription billing has no configured provider credentials; code exposes `NOT_CONFIGURED`. Provider checkout, invoicing, cancellation, and payment methods remain Phase 2/later work.
- Notification inbox/history remains unavailable and is explicitly disabled; Notification operations belong to later phases.
- OAuth provider keys, invitation onboarding, plan-selection UI, and paid-plan flows belong to Phase 2 and were not started.
- Dashboard, Auth, Automation, BPM, AI, Data, Definitions, and other feature pages retain their existing Phase 0 behavior except for adopting the shared shell/scope pipeline.
- A licensed/local Roboto asset remains unavailable in the repository.

Phase 2 has not been started.

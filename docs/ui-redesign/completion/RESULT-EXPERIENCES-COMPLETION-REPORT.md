# Result Experiences Completion Report

Updated: 2026-08-18

## Outcome

The panel now exposes builder output as first-class destinations:

- BPM work appears in `/work`; site-scoped work remains in `/sites/{siteKey}/portal`.
- Published entity definitions can become authenticated or public forms through `/forms`.
- Authenticated forms open at `/forms/{slug}` for active tenant members.
- Public forms open at `/f/{slug}` only when Storefront has an active `PUBLIC` publication.
- Published website pages are listed at `/sites/{siteKey}/published`.
- Each page receives a Cyan-hosted `/s/{tenantKey}/{siteKey}/...` URL before custom DNS is configured.
- Dashboard links open Cartable, Forms, entity data/forms, BPM Designer, Sites, site cartable, and Site Builder.

## Backend contract

`storefront-service` owns publication because it is the public experience layer. `V102__published_forms.sql` persists scope, target service/entity, visibility, lifecycle, creator, and timestamps. It does not duplicate definitions or submitted records.

Storefront calls the target service's existing `/internal/entities/**` API with the publication's persisted tenant/site scope. Anonymous access is allowed only for `PUBLIC` publications. `AUTHENTICATED` publications require active tenant membership. Every submission requires an idempotency key and maps it deterministically to one dynamic record key.

## UX and states

- English/Farsi and LTR/RTL use shared shell behavior and logical CSS.
- Desktop cards collapse to one column on mobile.
- Public forms use a focused, shell-free experience.
- Loading, empty, request error, validation, unavailable, success, and pending states are explicit.
- Submit buttons remain disabled until the response completes.
- Production code contains no sample forms, records, sites, or fake successes.

## Verification

- `./gradlew :storefront-service:test` — passed.
- `npm run lint` — passed with pre-existing non-fatal warnings.
- `npm run build` — passed and emitted all new routes.
- `npx playwright test tests/result-pages.e2e.spec.ts` — four functional tests passed; visual capture is a separate opt-in test.
- Visual review covered 1440px English light, 390px English dark, 390px Farsi RTL, and a public mobile form.

## Deployment boundary

Deploy `storefront-service` first so Flyway creates `published_forms`, then deploy `panel-web`. Redeploy `api-gateway` only if consumers must call `/public/forms/**` directly through `api.cyancoder.com`; panel-hosted `/f/**` and `/s/**` do not require that gateway route.

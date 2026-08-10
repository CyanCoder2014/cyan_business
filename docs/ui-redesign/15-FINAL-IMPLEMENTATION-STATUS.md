# Final Implementation Status — Phases 0–11

Status: **IMPLEMENTED AND VERIFIED**, subject to the truthful external and
follow-on gaps in `16-REMAINING-BACKEND-GAPS.md`.

Updated: 2026-08-10

## Outcome

The Cyan panel now uses one authenticated, tenant/site-aware shell across the
canonical product routes. It is wired to service-owned APIs and dynamic entity
contracts; production fixture registries, hardcoded project/bot stores, and the
old shell have been removed. The panel does not manufacture tenant scope,
records, provider readiness, access results, or mutation success.

Phases 0–10 remain represented by their audit and completion reports. Phase 11
closed the repository-wide hardening boundary with:

- an install/update/offline PWA lifecycle that caches only the offline document
  and static assets, never API mutations;
- safe-area and responsive spacing fixes, scrollable dense regions, bounded
  JSON/widget surfaces, and desktop/tablet/mobile shell alignment;
- shared async buttons with disabled, `aria-disabled`, `aria-busy`, and
  duplicate-handler guards;
- focus trapping, Escape handling, and focus return for shared dialogs and the
  tenant access drawers;
- safe structured errors with correlation IDs and retryability metadata;
- lazy client loading for the Automation and BPM graph builders;
- functional client/team filtering;
- BPM state candidate groups, access rules, transition roles/groups, and
  USER/ROLE/GROUP work assignment using the existing persisted BPM contract;
- canonical compatibility redirects and retirement of the runtime product
  roadmap and obsolete fixture-backed BFF routes;
- English/Farsi, LTR/RTL, light/dark, reduced-motion, touch-target, and mobile
  bottom-navigation hardening;
- deterministic E2E coverage for offline truth, dialog focus, BPM access,
  duplicate mutation protection, and earlier Phase 1–10 routes.

## Automation AI security boundary

`GAP-080` is resolved for the existing Automation `AI_OPERATION` node:

1. `tenant-service` publishes `ai.read`, `ai.execute`, and
   `automation.execute` in its canonical permission catalog.
2. AI-bearing definitions require effective `automation.manage` and
   `ai.execute` before save/lifecycle operations.
3. Human execution requires effective `automation.execute` and `ai.execute`.
4. `automation-orchestrator-service` resolves permissions and effective
   `ai-orchestrator` capability from `tenant-service`; browser-supplied role
   lists are not treated as the AI grant.
5. Executions persist initiating actor, authorization mode, flow/version,
   tenant/site, and a generated or supplied correlation key.
6. Permission and capability failures remain distinct and an AI node cannot
   become a fake successful step.

The approved post-Phase-11 backend closure now adds scoped custom provider
profiles, validated multimodal file/video input, and persisted asynchronous
generated binary artifacts. These are implemented in AI Orchestrator and Media,
not as frontend-only success states.

## Product flow status

| Flow | Status | Evidence boundary |
|---|---|---|
| Session scope/bootstrap | Operational | session-owned tenant/site is loaded by `/api/panel/bootstrap`; no local fallback scope |
| Admin client and head user | Operational | real tenant, identity, owner membership, capability overrides, and FREE billing subscription |
| Head-user role/member management | Operational | bounded server-side permission grants and last-owner protection |
| CRM + Automation project | Operational | real AI draft/run/release and dynamic service provisioning contracts |
| Automation AI call | Operational and hardened | tenant effective permission plus capability revalidation before save/activate/run |
| BPM/cartable | Operational | pageable assigned/visible/role/group/unassigned/completed queues, active form, transitions, locks, comments, attachments, and authoritative role/group/user assignment |
| Telegram/Bale to Automation/BPM | Operational when configured | provider secrets remain references; absent credentials are `NOT_CONFIGURED` |
| Site builder/public view | Operational | dynamic route/theme records and sandboxed Storefront render |
| Domain/certificate | Partial by external prerequisite | DNS verification is real; certificate provider remains truthful when not configured |
| Reports/media/search | Operational within documented contracts | report runs and CSV/JSON exports, real media bytes with reference-protected deletion, durable health history, and search definitions/sync/query |

## Quality evidence

- `npm run lint`: passes; legacy hook-dependency and raw-image warnings remain
  non-fatal and are recorded in the Phase 11 completion report.
- `npm run build`: passes. Shared first-load JS is 87.9 kB; route shells are
  approximately 88–114 kB and graph implementations are lazy-loaded.
- Tenant and Automation Orchestrator unit tests: pass.
- Playwright Phase 1–11 deterministic suite: see the Phase 11 completion report.
- Phase 11 visual set: 20 screenshots across 1440 desktop, 1600 builder, 834
  tablet, 390 mobile, and 360 Farsi RTL dark mobile states.

The screenshots use deterministic API interception only as test fixtures. No
test names, values, records, metrics, or statuses are part of production code.

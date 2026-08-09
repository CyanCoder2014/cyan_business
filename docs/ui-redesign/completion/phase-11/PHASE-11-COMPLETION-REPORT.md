# Phase 11 Completion Report

Status: **COMPLETE**

Completed: 2026-08-09

## Delivered

Phase 11 completed the approved hardening scope and did not begin a new product
phase. The implementation includes PWA offline/install/update lifecycle,
accessibility and focus hardening, responsive/safe-area corrections, normalized
errors, guarded mutations, lazy graph builders, compatibility migration, dead
fixture removal, final documentation, and the GAP-080 Automation AI permission
boundary.

The client/head-user and BPM/cartable work uses existing real contracts. Client
and team search now filters returned records. BPM exposes persisted candidate
groups, state read/edit/approve rules, transition roles/groups, and
USER/ROLE/GROUP assignment. The UI does not invent candidate users/roles or an
assignable-target endpoint.

Automation AI now checks tenant-effective permissions and effective capability
state through `tenant-service` for endpoint users. AI-bearing flow save and
lifecycle operations require builder plus AI permission; execution requires
Automation execution plus AI permission. Initiator, authorization mode,
tenant/site, flow version, and correlation are persisted as execution
provenance.

## Cleanup and migration

Removed after a repository-wide caller audit:

- old `AppShell`, project cards, and workspace controls;
- fixture, draft-roadmap, and static product-roadmap modules;
- local project and bot-session registries/clients;
- `/api/projects/**` and `/api/bot-sessions/**` local BFF routes.

The real `/bot/[sessionId]` AI Orchestrator view remains and now uses
`PanelShell`. `/iam`, `/integrations`, `/site-builder`, and roadmap compatibility
paths route to their canonical jobs without hardcoded tenant/site identifiers.

## PWA and UX behavior

- The service worker caches only the offline document, repository icon/fonts,
  and versioned static assets. API calls and non-GET mutations are never cached
  or reported as queued success.
- The runtime exposes offline, install-ready, and update-ready states in English
  and Farsi.
- Shared dialogs trap Tab, close with Escape, and restore focus; client and team
  overlays use the same keyboard principles.
- Async buttons expose pending semantics and route handlers guard duplicate
  invocation.
- Desktop/sidebar scrolling no longer allows account controls to overlap
  navigation; dense dashboard/JSON/transition surfaces are bounded.
- Mobile content has bottom-navigation/safe-area clearance and full-width action
  spacing. RTL uses logical CSS and key/URL inputs remain LTR.

## Verification

Commands executed:

```text
bash ./gradlew :tenant-service:test :automation-orchestrator-service:test
npm run lint
npm run build
npm run test:e2e
CAPTURE_PHASE_11=1 npx playwright test tests/phase11.e2e.spec.ts --grep "capture Phase 11"
```

Final exact results are recorded after the last verification run below.

- Backend targeted tests: passed (`17` Gradle tasks; both target service test
  suites successful).
- Lint: passed with pre-existing/non-fatal hook-dependency and raw-image
  warnings.
- Production build: passed; shared first-load JS 87.9 kB.
- Full panel E2E: `34 passed`, `8 skipped`, `0 failed` across `42` tests. The
  skipped cases are opt-in screenshot capture suites and the live-credential
  admin journey, not functional failures.
- Phase 11 functional coverage passed for offline truth, focus/search,
  persisted BPM access, and one-request assignment.
- Visual capture: `1 passed`, `0 failed`; the opt-in run refreshed 20
  screenshots after the final layout corrections.

## Visual review

The 20-image set covers 1440 desktop, 1600 dark builder, 834 tablet, 390 mobile,
and 360 Farsi RTL dark mobile. Review found and corrected:

- desktop sidebar account/navigation overlap;
- overly tall dashboard rows caused by a dense capability widget;
- cramped BPM transition role/group fields;
- mobile work-item action and collaboration padding;
- dialog focus return and mobile bottom-navigation clearance.

No reference screenshot names, records, metrics, providers, or statuses were
copied into runtime code.

## Truthful remaining boundary

GAP-081 through GAP-083 (custom/multimodal/generated-media AI), expanded BPM
candidate/queue/portal contracts, report export, protected media deletion, and
external provider prerequisites remain in
`docs/ui-redesign/16-REMAINING-BACKEND-GAPS.md`. They are not simulated.

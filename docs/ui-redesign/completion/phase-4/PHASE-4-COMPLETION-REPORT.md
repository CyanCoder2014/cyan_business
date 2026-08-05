# Phase 4 Completion Report

## Outcome

Phase 4 is complete. `/ai` provides persisted conversations and drafts; `/projects` and `/projects/[projectId]` provide real project discovery, capability-filtered workspaces, provisioning, immutable releases, and activity. `/projects/new` is a compatibility redirect. No seeded production conversation, fake readiness, fake preview URL, swallowed provisioning error, or fixed tenant/site value remains in these routes.

## Backend Contracts

- AI Orchestrator: close session, tenant/site-scoped draft attachments, immutable release snapshots, successful-run release creation, publish/rollback activation, active release, and scoped access checks.
- Media: bearer-facing prepare, expiring upload target, byte PUT, configured size limit, durable upload state, filesystem persistence, cancellation, truthful upload status, and real asset metadata. The former fabricated CDN URL and optimized variants were removed.
- Panel BFF: binary-safe proxying; it no longer converts uploaded bytes through text.

## User Experience

- Persistent session list, create/resume/close, backend messages/questions, blueprint-derived quick prompts, and refresh-safe draft links.
- Real byte upload progress and persisted asset attachment.
- Project search/filter, accessible blueprint cards, explicit unavailable preview, and saved-draft links.
- Project tabs filtered by capabilities, PLAN/APPLY confirmation, idempotency keys, run details, releases, publish/rollback confirmation, and activity.

## Verification

- `npm run lint` — passed with non-fatal exhaustive-dependency warnings.
- `npm run build` — passed.
- Full Playwright Phase 1–5 run — 19 passed, 2 screenshot-only skips; Phase 4 verifies run persistence, release creation, and a four-byte upload body.
- `:media-service:test` and targeted `ProjectReleaseServiceTest` — passed.
- Full AI test compilation passed. Three pre-existing Spring integration tests require a running MongoDB and failed earlier only with `MongoTimeoutException`; this environmental prerequisite is not hidden.

## Visual QA

Compared against all Phase 4 references. The implementation preserves the three-pane desktop studio, focused mobile conversation, restrained Cyan gradient actions, clear project summary, and shell density. It intentionally omits fabricated preview/readiness metrics shown in reference art.

Screenshots are under `screenshots/` for desktop, tablet, mobile, English light/dark, and Farsi RTL light.

Phase 6 and later were not started.

# Phase 1–7 End-to-End Verification Report

## Result

The Phase 1–7 panel journey passes against the minimum local service set. Authentication, MFA, persisted session scope, workspace/plan access, AI project generation, CRM and automation provisioning, execution, release creation, and release activation were exercised through the browser.

## Persisted Scope Fix

The repeated `The persisted session scope could not be loaded.` failure was caused by email-alias login sessions being stored under the submitted alias while the user and tenant membership were owned by the canonical username. Authentication now creates and renews sessions with the canonical user identity. Bootstrap also treats stale scope as recoverable, and permission evaluation honors platform wildcard grants. A real login verified scope read and update with HTTP 200 responses.

## Live Scenario

1. Sign in as the local administrator with password, development MFA code, and CAPTCHA challenge.
2. Load or create a real tenant workspace and activate the real local free plan when not already configured.
3. Click Home, AI Studio, Projects, Definitions, Data, BPM, Automation, and Work.
4. Start a persisted AI conversation and request a CRM project with a native automation and AI enrichment operation.
5. Answer the required business-name question and persist it to the linked draft/session.
6. Plan and apply provisioning through the real owning services.
7. Assert a successful provisioning run, create a release, publish it, and assert the release is `ACTIVE`.

## Service Set Used

The journey used the SSO auth/user/session/OTP/CAPTCHA services, tenant and billing services, panel, AI orchestrator, automation orchestrator, BPM, content, catalog, CRM, storefront, notification, report, and their configured persistence dependencies. Services were added only when provisioning traces proved they owned a required operation.

## Commands and Results

- `npm run lint` — passed (exit 0; warnings only).
- `npm run build` — passed.
- `npm run test:e2e` — 22 passed; 4 explicit-only live/capture tests skipped by design.
- `LIVE_PHASES_1_7=1 npm run test:e2e -- tests/live-phase1-7.e2e.spec.ts` — 1 passed (5.9-second scenario, 7.3-second run).
- Explicit visual capture run — 14 passed (28.6 seconds).
- SSO auth/session, AI orchestrator, automation orchestrator, and BPM Gradle test suites — passed.

## Visual Evidence

Forty-nine screenshots cover Phase 1 and Phases 3–7 at desktop, tablet, and mobile sizes, including English light/dark and Farsi RTL light. The live publication frame is `phase-7/screenshots/live-admin-crm-automation-published.png`.

No mock data or fake API success was used by the live journey. Test-only intercepted fixtures remain confined to deterministic visual and contract-isolation Playwright cases.

# Cyan Panel UI/UX Implementation Handoff

This package translates the current `panel-web` implementation, Cyan product goals, automation runtime, and BPM architecture into an implementation-ready frontend plan.

It is intentionally stricter than a visual mockup. Every page must:

- use real service-backed data
- preserve bearer refresh and same-origin BFF routing
- send real tenant/site scope
- respect plan, role, permission, and service capability access
- support English/Farsi, LTR/RTL, light/dark, desktop/tablet/mobile, and PWA
- include loading, partial, empty, validation, error, success, disabled, offline, and permission-denied states
- avoid hardcoded demo payloads and fake KPI counts
- avoid duplicating desktop and mobile business logic

## Package contents

| File | Purpose |
|---|---|
| `01-PRODUCT-IA-ROUTES.md` | Target information architecture, routes, and compatibility redirects |
| `02-DESIGN-SYSTEM.md` | Tokens, typography, layout, responsive, theme, RTL, and accessibility |
| `03-AUTH-PLANS-TENANCY-RBAC.md` | Authentication, onboarding, plan gates, tenant users, roles, and capabilities |
| `04-PAGE-SPECS.md` | Detailed functional and API-oriented page specifications |
| `05-AUTOMATION-BUILDER-SPEC.md` | n8n-style automation builder UX and runtime integration |
| `06-BPM-FLOW-BUILDER-SPEC.md` | BPM designer, cartable, active forms, and automation bridge |
| `07-API-INTEGRATION-RULES.md` | Data-fetching, scope, mutations, errors, caching, and no-mock rules |
| `08-CODEX-MASTER-PROMPT.md` | Master prompt and page-by-page prompt template |
| `09-DELIVERY-PLAN.md` | Recommended implementation phases, PR boundaries, and acceptance gates |
| `10-VISUAL-DELIVERABLES-MANIFEST.md` | Required desktop/mobile/light/dark/English/Farsi design images |
| `design-tokens.tokens.json` | Tokens Studio-compatible design token starter |
| `capability-navigation-matrix.csv` | Machine-readable navigation and access matrix |

## Recommended usage

1. Add this directory to the repository under `docs/ui-redesign/`.
2. Give Codex only one route group per task.
3. Attach the corresponding design image(s).
4. Include `08-CODEX-MASTER-PROMPT.md`.
5. Require a pre-implementation report.
6. Approve the report before code changes.
7. Reject any implementation that replaces unavailable API data with fixtures.
8. Require screenshots and Playwright coverage in both desktop and mobile viewports.

## Important product decision

The panel must be capability-driven, not a fixed menu with CSS-hidden items. Most deployments enable only a subset such as:

- AI Orchestrator
- Automation
- BPM
- Processor
- Notification
- Report
- Media
- Search

The shell, routes, actions, tabs, empty states, and AI suggestions must derive from the effective capability set returned for the signed-in user and active tenant/site.

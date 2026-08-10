# Codex Master Prompt for Cyan Panel UI Implementation

Copy this prompt into Codex and append the route-specific task.

```text
You are implementing the Cyan `panel-web` application.

Read these files completely before changing code:
1. panel-web/README.md
2. docs/ui-redesign/00-README.md
3. docs/ui-redesign/01-PRODUCT-IA-ROUTES.md
4. docs/ui-redesign/02-DESIGN-SYSTEM.md
5. docs/ui-redesign/03-AUTH-PLANS-TENANCY-RBAC.md
6. docs/ui-redesign/04-PAGE-SPECS.md
7. docs/ui-redesign/07-API-INTEGRATION-RULES.md
8. the route-specific design spec
9. relevant backend architecture/reference documents
10. all current route, component, API client, DTO, proxy, and Playwright files in scope

The attached screenshots are visual references. The repository API contracts and backend behavior are functional constraints.

Before editing, output a PRE-IMPLEMENTATION REPORT containing:
A. current route/component file map
B. current API calls and DTOs
C. working controls versus placeholders
D. access, tenant/site, locale, theme, and responsive behavior
E. differences from the target design/spec
F. reusable components/hooks required
G. missing or ambiguous backend contracts
H. exact files you plan to change
I. test plan

Stop after the report. Do not edit until the report is approved.

After approval, implement with these mandatory rules:

- Use real backend data. Never replace unavailable data with fixtures, hardcoded records, seeded chat messages, fake KPIs, or fake success.
- Preserve same-origin BFF routing, bearer refresh, and backend authorization.
- Use the active tenant/site context for every scoped API call. Remove hardcoded tenant-demo/site-commerce values in the implemented scope.
- Filter navigation and controls by effective capabilities, permissions, and plan features.
- Backend authorization remains authoritative; handle 401, 403, capability unavailable, and plan locked separately.
- Use Roboto for English and Vazir for Farsi.
- Support English/Farsi, LTR/RTL, light/dark, desktop/tablet/mobile, and PWA.
- Do not use oversized panel titles. Follow the design-system type scale.
- Reuse one data/action layer for desktop and mobile. Do not duplicate business logic.
- Implement loading, refresh loading, partial, empty, validation, error, success, disabled, offline, and permission-denied states.
- Use accessible semantic controls. No nested buttons. Add focus, keyboard, reduced-motion, and 44px touch targets.
- Use typed domain API clients. Do not call service URLs directly from route components.
- Show missing API functionality as disabled or unavailable with an explanation. Do not simulate it.
- Confirm publish, activate, delete, cancel, webhook, role, billing, and domain mutations.
- Add dirty-form protection for editors.
- Run lint, build, and route-specific Playwright tests.
- Capture desktop 1440x1000 and mobile 390x844 screenshots in English light, English dark, and Farsi RTL where relevant.

At completion, report:
1. changed files
2. API endpoints used
3. removed placeholders/hardcoded values
4. access behavior
5. responsive/RTL/theme behavior
6. tests run and results
7. screenshots generated
8. remaining backend gaps
```

## Route task template

```text
TASK: Implement route group: <route(s)>

Target user job:
<job>

Required capabilities:
<capabilities>

Required permissions:
<permissions>

Design references:
<attached images>

Route-specific spec:
<file and section>

Backend references:
<docs/files>

Acceptance scenarios:
1. ...
2. ...
3. ...

Do not work on unrelated routes.
```

## Automation task addendum

```text
Read docs/ui-redesign/05-AUTOMATION-BUILDER-SPEC.md and
AUTOMATION_REFERENCE_GUIDE.md.

Use @xyflow/react. Preserve backend node identity, position, input/output indices,
runtime mode, lifecycle, credential references, policies, pin data, and error
workflow. Do not claim support for unsupported n8n nodes. Do not import secrets.
```

## BPM task addendum

```text
Read docs/ui-redesign/06-BPM-FLOW-BUILDER-SPEC.md and
BPM_SERVICE_ARCHITECTURE.md.

Separate flow definition design from managed-object operation. Render active forms
from rendererDefinition. Expose transition execution, comments, attachments, and
history from real APIs. RUN_AUTOMATION_BLOCK must map to the automation runtime,
not a local fake action.
```

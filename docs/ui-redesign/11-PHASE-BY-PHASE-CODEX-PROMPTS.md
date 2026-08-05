# Cyan Panel — Phase-by-Phase Codex Prompts

These prompts are designed to be executed sequentially. Do not ask Codex to implement all phases in one run.

## How to use each phase

1. Check out a clean branch.
2. Attach the design image(s) listed for the phase.
3. Paste the phase prompt.
4. Codex must return the pre-implementation report and stop.
5. Review the report, especially:
   - APIs it found
   - missing backend contracts
   - files it intends to change
   - whether it plans to introduce mock data
6. When the report is acceptable, send the approval continuation prompt.
7. Run and review the implementation.
8. Merge only after tests and screenshots pass.
9. Start the next phase from the merged branch.

## Universal approval continuation prompt

Use this only after reviewing the pre-implementation report:

```text
The PRE-IMPLEMENTATION REPORT is approved, with these corrections if any:

<write corrections here, or write "No corrections">

Proceed with implementation only for the approved phase and files.

Mandatory:
- Do not expand scope into later phases.
- Do not add mock data, seeded production messages, fake metrics, fake API success,
  fake preview URLs, or hardcoded tenant/site values.
- Do not invent backend contracts.
- When an API is unavailable, implement an explicit unavailable/disabled state and
  document the exact missing endpoint and DTO.
- Preserve same-origin BFF routing, bearer refresh, backend authorization, and
  active tenant/site scope.
- Run lint, build, and the phase-specific tests.
- Capture the required screenshots.
- At completion, provide the completion report required by
  docs/ui-redesign/08-CODEX-MASTER-PROMPT.md.
```

---

# Phase 0 — Repository and Backend Contract Audit

Run this once before Phase 1. This phase must not redesign pages.

```text
You are auditing the Cyan `panel-web` application before a phased UI redesign.

Read completely:
- panel-web/README.md
- every file under docs/ui-redesign/
- README files and architecture/reference guides for:
  - SSO/IAM
  - tenant/site/workspace management
  - plan, billing, and payment
  - AI Orchestrator
  - dynamic entities
  - Automation
  - BPM
  - Processor
  - Notification
  - Report
  - Media
  - Search
  - Bot Adapter
  - Storefront, hosting, and domains

Inspect:
- the complete panel-web/app route tree
- panel-web/components
- panel-web/lib
- panel-web/app/api
- all DTO/type files used by panel-web
- all Playwright and unit tests
- package.json
- next.config files
- PWA/service-worker/manifest files if present
- environment variable examples
- OpenAPI or generated API specifications in the repository

Perform only a contract and architecture audit.

Produce:
1. Current route-to-component map.
2. Current route-to-API map.
3. Current BFF proxy map.
4. Current authentication and refresh behavior.
5. Current tenant/site handling and every hardcoded tenant/site occurrence.
6. Existing role, permission, plan, feature, and capability DTOs and APIs.
7. OAuth status for Google, GitHub, and LinkedIn.
8. Current responsive, locale, RTL, and theme implementation.
9. Current PWA implementation status.
10. Existing shared components and duplicated components.
11. Visible controls that are placeholders.
12. Backend calls that use hardcoded/demo mutation payloads.
13. Pages that mutate backend state during initial load.
14. Missing API contracts required by docs/ui-redesign/.
15. Recommended compatibility redirects and migration risks.
16. A proposed branch/PR sequence for Phases 1–11.

Create or update:
- docs/ui-redesign/12-CURRENT-CONTRACT-INVENTORY.md
- docs/ui-redesign/13-BACKEND-GAPS.md
- docs/ui-redesign/14-ROUTE-MIGRATION-MAP.md

Do not change runtime application code in this phase.
Do not add mocks.
Do not redesign any page.

Return the audit report and stop.
```

Exit gate:
- We know which features can be implemented from existing APIs.
- Every missing backend contract is explicit.
- Hardcoded tenant/site and demo mutation locations are enumerated.

---

# Phase 1 — Foundation, Design System, Unified Shell, Scope, and Access

Design references:
- `modern_saas_dashboard_overview.png`
- existing dark-mode Cyan dashboard image
- existing Farsi RTL Cyan dashboard image
- `cyan_ai_catalog_builder_dashboard.png` for mobile navigation behavior

```text
Read completely:
- panel-web/README.md
- every file under docs/ui-redesign/
- docs/ui-redesign/12-CURRENT-CONTRACT-INVENTORY.md
- docs/ui-redesign/13-BACKEND-GAPS.md
- docs/ui-redesign/14-ROUTE-MIGRATION-MAP.md

Inspect:
- app/layout.tsx
- app/globals.css
- components/panel-shell.tsx
- components/app-shell.tsx
- components/panel-provider.tsx
- lib/platform-auth.ts
- lib/platform-api.ts
- lib/service-api.ts
- lib/types.ts
- lib/platform-service-inventory.ts
- all current locale, theme, storage, access, navigation, and API proxy files
- package.json
- current PWA/manifest/service-worker files
- all shell-related tests

Implement only Phase 1 foundation:

1. Typography:
   - Roboto for English.
   - Vazir for Farsi.
   - Use next/font/local when approved font files already exist in the repository.
   - If font files are absent, do not download or invent them; document the missing
     asset and use the approved fallback temporarily.
   - Code/JSON uses a monospace stack.
   - Follow the title sizes in docs/ui-redesign/02-DESIGN-SYSTEM.md.

2. Design tokens:
   - implement `design-tokens.tokens.json` as maintainable CSS variables or typed
     tokens
   - light and dark semantic colors
   - spacing, radius, elevation, status, focus, typography, and responsive tokens
   - avoid route-specific hardcoded colors where semantic tokens apply

3. Unified shell:
   - replace split PanelShell/AppShell behavior with one authenticated AppShell
   - preserve compatibility during migration
   - desktop sidebar
   - tablet collapsed/overlay navigation
   - mobile bottom navigation with Home, AI, Build, Work, More
   - mobile Build and More capability-filtered sheets
   - correct active route state
   - safe-area handling

4. Locale and direction:
   - English/Farsi
   - document lang and dir
   - correct LTR/RTL layout mirroring
   - logical CSS properties
   - LTR treatment for URL, code, JSON, email, key, and identifier inputs

5. Theme:
   - light, dark, and system preference
   - no flash of wrong theme where practical
   - persisted preference
   - semantic tokens rather than per-page overrides

6. Real active scope context:
   - stable tenant/workspace identifier
   - stable site identifier when applicable
   - selector UI connected to actual API state
   - scope sent by typed API clients
   - remove hardcoded tenant-demo/site-commerce only in shared infrastructure and
     routes touched by this phase
   - if tenant/site list APIs are missing, do not fake selectors; show a truthful
     unavailable state and document the required contract

7. Access resolver:
   - effective roles
   - permissions
   - plan features
   - service capabilities
   - capability health/degraded status
   - one navigation registry
   - route guard
   - action guard
   - separate 401, 403, plan-locked, capability-disabled, and service-unavailable
     states
   - backend remains authoritative

8. Shared primitives:
   - Button and IconButton
   - Field, Select, Combobox
   - Tabs and SegmentedControl
   - Badge and StatusBadge
   - Card
   - Dialog, Drawer, BottomSheet
   - Toast
   - ConfirmDialog
   - Skeleton
   - EmptyState
   - ErrorState
   - PermissionState
   - PlanGate
   - Offline/Stale indicator
   - PageHeader
   - ResponsiveInspector
   - CodeViewer/JSON viewer shell
   - initial DataGrid shell, without redesigning Data Manager yet

9. Accessibility:
   - visible focus
   - semantic controls
   - no nested buttons
   - accessible labels/tooltips
   - focus trapping and restoration
   - reduced motion
   - 44px mobile touch targets

10. PWA shell foundation:
   - manifest and installability audit/fix
   - safe-area variables
   - offline banner
   - cache only shell/static assets unless an explicit data cache already exists
   - do not queue mutations offline

11. Do not redesign feature pages yet.
12. Do not change Automation, BPM, AI, Data, Definitions, Auth, or Dashboard
    business behavior except what is strictly necessary to adopt the shell.
13. Do not add mock data.
14. Do not replace service-backed data with local objects.

Before editing, produce the PRE-IMPLEMENTATION REPORT required by
docs/ui-redesign/08-CODEX-MASTER-PROMPT.md and stop for approval.
```

Phase 1 tests:
- shell renders at 1440, 1024, 834, 390, and 360 widths
- language and direction survive reload
- dark/light/system survive reload
- mobile navigation and sheets are keyboard/touch accessible
- route guard distinguishes plan lock, permission denial, and unavailable service
- changing active scope invalidates/reloads scoped queries
- no hardcoded tenant-demo/site-commerce remains in shared scope infrastructure

---

# Phase 2 — Authentication, OAuth, Onboarding, Plans, and Limited Access

Design references:
- `cyan_ai_business_app_landing_page.png`
- `cyan_app_signup_ui_design.png`
- existing mobile authentication reference

```text
Read completely:
- panel-web/README.md
- docs/ui-redesign/02-DESIGN-SYSTEM.md
- docs/ui-redesign/03-AUTH-PLANS-TENANCY-RBAC.md
- docs/ui-redesign/04-PAGE-SPECS.md sections Auth and Onboarding
- docs/ui-redesign/07-API-INTEGRATION-RULES.md
- docs/ui-redesign/08-CODEX-MASTER-PROMPT.md
- docs/ui-redesign/12-CURRENT-CONTRACT-INVENTORY.md
- docs/ui-redesign/13-BACKEND-GAPS.md
- SSO Auth/User/Captcha/OTP/Session/FIDO architecture and DTOs
- tenant/workspace/site APIs
- plan/billing APIs

Inspect:
- app/auth/**
- all auth components
- lib/platform-auth.ts
- current token storage and refresh
- `/api/sso/**` BFF routes
- registration/login/captcha DTOs
- any OAuth callback routes
- any onboarding, billing, tenant, or invitation code
- auth and onboarding tests

Implement only:

1. `/auth`
   - Sign in and Register modes
   - email/username and password
   - captcha lifecycle and expiry feedback
   - show/hide password
   - OTP/MFA challenge only when backend response requires it
   - account locked, invalid credentials, provider unavailable, expired captcha,
     and recoverable error states
   - returnTo handling
   - no raw backend error body

2. OAuth:
   - Google
   - GitHub
   - LinkedIn
   - implement only providers with confirmed backend start/callback contracts
   - unavailable providers remain visible only when product wants discovery, but
     disabled with a clear configuration explanation
   - never simulate OAuth success
   - preserve state/PKCE/account-link behavior defined by backend

3. `/onboarding`
   - accept invitation
   - select existing workspace/tenant
   - create workspace/tenant when API exists
   - select site when applicable
   - choose plan
   - continue with limited access
   - completion checklist
   - resume interrupted onboarding

4. Limited-access mode:
   - user can enter the panel without selecting a paid plan
   - dashboard/product-tour access only
   - locked mutations display PlanGate
   - no redirect loop
   - no fake plan/usage data

5. Plan selection:
   - current plan
   - plan comparison from API
   - limits/features
   - request enterprise
   - payment redirect/confirmation only when API exists
   - billing mutations require confirmation

6. Preserve:
   - bearer refresh
   - logout
   - same-origin BFF routing
   - active session behavior

7. Mobile:
   - single-column auth
   - usable OAuth buttons
   - keyboard-safe layout
   - onboarding as responsive stepper/cards
   - safe-area support

8. English/Farsi, LTR/RTL, light/dark.

9. Do not redesign Dashboard yet.
10. Do not create local-only workspace or plan state.
11. Do not add mock providers, plans, invitations, or tenants.

Before editing, produce the PRE-IMPLEMENTATION REPORT and stop for approval.
```

Acceptance:
- password/captcha sign-in works from real APIs
- registration automatically signs in only if current contract supports it
- OAuth provider state is truthful
- no-plan user reaches limited dashboard
- invited user joins correct tenant
- all errors are normalized and localized
- desktop and mobile Playwright coverage

---

# Phase 3 — Capability-Aware Dashboard and Notification Center Shell

Design references:
- `modern_saas_dashboard_overview.png`
- dark Cyan dashboard reference
- Farsi RTL dashboard reference

```text
Read:
- panel-web/README.md dashboard and notifications sections
- docs/ui-redesign/01-PRODUCT-IA-ROUTES.md
- docs/ui-redesign/03-AUTH-PLANS-TENANCY-RBAC.md
- docs/ui-redesign/04-PAGE-SPECS.md Dashboard and Notification Center sections
- docs/ui-redesign/07-API-INTEGRATION-RULES.md
- relevant AI, BPM, Automation, Notification, Bot, Site, Billing/Usage API docs

Inspect:
- app/page.tsx and dashboard components
- notification bell implementation
- current dashboard API clients
- current Promise.allSettled behavior
- activity and metric DTOs
- current roadmap/static data use
- tests

Implement only:

1. Route migration:
   - canonical `/dashboard`
   - compatibility redirect from `/`

2. Capability-aware dashboard:
   - active project/draft hero from real data
   - recent AI generations/projects
   - provisioning runs
   - active capabilities and health
   - assigned BPM work
   - failed/running automation executions
   - notification delivery summary
   - active bots/sites when capabilities exist
   - usage and plan limits
   - recent activity

3. Widget rules:
   - render only when capability and permission allow
   - plan-locked discovery is visually distinct from functional widgets
   - each metric has source/update time
   - no fake totals
   - each widget has independent loading, empty, partial-error, stale, and retry
     behavior
   - rows link to real detail routes

4. Dashboard customization:
   - no free-form dashboard builder yet
   - preserve stable order based on capabilities
   - mobile prioritizes active project, work, failures, and recent activity

5. Notification bell:
   - real unread count
   - opens notification inbox drawer
   - mark one/all read when APIs exist
   - deep links
   - full-page link to `/notifications`
   - unavailable state when backend contract is missing

6. Limited-access dashboard:
   - product capability explanations
   - plan comparison CTA
   - no pretending services are enabled
   - no fake metrics

7. English/Farsi, LTR/RTL, light/dark, desktop/tablet/mobile.

8. Do not redesign AI Studio or other workspaces in this phase.
9. Remove dashboard-only hardcoded tenant/site values.
10. Do not use static roadmap data as operational truth.

Before editing, produce the PRE-IMPLEMENTATION REPORT and stop.
```

Acceptance:
- one failed service does not blank the page
- no-plan and full-plan variants work
- notification drawer works from shell
- every displayed count is traceable to a real response
- mobile dashboard is not merely a scaled desktop grid

---

# Phase 4 — AI Studio, Projects, Project Workspace, Provisioning, and Releases

Design references:
- `imagegen.png` or the generated AI Studio desktop image from this batch
- `cyan_ai_catalog_builder_dashboard.png`
- `blueprints_app_interface_mockup_design.png`

```text
Read:
- panel-web/README.md sections:
  - `/projects/new`
  - `/projects`
  - `/projects/[projectId]`
  - media upload behavior
- docs/ui-redesign/04-PAGE-SPECS.md AI Studio, Projects, Project Workspace
- docs/ui-redesign/07-API-INTEGRATION-RULES.md
- AI_ORCHESTRATOR_SERVICE_ARCHITECTURE.md
- the AI Orchestrator persistent control-plane plan
- media-service upload contracts
- provisioning and release DTOs
- project/session compatibility BFF routes

Inspect:
- app/projects/new/**
- app/projects/**
- app/projects/[projectId]/**
- app/bot/[sessionId]/**
- lib/platform-api.ts
- media client
- project/session compatibility clients
- AI WebSocket support
- all project and AI tests

Implement only:

1. Canonical `/ai`
   - persistent session list
   - create/resume/close session
   - append user message
   - display backend messages and questions
   - no seeded production conversation
   - session linked to draft/project
   - quick prompts derive from available capabilities/blueprints

2. Draft/project lifecycle:
   - list/search/filter projects
   - create from blueprint/app type
   - open project
   - patch prompt/answers
   - resolve/recompute
   - display pending questions
   - display backend validation/readiness
   - no client-calculated readiness pretending to be authoritative

3. Project workspace:
   - Overview
   - AI
   - Structure
   - Automations
   - BPM
   - Data
   - Channels
   - Site
   - Releases
   - Provisioning
   - Activity
   - tabs filtered by capabilities

4. Provisioning:
   - PLAN and APPLY where supported
   - run timeline
   - step result
   - duration
   - retry eligibility
   - partial success
   - idempotency key
   - polling/realtime with backoff
   - no swallowed errors

5. Releases:
   - immutable releases
   - current active release
   - publish confirmation
   - rollback confirmation when API exists
   - no fake preview endpoint

6. Blueprints:
   - real filters/search only when they query/filter actual loaded data
   - Preview button works or is unavailable with explanation
   - cards are accessible; no nested buttons
   - saved drafts link to project detail

7. Attachments:
   - prepare upload
   - upload bytes to returned storage
   - real progress
   - failure/retry/cancel
   - attach resulting asset metadata to session/draft
   - do not append metadata to prompt as a substitute for upload unless current
     backend contract explicitly requires it

8. Mobile:
   - AI conversation
   - project summary bottom sheet
   - pending questions
   - provision/publish actions with confirmation
   - shared hooks with desktop

9. Migrate `/projects/new` to `/ai` with compatibility redirect.

10. Do not build Definitions, Automation, BPM, Site, or Bot editors in this phase;
    project tabs may link to current routes.

Before editing, produce the PRE-IMPLEMENTATION REPORT and stop.
```

Acceptance:
- refresh preserves real conversation and draft
- known draft appears in list and workspace
- file upload sends bytes and reports progress
- provisioning run survives page refresh
- no fake preview/readiness/session messages
- mobile AI flow is fully usable

---

# Phase 5 — Definitions & Forms and Definition-Driven Data Manager

Design references:
- `saas_schema_editor_dashboard.png`
- `modern_saas_product_data_manager_dashboard.png`
- `clean_ui_design_for_product_schema.png` for mobile concepts

```text
Read:
- panel-web/README.md `/maker` and `/data`
- docs/ui-redesign/04-PAGE-SPECS.md Definitions and Data
- docs/ui-redesign/07-API-INTEGRATION-RULES.md
- dynamic entity service architecture/reference docs
- template/definition/validation/record DTOs
- strict nested-object/list validation behavior
- dynamic scenario examples

Inspect:
- app/maker/**
- app/data/**
- lib/dynamic-api.ts
- dynamic service proxies
- definition and record types
- all current hardcoded data buckets and mutation payloads
- current form/rendering utilities
- tests

Implement only:

1. Routes:
   - `/definitions`
   - `/definitions/[serviceKey]/[entityKey]`
   - `/data`
   - `/data/[serviceKey]/[entityKey]`
   - compatibility redirect from `/maker`

2. Definition catalog:
   - dynamic service selector
   - definitions
   - templates
   - versions/status
   - service ownership visible
   - create from template with explicit entity key
   - prevent accidental key collision
   - no definition creation during page load

3. Visual definition editor:
   - General
   - Fields
   - nested objects/lists
   - Validations
   - Operations
   - List/grid configuration
   - Object/detail configuration
   - Relations
   - Permissions
   - Scope
   - JSON/DSL advanced view
   - Versions
   - drag/reorder with keyboard alternative
   - visual field inspector
   - localized labels/help
   - definition validation
   - schema diff
   - dirty-form protection
   - save/publish confirmation where lifecycle exists

4. Generated forms:
   - create/edit form from live definition
   - nested list/object editor
   - exact nested field error mapping
   - relation selectors
   - enum/options
   - required/default/help
   - create/edit visibility
   - no product-specific assumptions

5. Data Manager:
   - entity catalog
   - columns derive from definition metadata
   - server-side pagination/filter/sort when supported
   - search only when real contract exists
   - create/edit/delete
   - record detail
   - bulk actions only when real APIs support them
   - import/export controls disabled with explanation when unsupported
   - stale conflict and validation handling
   - no hardcoded demo create/update payloads
   - active row is actual selection, not first row

6. Mobile:
   - catalog cards/list
   - responsive table/card representation
   - full record edit
   - full definition edit through stacked screens/bottom sheets
   - not read-only

7. English/Farsi, LTR/RTL, light/dark.

8. Do not modify BPM or Automation builders except shared definition/form pickers
   required by this phase.

Before editing, produce the PRE-IMPLEMENTATION REPORT and stop.
```

Acceptance:
- manually create a nested entity definition
- save and reload without losing nested structure
- create a record from generated form
- strict backend nested validation appears at exact field
- switch to another entity with different columns/forms
- mobile supports definition and record editing

---

# Phase 6 — Automation Builder, Executions, Schedules, and n8n Import/Export

Design reference:
- `modern_automation_builder_dashboard.png`

```text
Read completely:
- panel-web/README.md `/automation`
- docs/ui-redesign/05-AUTOMATION-BUILDER-SPEC.md
- docs/ui-redesign/07-API-INTEGRATION-RULES.md
- AUTOMATION_REFERENCE_GUIDE.md
- automation-orchestrator architecture and DTOs
- batch-worker architecture and DTOs
- n8n analyze/import/export contracts
- credential/secret reference contracts
- execution, attempt, dead-letter, callback, schedule, lifecycle, and policy DTOs

Inspect:
- app/automation/**
- current automation API clients in lib/service-api.ts or other files
- installed @xyflow/react version
- existing graph utilities
- current raw JSON editors
- current fixed three-node schedule creation
- execution start/refresh/cancel code
- tests

Implement only:

1. Routes:
   - `/automations`
   - `/automations/new`
   - `/automations/[flowKey]`
   - `/automations/[flowKey]/executions`
   - `/automations/executions/[executionId]`
   - compatibility redirect from `/automation`

2. Automation catalog:
   - definitions, versions, lifecycle, schedule, last execution, health
   - real filtering/search
   - capability and permission aware
   - create/duplicate/archive only when API exists

3. XYFlow editor:
   - node palette generated from backend metadata where available
   - if metadata endpoint is missing, use a versioned frontend UI metadata registry
     only for rendering forms; backend validation remains execution truth
   - preserve node IDs, positions, input/output indices, runtime mode, and policies
   - connect, reconnect, delete, multi-select, copy/paste, undo/redo when feasible
   - keyboard alternatives
   - minimap, zoom, fit
   - validation badges

4. Native nodes:
   - expose only node types documented by the runtime
   - category grouping
   - node inspector driven by metadata/schema
   - Parameters, Input, Output, Settings, Error Handling, Notes
   - credential reference selector, never secret value
   - retry, timeout, concurrency, and error policy
   - expression/code mode
   - `$json`, prior node, item, environment, and execution-data autocomplete when
     contract supports it
   - do not execute untrusted code in browser

5. Workflow settings:
   - flow key/name/version
   - labels/environment
   - VARIABLES vs N8N_ITEMS runtime mode
   - input/output schema
   - error workflow
   - roles
   - pin data
   - schedule
   - changing runtime mode requires validation and confirmation

6. Lifecycle:
   - Draft, Submitted, Approved, Active, Retired
   - buttons derive from allowed backend actions
   - diff before activation
   - validate missing credentials/schedule/webhook impact
   - confirmations

7. Executions:
   - manual input generated from input schema
   - full run
   - partial run only when backend supports it
   - running node states
   - node attempts
   - input/output
   - waits/callbacks
   - dead letters
   - retry
   - cancel confirmation
   - child workflows
   - sanitized diagnostic export
   - WebSocket/SSE or backoff polling

8. Schedules and batches:
   - create/edit schedule from user inputs
   - no fixed graph or fixed batch payload
   - lifecycle review before activate
   - run now
   - batch run history

9. n8n:
   - analyze file first
   - list supported/unsupported nodes
   - block unsupported native import
   - never import credential secrets
   - explicit confirmation
   - export supported graph accurately

10. Mobile:
   - full-screen canvas
   - add-node sheet
   - node inspector sheet
   - workflow settings screen
   - execution detail
   - same hooks and validation as desktop

11. English/Farsi, LTR/RTL, light/dark.
12. Do not create fake connectors or node types.

Before editing, produce the PRE-IMPLEMENTATION REPORT and stop.
```

Acceptance scenarios are those in `05-AUTOMATION-BUILDER-SPEC.md`, plus:
- refresh preserves layout
- failed execution can be inspected and retried where allowed
- unsupported n8n node blocks import
- mobile can edit and test a simple flow

---

# Phase 7 — BPM Designer, Work Queue, Active Forms, Comments, Attachments, and Automation Bridge

Design references:
- `purchase_requisition_approval_flow_builder.png`
- `mobile_app_flow_builder_interface.png`

```text
Read completely:
- panel-web/README.md `/maker` BPM portions and `/flows`
- docs/ui-redesign/06-BPM-FLOW-BUILDER-SPEC.md
- docs/ui-redesign/07-API-INTEGRATION-RULES.md
- BPM_SERVICE_ARCHITECTURE.md
- Processor service architecture
- Automation reference for RUN_AUTOMATION_BLOCK
- dynamic form/renderer contracts
- media attachment contracts
- BPM flow, metadata, managed object, active form, transition, comment, attachment,
  assignment, and history DTOs

Inspect:
- app/flows/**
- BPM portions of app/maker/**
- lib/bpm-api.ts
- BPM BFF proxies
- current custom canvas
- current assigned/visible queue implementation
- shared form renderer from Phase 5
- automation picker/API from Phase 6
- tests

Implement only:

1. Routes:
   - `/bpm`
   - `/bpm/new`
   - `/bpm/[flowKey]`
   - `/work`
   - `/work/[objectId]`
   - compatibility handling for `/flows`

2. BPM catalog:
   - flow definitions
   - status/version
   - assigned/visible counts
   - activate/start permissions
   - no silent starter flow creation

3. XYFlow BPM designer:
   - state nodes
   - transition edges
   - start/terminal indicators
   - selected state/transition inspector
   - state list alternative
   - minimap/zoom
   - drag and keyboard editing
   - preserve IDs and positions when supported

4. State inspector:
   - display name
   - key
   - terminal
   - form/entity binding
   - renderer
   - processor
   - submit mode and URL when applicable
   - candidate users/roles/groups
   - access rule
   - review-comment requirement
   - on-enter actions

5. Form/entity binding:
   - select real service/definition
   - preview generated form using rendererDefinition
   - map defaults/payload
   - validate required fields
   - never hardcode form fields

6. Actions:
   - metadata-driven forms
   - ADD_AUDIT_ENTRY
   - SET_ASSIGNEE
   - SET_ACCESS_RULE
   - UPDATE_OBJECT_FIELDS
   - COPY_FIELDS
   - REMOVE_FIELDS
   - CALL_API
   - CALL_OPERATOR
   - NOTIFY_OWNER
   - RUN_AUTOMATION_BLOCK
   - no generic placeholder params

7. Automation bridge:
   - select active automation flow/version or inline pipeline when supported
   - sync/async
   - input mapping
   - output mapping
   - error behavior
   - validate missing/deactivated automation reference

8. Transitions:
   - source/target
   - label
   - users/roles/groups
   - comment requirement
   - nested condition builder
   - priorities
   - actions
   - human-readable preview
   - JSON advanced view

9. Flow validation:
   - unique IDs
   - valid start
   - reachability
   - terminal rules
   - source/target
   - required bindings/actions/processors/automations
   - assignment/access conflict
   - show errors on graph, inspector, and summary

10. Work Queue:
    - Assigned to me
    - My groups/roles
    - Visible to me
    - Unassigned
    - Completed when permitted
    - real filters/search/pagination
    - SLA/age/priority

11. Work Item:
    - real active form
    - submit form
    - execute allowed transition
    - confirmation when necessary
    - comments with scope
    - media attachments
    - assignment/reassignment when allowed
    - lock/unlock when supported
    - payload
    - audit and transition history
    - refresh active form/options after mutation

12. Mobile:
    - editable designer through canvas + sheets
    - usable Work Queue
    - active form submission
    - transition action
    - comments/attachments
    - not read-only

13. English/Farsi, LTR/RTL, light/dark.

Before editing, produce the PRE-IMPLEMENTATION REPORT and stop.
```

Acceptance scenarios are those in `06-BPM-FLOW-BUILDER-SPEC.md`.

---

# Phase 8 — Team, Roles, Permissions, Clients, Profile, Security, Billing, and Settings

Design references:
- use the Phase 1 shell/dashboard style
- use auth/onboarding plan-card style for Billing
- no unique generated image is required for every settings page

```text
Read:
- docs/ui-redesign/03-AUTH-PLANS-TENANCY-RBAC.md
- docs/ui-redesign/04-PAGE-SPECS.md Team & Access, Profile, Billing, Settings
- IAM, realm, client, role, membership, assignment, invitation, session, MFA,
  device, tenant/client, billing, and plan API docs
- current `/iam` implementation
- current shared IAM methods in lib/service-api.ts

Inspect:
- app/iam/**
- any team, role, client, profile, billing, and settings code
- SSO/IAM BFF proxies
- current access JSON rendering
- current workspace/site localStorage settings
- tests

Implement only:

1. Routes:
   - `/team/users`
   - `/team/roles`
   - `/clients`
   - `/profile`
   - `/billing`
   - `/settings`
   - compatibility redirect from `/iam`

2. Team users:
   - list/search/filter
   - invitation/create based on actual contract
   - status
   - roles
   - groups/client scope
   - last login
   - suspend/remove/resend invitation
   - detail drawer/page
   - prevent unauthorized cross-tenant access

3. Roles and permissions:
   - role list
   - permission matrix
   - capability restrictions
   - member assignment
   - inherited/effective access explanation
   - prevent privilege escalation
   - confirmation for high-impact grants/removals

4. Client management:
   - visible only for authorized admins
   - client tenant list
   - assigned admins
   - plan/capabilities/status/users
   - a client admin cannot grant permissions they do not hold
   - do not create fake hierarchy when backend does not support it

5. Profile/security:
   - personal information
   - password
   - MFA
   - linked OAuth identities
   - sessions/devices
   - revoke/logout all
   - notification preferences
   - locale/theme
   - no raw access JSON as primary UI

6. Billing:
   - current plan
   - usage
   - limits
   - invoices
   - payment method
   - change/cancel
   - failed payment state
   - all data from APIs
   - confirmation and redirect handling

7. Settings:
   - General
   - Workspace
   - Sites
   - Localization
   - Security defaults
   - AI defaults
   - Automation defaults
   - BPM defaults
   - Notification providers
   - Search
   - Retention
   - Audit
   - sections filtered by capability/permission
   - workspace/site selector is real and shared with scope context, not cosmetic

8. Mobile, Farsi/RTL, dark mode, accessibility.
9. Do not implement Bots, Sites, or Domains editors in this phase.
10. Do not use localStorage as authoritative tenant, plan, role, or permission data.

Before editing, produce the PRE-IMPLEMENTATION REPORT and stop.
```

Acceptance:
- tenant admin invites a user and assigns an allowed role
- client admin cannot escape scope or grant higher privilege
- user revokes a session
- plan and invoices are real
- settings update active scope/defaults correctly

---

# Phase 9 — Bots, Channels, Sites, Commerce Presentation, Domains, Hosting, and Notifications

Design references:
- `client_apps_and_bot_management_dashboard.png`
- `bot_experience_dashboard_ui_mockup.png`
- `cyan_site_builder_dashboard_interface.png`
- existing public/mobile storefront references

```text
Read:
- panel-web/README.md `/integrations`, `/bot`, `/bot/[sessionId]`,
  `/site-builder`, `/notifications`, and relevant commerce sections
- docs/ui-redesign/04-PAGE-SPECS.md Bots, Sites and Domains, Notification Center
- Bot Adapter architecture and DTOs
- Storefront, Content, Media, Search, Commerce, Cart, Checkout, Pricing, Payment,
  hosting/domain/certificate docs
- Notification service architecture and DTOs
- secret/Vault reference contracts

Inspect:
- app/integrations/**
- app/bot/**
- app/site-builder/**
- app/notifications/**
- app/commerce/**
- bot/storefront/notification/payment/media clients and proxies
- current hardcoded channel/test/route/commerce payloads
- tests

Implement only:

1. Routes:
   - `/bots`
   - `/bots/[integrationKey]`
   - `/sites`
   - `/sites/[siteId]/builder`
   - `/domains`
   - `/notifications`
   - compatibility redirects from `/integrations`, `/bot`, `/site-builder`

2. Bots:
   - integration/channel list
   - Telegram and Bale
   - provider config
   - secret reference only
   - webhook status/register with confirmation
   - session mapping
   - inbound/outbound operations
   - delivery history
   - retry failed delivery
   - mini-app builds
   - publish confirmation
   - separate Configuration, Sessions, Deliveries, Provisioning tabs
   - real add/edit form; remove fixed telegram-main and @john_doe behavior

3. Sites:
   - site list
   - routes/pages
   - reusable blocks/sections
   - theme
   - SEO
   - assets
   - draft/preview/publish
   - route conflict/slug validation
   - preview real returned render in sandboxed iframe or approved safe renderer
   - no stylized fake preview
   - no fixed page values on Add Page

4. Commerce presentation:
   - only expose operator/customer features supported by current APIs
   - do not retain developer seeding UI as the primary user experience
   - no hardcoded customer/address/amount/provider
   - payment/provider state comes from API
   - keep admin/developer seeding under protected platform/dev tooling if needed

5. Domains and hosting:
   - real domain records
   - ownership/verification
   - DNS instructions
   - certificate state
   - redirects
   - environment
   - history
   - polling/realtime
   - do not mock domain or certificate success
   - destructive/domain mutations require confirmation

6. Notifications:
   - user inbox
   - unread/read
   - deep links
   - templates
   - provider/channel selection
   - render preview
   - test dispatch
   - delivery/message history
   - retry
   - no external example webhook default
   - model editor validates JSON/schema
   - shell bell uses the same inbox data

7. Mobile/PWA:
   - bot and site operations
   - domain status
   - notification inbox
   - public PWA preview
   - safe area

8. English/Farsi, RTL, dark mode.
9. Never display token/secret values.
10. Do not invent hosting/domain APIs.

Before editing, produce the PRE-IMPLEMENTATION REPORT and stop.
```

---

# Phase 10 — Reports, Media, Search, Operational Tools, and Settings Completion

Design reference:
- use shared list/grid/detail patterns from Dashboard and Data Manager

```text
Read:
- panel-web/README.md `/search`, `/qa`, `/roadmap`, and report/media references
- docs/ui-redesign/04-PAGE-SPECS.md Reports, Media, Search
- report-service architecture and dynamic report contracts
- media-service upload/assets contracts
- search-index-service definition/sync/query/suggest contracts
- QA/health APIs
- current route classification decisions from Phase 0

Inspect:
- app/search/**
- any report/media routes
- app/qa/**
- app/roadmap/**
- report/media/search clients and proxies
- current raw JSON rendering
- current definition creation during page load
- tests

Implement only:

1. `/reports`
   - report definitions
   - parameter form
   - run
   - run status/history
   - result table/chart where data shape permits
   - export only when backend supports it
   - clear partial/failure state
   - do not claim the final dynamic report path works if backend verification is
     still pending

2. `/media`
   - asset grid/list
   - real upload progress
   - folders/tags when API supports
   - details and usage references
   - image/document preview
   - deletion protection
   - pagination/search
   - no metadata-only pretend upload

3. `/search`
   - correct navigation label
   - index definitions
   - source fields/analyzers
   - save
   - sync start/progress/history
   - indexed document counts
   - query/suggest tester
   - structured result display, not raw JSON
   - no definition creation during initial page load

4. Operational/admin tools:
   - classify QA and Roadmap as platform/admin/developer tools
   - protect them by role/capability
   - do not mix product-planning static data into tenant operations
   - QA checks may show HTTP status, duration, retry one, export, environment,
     and history when APIs/storage permit

5. Finish capability-filtered settings for Report, Media, and Search.

6. Mobile/Farsi/RTL/dark/accessibility.

Before editing, produce the PRE-IMPLEMENTATION REPORT and stop.
```

---

# Phase 11 — System Hardening, PWA, Accessibility, Performance, Testing, and Migration Cleanup

This phase should not introduce major new product features.

```text
Read:
- every file under docs/ui-redesign/
- all completion reports from Phases 1–10
- all remaining TODO/backend-gap documents
- package.json
- Next.js configuration
- PWA/manifest/service-worker
- test configuration
- analytics/error-monitoring configuration if present

Inspect the complete panel-web source.

Implement only hardening and cleanup:

1. Remove obsolete shell/components after confirming no route uses them.
2. Complete compatibility redirects and route migration.
3. Remove dead navigation labels and hidden accidental routes.
4. Search the entire panel for:
   - tenant-demo
   - site-commerce
   - fixed demo payloads
   - seeded chat messages
   - fake metrics
   - fake preview URLs
   - swallowed catches
   - raw backend error rendering
   - mutation during page load
   - nested buttons
   - English-only hardcoded visible copy
   - physical left/right CSS that breaks RTL
5. PWA:
   - installability
   - manifest icons supplied by repository
   - offline shell
   - update prompt
   - stale-data labels
   - safe area
   - no offline mutation success without queue
6. Accessibility:
   - WCAG 2.2 AA audit
   - keyboard-only journeys
   - screen-reader names
   - focus management
   - reduced motion
   - contrast
   - graph keyboard alternatives
   - 44px touch targets
7. Performance:
   - bundle analysis
   - lazy-load large builders
   - virtualization for grids/logs
   - avoid unnecessary duplicate queries
   - cancel stale requests
   - optimize large JSON/code views
8. Security:
   - secret redaction
   - no token logging
   - sandbox external renders
   - sanitize diagnostic bundles
   - review localStorage token risk without changing backend contract blindly
9. Reliability:
   - query retry policy
   - mutation idempotency
   - consistent confirmation
   - dirty-form protection
   - correlation IDs in errors
10. Internationalization:
    - complete English/Farsi keys
    - Vazir/Roboto application
    - RTL visual audit
    - date/number formatting
    - keep code/URL/key inputs LTR
11. Theme:
    - light/dark parity
    - no unreadable charts/graphs
12. Testing:
    - route smoke tests
    - auth/onboarding
    - access/plan/capability
    - tenant/site switch
    - AI session/draft
    - definition/record
    - automation edit/execute
    - BPM design/work item
    - team/role
    - bot/site/domain/notification
    - reports/media/search
13. Visual regression screenshots:
    - desktop 1440×1000
    - builder 1600×1000
    - tablet 834×1112
    - mobile 390×844
    - small mobile 360×800
    - English light/dark
    - Farsi light/dark RTL for Tier 1 screens
14. Run:
    - npm run lint
    - npm run build
    - npm run test:e2e
    - any unit/integration checks added during phases

Produce:
- docs/ui-redesign/15-FINAL-IMPLEMENTATION-STATUS.md
- docs/ui-redesign/16-REMAINING-BACKEND-GAPS.md
- docs/ui-redesign/17-ROUTE-AND-CAPABILITY-MATRIX-FINAL.md
- final screenshot manifest

Before editing, produce the PRE-IMPLEMENTATION REPORT and stop.
```

Final exit gate:
- no production mock data
- no hardcoded scope
- access is capability/permission/plan aware
- mobile/PWA is operational
- English/Farsi and light/dark are complete
- complex builders work from real backend definitions and runtime state
- remaining gaps are explicitly backend gaps, not hidden frontend simulations

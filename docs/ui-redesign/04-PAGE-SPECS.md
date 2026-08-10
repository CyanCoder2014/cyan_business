# Page Specifications

This file defines the minimum design and implementation contract for each primary page.

## Common contract for every page

Each route must define:

- job-to-be-done
- required capabilities
- required permissions
- active tenant/site behavior
- load APIs
- mutation APIs
- loading/partial/empty/error/success states
- desktop/tablet/mobile layout
- English/Farsi copy
- light/dark design
- analytics events where needed
- Playwright acceptance scenarios

No page may render fake records when the API is empty or unavailable.

---

## 1. Auth `/auth`

### Jobs
- sign in
- register
- OAuth via Google, GitHub, LinkedIn
- complete captcha/OTP/MFA when required
- recover account
- return to requested route

### Layout
Desktop split marketing/auth. Mobile single-column.

### States
- initial provider loading
- captcha loading/expired
- provider unavailable
- invalid credentials
- OTP required
- account locked
- successful redirect

### APIs
Preserve current SSO register/login/refresh/logout/captcha routes. OAuth buttons require real backend contracts.

---

## 2. Onboarding `/onboarding`

### Jobs
- create or join workspace
- choose tenant/site
- select plan or continue limited
- finish initial preferences

### Components
- stepper
- invitations
- tenant form
- plan cards
- capability summary
- completion checklist

### Acceptance
A user with no plan reaches dashboard limited mode without a redirect loop.

---

## 3. Dashboard `/dashboard`

### Jobs
- resume latest project
- understand current capability/status
- see work requiring attention
- start AI creation

### Data
- latest drafts/projects
- provisioning runs
- assigned BPM work
- failed automation executions
- notification delivery alerts
- active sites/bots
- usage/plan limits

### Rules
- cards derive from enabled capabilities
- each metric has source and updated time
- partial failures are scoped to a widget
- recent activity rows link to detail

---

## 4. AI Studio `/ai`

### Jobs
- create/resume persistent conversation
- generate or edit a project
- answer blueprint questions
- attach real media/files
- inspect proposed changes before applying

### Layout
Desktop: conversation + project summary inspector.
Mobile: conversation + collapsible project summary bottom sheet.

### APIs
Primary:
- session create/read/message/close
- draft list/read/patch/resolve
- provisioning plan/apply
- media upload preparation and actual upload

### No-mock requirements
- no seeded messages when no session exists
- no client-derived readiness percentage
- no fake preview URL
- attachments show real byte upload progress

---

## 5. Projects `/projects`

### Jobs
- list/search/filter projects
- create from blueprint
- view status and last release
- archive/duplicate when APIs exist

### Grid/list columns
- name
- tenant/site
- app type
- draft status
- latest revision
- release status
- provisioning status
- owner
- updated

### Mobile
Cards with filters in a bottom sheet.

---

## 6. Project workspace `/projects/[projectId]`

### Tabs
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

### Overview
- draft metadata
- pending questions
- capabilities
- validation/readiness from backend
- last run and active release

### Provisioning
- plan/apply modes
- step-by-step run timeline
- retry eligible step
- idempotency key
- partial-success state

---

## 7. Definitions `/definitions`

### Jobs
- select dynamic service
- browse definitions/templates
- create from template
- create manually
- view versions/status

### List
Do not show one combined list without service ownership. Include service, entity type, scope, version, record count where available.

---

## 8. Definition editor `/definitions/[serviceKey]/[entityKey]`

### Sections
- General
- Fields
- Nested objects/lists
- Validations
- Operations
- List/grid configuration
- Object/detail configuration
- Relations
- Permissions
- Scope
- JSON/DSL
- Versions

### Field editor
Field properties:
- key
- localized label
- type
- required
- default
- help
- visibility
- validation
- options
- relation
- nested children
- list/detail/form placement

### Mutation
Use definition update API. Validate before save. Show schema diff. Prevent navigation with unsaved changes.

### Mobile
Full editing is required through stacked sections and bottom sheets; not read-only.

---

## 9. Data `/data/[serviceKey]/[entityKey]`

### Jobs
- render grid from live definition
- search/filter/sort/page
- create/edit/delete records
- resolve relations
- import/export when APIs exist

### Grid behavior
Columns come from definition display metadata. Record form comes from the same definition.

### States
- definition missing
- no records
- validation failure with field mapping
- relation loading failure
- stale update conflict
- permission-limited actions

---

## 10. Automations `/automations` and editor

See `05-AUTOMATION-BUILDER-SPEC.md`.

---

## 11. BPM `/bpm`, `/bpm/[flowKey]`, `/work`

See `06-BPM-FLOW-BUILDER-SPEC.md`.

---

## 12. Team & Access

### Users
- search, status, roles, groups/client scope, last login
- invite/create
- suspend/remove
- resend invitation
- open user detail

### Roles
- role list
- permission matrix
- capability restrictions
- member assignments
- prevent privilege escalation

### Clients
Visible only when client management is supported:
- client tenants
- assigned admins
- active plan/capabilities
- users
- status

---

## 13. Profile `/profile`

- personal information
- language/theme
- password
- MFA
- linked OAuth identities
- active sessions/devices
- logout all
- notification preferences

Workspace/site selection belongs to the shell or settings, not personal profile text fields.

---

## 14. Billing `/billing`

- plan state
- usage
- comparison
- payment method
- invoices
- change/cancel confirmation
- failed payment states

---

## 15. Settings `/settings`

Sections are capability-filtered:
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

---

## 16. Bots `/bots`

- channels/integrations
- provider configuration
- secret reference, never secret value
- webhook health
- session mapping
- delivery history
- retry failed delivery
- mini-app builds
- publish confirmation

Separate configuration, conversation sessions, and delivery operations through tabs.

---

## 17. Sites and domains

### Sites
- site list
- route/page builder
- theme
- preview
- publish/release
- SEO
- assets

### Domains
- domain ownership
- DNS instructions/status
- certificate status
- redirects
- environment
- verification history

No domain status should be mocked.

---

## 18. Notification center

- in-app user notification inbox
- unread/read state
- filters
- deep links
- bulk mark-read

Builder/operations:
- templates
- providers
- render preview
- dispatch test
- message history
- retry

The shell bell opens the inbox drawer and links to the full page.

---

## 19. Reports, media, and search

### Reports
- dynamic report definitions
- parameter form
- run status
- result table/chart
- export
- history

### Media
- asset upload
- real progress
- folders/tags
- usage references
- image details
- delete protection when referenced

### Search
- index definitions
- source fields/analyzers
- sync runs/progress
- document counts
- query/suggest tester
- errors by source

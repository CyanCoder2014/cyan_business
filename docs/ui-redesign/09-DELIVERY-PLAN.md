# Delivery Plan

## Phase 0 — Contract inventory

Deliverables:
- effective access/bootstrap contract
- tenant/site selector APIs
- plan APIs
- OAuth contracts
- OpenAPI/schema export for UI-facing services
- list of missing endpoints

No major visual rewrite should begin before scope and access are real.

## Phase 1 — Foundation

PRs:
1. Design tokens and local fonts
2. Shared primitives and states
3. Unified `AppShell`
4. Active scope context
5. Access/capability resolver
6. Typed error normalization
7. Query/mutation foundation
8. PWA shell and offline indicator

Acceptance:
- English/Farsi
- light/dark
- desktop/mobile
- capability-filtered nav
- real tenant/site switch

## Phase 2 — Identity and commercial access

- auth redesign
- Google/GitHub/LinkedIn when backend ready
- onboarding
- limited no-plan mode
- profile/security
- billing/plan
- tenant user/role management

## Phase 3 — AI and projects

- persistent AI sessions
- real attachment upload
- project list/workspace
- blueprint filters
- provisioning run UI
- releases

## Phase 4 — Definitions and Data

- visual definition editor
- nested objects/lists
- validations
- operation/list/detail configuration
- version/diff
- definition-driven record grid/forms
- relation inputs
- mobile parity

## Phase 5 — Automation

- graph editor foundation
- node metadata
- inspector/expression editor
- lifecycle
- test executions
- execution history
- n8n analyze/import/export
- schedules and credentials

## Phase 6 — BPM

- flow designer
- state/transition metadata forms
- form/entity/processor wiring
- automation bridge
- work queue
- active form
- transitions
- comments/attachments/history

## Phase 7 — Channels and publishing

- bot/channel configuration
- delivery operations
- site/page builder
- domains/hosting
- notification center
- reports/media/search

## PR boundaries

A PR should normally contain:
- one route or one shared foundation concern
- API client/types
- page components
- localization keys
- tests
- screenshots
- docs update

Avoid a single “redesign all pages” PR.

## Definition of done per route

- no hardcoded scoped IDs
- no production fixtures
- all visible primary controls work or are explicitly unavailable
- permissions and plan gates
- desktop/tablet/mobile
- English/Farsi
- LTR/RTL
- light/dark
- PWA safe-area behavior
- loading/partial/empty/error/success/offline
- keyboard and screen reader
- lint/build/e2e
- screenshot comparison

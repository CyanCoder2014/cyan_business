# Cyan Panel UI/UX Roadmap

## Goal

Rebuild `panel-web` so the shipped panel matches the `UI-UX` reference set as closely as possible, with:

- light theme as the default
- multilingual support for English and Farsi
- Vazir for Farsi presentation
- a workspace shell consistent across desktop and mobile
- live wiring to available backend microservices
- explicit notes for any feature where the panel can present a complete UX but the backend contract is still partial

## Reference Inventory

### Auth and marketing

- `UI-UX/1.png`
- Purpose: dual-column sign-in and account creation entry point with product positioning on the left and auth form on the right
- Key traits:
  - airy light canvas
  - subtle blue-to-violet accent gradient
  - value-prop bullets by product area
  - enterprise trust strip
  - polished auth card with segmented sign in/create account tabs

### Panel workspace

- `UI-UX/2.png`
- Purpose: AI Studio main workspace
- Key traits:
  - left sidebar navigation
  - top workspace and site selectors
  - AI chat composer in center
  - generated draft summary on the right
  - small metric tiles and recent generation list below

- `UI-UX/3.png`
- Purpose: blueprint catalog and draft generation launcher
- Key traits:
  - filter/search row
  - blueprint cards with preview and use actions
  - right detail rail with included services and DSL preview
  - saved drafts table

- `UI-UX/4.png`
- Purpose: maker definitions designer
- Key traits:
  - entity tree on the left
  - central field/validation/relation tabs
  - right rail for tenancy, relation settings, and DSL/API summary
  - version history footer

- `UI-UX/5.png`
- Purpose: data manager
- Key traits:
  - KPI cards
  - record type sidebar
  - central data grid with filters/import/export
  - right detail drawer
  - analytics at bottom

- `UI-UX/6.png`
- Purpose: visual site builder
- Key traits:
  - page list and section palette left
  - desktop/mobile preview center
  - content/style inspector right
  - preview/draft/publish controls top right

- `UI-UX/7.png`
- Purpose: client apps and bots channel manager
- Key traits:
  - channel cards for website, Telegram, Bale, mini app, mobile app
  - message delivery table
  - right rail with connection details and QR/test area

- `UI-UX/9.png`
- Purpose: bot experience simulator
- Key traits:
  - Telegram and Bale side-by-side mock conversations
  - capabilities rail
  - delivery status chart

- `UI-UX/10.png`
- Purpose: flow builder
- Key traits:
  - node palette left
  - BPM canvas center
  - selected node config on the right
  - activity log and test panel below

### Public storefront / PWA

- `UI-UX/8.png`
- `UI-UX/PWA/*.png`
- Purpose: public web/PWA surfaces generated from the same platform
- Key traits:
  - product-market oriented landing layout
  - route cards and product tiles
  - large mobile mockup
  - FAQ/testimonial/newsletter sections

### Farsi

- `UI-UX/Farsi/1.png`
- `UI-UX/Farsi/2.png`
- Purpose: localized dashboard and mobile-first workspace
- Key traits:
  - full RTL layout
  - Vazir typography
  - translated sidebar, cards, and metrics
  - bottom navigation on mobile

### Dark

- `UI-UX/Dark/*.png`
- Purpose: secondary dark theme
- Requirement decision:
  - keep dark theme available
  - ship light theme by default

## Design System Contract

### Visual direction

- background: warm white with soft blue-violet atmospheric glow
- surfaces: rounded white panels with thin cool-gray borders
- accent gradient: blue to violet for primary CTAs and key highlights
- typography:
  - English: modern sans stack
  - Farsi: Vazir
- density:
  - generous whitespace
  - medium information density inside cards/tables
- motion:
  - soft hover raise
  - stagger/fade page load where practical without extra dependencies

### Layout primitives

- persistent left sidebar on desktop
- fixed top workspace bar
- main content area with route-specific panels
- right rail on data-heavy screens
- responsive card stacks under tablet widths
- mobile bottom nav for the localized compact dashboard experience

## Route Mapping

### Primary panel routes

- `/` -> dashboard matching the Farsi/English dashboard references
- `/projects/new` -> AI Studio
- `/projects` -> Blueprints
- `/maker` -> Maker Definitions
- `/data` -> Data Manager
- `/site-builder` -> Site Builder
- `/integrations` -> Client Apps / Bots
- `/bot` -> Bot Experience
- `/flows` -> Flow Builder

### Supporting routes

- `/commerce` -> commerce operations summary built from cart/checkout/payment services
- `/search` -> search index management
- `/automation` -> automation execution console
- `/iam` -> IAM overview
- `/roadmap` -> internal build roadmap and gap tracking page
- `/notifications` -> notification operations summary

## Backend Wiring Map

### Already supported by existing APIs

- AI Studio
  - `ai-orchestrator-service`
  - endpoints:
    - `/endpoint/ai-orchestrator/generate/app`
    - `/endpoint/ai-orchestrator/blueprints`
    - `/endpoint/ai-orchestrator/drafts`
    - `/endpoint/ai-orchestrator/drafts/{draftId}/provision`
    - `/endpoint/ai-orchestrator/drafts/{draftId}/runs`

- maker/data definitions and records
  - `dynamic-entity-core` through service-owned `/endpoint/entities/**`
  - wired through `panel-web/app/api/platform/dynamic/**`

- bot channels and mini apps
  - `bot-adapter-service`
  - endpoints:
    - `/endpoint/bot-adapter/integrations`
    - `/endpoint/bot-adapter/messages`
    - `/endpoint/bot-adapter/messages/{messageId}/retry`
    - `/endpoint/bot-adapter/mini-apps`
    - `/endpoint/bot-adapter/mini-apps/{channel}/{integrationKey}/{buildKey}/publish`

- flows
  - `bpm-service`
  - endpoints:
    - `/endpoint/bpm/flows`
    - `/endpoint/bpm/metadata/state-actions`
    - `/endpoint/bpm/metadata/transition-conditions`
    - `/endpoint/bpm/managed-objects`

- search
  - `search-index-service`
  - endpoints:
    - `/endpoint/search-index/search`
    - `/endpoint/search-index/suggest`
    - `/endpoint/search-index/sync/{serviceKey}/{entityKey}`

- automation
  - `automation-orchestrator-service`
  - endpoints:
    - `/endpoint/automation-orchestrator/executions/start`
    - `/endpoint/automation-orchestrator/executions/{executionId}`
    - `/endpoint/automation-orchestrator/executions/{executionId}/cancel`

- payments
  - `payment-service`
  - `payment-orchestrator-service`

- IAM
  - `sso-user-service`
  - `/api/sso/iam/**`

- storefront preview
  - `storefront-service`
  - public endpoints:
    - `/public/storefront/resolve`
    - `/public/storefront/render`
    - `/public/storefront/page`

## Known Gaps Between UI and Service Contracts

### Gap 1: auth/marketing page is not backed by a dedicated panel auth flow yet

- reference shows a polished create-account/sign-in product entry
- current `panel-web` does not expose a production auth flow
- service status:
  - SSO microservices exist
  - panel-side integrated sign-up/sign-in orchestration is still incomplete
- implementation decision:
  - document the auth screen in README/roadmap
  - keep the panel app focused on authenticated workspace flows

### Gap 2: maker visual editing is richer than current definition APIs

- reference shows drag/reorder/table-level editing and schema map visualization
- current dynamic entity APIs support templates, definition save, and record submit
- missing backend-native capabilities:
  - explicit field reorder endpoint
  - schema diff/version history query designed for panel cards
  - richer relation graph endpoint
- implementation decision:
  - use existing definition JSON and derived UI summaries
  - note version/relation visualization as panel-side derivation for now

### Gap 3: data manager analytics cards are mostly projection/UI summaries

- reference shows product KPIs, top categories, inventory charting, and side detail drawer
- raw record access exists, but not every analytic aggregate is exposed as a dedicated API
- implementation decision:
  - derive dashboard summaries from live records when present
  - fall back to seeded demo values when environments are empty

### Gap 4: site builder WYSIWYG editing is more advanced than current storefront APIs

- preview/render support exists
- full section editing, style editing, and publish history are not fully represented by a dedicated page-builder backend contract
- implementation decision:
  - present an editor-grade panel UI
  - wire route resolve/render live
  - keep section/style inspector state panel-side until a dedicated builder persistence model exists

### Gap 5: bot experience simulator is primarily a panel UX layer

- bot delivery and integration APIs exist
- side-by-side Telegram/Bale simulation is not a backend concern
- implementation decision:
  - use integration/message APIs for live status
  - keep the simulator shell as a panel experience layer

### Gap 6: blueprint imagery and some screen-rich assets are product UI content, not backend entities

- current services expose blueprint metadata and generated DSL
- image previews and category art in the reference are not service-owned assets today
- implementation decision:
  - render rich blueprint cards with panel-owned visual placeholders
  - keep service-owned facts live

## Delivery Plan

### Phase 1

- establish the shared shell
- add bilingual support and theme switching
- wire Vazir
- align dashboard and AI Studio

### Phase 2

- rebuild blueprints, maker, data manager, site builder, integrations, bot experience, and flow builder
- connect each page to the most relevant backend endpoint

### Phase 3

- document residual API gaps
- tighten responsive/mobile states
- refresh the root GitHub README with product-level messaging and images

## Acceptance Criteria

- light theme loads by default
- Farsi mode uses Vazir and RTL layout
- key routes visually resemble the reference composition
- pages do not hard-fail if services are unavailable
- service-backed widgets show live data where available
- roadmap documents where the panel currently derives or mocks data because the backend contract is not yet specialized

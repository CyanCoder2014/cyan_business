# Cyan Panel Web

Next.js 14 control panel for Cyan's AI-assisted business platform.

This document is the current implementation reference for product design, UI/UX
redesign, screenshot-based review, and frontend development. It describes what
the panel does today, which backend calls each page makes, which controls are
functional, and which visible elements are placeholders.

Last implementation review: 2026-07-24.

## Product purpose

The panel is intended to be one operator workspace for:

- generating application drafts from natural-language prompts
- starting from reusable application blueprints
- defining dynamic entities and form structures
- connecting dynamic forms to BPM states
- building and operating BPM flows
- managing tenant/site-scoped business records
- configuring public storefront routes
- connecting Telegram and Bale bots and mini apps
- running automations and scheduled batch jobs
- configuring search, notifications, commerce, payment, and identity
- checking live platform integrations

The current product is broader than the navigation suggests. Several implemented
routes are hidden from the main navigation, and several navigation labels do not
match the page they open. See [Known UI/UX issues](#known-uiux-issues).

## How to use this document during the redesign

When redesigning a page from screenshots or design documents:

1. Treat the API contracts and data ownership documented here as implementation
   constraints.
2. Treat supplied screenshots and design documents as the visual source of truth.
3. Preserve working user journeys unless the redesign explicitly replaces them.
4. Do not infer that a visible control currently works. Check the route section
   below; placeholder controls are called out.
5. Reuse shared shell, state, API, and type modules instead of duplicating them.
6. Keep English/Farsi, LTR/RTL, light/dark, desktop/mobile, loading, empty, error,
   success, disabled, and permission-denied states in every redesigned page.
7. Do not replace service-backed data with fixtures to make a screen look full.

### Suggested handoff prompt for the UI implementation model

Use this after attaching the relevant screenshots and design documents:

```text
Read panel-web/README.md completely, then inspect the current source for the
route(s) in scope. The README describes current behavior and API contracts; the
attached images/documents are the visual and UX source of truth.

Before editing, report:
1. the current route/component/API mapping,
2. visual and interaction differences from the supplied design,
3. reusable components that should be created or changed,
4. unclear requirements or backend gaps.

Then implement the approved page without replacing service-backed data with
fixtures or changing backend contracts. Preserve authentication, bearer refresh,
tenant/site headers, English/Farsi, LTR/RTL, light/dark, and responsive behavior.
Implement loading, empty, partial, error, success, validation, disabled, and
permission-denied states. Clearly identify any control that cannot be completed
without a missing API. Run lint, build, and relevant end-to-end tests, then report
changed files and remaining gaps.
```

## Technology and application shape

- Next.js `14.2` App Router
- React `18.3`
- TypeScript
- client-rendered workspace pages
- same-origin Next.js BFF routes for backend access
- bearer-token authentication with refresh support
- CSS variables and one global stylesheet
- English and Farsi with document-level LTR/RTL switching
- light and dark themes
- separate desktop and mobile markup on several major pages
- `@xyflow/react` is installed, but the current flow page renders a simple custom
  state canvas rather than an interactive XYFlow graph

The root layout is `app/layout.tsx`. Global application state is provided by
`components/panel-provider.tsx`. Authenticated pages use
`components/panel-shell.tsx`, either directly or through the older
`components/app-shell.tsx` wrapper.

## Route map

| Route | Current page | Main service dependencies | Main navigation |
|---|---|---|---|
| `/auth` | Sign in and registration | SSO Auth, User, Captcha, Session | No |
| `/` | Dashboard | AI, BPM, Bot Adapter | Dashboard |
| `/projects/new` | AI Studio | AI Orchestrator, Media | AI Studio |
| `/projects` | Blueprints and drafts | AI Orchestrator | Blueprints |
| `/projects/[projectId]` | Draft/project detail | AI Orchestrator | Blueprints |
| `/maker` | Entity, form, and flow maker | Dynamic services, BPM, AI | Maker |
| `/data` | Dynamic record manager | Catalog, Content, CRM, Inventory | Data |
| `/forms` | Private/public form results | Storefront, dynamic services | My forms |
| `/forms/[slug]` | Authenticated tenant form | Storefront, target dynamic service | No |
| `/f/[slug]` | Public published form | Storefront, target dynamic service | No |
| `/flows` | BPM flow catalog and cartable | BPM, AI | Flow Builder |
| `/integrations` | Client apps, bots, mini apps | Bot Adapter | Client Apps/Bots |
| `/bot` | Bot delivery experience | Bot Adapter | Bot Experience |
| `/bot/[sessionId]` | AI conversation detail | AI Orchestrator | Incorrectly highlights Client Apps/Bots |
| `/site-builder` | Storefront route builder | Storefront, dynamic runtime | Site Builder |
| `/sites/[siteId]/published` | Published website result links | Storefront | Sites |
| `/s/[tenantKey]/[siteKey]/...` | Cyan-hosted public website | Storefront public renderer | No |
| `/search` | Search builder | Search Index | Incorrectly labeled Media |
| `/automation` | Automation and batch builder | Automation, Batch Worker | Incorrectly labeled Analytics |
| `/iam` | Profile and local panel settings | SSO User, SSO Auth | Settings |
| `/notifications` | Notification template/test builder | Notification | Hidden |
| `/commerce` | Commerce and payment test builder | Cart, Checkout, Pricing, Payment | Hidden |
| `/qa` | Live integration smoke harness | AI, Storefront, BPM, Bot Adapter | Hidden |
| `/roadmap` | Product roadmap with live counts | AI, BPM, Storefront, Catalog, Bot Adapter | Hidden |

## Global shell and navigation

### Authentication guard

`PanelShell` checks only whether an access token exists in browser local storage.
If not, it redirects to:

```text
/auth?returnTo=<current-path>
```

There is no page-level route middleware and no role/capability-based hiding of
navigation items. Backend authorization remains authoritative.

### Desktop shell

The desktop shell contains:

- fixed left sidebar with Cyan branding and primary navigation
- Pro-plan promotional card
- workspace identity badge
- top header with workspace and site switcher-looking controls
- notification icon
- help/settings link
- account menu with profile link, language toggle, theme toggle, and logout
- page kicker, title, subtitle, and route content

Current behavior:

- workspace/site switchers look interactive but do not open selectors
- the notification bell has no action
- Manage plan has no action
- help links to `/iam`
- account language/theme actions work
- logout calls the SSO logout endpoint and clears local auth state

### Mobile shell

At `820px` and below:

- the desktop sidebar and page intro are hidden
- a five-item sticky bottom navigation is shown
- several pages replace their desktop content with separately authored mobile
  markup

The bottom navigation contains Dashboard, AI Studio, Data, Flow Builder, and
Client Apps/Bots. Other routes are not directly reachable from it.

### Locale, direction, and theme

`PanelProvider` stores these values in `localStorage`:

| Key | Purpose |
|---|---|
| `cyan.panel.locale` | `en` or `fa` |
| `cyan.panel.theme` | `light` or `dark` |
| `cyan.panel.workspace` | displayed workspace name |
| `cyan.panel.site` | displayed site name |

Locale changes update `<html lang>` and `<html dir>`. Theme changes update
`data-theme`. Workspace and site names are visual preferences only; most API
calls still use hardcoded `tenant-demo` and `site-commerce`.

## Authentication and API transport

### Browser authentication state

The browser stores:

| Storage key | Value |
|---|---|
| `cyan.panel.authToken` | access token |
| `cyan.panel.refreshToken` | refresh token |
| `cyan.panel.authExpiresAt` | client-calculated expiration timestamp |
| `cyan.panel.sessionId` | SSO session identifier |
| `cyan.panel.username` | normalized username/email |

`platformFetch` attaches the bearer token. It refreshes within 60 seconds of
expiry, retries the failed request once after refresh, and redirects to `/auth`
after an unrecoverable `401`.

### Same-origin BFF routes

The browser normally calls relative Next.js routes:

```text
/api/sso/{service-segment}/...
/api/platform/service/{serviceKey}/...
/api/platform/dynamic/{serviceKey}/...
```

The server-side route handlers call the microservices directly. In this mode the
browser does not need direct access to Eureka or `api-gateway`.

The generic service proxy supports:

- `sso-user-service`
- `bot-adapter-service`
- `ai-orchestrator-service`
- `bpm-service`
- `storefront-service`
- `notification-service`
- `search-index-service`
- `automation-orchestrator-service`
- `batch-worker-service`
- `payment-service`
- `payment-orchestrator-service`

The dynamic service proxy supports:

- `content-service`
- `catalog-service`
- `crm-service`
- `commerce-service`
- `finance-service`
- `inventory-service`
- `report-service`
- `storefront-service`
- `media-service`
- `cart-service`
- `checkout-service`
- `bpm-service`
- `payment-service`
- `pricing-promotion-service`
- `notification-service`
- `search-index-service`

Both proxies forward `Authorization`, `X-Tenant-Key`, and `X-Site-Key` when
present.

## Detailed page reference

### `/auth` — sign in and registration

User goal:

- create a panel account
- pass the captcha challenge
- sign in
- return to the originally requested panel route

Desktop layout:

- left marketing area with product message, feature list, mock product cards,
  trust statements, and footer
- right authentication panel
- sign-in/sign-up tabs

Mobile layout:

- condensed authentication card
- same form behavior as desktop

Functional controls:

- switch between sign-in and account creation
- show/hide password
- refresh captcha
- register, then automatically sign in
- sign in

Placeholder controls:

- Google sign-in
- GitHub sign-in
- magic-link sign-in
- Terms, Privacy, Docs, Changelog, and Status footer links

API calls:

| Action | Method and browser path | Backend owner |
|---|---|---|
| Load/refresh captcha | `POST /api/sso/captcha/challenges?clientId=cyan-panel` | SSO Captcha |
| Register | `POST /api/sso/users/register` | SSO User |
| Sign in | `POST /api/sso/auth/login` | SSO Auth |
| Refresh token | `POST /api/sso/auth/refresh` | SSO Auth |
| Logout | `POST /api/sso/auth/logout` | SSO Auth/Session |

Sign-in payload includes `clientId=cyan-panel`, username, password,
`captchaChallengeId`, `captchaAnswer`, optional `otpCode`, and
`deviceId=panel-web`.

Current UX concerns:

- form errors render raw backend response text
- captcha expiry is not shown as a countdown
- OTP is supported by the API type but not presented in the UI
- workspace entered during registration is stored only in browser preferences
- the phone field visually hardcodes `+1`
- tokens are stored in local storage

### `/` — dashboard

User goal:

- see the most recent generated app
- understand live workspace counts and recent activity
- continue an existing draft or start a new one

Layout:

- large latest-draft hero
- capability cards linking to major workspaces
- draft, flow, bot-channel, and delivery statistics
- generated-draft summary rail
- recent activity rail

API calls on load:

| Method and backend path | Purpose |
|---|---|
| `GET /endpoint/ai-orchestrator/drafts?tenantKey=tenant-demo&siteKey=site-commerce` | Draft count/latest draft |
| `GET /endpoint/bot-adapter/integrations?...` | Bot-channel counts |
| `GET /endpoint/bot-adapter/messages?...` | Delivery counts/activity |
| `GET /endpoint/bpm/flows` with tenant/site headers | Flow counts/activity |

All requests use `Promise.allSettled`; partial data can render when one service
fails.

Current UX concerns:

- scope is hardcoded and ignores the visible workspace/site preferences
- the overflow `...` hero button has no action
- cards mix application capabilities and navigation concepts
- errors are concatenated into one small status line
- recent activity has no pagination or detail links

### `/projects/new` — AI Studio

User goal:

- describe an application conversationally
- select an app-generation mode
- attach reference files
- answer AI follow-up questions
- inspect the generated DSL, services, and publish readiness
- continue into Maker or a persisted draft

Desktop layout:

- chat history and response cards
- quick-prompt chips for Shop, CRM, BPM, Telegram, Landing Page, and PWA
- prompt composer with attachment and enhancement actions
- generation mode selector: Smart, Shop, Website, CRM, or BPM
- DSL, service, readiness, and preview summary cards
- generated module summary
- recent generation list
- inspector modal for DSL, services, or checklist

Mobile layout:

- independently rendered chat thread and compact composer
- only the first four quick prompts
- no full desktop summary rail

API calls:

| Action | Method and backend path |
|---|---|
| Load blueprints | `GET /endpoint/ai-orchestrator/blueprints` |
| Load drafts | `GET /endpoint/ai-orchestrator/drafts?tenantKey=tenant-demo&siteKey=site-commerce` |
| Generate | `POST /endpoint/ai-orchestrator/generate/app` |
| Prepare attachment metadata | `POST /internal/media/assets/prepare-upload` on `media-service` |
| Optional realtime generate | WebSocket from `NEXT_PUBLIC_AI_STUDIO_WS_URL` |

Generation requests include the prompt, selected `appType`, tenant/site,
conversation `sessionId`, `execute=false`, answers, locale/channels, and the
available service inventory.

Attachment behavior is currently metadata-only:

- multiple files can be selected
- the panel calls media `prepare-upload`
- it does not upload the file bytes to returned storage
- asset metadata is appended to the AI prompt

Preview behavior is also provisional. The panel constructs:

```text
https://preview.cyan.app/{siteKey}/{appKey}
```

It does not verify that the preview exists.

Current UX concerns:

- three seeded chat messages appear even when no backend session exists
- backend messages are not persisted through the conversation-session API here
- readiness is a client-side formula, not a backend validation result
- service count is derived from API/flow counts rather than actual services
- attachment progress does not represent byte upload progress
- desktop and mobile experiences can drift
- generated errors are raw strings

### `/projects` — blueprints and saved drafts

User goal:

- browse application blueprints
- inspect blueprint capabilities and DSL hints
- generate and provision a draft
- review recently saved drafts

Layout:

- search and filter toolbar
- category chips
- blueprint cards
- saved-drafts table
- selected-blueprint detail rail
- separate mobile blueprint grid and bottom sheet

Functional controls:

- select a blueprint
- generate a draft from a blueprint
- automatically attempt provisioning when the returned draft is `READY` or
  `DRAFT`

Placeholder controls:

- search input
- tag/complexity/category filters
- Preview button
- mobile Generate from blueprint button

API calls:

| Action | Method and backend path |
|---|---|
| List blueprints | `GET /endpoint/ai-orchestrator/blueprints` |
| List drafts | `GET /endpoint/ai-orchestrator/drafts` |
| Create draft | `POST /endpoint/ai-orchestrator/drafts` |
| Provision generated draft | `POST /endpoint/ai-orchestrator/drafts/{draftId}/provision` |

Current UX concerns:

- blueprint cards contain nested `<button>` elements
- provisioning errors are intentionally swallowed after draft creation
- the saved-draft rows are not links
- filtering controls are visual only
- only six blueprints and four drafts are shown

### `/projects/[projectId]` — project detail

User goal:

- inspect one AI-generated draft
- review its prompt, scope, status, capabilities, questions, endpoints, and DSL
- inspect and start provisioning runs
- open linked conversation sessions

Layout:

- draft timeline
- delivery API list
- manual actions
- linked conversation sessions
- DSL count summary
- provisioning run panel
- complete raw DSL JSON

API calls:

| Action | Method and backend path |
|---|---|
| Load draft | `GET /endpoint/ai-orchestrator/drafts/{projectId}` |
| Load linked sessions | `GET /endpoint/ai-orchestrator/sessions?draftId={projectId}` |
| Load provisioning runs | `GET /endpoint/ai-orchestrator/drafts/{projectId}/runs` |
| Provision | `POST /endpoint/ai-orchestrator/drafts/{projectId}/provision` |

Current UX concerns:

- uses the older `AppShell` and English-only page copy
- loading, missing, and error states share the same text-only panel
- provisioning does not poll running jobs
- raw DSL dominates the lower page
- conversation links open `/bot/[sessionId]`, which is a read-only detail page

### `/maker` — unified entity, form, and flow maker

User goal:

- choose a dynamic service and template
- create or update an entity/form definition
- inspect fields
- connect the entity definition to a BPM state
- configure state actions and transition conditions
- save a linked BPM flow
- ask AI to generate entity/flow/automation drafts

Desktop layout:

- service/template selectors
- definition list and field summary
- JSON definition editor
- AI generation prompt/banner
- flow selector and state list
- selected state mapping editor
- action selector/list
- transition and condition editor
- AI draft summary

Mobile layout:

- field list
- linked-flow summary
- save flow/form action
- most desktop editing capabilities are unavailable

Dynamic entity API calls:

| Action | Method and backend path |
|---|---|
| List definitions | `GET /endpoint/entities/definitions` |
| List templates | `GET /endpoint/entities/templates` |
| Create from template | `POST /endpoint/entities/templates/{templateKey}/definitions` |
| Update definition | `PUT /endpoint/entities/definitions/{entityKey}` |

These calls target the currently selected dynamic service through:

```text
/api/platform/dynamic/{serviceKey}
```

BPM API calls:

| Action | Method and backend path |
|---|---|
| List flows | `GET /endpoint/bpm/flows` |
| List action structures | `GET /endpoint/bpm/metadata/state-actions` |
| Get condition structures | `GET /endpoint/bpm/metadata/transition-conditions` |
| Save flow | `POST /endpoint/bpm/flows` |

AI API call:

```text
POST /endpoint/ai-orchestrator/generate/app
```

When Sync entity to state is used, the panel sets:

- `formKey`
- `entityKey`
- `entityService`
- `rendererKey`
- `rendererService`
- `submitMode=DYNAMIC`

Current UX concerns:

- the primary schema editor is raw JSON
- there is no visual field editor for nested objects/lists/validation rules
- state actions are added with generic placeholder params, not metadata-driven
  forms
- `processorKey`, roles, assignments, comments, attachments, automation params,
  and most state fields are not editable
- no interactive graph or drag-and-drop state/edge editing
- no unsaved-change protection
- no schema diff, version history, or validation preview
- create-from-template uses the template key as the entity key, which can collide
- mobile is primarily read-only

### `/data` — dynamic record manager

User goal:

- browse records for common business entities
- create demo records
- inspect a record
- apply a predefined update

Configured buckets:

| Label | Service | Entity/template key |
|---|---|---|
| Products | Catalog | `catalog-product` |
| Contents | Content | `landing-page` |
| Customers | CRM | `crm-contact` |
| Inventory | Inventory | `stock-item` |

API calls:

| Action | Method and backend path |
|---|---|
| Count/load bucket records | `GET /endpoint/entities/records/{entityKey}` |
| Ensure definition for demo create | `POST /endpoint/entities/templates/{templateKey}/definitions` |
| Create demo record | `POST /endpoint/entities/records/{entityKey}` |
| Apply predefined edit | `PATCH /endpoint/entities/records/{entityKey}/{recordKey}` |

Functional controls:

- switch bucket
- create a hardcoded example record
- update the first record with a hardcoded transformation

Placeholder controls:

- search
- filters
- import
- export
- preview
- row selection; the first record is always treated as active
- sales/inventory insight cards

Current UX concerns:

- table columns are product-specific even for content, CRM, and inventory
- record creation is not generated from the entity definition
- no form-level validation feedback
- no delete, pagination, sorting, bulk selection, or relation UI
- changing bucket can leave a stale status message
- fixed demo payloads can fail when definitions evolve

### `/flows` — BPM flow builder and cartable

User goal:

- browse BPM flow definitions
- generate a flow draft with AI
- save or activate a flow
- start a managed object
- inspect assigned/visible work
- inspect transition availability and runtime payloads

Layout:

- flow metrics
- flow catalog
- custom state canvas
- transition table
- action/condition metadata summary
- assigned-to-me list
- selected managed-object detail
- separate limited mobile flow path

API calls on load:

| Method and backend path | Purpose |
|---|---|
| `GET /endpoint/bpm/flows` | Flow catalog |
| `GET /endpoint/bpm/metadata/state-actions` | Supported state actions |
| `GET /endpoint/bpm/metadata/transition-conditions` | Supported condition operators |
| `GET /endpoint/bpm/managed-objects/assigned-to-me` | Personal cartable |
| `GET /endpoint/bpm/managed-objects/visible-to-me` | Visible queue |

Action calls:

| Action | Method and backend path |
|---|---|
| Save | `POST /endpoint/bpm/flows` |
| Activate | `POST /endpoint/bpm/flows/{flowKey}/activate/{version}` |
| Start object | `POST /endpoint/bpm/managed-objects` |
| Load transition options | `GET /endpoint/bpm/managed-objects/{objectId}/transitions` |
| AI flow draft | `POST /endpoint/ai-orchestrator/generate/app` |

The shared BPM client also implements active-form loading/submission and managed
object transitions, but this page does not expose those actions:

```text
GET  /endpoint/bpm/managed-objects/{id}/active-form
POST /endpoint/bpm/managed-objects/{id}/active-form/submissions
POST /endpoint/bpm/managed-objects/{id}/transitions
```

Current UX concerns:

- the page is called a builder but does not edit states or edges
- canvas nodes are layout boxes without connectors or interaction
- transition options are displayed but cannot be executed
- active forms cannot be rendered or submitted
- comments, attachments, assignment, and transition history are absent
- starting an object sends only a small fixed payload
- saving with no flow silently creates a hardcoded purchase-order starter

### `/integrations` — client apps, bots, and mini apps

User goal:

- list Telegram/Bale bot integrations
- inspect delivery history
- send a test message
- register a webhook
- create and publish a mini-app build

API calls:

| Action | Method and backend path |
|---|---|
| List integrations | `GET /endpoint/bot-adapter/integrations?...` |
| Create/update integration | `POST /endpoint/bot-adapter/integrations` |
| Register webhook | `POST /endpoint/bot-adapter/integrations/{channel}/{integrationKey}/register-webhook` |
| List messages | `GET /endpoint/bot-adapter/messages?...` |
| Send test message | `POST /endpoint/bot-adapter/messages` |
| List mini apps | `GET /endpoint/bot-adapter/mini-apps?...` |
| Create/update mini app | `POST /endpoint/bot-adapter/mini-apps` |
| Publish mini app | `POST /endpoint/bot-adapter/mini-apps/{channel}/{integrationKey}/{buildKey}/publish` |

Current Add channel behavior creates a fixed Telegram integration:

- integration key `telegram-main`
- bot username `@cyan_assistant_bot`
- Vault reference `vault://bots/retail-demo`
- fixed preview URL

Current test behavior sends a fixed order-confirmation message to `@john_doe`,
then attempts webhook registration.

Current UX concerns:

- there is no real add/edit channel form
- sensitive token references are displayed in plain text
- no confirmation before webhook registration/publish
- errors from refreshing all three resources are all-or-nothing
- delivery retry API exists but is not exposed
- no inbound message/session mapping UI
- “Channel Settings,” “Session Mapping,” and “Provisioning” chips are not tabs

### `/bot` — bot experience

User goal:

- compare live Telegram and Bale channel state
- inspect recent outbound messages
- inspect published mini-app readiness
- jump to the integration workspace

API calls:

```text
GET /endpoint/bot-adapter/integrations
GET /endpoint/bot-adapter/messages
GET /endpoint/bot-adapter/mini-apps
```

This page is read-only. It presents channel cards, message streams, capability
summaries, and delivery counts.

Current UX concerns:

- it is an operational summary, not an interactive bot conversation experience
- it does not list AI conversation sessions
- it duplicates information from `/integrations`
- the relationship between outbound delivery and AI sessions is not explained

### `/bot/[sessionId]` — AI conversation session detail

User goal:

- inspect one persistent AI conversation
- review extracted answers and pending questions
- see the linked draft and session metadata

API call:

```text
GET /endpoint/ai-orchestrator/sessions/{sessionId}
```

This page is read-only. The “Resume in bot studio” link goes to `/bot`, but that
page does not currently resume or append to the selected session.

Current UX concerns:

- uses the older English-only `AppShell`
- no composer or resume action
- no channel-specific visual treatment
- date formatting is browser-locale dependent
- active navigation highlights Client Apps/Bots instead of Bot Experience

### `/site-builder` — storefront route builder

User goal:

- list storefront routes
- create/edit a route key, title, and path
- save as draft or publish
- resolve and render a public storefront path
- inspect the target entity and rendered result

API calls:

| Action | Method and backend path |
|---|---|
| List routes | `GET /endpoint/entities/records/site-route` on Storefront |
| Ensure route definition | `POST /endpoint/entities/templates/site-route/definitions` |
| Save/publish route | `POST /endpoint/entities/records/site-route` |
| Resolve public route | `GET /public/storefront/resolve?path={path}` |
| Render public route | `GET /public/storefront/render?path={path}` |

Persisted route data contains:

- path and route type
- target entity reference
- navigation metadata
- SEO metadata
- rendering/theme metadata
- indexing and sitemap settings
- publication status

Current UX concerns:

- most route properties are generated constants and cannot be edited
- Add page immediately fills fixed values rather than opening a deliberate flow
- preview is a stylized panel, not the returned HTML in an isolated frame
- raw target JSON is rendered as paragraph text
- no component/block editing
- no asset picker
- Content, SEO, and Rendering chips are not tabs
- no route conflict, slug, or publish validation

### `/search` — search builder

User goal:

- create an index definition
- identify the source service/entity
- trigger synchronization
- query search and suggestions

API calls:

| Action | Method and backend path |
|---|---|
| Ensure index templates | `POST /endpoint/entities/templates/{template}/definitions` |
| List index definitions | `GET /endpoint/entities/records/index-definition` |
| Save index definition | `POST /endpoint/entities/records/index-definition` |
| Start source sync | `POST /endpoint/search-index/sync/{sourceServiceKey}/{sourceEntityKey}` |
| Search | `GET /endpoint/search-index/search?...` |
| Suggest | `GET /endpoint/search-index/suggest?...` |

Important current behavior: opening the page attempts to create the
`index-definition` and `search-document` definitions. Page load is therefore
not purely read-only.

Current UX concerns:

- navigation labels this route “Media”
- uses older `AppShell` and English-only copy
- search results and suggestions are raw JSON
- index fields/analyzers are fixed in code
- clicking a definition updates only `indexKey`
- no sync progress or indexed-document counts

### `/automation` — automation and scheduled batch builder

User goal:

- start sync or async automation execution
- inspect, refresh, or cancel an execution
- save a durable batch definition
- activate a scheduled automation flow for that batch
- run a batch immediately and inspect counts

Automation execution APIs:

| Action | Method and backend path |
|---|---|
| Start | `POST /endpoint/automation-orchestrator/executions/start` |
| Refresh | `GET /endpoint/automation-orchestrator/executions/{executionId}` |
| Cancel | `POST /endpoint/automation-orchestrator/executions/{executionId}/cancel` |

Automation definition APIs used for scheduling:

```text
GET  /endpoint/automation-flows
POST /endpoint/automation-flows
POST /endpoint/automation-flows/{flowKey}/versions/{version}/SUBMIT
POST /endpoint/automation-flows/{flowKey}/versions/{version}/APPROVE
POST /endpoint/automation-flows/{flowKey}/versions/{version}/ACTIVATE
```

Batch APIs:

```text
POST /endpoint/batch/definitions
POST /endpoint/batch/definitions/{definitionKey}/runs
GET  /endpoint/batch/runs?limit=50
```

Current UX concerns:

- navigation labels this route “Analytics”
- the primary experience is raw JSON forms
- no node palette, graph, edge editor, credential picker, expression editor, or
  node metadata browser
- scheduling creates a fixed three-node graph
- activating a schedule performs three lifecycle mutations without a review step
- no confirmation before cancellation or activation
- no polling, log timeline, node-level execution result, or retry action
- no validation before JSON parsing

### `/notifications` — notification builder

User goal:

- create a notification template
- send a synchronous test notification
- inspect the saved message record

API calls:

| Action | Method and backend path |
|---|---|
| Ensure template/message definitions | `POST /endpoint/entities/templates/{template}/definitions` |
| List templates | `GET /endpoint/entities/records/notification-template` |
| Save template | `POST /endpoint/entities/records/notification-template` |
| Dispatch | `POST /endpoint/notifications/send` |
| Read dispatched message | `GET /endpoint/notifications/messages/{messageKey}` |

Important current behavior: opening the page attempts to create both dynamic
definitions.

Current UX concerns:

- hidden from navigation
- older English-only shell
- channel/provider are free-text inputs
- recipient defaults to an external example webhook
- model is raw JSON with no validation preview
- no template render preview, provider health, history table, or retry

### `/commerce` — commerce and payment builder

User goal:

- seed cart, checkout, promotion, tax, and payment-method data
- initiate a payment session
- inspect raw backend results

API calls:

| Action | Service/API |
|---|---|
| Ensure definitions | Dynamic template-definition APIs on Cart, Checkout, Pricing |
| Load/create carts | Dynamic records on `cart-service` |
| Load/create checkouts | Dynamic records on `checkout-service` |
| Load/create promotions and tax | Dynamic records on `pricing-promotion-service` |
| List/create payment methods | `/endpoint/payment/admin/methods` |
| Initiate session | `POST /endpoint/payment-orchestrator/sessions/initiate` |

Important current behavior: page load attempts to create four dynamic definitions.
“Seed commerce runtime” writes fixed demo records and a sandbox payment method.

Current UX concerns:

- hidden from navigation
- older English-only shell
- this is a developer seeding tool rather than an operator commerce UI
- all results are raw JSON
- hardcoded addresses, customer, amounts, URLs, and provider
- no order list, cart detail, checkout timeline, payment state machine, refund,
  capture, or reconciliation UI

### `/iam` — profile and settings

User goal:

- review the signed-in user
- review resolved panel access
- change displayed workspace/site labels
- sign out

API calls:

| Action | Method and backend path |
|---|---|
| Load user | `GET /api/sso/users/{username}` |
| Resolve access | `GET /api/sso/iam/users/{username}/access?clientId=cyan-panel` |
| Logout | `POST /api/sso/auth/logout` |

Workspace/site preferences are written only to local storage. They do not create
or select tenant/site resources and do not change most API request scopes.

The shared service client contains additional IAM administration methods for
realms, clients, roles, memberships, assignments, and managed user provisioning,
but this page does not expose them.

Current UX concerns:

- navigation calls the page Settings while its primary content is Profile
- access is raw JSON
- no editable profile, password change, MFA, sessions, devices, realm, role, or
  membership management
- no warning that workspace/site values are cosmetic

### `/qa` — integration smoke harness

User goal:

- run read-only live checks against critical panel dependencies
- inspect returned payloads

Checks run sequentially:

1. AI drafts registry
2. Storefront resolve
3. Storefront render
4. BPM flows
5. BPM action metadata
6. BPM managed objects
7. Bot integrations
8. Bot outbound deliveries

API paths are the same list/get calls documented for those pages.

Current UX concerns:

- hidden from navigation
- older English-only shell
- large raw payloads dominate the results
- no duration, HTTP status, retry-one, export, environment, or historical runs
- sequential execution increases total wait time

### `/roadmap` — product roadmap

User goal:

- compare planned product tracks with live workspace counts

Live signals:

```text
GET /endpoint/ai-orchestrator/drafts
GET /endpoint/bpm/flows
GET storefront-service /endpoint/entities/definitions
GET catalog-service /endpoint/entities/definitions
GET /endpoint/bot-adapter/integrations
GET /endpoint/bot-adapter/mini-apps
```

The page combines those counts with static roadmap data from
`lib/product-roadmap.ts`.

Current UX concerns:

- hidden from navigation except for a dashboard “View all” link
- older English-only shell
- product-planning content is mixed into the operational product
- live counts do not prove roadmap completion

## Internal Next.js API routes

These routes exist inside `panel-web`; they are not microservice routes.

### Service proxies

```text
/api/sso/[...path]
/api/platform/service/[serviceKey]/[...path]
/api/platform/dynamic/[serviceKey]/[...path]
```

They forward supported methods and return backend response text/status with the
backend content type.

### Project registry compatibility routes

```text
GET  /api/projects
POST /api/projects
GET  /api/projects/{projectId}
PUT  /api/projects/{projectId}
```

These adapt the older `ProjectDraft` shape to AI Orchestrator drafts. Current
main pages call the AI Orchestrator client directly, so this compatibility layer
is not the primary project UI path.

### Bot-session compatibility routes

```text
GET   /api/bot-sessions
POST  /api/bot-sessions
GET   /api/bot-sessions/{sessionId}
PATCH /api/bot-sessions/{sessionId}
POST  /api/bot-sessions/{sessionId}/messages
```

These adapt the older bot-session shape to AI Orchestrator conversation sessions.
The current `/bot` page does not use this compatibility client.

## Shared dynamic entity API

`lib/dynamic-api.ts` provides the shared CRUD contract used by Maker, Data,
Site Builder, Search, Notifications, Commerce, QA, and Roadmap.

```text
GET    /endpoint/entities/templates
GET    /endpoint/entities/definitions
POST   /endpoint/entities/templates/{templateKey}/definitions
PUT    /endpoint/entities/definitions/{entityKey}
GET    /endpoint/entities/records/{entityKey}
POST   /endpoint/entities/records/{entityKey}
PUT    /endpoint/entities/records/{entityKey}/{recordKey}
PATCH  /endpoint/entities/records/{entityKey}/{recordKey}
DELETE /endpoint/entities/records/{entityKey}/{recordKey}
```

Requests should carry tenant/site scope. Definition responses contain structured
`definition`, while records contain structured `data`.

## Environment variables

### Browser-visible variables

| Variable | Purpose |
|---|---|
| `NEXT_PUBLIC_AI_STUDIO_WS_URL` | Optional AI Studio WebSocket endpoint |
| `NEXT_PUBLIC_AVAILABLE_SERVICE_KEYS` | Comma-separated service inventory sent with AI requests |
| `NEXT_PUBLIC_PLATFORM_API_BASE_URL` | Legacy/direct base fallback; current primary calls use same-origin BFF routes |

Default AI-visible service inventory:

```text
ai-orchestrator-service
notification-service
bpm-service
automation-orchestrator-service
report-service
sso-auth-service
sso-user-service
sso-captcha-service
media-service
processor-service
```

Add `batch-worker-service` when the deployment supports durable ETL generation.

### Server-only service URLs

| Variable | Local default |
|---|---|
| `SSO_AUTH_SERVICE_BASE_URL` | `http://localhost:9001` |
| `SSO_USER_SERVICE_BASE_URL` | `http://localhost:9002` |
| `SSO_CAPTCHA_SERVICE_BASE_URL` | `http://localhost:9003` |
| `SSO_OTP_SERVICE_BASE_URL` | `http://localhost:9004` |
| `SSO_SESSION_SERVICE_BASE_URL` | `http://localhost:9005` |
| `SSO_FIDO_SERVICE_BASE_URL` | `http://localhost:9006` |
| `CONTENT_SERVICE_BASE_URL` | `http://localhost:9101` |
| `CATALOG_SERVICE_BASE_URL` | `http://localhost:9102` |
| `CRM_SERVICE_BASE_URL` | `http://localhost:9103` |
| `COMMERCE_SERVICE_BASE_URL` | `http://localhost:9104` |
| `FINANCE_SERVICE_BASE_URL` | `http://localhost:9105` |
| `INVENTORY_SERVICE_BASE_URL` | `http://localhost:9106` |
| `REPORT_SERVICE_BASE_URL` | `http://localhost:9107` |
| `PAYMENT_SERVICE_BASE_URL` | `http://localhost:9114` |
| `STOREFRONT_SERVICE_BASE_URL` | `http://localhost:9115` |
| `MEDIA_SERVICE_BASE_URL` | `http://localhost:9116` |
| `CART_SERVICE_BASE_URL` | `http://localhost:9117` |
| `CHECKOUT_SERVICE_BASE_URL` | `http://localhost:9118` |
| `BPM_SERVICE_BASE_URL` | `http://localhost:9119` |
| `AUTOMATION_ORCHESTRATOR_SERVICE_BASE_URL` | `http://localhost:9120` |
| `AI_ORCHESTRATOR_SERVICE_BASE_URL` | `http://localhost:9121` |
| `NOTIFICATION_SERVICE_BASE_URL` | `http://localhost:9122` |
| `PAYMENT_ORCHESTRATOR_SERVICE_BASE_URL` | `http://localhost:9123` |
| `PRICING_PROMOTION_SERVICE_BASE_URL` | `http://localhost:9124` |
| `SEARCH_INDEX_SERVICE_BASE_URL` | `http://localhost:9125` |
| `BOT_ADAPTER_SERVICE_BASE_URL` | `http://localhost:9126` |
| `BATCH_WORKER_SERVICE_BASE_URL` | `http://localhost:9127` |

## Known UI/UX issues

These are current implementation problems, not desired product behavior.

### Information architecture

- `/search` is labeled Media even though it is Search Builder.
- `/automation` is labeled Analytics even though it is Automation Builder.
- Notifications, Commerce, QA, and Roadmap are hidden from navigation.
- Bot Experience and Client Apps/Bots overlap without a clear mental model.
- `/iam` combines profile, local preferences, and access debugging.
- the product mixes end-user operator pages with developer/demo seeding tools.

### Tenant and site scope

- most pages hardcode `tenant-demo` and `site-commerce`
- header workspace/site controls are not selectors
- values saved in Settings do not update most API calls
- there is no shared scope context with stable tenant/site IDs

### Shell and layout consistency

- pages use both `PanelShell` and the older `AppShell`
- older pages have English-only headings even in Farsi mode
- several major pages maintain separate desktop/mobile markup
- mobile features are often a reduced read-only subset
- hidden routes often highlight Dashboard or no navigation item

### Interaction quality

- multiple visible controls are placeholders
- mutations frequently use hardcoded demo payloads
- some pages mutate backend definitions during initial page load
- raw JSON is used where structured editors, summaries, or timelines are needed
- there is no common toast, dialog, loading skeleton, empty-state, or error-state
  component
- errors frequently expose raw backend response bodies
- no standardized confirmation for publish, activate, cancel, or external calls
- no dirty-form protection or undo

### Data presentation

- tables are not responsive data grids
- no shared pagination, sorting, filtering, column management, or bulk actions
- current record detail selection is incomplete in Data Manager
- timestamps and status colors are inconsistent
- some summary metrics are client-derived approximations
- preview experiences are mocked rather than isolated real renders

### Accessibility

- several icon-only buttons use text glyphs instead of a consistent icon system
- some controls have no visible focus/selected-state design
- Blueprints contains nested interactive buttons
- visual chips are sometimes presented as if they were tabs or filters
- raw JSON blocks can create long, difficult keyboard/reader experiences
- responsive pages need keyboard, focus trap, dialog, and reduced-motion review

## Recommended redesign order

1. Establish the product information architecture and correct route names.
2. Create one authenticated shell with real tenant/site selection.
3. Create shared design tokens and primitives: buttons, fields, tabs, dialogs,
   drawers, toasts, skeletons, empty/error states, data grids, JSON/code viewer,
   and permission state.
4. Redesign AI Studio because it is the primary creation entry point.
5. Unify Maker and Flow Builder around definition/state/action metadata.
6. Redesign Data Manager from live entity definitions rather than fixed buckets
   and columns.
7. Unify Client Apps/Bots and Bot Experience or clearly separate configuration
   from operations.
8. Build the Site Builder around real route/page/block editing and real preview.
9. Replace raw Automation JSON with a graph and metadata-driven node forms.
10. Decide whether Search, Notifications, Commerce, QA, and Roadmap are product
    pages, admin tools, or developer tools, then place them accordingly.

## Redesign acceptance checklist

Every redesigned route should specify and implement:

- primary persona and job-to-be-done
- primary and secondary actions
- route title and navigation location
- tenant/site scope behavior
- required permissions
- desktop, tablet, and mobile layout
- English and Farsi copy
- LTR and RTL behavior
- light and dark themes
- initial loading, refresh loading, empty, partial, error, success, disabled, and
  permission-denied states
- API calls and mutation confirmations
- optimistic or pessimistic update behavior
- validation messages
- keyboard and screen-reader behavior
- analytics events, if required
- test IDs only where stable semantic selectors are insufficient

## Source map

| Area | Source |
|---|---|
| Global layout | `app/layout.tsx` |
| Global styles/tokens | `app/globals.css` |
| Authenticated shell/navigation | `components/panel-shell.tsx` |
| Legacy shell wrapper | `components/app-shell.tsx` |
| Locale/theme/workspace state | `components/panel-provider.tsx` |
| Auth and refresh | `lib/platform-auth.ts` |
| AI and bot adapter APIs | `lib/platform-api.ts` |
| Dynamic entity APIs | `lib/dynamic-api.ts` |
| BPM APIs/types | `lib/bpm-api.ts` |
| Notification/search/automation/payment/IAM APIs | `lib/service-api.ts` |
| Storefront APIs | `lib/storefront-api.ts` |
| Media upload preparation | `lib/media-api.ts` |
| Domain DTOs | `lib/types.ts` |
| Service inventory | `lib/platform-service-inventory.ts` |
| Static roadmap | `lib/product-roadmap.ts` |

## Local development

```bash
cd panel-web
npm install
npm run dev
```

Default Next.js development URL:

```text
http://localhost:3000
```

Useful checks:

```bash
npm run lint
npm run build
npm run test:e2e
```

Run the BFF target services needed by the page being tested. The browser uses
same-origin `/api/**` routes, while the Next.js server needs access to the
service URLs listed above.

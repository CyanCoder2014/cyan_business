# AI App/Web/Bot Maker Roadmap

## Product Position
Cyan should be sold as a mobile-friendly AI business app maker: a user can create a complete website, PWA, shop, CRM, BPM/form system, automation flow, or Telegram/Bale bot from a prompt or from ready blueprints, then edit everything manually in the panel.

## Frontend Surfaces
- `panel-web`: admin/maker panel for AI chat, drafts, entity structure, data management, automation, and client app/bot integrations.
- Public presentation side: SEO-facing website/PWA rendered through `storefront-service` public routes.
- Bot presentation side: Telegram and Bale adapters using `ai-orchestrator-service` draft/session/provisioning APIs.
- Next phase: Telegram mini app and mobile app shells after the bot and PWA contracts are stable.

## Panel Workspaces
- AI Studio: prompt-based creation and modification through `POST /endpoint/ai-orchestrator/generate/app`.
- Blueprint editor: select seeded or saved drafts and review DSL before execution.
- Maker Part 1: edit definitions, fields, validations, relations, and tenant/site scope.
- Maker Part 2: manage records for content, products, CRM, commerce, finance, inventory, and reports.
- Flow Builder: manage BPM states, submit forms, automation actions, and event fan-out rules.
- Client Apps/Bots: manage website/PWA, Telegram, Bale, future mini app, and mobile app connections.

## Backend Status
The current backend already has the main contracts for this phase:
- `api-gateway` routes AI orchestration, public storefront, BPM, notification, payment, media, and search paths.
- `ai-orchestrator-service` owns app generation, blueprints, drafts, sessions, and provisioning runs.
- `ai-orchestrator-service` sessions are listable by tenant/site/client/draft so bot and panel conversations can be resumed.
- `storefront-service` owns public route resolution, HTML rendering, sitemap, and robots output.
- Dynamic services expose strict definition/record APIs through `dynamic-entity-core`.
- `panel-web` proxies service-owned `/endpoint/entities/**` APIs to each local dynamic service for Maker/Data workspaces.
- `bot-adapter-service` owns Telegram/Bale webhook ingestion, channel integration mappings, chat-to-session continuity, and idempotent inbound message processing.
- Automation should continue to fan out through `event-service`.

## Backend Gaps To Avoid Before Market
- Do not create a second draft registry in the frontend for production; `panel-web` should keep using `ai-orchestrator-service` drafts/sessions as the source of truth and only fall back locally in development.
- Store actual Telegram/Bale bot tokens only in a secret manager; `bot-adapter-service` stores `tokenSecretRef`, not token values.
- Add a presentation-template registry if storefront themes need versioned reusable templates beyond dynamic records.
- Add publish-state and domain-binding workflows before promising custom domains.

## Resume Roadmap 1, 2, 3, 4
### 1. Visual website/page builder
- Current repo baseline: `panel-web/app/site-builder/page.tsx` already creates `landing-page`, `theme-layout`, and `site-route` records.
- Next execution slice: move from record publishing to reusable sections, theme presets, preview/publish split, and domain-ready route workflow.
- Service boundary: `content-service` owns page records, `storefront-service` owns theme and route records, `media-service` owns assets, `search-index-service` owns discovery sync.
- Exit gate: an operator can publish a tenant/site-scoped landing page without hand-editing JSON and verify it through `GET /public/storefront/render?path=/`.

### 2. Outbound Telegram/Bale messaging
- Current repo baseline: `bot-adapter-service` already owns webhook ingestion, channel integrations, session mapping, and idempotent inbound storage.
- Next execution slice: add outbound provider delivery APIs, secure token-secret usage, delivery retries, and operator-facing health/status.
- Service boundary: `bot-adapter-service` owns provider delivery and session continuity; `ai-orchestrator-service`, `notification-service`, and `bpm-service` become callers, not token owners.
- Exit gate: Telegram and Bale can receive workflow or AI replies with observable delivery status and no token leakage.

### 3. Advanced form/flow builder
- Current repo baseline: `panel-web/app/maker/page.tsx` builds dynamic definitions and `panel-web/app/flows/page.tsx` publishes BPM flow JSON.
- Next execution slice: connect maker fields directly to BPM form/state contracts, transition conditions, action presets, and managed-object lifecycle tooling.
- Service boundary: `dynamic-entity-core` remains the strict schema/record layer, `bpm-service` owns workflow state, and automation fan-out must still go through `event-service`.
- Exit gate: a form can be designed, published, attached to a flow, submitted, and advanced across at least one approval path with service-matching validation.

### 4. End-to-end test harness and market-readiness checklist
- Current repo baseline: this document already lists a market-ready gate, but it is still a manual checklist.
- Next execution slice: script smoke paths for app generation, storefront publish/render, bot integration save, bot session continuity, and BPM flow publish; then turn mobile/PWA/Farsi-English checks into explicit pass/fail gates.
- Service boundary: `panel-web` owns the UX gate, while `api-gateway`, `ai-orchestrator-service`, `storefront-service`, `bpm-service`, and `bot-adapter-service` provide the contracts being verified.
- Exit gate: market-readiness claims are blocked unless panel build/lint, target backend tests, and smoke routes pass.

## Suggested Execution Order
1. Harden the website/page builder enough to publish stable public routes.
2. Finish outbound Telegram/Bale messaging so published apps can also operate as bot channels.
3. Merge form and flow builder contracts so structured apps can collect and route data without manual JSON surgery.
4. Freeze the release gate with an automated harness before claiming market readiness.

## Market-Ready Test Gate
- Build `panel-web` and run lint/type checks.
- Run `./gradlew test` or at least targeted tests for `ai-orchestrator-service`, `storefront-service`, `bpm-service`, and `api-gateway`.
- Smoke test gateway calls for `POST /endpoint/ai-orchestrator/generate/app`, `GET /public/storefront/render?path=/`, and bot session endpoints.
- Test mobile viewport, PWA manifest, light/dark mode, and Farsi/English direction switching.

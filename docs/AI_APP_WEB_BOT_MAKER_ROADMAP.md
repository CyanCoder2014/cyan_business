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

## Market-Ready Test Gate
- Build `panel-web` and run lint/type checks.
- Run `./gradlew test` or at least targeted tests for `ai-orchestrator-service`, `storefront-service`, `bpm-service`, and `api-gateway`.
- Smoke test gateway calls for `POST /endpoint/ai-orchestrator/generate/app`, `GET /public/storefront/render?path=/`, and bot session endpoints.
- Test mobile viewport, PWA manifest, light/dark mode, and Farsi/English direction switching.

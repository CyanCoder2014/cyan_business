# Cyan Panel Web

Next.js control panel for the structured app-maker flow.

## What this workspace is for

- generate app drafts from prompts
- store app drafts in a backend registry
- review generated DSL before execution
- inspect provisioning output
- keep a browser-local draft cache for quick reopening
- provide the UI surface for future Telegram/Bale bot adapters

## Runtime contract

The UI expects the orchestrator on:

- `NEXT_PUBLIC_PLATFORM_API_BASE_URL`

Default:

- `http://localhost:8001`

Primary endpoint:

- `POST /endpoint/ai-orchestrator/generate/app`

Panel routes:

- `/`
- `/projects`
- `/projects/new`
- `/bot`

## Local run

```bash
cd panel-web
npm install
npm run dev
```

## Current scope

- dashboard landing
- app studio
- draft/project detail view
- backend API client for the AI orchestrator
- browser-local draft cache
- mobile-first maker/data/flow/integration workspaces
- PWA manifest and installable panel metadata
- Farsi/English direction toggle and light/dark UI toggle
- backend-first project registry with local development fallback
- dynamic service proxy for Maker/Data access to `/endpoint/entities/**`

## Next step

Finish production bot adapters: Telegram/Bale webhook ingestion, token storage, tenant mapping, and idempotent message processing.

See `../docs/AI_APP_WEB_BOT_MAKER_ROADMAP.md` for the market roadmap and backend gaps.

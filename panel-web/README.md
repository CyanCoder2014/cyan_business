# Naviya Panel Web

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

- `http://localhost:8080`

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

## Next step

Replace the browser-local draft cache with a real backend draft registry so reopened projects do not require a fresh AI call.

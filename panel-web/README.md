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

For local compatibility with legacy routes:

- `NEXT_PUBLIC_PLATFORM_API_BASE_URL`
- `NEXT_PUBLIC_AVAILABLE_SERVICE_KEYS` (optional comma-separated deployment inventory)

Default:

- `http://localhost:8001`

When the inventory variable is omitted, AI request bodies advertise the lightweight
panel deployment: AI orchestrator, notification, BPM, automation, report, SSO
auth/user/captcha, media, and processor. Add `batch-worker-service` for durable
high-volume ETL generation, or override the complete list for another deployment.

In Kubernetes production, the panel uses its same-origin `/api/platform/**` and
`/api/sso/**` BFF routes. The BFF calls Kubernetes Services using server-side
`*_SERVICE_BASE_URL` variables, so neither `api-gateway` nor
`discovery-server` is required.

Primary endpoint:

- `POST /endpoint/ai-orchestrator/generate/app`

Panel routes:

- `/`
- `/roadmap`
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
- roadmap workspace for the four launch-critical tracks
- PWA manifest and installable panel metadata
- Farsi/English direction toggle and light/dark UI toggle
- backend-first project registry with local development fallback
- dynamic service proxy for Maker/Data access to `/endpoint/entities/**`

## Next step

Finish production bot adapters: Telegram/Bale webhook ingestion, token storage, tenant mapping, and idempotent message processing.

See `../docs/AI_APP_WEB_BOT_MAKER_ROADMAP.md` for the market roadmap and backend gaps.

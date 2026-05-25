# AI Orchestrator Service Architecture

This repo now includes [ai-orchestrator-service](/Users/farid/Projects/Cyan/old-cyan/cyan_business/ai-orchestrator-service:1).

It is modeled on the existing `AI-Orchestrator/ai-orchestrator-service`, but adapted for this platform:

- generates application DSL for this repo’s microservices
- can execute provisioning against dynamic services and `bpm-service`
- returns final API handoff for UI or bot integration

## Main Endpoints

- `POST /endpoint/ai-orchestrator/generate/app`
- `POST /internal/ai-orchestrator/generate/app`

Request:

```json
{
  "prompt": "I want a small ecommerce site with blog and CRM",
  "tenantKey": "tenant-demo",
  "siteKey": "site-shop-a",
  "execute": true,
  "answers": {}
}
```

## Flow

1. fetch platform metadata from service internal APIs
2. retrieve local RAG knowledge
3. build orchestration prompt
4. generate `PlatformAppDslDefinition` through:
   - OpenAI-compatible API
   - OpenRouter
   - GapGPT
   - Ollama
   - heuristic fallback
5. validate the generated DSL
6. optionally provision:
   - entity definitions
   - starter records
   - storefront routes
   - BPM flows
7. return delivery endpoints for UI/bot usage

## Generated DSL

Core model:

- `app`
- `entities[]`
- `routes[]`
- `flows[]`
- `delivery`
- `manualActions[]`

It is stored in memory for the request/response cycle and can be provisioned immediately.

## Provisioning Behavior

The executor currently provisions through internal APIs:

- dynamic entity definition creation from templates
- dynamic record creation
- storefront `site-route` records
- BPM flow creation

It also creates a default `theme-layout` record for scoped storefront delivery.

## Supported App Shapes

Current orchestration target:

- website
- blog
- shop
- CRM
- BPM-assisted mixed business app

The heuristic fallback already knows how to create:

- landing page
- optional blog page
- optional starter product
- optional CRM lead entity
- optional BPM order review flow

## Current Boundaries

Implemented:

- same LLM/provider-routing idea as the reference service
- metadata-driven prompt generation
- RAG bootstrap from local markdown
- DSL validation
- provisioning into this platform
- API delivery summary for UI/bot

Not yet implemented:

- persistent conversation/session memory
- external registrar/domain purchasing integration
- direct Telegram bot adapter
- payment gateway onboarding automation
- rich multi-turn follow-up flow storage

When domain purchase or DNS setup is requested, the orchestrator currently emits a `manualActions` item rather than pretending the platform can complete it.

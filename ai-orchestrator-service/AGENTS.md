# ai-orchestrator-service Agent Guide

## Purpose
`ai-orchestrator-service` turns prompts, blueprints, drafts, and conversations into platform provisioning actions across dynamic services.

## Owns
- blueprint catalog
- draft generation and storage
- provisioning runs
- conversation sessions
- LLM provider routing and RAG bootstrap

## Main APIs
- `/endpoint/ai-orchestrator/generate/app`
- `/endpoint/ai-orchestrator/blueprints/**`
- `/endpoint/ai-orchestrator/drafts/**`
- `/endpoint/ai-orchestrator/sessions/**`
- matching `/internal/ai-orchestrator/**`

## Dependencies
- internal template APIs across many domain services
- optional LLM providers and local heuristic fallback

## Flow Role
1. Interpret user intent.
2. Pick blueprints/templates.
3. Create definitions and optionally records in target services.
4. Track draft and provisioning run state.

## Change Rules
- Prefer template-backed provisioning over freeform schema invention.
- Keep service-key inventory aligned with actual platform services.

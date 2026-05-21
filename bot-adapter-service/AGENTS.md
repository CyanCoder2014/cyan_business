# bot-adapter-service Agent Guide

## Purpose
`bot-adapter-service` owns public Telegram/Bale webhook ingestion and maps external bot chats to AI orchestrator conversation sessions.

## Owns
- tenant/site/channel integration records
- external chat to `ai-orchestrator-service` session mapping
- idempotent inbound webhook processing

## Main APIs
- `POST /endpoint/bot-adapter/integrations`
- `GET /endpoint/bot-adapter/integrations`
- `POST /public/bot-adapter/{channel}/{integrationKey}/webhook`

## Dependencies
- Calls `ai-orchestrator-service` endpoint APIs.
- Does not own business records directly.

## Change Rules
- Never store bot token secrets in logs or responses.
- Preserve idempotency by channel/integration/external message id.
- Keep tenant/site mapping explicit for every integration.

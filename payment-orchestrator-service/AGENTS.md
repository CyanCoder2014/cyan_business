# payment-orchestrator-service Agent Guide

## Purpose
`payment-orchestrator-service` is the abstraction layer between checkout/business flows and concrete payment transaction handling.

## Owns
- payment-session initiation policy
- available payment-method lookup
- transaction verification bridge
- dynamic templates for orchestrated payment configuration

## Main APIs
- `GET /internal/payment-orchestrator/methods`
- `POST /internal/payment-orchestrator/sessions/initiate`
- `POST /internal/payment-orchestrator/transactions/{transactionKey}/verify`

## Dependencies
- `payment-service`
- `checkout-service`

## Change Rules
- Keep this service orchestration-focused; provider-specific logic belongs lower in `payment-service`.

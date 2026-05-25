# finance-automation-service Agent Guide

## Purpose
`finance-automation-service` consumes business events for finance-side follow-up actions and audit-friendly projections.

## Owns
- Kafka consumer group `finance-automation-service`
- action log/query API

## Consumes
- finance and commerce-relevant events

## Main APIs
- `GET /api/finance-automation-service/actions`

## Change Rules
- Maintain idempotent event handling and clear status capture.

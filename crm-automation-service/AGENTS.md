# crm-automation-service Agent Guide

## Purpose
`crm-automation-service` consumes business events for CRM-oriented downstream actions and projections.

## Owns
- Kafka consumer group `crm-automation-service`
- action log/query API

## Consumes
- CRM and commerce-relevant events

## Main APIs
- `GET /api/crm-automation-service/actions`

## Change Rules
- Keep consumers idempotent.
- Treat this service as downstream automation, not source-of-truth CRM storage.

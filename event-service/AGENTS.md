# event-service Agent Guide

## Purpose
`event-service` is the integration event hub between source services and Kafka automation consumers.

## Owns
- idempotent business event storage
- Kafka publish dispatch
- topic configuration for `business-events`

## Main APIs
- business event ingest APIs

## Dependencies
- upstream business services delivering outbox events
- Kafka
- automation consumer services

## Flow Role
1. Accept event from source service.
2. Persist it idempotently.
3. Publish envelope to Kafka.
4. Allow fan-out to independent consumers.

## Change Rules
- Do not put source-domain business logic here.
- Preserve idempotency and retry behavior.

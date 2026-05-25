# inventory-automation-service Agent Guide

## Purpose
`inventory-automation-service` reacts to business events that should affect stock-side workflows or future reservation/manufacturing behavior.

## Owns
- Kafka consumer group `inventory-automation-service`
- action log/query API

## Consumes
- inventory, commerce, and catalog-relevant events

## Main APIs
- `GET /api/inventory-automation-service/actions`

## Change Rules
- Downstream stock actions must tolerate duplicate delivery.

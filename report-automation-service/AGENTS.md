# report-automation-service Agent Guide

## Purpose
`report-automation-service` consumes the full business event stream to build denormalized analytics or projection records.

## Owns
- Kafka consumer group `report-automation-service`
- event-fed projection/read-model API

## Main APIs
- `GET /api/report-automation-service/records`

## Change Rules
- Keep it projection-oriented; operational writes belong to source services.

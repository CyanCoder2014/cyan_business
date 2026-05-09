# notification-service Agent Guide

## Purpose
`notification-service` centralizes template-based and ad hoc notification dispatch across channels.

## Owns
- notification template records
- sync and async dispatch APIs
- sender registry for email, SMS, push, webhook, MQTT, and similar channels

## Main APIs
- `POST /endpoint/notifications/send`
- `POST /endpoint/notifications/send-async`
- `GET /endpoint/notifications/messages/{messageKey}`
- matching `/internal/notifications/**`

## Dependencies
- Kafka for queued flows
- `checkout-service` and other orchestrators

## Change Rules
- Keep template resolution stable because callers store `templateKey`, not rendered content.

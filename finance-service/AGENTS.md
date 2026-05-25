# finance-service Agent Guide

## Purpose
`finance-service` owns finance transaction records and settlement-style business data.

## Owns
- finance definitions and records
- template `finance-transaction`
- local finance outbox events

## Dependencies
- `dynamic-entity-core`
- `event-service`
- `report-service`
- `finance-automation-service`

## Flow Role
1. Persist transaction data.
2. Emit outbox event.
3. Support finance reporting and payment-adjacent downstream actions.

## Change Rules
- Keep references to invoice, order, customer, and payment entities consistent.

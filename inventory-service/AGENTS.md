# inventory-service Agent Guide

## Purpose
`inventory-service` owns stock and work-order style records for inventory and future manufacturing flows.

## Owns
- inventory definitions and records
- templates such as `stock-item` and `work-order`
- local inventory outbox events

## Dependencies
- `dynamic-entity-core`
- `event-service`
- `inventory-automation-service`
- `report-service`

## Change Rules
- Inventory record semantics affect checkout, reservations, and downstream automation.

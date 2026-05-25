# crm-service Agent Guide

## Purpose
`crm-service` owns leads, contacts, accounts, and other customer-facing relationship records.

## Owns
- CRM definitions and records
- templates such as `crm-lead` and `crm-contact`
- local CRM outbox events

## Dependencies
- `dynamic-entity-core`
- `event-service` for downstream fan-out
- consumed by report and automation services

## Flow Role
1. Persist CRM record.
2. Write local outbox event.
3. Deliver event to `event-service`.
4. Let automation and reporting react independently.

## Change Rules
- Preserve outbox semantics for downstream automations.

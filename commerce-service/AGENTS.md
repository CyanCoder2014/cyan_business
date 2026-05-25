# commerce-service Agent Guide

## Purpose
`commerce-service` owns order, invoice, and document-style commerce records.

## Owns
- commerce definitions and records
- templates such as `sales-order` and `sales-invoice`
- local commerce outbox events

## Dependencies
- `dynamic-entity-core`
- `event-service`
- referenced by checkout, finance, reporting, and BPM

## Flow Role
1. Persist order or invoice data.
2. Publish durable business event through the outbox chain.
3. Feed finance, inventory, CRM automation, and reports.

## Change Rules
- Order and invoice keys become cross-service references; do not rename lightly.

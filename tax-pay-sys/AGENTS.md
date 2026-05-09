# tax-pay-sys Agent Guide

## Purpose
`tax-pay-sys` is the specialized tax integration service. It exposes tax operations, server information, invoice submission endpoints, and downstream clients to related domain services.

## Owns
- tax API controllers
- invoice send/inquiry flows
- tax-specific dynamic template support

## Main APIs
- `/v2/api/tax-service/**`
- `/v2/api/tax/invoice/send-invoice`
- `/v2/api/tax/server-info/get-info`

## Dependencies
- `factor-service`
- `client-service`
- MySQL, Mongo, Eureka, auth issuer config

## Change Rules
- Treat this as an external integration boundary.
- Preserve payload compatibility and timeout assumptions for tax provider interactions.

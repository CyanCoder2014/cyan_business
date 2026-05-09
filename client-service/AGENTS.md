# client-service Agent Guide

## Purpose
`client-service` is a legacy domain service for clients and companies, with command/query style APIs and some dynamic-runtime settings.

## Owns
- client records
- company records

## Main APIs
- `/v2/api/client-service/clients`
- `/v2/api/client-service/companies`

## Dependencies
- MySQL, Mongo, Eureka, resource-server auth
- used by `tax-pay-sys` and related business flows

## Change Rules
- Preserve existing legacy routes; other services already call them directly.

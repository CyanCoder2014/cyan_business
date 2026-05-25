# factor-service Agent Guide

## Purpose
`factor-service` is a legacy factor/invoice-style domain service used by specialized financial and tax flows.

## Owns
- factor command and query operations

## Main APIs
- `/v2/api/factor-service/factors`

## Dependencies
- MySQL, Mongo, Eureka, resource-server auth
- consumed by `tax-pay-sys`

## Change Rules
- Keep contracts stable for tax integration.

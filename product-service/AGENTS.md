# product-service Agent Guide

## Purpose
`product-service` is a legacy product domain service with command/query APIs alongside newer dynamic-runtime configuration.

## Owns
- product command and query operations

## Main APIs
- `/v2/api/product`
- `/v2/api/product-service/products`

## Dependencies
- PostgreSQL-or-MySQL datasource config, Mongo, Eureka, resource-server auth

## Change Rules
- This overlaps conceptually with `catalog-service`; be explicit about whether a change targets legacy product flows or new dynamic catalog flows.

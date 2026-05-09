# buyer-service Agent Guide

## Purpose
`buyer-service` is a legacy buyer domain service with command/query style APIs and some dynamic-runtime settings layered in.

## Owns
- buyer aggregates and query models
- buyer command and query endpoints

## Main APIs
- `/v2/api/buyer`
- `/v2/api/buyer-service/buyers`

## Dependencies
- legacy shared `generic` commands/events
- Eureka, Mongo, MySQL, and auth issuer config

## Change Rules
- Treat this as a legacy bounded context; avoid forcing new dynamic patterns unless intentionally migrating it.

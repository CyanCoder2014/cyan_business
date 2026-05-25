# discovery-server Agent Guide

## Purpose
`discovery-server` is the Eureka registry for the platform. Services register here so gateway and peer services can resolve `lb://service-name` targets.

## Owns
- service registration
- service discovery metadata
- local registry UI

## Flow Role
1. Service boots.
2. Service registers with Eureka.
3. Gateway and other services resolve targets through the registry.

## Change Rules
- Keep it infrastructure-only.
- Avoid putting business logic here.
- If service IDs change, update gateway routes and client references together.

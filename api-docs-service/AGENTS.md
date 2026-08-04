# api-docs-service Agent Guide

## Purpose
`api-docs-service` aggregates controller-derived OpenAPI documents from the
microservices enabled for a deployment.

## Owns
- configured service documentation targets
- authenticated retrieval and bounded caching of service OpenAPI documents
- per-service and merged OpenAPI catalog endpoints

## Main APIs
- `GET /endpoint/api-docs/services`
- `GET /endpoint/api-docs/services/{serviceKey}`
- `GET /endpoint/api-docs/aggregate`
- matching Basic-auth internal routes under `/internal/api-docs`

## Change Rules
- Never return or log configured service credentials.
- Preserve each operation's owning service in merged documents.
- Do not require Eureka or the local gateway in Kubernetes.
- Do not expose internal specifications through public routes.

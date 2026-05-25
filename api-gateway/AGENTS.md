# api-gateway Agent Guide

## Purpose
`api-gateway` is the single ingress router. It exposes gateway routes for legacy APIs, SSO APIs, dynamic business services, public storefront/media/search/payment endpoints, and internal orchestration paths that are intentionally routable.

## Owns
- Spring Cloud Gateway route configuration
- CORS behavior
- public path exposure policy

## Main Routes
- `/api/**` for most business services
- `/api/sso/**` and `/.well-known/**` for auth
- `/public/storefront/**`, `/public/media/**`, `/public/search-index/**`, `/public/payment/**`
- `/endpoint/**` and `/internal/**` for selected orchestration-era services

## Dependencies
- `discovery-server` for service lookup
- `sso-auth-service` JWKS for resource-server validation

## Flow Role
1. Accept incoming request.
2. Match configured path predicate.
3. Forward to `lb://` target or fixed URI.

## Change Rules
- Keep gateway paths aligned with controller mappings in target services.
- Be careful when exposing `/internal/**`; these are service-to-service APIs by design.
- Update CORS only with clear frontend/public requirements.

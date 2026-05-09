# sso-session-service Agent Guide

## Purpose
`sso-session-service` manages login session lifecycle for the platform.

## Owns
- session creation
- session lookup
- session revocation

## Main APIs
- `POST /api/sso/sessions`
- `GET /api/sso/sessions/{sessionId}`
- `POST /api/sso/sessions/revoke`

## Dependencies
- used by `sso-auth-service`

## Change Rules
- Keep revocation semantics explicit.
- Avoid embedding token logic here; token issuance belongs to `sso-auth-service`.

# sso-fido-service Agent Guide

## Purpose
`sso-fido-service` provides FIDO/WebAuthn-style challenge and verification endpoints used by the auth layer.

## Main APIs
- `POST /api/sso/fido/challenge`
- `POST /api/sso/fido/verify`

## Dependencies
- used by `sso-auth-service`

## Change Rules
- Treat challenge verification as security-sensitive.
- Keep request/response contracts stable for the auth coordinator.

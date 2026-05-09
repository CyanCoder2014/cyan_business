# sso-user-service Agent Guide

## Purpose
`sso-user-service` is the user directory for platform authentication. It stores users and verifies passwords for `sso-auth-service`.

## Owns
- stored user records
- password verification
- user lookup by username

## Main APIs
- `POST /api/sso/users`
- `GET /api/sso/users/{username}`
- `POST /api/sso/users/verify-password`

## Dependencies
- used by `sso-auth-service`

## Change Rules
- Keep credentials handling minimal and deterministic.
- Coordinate any password hashing changes with auth service behavior.

# sso-otp-service Agent Guide

## Purpose
`sso-otp-service` provides one-time-password send and verify operations for auth flows.

## Main APIs
- `POST /api/sso/otp/send`
- `POST /api/sso/otp/verify`

## Dependencies
- used by `sso-auth-service`

## Change Rules
- Keep delivery and verification logic simple and auditable.
- If provider integrations are added, isolate them behind service classes.

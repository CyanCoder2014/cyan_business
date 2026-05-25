# sso-captcha-service Agent Guide

## Purpose
`sso-captcha-service` issues and verifies captcha challenges for login protection and abuse control.

## Main APIs
- `POST /api/sso/captcha/challenges`
- `POST /api/sso/captcha/verify`

## Dependencies
- used by `sso-auth-service`

## Change Rules
- Keep challenge generation stateless or clearly persisted.
- Preserve compatibility with auth request payloads.

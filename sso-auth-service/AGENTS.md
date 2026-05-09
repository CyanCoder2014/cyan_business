# sso-auth-service Agent Guide

## Purpose
`sso-auth-service` is the token issuer and auth coordinator for the platform. It handles login, logout, refresh, introspection, OTP/FIDO handoff, and publishes JWKS and OpenID metadata.

## Owns
- JWT issuance settings
- refresh token persistence
- auth orchestration across user, session, OTP, captcha, and FIDO services

## Main APIs
- `POST /api/sso/auth/login`
- `POST /api/sso/auth/logout`
- `POST /api/sso/auth/refresh`
- `POST /api/sso/auth/introspect`
- `POST /api/sso/auth/otp/send`
- `POST /api/sso/auth/fido/challenge`
- `POST /api/sso/auth/fido/verify`
- `GET /.well-known/jwks.json`
- `GET /.well-known/openid-configuration`

## Dependencies
- `sso-user-service`
- `sso-session-service`
- `sso-otp-service`
- `sso-captcha-service`
- `sso-fido-service`

## Flow Role
1. Validate login inputs and second-factor prerequisites.
2. Verify user credentials or delegated factor.
3. Create session and tokens.
4. Expose issuer metadata for all resource-server services.

## Change Rules
- Preserve issuer, audience, and JWKS stability unless coordinated platform-wide.
- Any auth flow change must be checked against every resource server using this issuer.

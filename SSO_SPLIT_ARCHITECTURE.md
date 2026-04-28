# Split SSO Architecture

## Overview

This repository now contains a split SSO stack inspired by the feature shape of `../../../ciam/aaa` and `../../../ciam/ciam-v1`.

The goal was not to clone the CIAM monolith, but to extract the main feature families and distribute them into connected services inside `cyan_business`.

## New Services

### `sso-auth-service`

Role:

- orchestrates login
- validates captcha
- validates username/password
- validates OTP when MFA is enabled
- creates a session
- issues RSA-signed JWT access tokens
- exposes JWKS and a minimal OpenID configuration endpoint

Important endpoints:

- `POST /api/sso/auth/login`
- `POST /api/sso/auth/logout`
- `POST /api/sso/auth/otp/send`
- `POST /api/sso/auth/refresh`
- `POST /api/sso/auth/introspect`
- `POST /api/sso/auth/fido/challenge`
- `POST /api/sso/auth/fido/verify`
- `GET /.well-known/jwks.json`
- `GET /.well-known/openid-configuration`

## `sso-user-service`

Role:

- user directory
- password verification
- role and MFA flag lookup

Important endpoints:

- `POST /api/sso/users`
- `GET /api/sso/users/{username}`
- `POST /api/sso/users/verify-password`

Current implementation note:

- in-memory store
- seeded demo users:
  - `cyan-admin / admin123`
  - `cyan-user / user123`

## `sso-captcha-service`

Role:

- issues login captcha challenges
- verifies challenge responses

Important endpoints:

- `POST /api/sso/captcha/challenges`
- `POST /api/sso/captcha/verify`

Current implementation note:

- simple arithmetic captcha
- in-memory challenge storage

## `sso-otp-service`

Role:

- generates and verifies OTP codes for MFA

Important endpoints:

- `POST /api/sso/otp/send`
- `POST /api/sso/otp/verify`

Current implementation note:

- in-memory OTP storage
- development response includes the generated code for local testing

## `sso-session-service`

Role:

- session creation
- session lookup
- session revocation

Important endpoints:

- `POST /api/sso/sessions`
- `GET /api/sso/sessions/{sessionId}`
- `POST /api/sso/sessions/revoke`

Current implementation note:

- in-memory session registry

## `sso-fido-service`

Role:

- dedicated FIDO/WebAuthn-oriented challenge service boundary
- challenge creation
- challenge verification

Important endpoints:

- `POST /api/sso/fido/challenge`
- `POST /api/sso/fido/verify`

Current implementation note:

- lightweight placeholder flow inspired by the `aaa` FIDO feature family
- challenge state is in-memory
- this is a service boundary ready for real WebAuthn implementation later

## Gateway Integration

The API gateway now routes these paths:

- `/api/sso/auth/**`
- `/api/sso/users/**`
- `/api/sso/captcha/**`
- `/api/sso/otp/**`
- `/api/sso/sessions/**`
- `/api/sso/fido/**`
- `/.well-known/**`

The gateway allows unauthenticated access to:

- `/api/sso/**`
- `/.well-known/**`
- `/eureka/**`

All other routes still require JWT.

## JWT Integration

The gateway and business services were changed from Keycloak issuer configuration to the new JWK endpoint:

- `http://localhost:9001/.well-known/jwks.json`

Updated services:

- `api-gateway`
- `client-service`
- `factor-service`
- `tax-pay-sys`

## Claim Shape

The JWT issued by `sso-auth-service` includes:

- `sub`
- `client_id`
- `preferred_username`
- `session_id`
- `realm_access.roles`
- `resource_access`

The `resource_access` structure intentionally matches the shape expected by the existing `tax-pay-sys` role converter.

Current audience/resource name:

- `cyan-business`

That means `tax-pay-sys` can still derive authorities like:

- `ROLE_cyan-business_user`

## Login Flow

### Login with password only

1. client gets captcha from `sso-captcha-service`
2. client submits login request to `sso-auth-service`
3. auth service verifies captcha
4. auth service verifies password through `sso-user-service`
5. auth service creates session through `sso-session-service`
6. auth service returns JWT

### Login with MFA

1. client gets captcha
2. client requests OTP through `sso-auth-service`
3. auth service verifies captcha
4. auth service checks user MFA flag
5. auth service triggers `sso-otp-service`
6. client submits login with OTP
7. auth service verifies OTP
8. auth service creates session
9. auth service returns JWT

### Refresh flow

1. client submits refresh token to `sso-auth-service`
2. auth service validates refresh token state
3. auth service checks that the linked session is still active
4. auth service issues a new access token and refresh token

### FIDO flow

1. client asks `sso-auth-service` for a FIDO challenge
2. auth service delegates to `sso-fido-service`
3. client submits challenge proof
4. auth service delegates verification to `sso-fido-service`

## Relation To `aaa` and `ciam-v1`

The CIAM projects contain much more functionality, including:

- authorization server patterns
- custom OAuth grant types
- SSO login variants
- richer captcha variants
- MFA orchestration
- FIDO2/WebAuthn hooks
- session/device tracking
- Redis and Mongo backed stores

This implementation adopts the same feature families but deliberately keeps them smaller and independent:

- auth orchestration
- captcha
- OTP
- sessions
- user identity
- FIDO challenge boundary

## Current Limitations

- no persistent storage for new SSO services
- no SMS/email gateway for OTP delivery
- no real WebAuthn/FIDO2 cryptographic verification yet
- no distributed revocation check in downstream resource services

## Recommended Next Steps

1. persist user, OTP, and session data
2. move refresh token state to persistent storage
3. replace the placeholder FIDO challenge flow with real WebAuthn verification
4. add downstream session-revocation enforcement
5. secure internal service-to-service calls

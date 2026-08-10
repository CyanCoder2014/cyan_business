# Phase 8 Completion Report

## Outcome

Phase 8 delivers tenant-owned team access, roles and permissions, platform client provisioning, profile/security, billing, and workspace settings. Business clients are implemented as tenants; the existing SSO OAuth client model was not repurposed. Identity credentials remain owned by `sso-user-service`, tenant memberships and roles by `tenant-service`, subscriptions by `billing-service`, and sessions by `sso-session-service`.

No production mock data, hardcoded tenant/site identifiers, fake access results, fake billing success, or in-memory production persistence was added.

## Backend Contracts

- Persisted tenant roles and role permissions with optimistic revision checks and a Flyway migration.
- Tenant permission catalog, role CRUD, user listing/provisioning, membership update/suspension, and effective-access APIs.
- Bounded grants prevent managers from assigning permissions they do not hold.
- The final active `TENANT_OWNER` cannot be suspended or demoted.
- Internal SSO identity provisioning uses authenticated service-to-service Basic auth and is idempotent by username/email ownership.
- Platform administrators can provision a business client atomically with a head user, `TENANT_OWNER` membership, selected capabilities, and a real FREE subscription.
- FREE plan provisioning calls a billing-owned internal operation with an idempotency key. EXTERNAL billing remains truthfully `NOT_CONFIGURED` until provider prerequisites exist.
- Profile updates and password changes persist in `sso-user-service`.
- Current-user session listing, owned-session revocation, and revoke-all contracts are implemented and protected by bearer authentication.
- Panel bootstrap resolves tenant-effective permissions and no longer makes an absent legacy session-scope record disable every page. It exposes a recoverable workspace-selection state.

## User Experience

- `/team/users` provides real identity creation, tenant role assignment, activation/suspension, responsive rows/cards, loading, empty, error, and pending states.
- `/team/roles` provides system/custom role navigation, permission grouping, member counts, and bounded role creation.
- `/clients` provides the platform-admin client wizard for tenant identity, head user, plan, and selected service capabilities.
- `/profile` provides persisted email/phone editing, password change, session inventory/revocation, MFA status, theme, locale, and RTL direction.
- `/billing` shows the active subscription, real available plans, FREE-plan activation, and truthful provider state.
- `/settings` shows the real tenant boundary, capability locks, health, source, and operational reason.
- `/iam` redirects to `/settings`.
- Every async mutation uses a disabled, `aria-busy` pending button to prevent duplicate clicks.

## Truthful Unavailable States

- MFA enable/disable is not exposed because an OTP-confirmation contract is still required; the persisted current MFA status is shown.
- Invoice, usage-meter, payment-method, cancellation, and external-provider operations remain visibly not configured because their owning billing/provider contracts do not yet exist.
- Invitation email delivery is not simulated. Administrators can provision an identity directly with an initial password; no fake invitation-success state is shown.

## Verification

- `npm run lint` — passed; existing unrelated hook/accessibility warnings remain non-fatal.
- `npm run build` — passed.
- `:tenant-service:test`, `:sso-user-service:test`, `:sso-session-service:test`, and `:billing-service:test` — passed.
- Phase 8 Playwright interaction tests passed for user creation de-duplication, role creation, billing, settings, profile, and client-wizard navigation.
- Pending user creation was delayed deliberately in the browser test; the submit action remained disabled and exactly one request was issued.

## Visual QA

Compared against all Phase 8 reference images. Thirty-six captures were produced and representative desktop light, desktop dark, tablet, mobile, English, and Farsi RTL renders were inspected for shell alignment, card padding, row density, drawer sizing, action spacing, contrast, mirroring, and mobile bottom-navigation clearance.

Screenshots are stored in `screenshots/` for team users, roles, clients, profile, billing, and settings across the required viewport/theme/locale combinations.

## Phase Boundary

Phase 9 and all later phases were not started.

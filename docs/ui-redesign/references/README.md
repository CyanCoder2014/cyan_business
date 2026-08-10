# Cyan Phase 1–2 Visual References

Copy this directory into the repository at:

```text
docs/ui-redesign/references/
```

## Phase 1

- `phase-1/01-shell-desktop-en-light.png`
- `phase-1/02-shell-mobile-en-light.png`
- `phase-1/03-shell-desktop-en-dark.png`
- `phase-1/04-shell-mobile-en-dark.png`
- `phase-1/05-shell-desktop-fa-rtl-light.png`
- `phase-1/06-shell-mobile-fa-rtl-light.png`

These define:
- authenticated shell
- sidebar/header
- desktop/mobile navigation
- light/dark styling
- Farsi/RTL layout
- card density, typography, spacing, and visual hierarchy

## Phase 2

- `phase-2/01-auth-onboarding-desktop-en-light.png`
- `phase-2/02-registration-mobile-en-light.png`

These define:
- sign-in/register composition
- OAuth buttons
- onboarding/workspace/plan selection
- limited-access messaging
- mobile auth structure

## Missing state-specific images

There are no separate pixel-reference images yet for:

- MFA challenge
- provider unavailable
- captcha expired
- no-plan dashboard
- plan service not configured
- permission denied
- capability unavailable

For those states, Codex should follow:
- `docs/ui-redesign/02-DESIGN-SYSTEM.md`
- `docs/ui-redesign/03-AUTH-PLANS-TENANCY-RBAC.md`
- `docs/ui-redesign/04-PAGE-SPECS.md`
- shared state components from Phase 1

Do not block Phase 1 because every error state lacks a dedicated screenshot.

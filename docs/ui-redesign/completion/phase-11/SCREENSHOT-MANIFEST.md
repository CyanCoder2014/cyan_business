# Phase 11 Screenshot Manifest

Captured: 2026-08-09

Source: `panel-web/tests/phase11.e2e.spec.ts`

All screenshots use deterministic API interception for layout verification.
Values are test-only and are not production fixtures. Images were visually
reviewed for shell geometry, card/control padding, responsive flow, bottom-nav
clearance, dark contrast, and Farsi RTL mirroring.

## States

| Suffix | Viewport | Locale/theme |
|---|---:|---|
| `desktop-en-light` | 1440×1000 | English light |
| `builder-en-dark` | 1600×1000 | English dark |
| `tablet-en-light` | 834×1112 | English light |
| `mobile-en-light` | 390×844 | English light |
| `small-mobile-fa-dark` | 360×800 | Farsi RTL dark |

## Files

Each state exists for:

- `dashboard-*` — shell and composed partial/empty operational widgets;
- `clients-*` — platform client registry and responsive action/search layout;
- `bpm-*` — graph/list alternative, lifecycle toolbar and transition inspector;
- `work-*` — active form, actions, history and assignment/collaboration cards.

Total: **20 PNG files** under `screenshots/`.

Earlier phase completion directories retain the wider route screenshots for
Auth, AI, Projects, Definitions, Data, Automation, Team, Profile, Billing,
Settings, Bots, Sites, Domains, Notifications, Commerce, Reports, Media, Search,
and Platform Health. Phase 11 adds cross-cutting viewport regression rather than
copying those images.

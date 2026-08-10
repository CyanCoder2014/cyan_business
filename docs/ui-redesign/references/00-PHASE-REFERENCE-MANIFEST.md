# Cyan Visual References — All Implementation Phases

Copy the phase directories into:

```text
docs/ui-redesign/references/
```

These images are the visual direction. They do not authorize mock data, fake
API success, hardcoded tenant/site scope, or invented backend capabilities.

## Phase mapping

### `phase-0-contract-audit`
- No image required; audit/documentation phase.

### `phase-1-foundation-shell`
- `phase-1-foundation-shell/01-shell-desktop-en-light.png`
- `phase-1-foundation-shell/02-shell-mobile-en-light.png`
- `phase-1-foundation-shell/03-shell-desktop-en-dark.png`
- `phase-1-foundation-shell/04-shell-mobile-en-dark.png`
- `phase-1-foundation-shell/05-shell-desktop-fa-rtl-light.png`
- `phase-1-foundation-shell/06-shell-mobile-fa-rtl-light.png`

### `phase-2-auth-onboarding-plans`
- `phase-2-auth-onboarding-plans/01-auth-onboarding-desktop-en-light.png`
- `phase-2-auth-onboarding-plans/02-auth-product-intro-desktop-en-light.png`
- `phase-2-auth-onboarding-plans/03-registration-mobile-en-light.png`

### `phase-3-dashboard-notifications`
- `phase-3-dashboard-notifications/01-dashboard-desktop-en-light.png`
- `phase-3-dashboard-notifications/02-dashboard-mobile-en-light.png`
- `phase-3-dashboard-notifications/03-dashboard-desktop-en-dark.png`
- `phase-3-dashboard-notifications/04-dashboard-mobile-en-dark.png`
- `phase-3-dashboard-notifications/05-dashboard-desktop-fa-rtl-light.png`
- `phase-3-dashboard-notifications/06-dashboard-mobile-fa-rtl-light.png`

### `phase-4-ai-projects-provisioning`
- `phase-4-ai-projects-provisioning/01-ai-studio-desktop-en-light.png`
- `phase-4-ai-projects-provisioning/02-ai-studio-mobile-en-light.png`
- `phase-4-ai-projects-provisioning/03-ai-project-summary-mobile-en-light.png`
- `phase-4-ai-projects-provisioning/04-blueprints-desktop-en-light.png`
- `phase-4-ai-projects-provisioning/05-blueprints-mobile-en-light.png`

### `phase-5-definitions-data`
- `phase-5-definitions-data/01-definition-editor-desktop-en-light.png`
- `phase-5-definitions-data/02-definition-editor-desktop-alternative.png`
- `phase-5-definitions-data/03-definition-editor-mobile-en-light.png`
- `phase-5-definitions-data/04-data-manager-desktop-en-light.png`
- `phase-5-definitions-data/05-data-manager-mobile-en-light.png`

### `phase-6-automation-builder`
- `phase-6-automation-builder/01-automation-builder-desktop-en-light.png`
- `phase-6-automation-builder/02-automation-builder-desktop-alternative.png`
- `phase-6-automation-builder/03-automation-builder-mobile-en-light.png`

### `phase-7-bpm-work-queue`
- `phase-7-bpm-work-queue/01-bpm-designer-desktop-en-light.png`
- `phase-7-bpm-work-queue/02-bpm-mobile-editor-en-light.png`
- `phase-7-bpm-work-queue/03-work-queue-layout-pattern.png`

### `phase-8-team-access-profile-billing-settings`
- `phase-8-team-access-profile-billing-settings/01-team-users-list-detail-pattern.png`
- `phase-8-team-access-profile-billing-settings/02-role-permission-editor-pattern.png`
- `phase-8-team-access-profile-billing-settings/03-billing-plan-pattern.png`
- `phase-8-team-access-profile-billing-settings/04-profile-settings-form-pattern.png`
- `phase-8-team-access-profile-billing-settings/05-mobile-account-form-pattern.png`

### `phase-9-bots-sites-domains-notifications`
- `phase-9-bots-sites-domains-notifications/01-bots-channels-management-desktop.png`
- `phase-9-bots-sites-domains-notifications/02-bot-operations-experience-desktop.png`
- `phase-9-bots-sites-domains-notifications/03-site-builder-desktop.png`
- `phase-9-bots-sites-domains-notifications/04-public-site-desktop.png`
- `phase-9-bots-sites-domains-notifications/05-public-site-mobile.png`
- `phase-9-bots-sites-domains-notifications/06-domain-notification-layout-pattern.png`

### `phase-10-reports-media-search-tools`
- `phase-10-reports-media-search-tools/01-report-dashboard-pattern.png`
- `phase-10-reports-media-search-tools/02-report-data-result-pattern.png`
- `phase-10-reports-media-search-tools/03-media-library-mobile-pattern.png`
- `phase-10-reports-media-search-tools/04-search-index-editor-pattern.png`
- `phase-10-reports-media-search-tools/05-operational-tool-shell-pattern.png`

### `phase-11-hardening-pwa-visual-regression`
- `phase-11-hardening-pwa-visual-regression/01-desktop-en-light-reference.png`
- `phase-11-hardening-pwa-visual-regression/02-mobile-en-light-reference.png`
- `phase-11-hardening-pwa-visual-regression/03-desktop-en-dark-reference.png`
- `phase-11-hardening-pwa-visual-regression/04-mobile-en-dark-reference.png`
- `phase-11-hardening-pwa-visual-regression/05-desktop-fa-rtl-light-reference.png`
- `phase-11-hardening-pwa-visual-regression/06-mobile-fa-rtl-light-reference.png`

## Important implementation rules

1. Written API and architecture documents remain the functional source of truth.
2. Images define visual hierarchy, spacing, density, navigation, and responsive behavior.
3. Do not copy sample names, counts, records, providers, or statuses into production.
4. Do not block a phase because every loading/error/locked state lacks a dedicated image.
5. State-specific UI should follow the shared design system and Phase 1 primitives.
6. For Phase 8 and Phase 10, shared list/editor/form patterns are intentionally reused.
7. Phase 11 must generate new screenshots from the implemented application.

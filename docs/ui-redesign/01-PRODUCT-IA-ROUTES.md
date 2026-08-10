# Target Product Information Architecture and Routes

## 1. Navigation principles

1. Navigation represents user jobs, not microservice names.
2. A module appears only when the user has both:
   - service capability for the active tenant/site
   - permission for the route or action
3. Plan-disabled modules may appear in a locked `Discover` area, but must not look functional.
4. Platform-admin tools must never be mixed with tenant-operator tools.
5. Builder pages and runtime/operator pages are separate:
   - **Design**: definitions, forms, automations, BPM, site/bot builders
   - **Operate**: records, work queue, deliveries, reports, notifications
6. Existing URLs should remain as redirects during migration.

## 2. Recommended primary navigation

### Home
- Dashboard
- AI Studio
- Projects

### Build
- Definitions & Forms
- Automations
- BPM Flows
- Sites & Commerce
- Bots & Channels

### Operate
- Data
- Work Queue
- Notifications
- Reports
- Media
- Search

### Manage
- Team & Access
- Clients
- Domains & Hosting
- Integrations
- Settings
- Billing & Plan

### Platform administration
Visible only to platform administrators:
- Tenants
- Service capabilities
- Plans
- Global users
- Environments
- Health / QA

## 3. Target route map

| Target route | Purpose | Current route / migration |
|---|---|---|
| `/auth` | Login, registration, OAuth, recovery, MFA | keep |
| `/onboarding` | Plan selection, tenant creation/join, workspace setup | new |
| `/dashboard` | Capability-aware home | redirect `/` |
| `/ai` | Persistent AI Studio session UI | redirect `/projects/new` |
| `/projects` | Project list, filters, releases, provisioning | keep |
| `/projects/[projectId]` | Project workspace | keep |
| `/definitions` | Service/entity/form definition catalog | split from `/maker` |
| `/definitions/[serviceKey]/[entityKey]` | Visual definition editor | new |
| `/data` | Definition-driven data catalog | keep |
| `/data/[serviceKey]/[entityKey]` | Generic grid and record editor | new |
| `/automations` | Automation definitions and executions | rename `/automation` |
| `/automations/[flowKey]` | n8n-style automation editor | new |
| `/bpm` | BPM flow definitions | rename `/flows` builder portion |
| `/bpm/[flowKey]` | BPM designer | new |
| `/work` | Assigned/visible managed objects and active forms | split from `/flows` |
| `/sites` | Site/app list | replace part of `/site-builder` |
| `/sites/[siteId]/builder` | Page/route/site builder | migrate `/site-builder` |
| `/bots` | Bot/channel list | merge configuration from `/integrations` |
| `/bots/[integrationKey]` | Bot configuration, sessions, delivery | merge `/bot` and `/integrations` |
| `/notifications` | Templates, providers, history | keep and expose |
| `/reports` | Report catalog and run history | new primary page |
| `/media` | Asset library | new real route; stop labeling Search as Media |
| `/search` | Indexes, sync, query diagnostics | keep and label correctly |
| `/team/users` | Tenant users and invitations | new |
| `/team/roles` | Roles, permissions, capability grants | new |
| `/clients` | Client/tenant management for allowed admins | new |
| `/profile` | Personal account, sessions, security | split from `/iam` |
| `/settings` | Tenant/site preferences and defaults | split from `/iam` |
| `/billing` | Plan, invoices, payment methods | new |
| `/domains` | Host/domain/DNS/certificate management | new |
| `/platform/*` | Global administration | new protected area |

## 4. Project workspace structure

`/projects/[projectId]` should become the stable project shell with tabs:

- Overview
- AI conversation
- Structure
- Automations
- BPM
- Data
- Channels
- Site
- Releases
- Provisioning
- Activity

Tabs are filtered by capabilities. The project is the shared context for tenant/site, draft, release, and related resources.

## 5. Navigation model

```ts
type NavigationItem = {
  id: string;
  href: string;
  labelKey: string;
  icon: IconName;
  group: "home" | "build" | "operate" | "manage" | "platform";
  requiredCapabilities?: string[];
  requiredPermissions?: string[];
  requiredPlanFeatures?: string[];
  match?: string[];
  mobilePriority?: number;
  showLockedDiscovery?: boolean;
};
```

Do not implement access rules separately inside every page. Use one access resolver and one navigation registry.

## 6. Mobile navigation

The mobile bottom bar contains five high-frequency destinations:

- Home
- AI
- Build
- Work
- More

`Build` opens a capability-filtered sheet for Definitions, Automations, BPM, Sites, and Bots.

`More` opens Data, Notifications, Reports, Media, Search, Team, Settings, Billing, and Domains.

This avoids making inaccessible routes unreachable while keeping the bottom bar stable.

## 7. Compatibility redirects

Maintain redirects for at least one release cycle:

```text
/                 -> /dashboard
/projects/new     -> /ai
/maker            -> /definitions
/flows            -> /bpm or /work based on query/action
/automation       -> /automations
/integrations     -> /bots
/bot              -> /bots
/site-builder     -> /sites/{activeSite}/builder
/iam              -> /profile
```

Never silently redirect an edit URL to a generic list when the resource identifier is available.

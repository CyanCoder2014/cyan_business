# Final Route, Capability, Permission, and Owner Matrix

Updated: 2026-08-09

Navigation is filtered for usability, but every owner remains authoritative.
Tenant/site headers come from the persisted session bootstrap and are forwarded
by the authenticated platform client/BFF.

| Canonical route | Job | Capability | Representative permission | Owner/API | Status |
|---|---|---|---|---|---|
| `/auth` | sign in/register/captcha/MFA | identity | public then authenticated | SSO services via `/api/sso/**` | canonical |
| `/dashboard` | scoped operational overview | composed | `panel:read` | AI/BPM/Automation/Bot/Notification + bootstrap | canonical, partial-safe |
| `/ai` | AI conversation/provisioning | `ai-orchestrator` | `project.create`, `ai.execute` | `ai-orchestrator-service` | canonical |
| `/projects` | project drafts | `ai-orchestrator` | `project.read` | `ai-orchestrator-service` | canonical |
| `/projects/[projectId]` | run/release/publish project | `ai-orchestrator` | `project.create` | `ai-orchestrator-service` | canonical |
| `/definitions` | definition catalog | `dynamic-entities` | `definition.read` | dynamic service owner | canonical |
| `/definitions/[serviceKey]/[entityKey]` | definition editor | `dynamic-entities` | `definition.manage` | selected dynamic service | canonical |
| `/data` | entity data catalog | `dynamic-entities` | `record.read` | dynamic service owners | canonical |
| `/data/[serviceKey]/[entityKey]` | definition-driven records | `dynamic-entities` | `record.read/manage` | selected dynamic service | canonical |
| `/automations` | automation registry/runs | `automation` | `automation.read` | `automation-orchestrator-service` | canonical |
| `/automations/new` | create graph | `automation` | `automation.manage`; AI nodes also `ai.execute` | Automation → Tenant/AI | canonical, lazy |
| `/automations/[flowKey]` | versioned graph/lifecycle | `automation` | `automation.manage`; AI nodes also `ai.execute` | Automation → Tenant/AI | canonical, lazy |
| `/automations/**/executions` | run history/detail | `automation` | `automation.read/execute` | Automation | canonical |
| `/bpm` | process registry | `bpm` | `bpm.read` | `bpm-service` | canonical |
| `/bpm/new` | process designer | `bpm` | `bpm.manage` | BPM | canonical, lazy |
| `/bpm/[flowKey]` | process access/lifecycle editor | `bpm` | `bpm.manage` | BPM | canonical, lazy |
| `/work` | assigned/visible cartable | `bpm` | `bpm.read` plus object access | BPM | canonical |
| `/work/[objectId]` | form/transition/collaboration | `bpm` | server object/state/transition rule | BPM; Media for bytes | canonical |
| `/team/users` | tenant members | tenancy | `team.read/manage` | `tenant-service` → SSO User | canonical |
| `/team/roles` | bounded roles/permissions | tenancy | `team.read`, `roles.manage` | `tenant-service` | canonical |
| `/clients` | tenant/head-user provisioning | platform admin | `realm:manage` | Tenant → SSO/Billing | canonical, platform-only |
| `/profile` | identity/session/preferences | identity | authenticated self | SSO User/Session | canonical |
| `/billing` | plan and billing state | billing | `billing.read/manage` | `billing-service` | canonical |
| `/settings` | tenant/capability state | tenancy | `settings.read/manage` | Tenant/Billing/health | canonical |
| `/bots` | Telegram/Bale integrations | `bot-adapter` | `bot.read/manage` | `bot-adapter-service` | canonical |
| `/bots/[integrationKey]` | webhook/session/delivery/process binding | `bot-adapter` | `bot.manage` | Bot Adapter → Automation/BPM | canonical |
| `/sites` | site registry | `site-builder` | `site.read/manage` | Storefront/Tenant site | canonical |
| `/sites/[siteId]/builder` | route/theme/publish/preview | `site-builder` | `site.manage` | `storefront-service` dynamic + public render | canonical |
| `/domains` | ownership/DNS/certificate state | `site-builder` | `site.manage` | domain/hosting owner | canonical, provider-aware |
| `/notifications` | inbox/providers/history | notification | scoped notification access | `notification-service` | canonical |
| `/commerce` | commerce operations read model | commerce | scoped record read | Commerce/Cart/Checkout/Payment | canonical, contract-limited |
| `/reports` | report catalog | `report` | `report.read` | `report-service` | canonical |
| `/reports/[reportKey]` | run/result history | `report` | `report.read/manage` | Report | canonical; no export |
| `/media` | upload/list/preview/usage | `media` | `media.read/manage` | `media-service` | canonical; protected delete |
| `/search` | index/sync/query | `search` | `search.read/manage` | `search-index-service` | canonical |
| `/platform/health` | live service checks | platform admin | platform-admin realm role | platform BFF/service endpoints | canonical, protected |
| `/api-docs` | live controller catalog | platform/build audience | authenticated access | API Docs service | canonical |

## Compatibility routes retained for one migration window

| Compatibility route | Destination | Notes |
|---|---|---|
| `/automation` | `/automations` | no state is fabricated |
| `/bot` | `/bots` | integration registry |
| `/bot/[sessionId]` | canonical PanelShell AI-session presentation | identifier preserved; resume link targets `/ai?sessionId=…` |
| `/flows` | `/bpm` | canonical process registry |
| `/iam` | `/profile` | self-service identity intent |
| `/integrations` | `/bots` | bot/integration owner |
| `/maker` | `/definitions` | definition builder |
| `/site-builder` | `/sites` | a site must be selected; no hardcoded site key |
| `/roadmap`, `/platform/roadmap` | `/platform/health` | static runtime roadmap retired |

Removed internal panel routes: `/api/projects/**` and `/api/bot-sessions/**`.
They were local registries with no remaining production callers; canonical
project and session clients call `ai-orchestrator-service` through the BFF.

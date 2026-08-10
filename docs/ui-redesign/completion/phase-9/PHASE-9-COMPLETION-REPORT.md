# Phase 9 Completion Report

## Outcome

Phase 9 delivers tenant-scoped Telegram and Bale bot operations, bot-to-Automation/BPM process bindings, persisted site and domain administration, a real definition-backed site builder, operator-facing commerce presentation, and notification inbox/template/provider/delivery operations.

No production mock data, hardcoded tenant/site identifiers, raw secret values, fake webhook/domain/certificate success, or frontend-only backend success was added. Phase 10 and later phases were not started.

## Bots, Automation, and BPM

- `/bots` and `/bots/[integrationKey]` replace fixed integration examples with real scoped integration records for Telegram and Bale.
- Bot tokens and webhook secrets are accepted and persisted only as secret references. Raw token/secret request fields are rejected, values are excluded from responses, and webhook secrets are resolved at execution time.
- Public webhook ingestion validates the provider secret header with a constant-time comparison and preserves channel/integration/external-message idempotency.
- Inbound messages are persisted before downstream work. AI conversation processing and process dispatch are independent, so an unavailable AI operation does not silently discard an Automation/BPM trigger.
- A process binding can match every message or a command prefix and target an existing published Automation or BPM flow. The owning service is called through its authenticated internal API; targets are validated before the binding is saved.
- Automation starts are asynchronous and use a stable `bot:{inboundId}:{bindingId}` idempotency key, tenant/site scope, correlation context, and a truthful failure record.
- BPM starts create a managed object referencing the persisted inbound message and identify the actor as `bot:{channel}:{chatId}` with `BOT_EXTERNAL` role context.
- Dispatch attempts, target references, and errors are persisted and exposed as binding history.
- Endpoint operations now require both platform capability authorization and an active tenant membership, preventing permission-only cross-tenant access.
- Delivery history and failed-delivery retry use persisted outbound records and idempotency keys. Mini-app builds use real stored records and explicit publish confirmation.

## Sites, Domains, Commerce, and Notifications

- `/sites` uses the existing storefront-owned site registry. `/sites/[siteId]/builder` reads and writes real route and theme definitions, checks route conflicts, starts new pages blank, and renders backend-returned public output in a sandboxed iframe.
- Builder sections cover pages, reusable theme/block data, SEO, assets, draft state, preview, and publish operations only where an existing contract supports them.
- `/domains` is backed by storefront-owned persisted domain and event tables. Hostnames are validated, DNS TXT challenges are generated and stored, verification performs a real DNS TXT lookup, and history is retained.
- Certificate state remains `NOT_CONFIGURED`; no certificate or hosting success is simulated without a provider contract.
- `/commerce` is now a read-only operator presentation of actual cart, checkout, order, and payment records instead of developer seeding controls.
- `/notifications` combines the real inbox with template preview/test, provider status, delivery history, and retry. Provider selectors submit provider keys rather than their decorated status labels.
- SMTP, SMS, push, and MQTT return `NOT_CONFIGURED` until real provider configuration exists. HTTPS webhook dispatch is disabled by default and performs a real request only when explicitly enabled.
- Spring Boot 4 Flyway starters and legacy-schema baselines were added to the Phase 9 SQL-owning services so migrations execute before Hibernate instead of merely existing on disk.

## User Experience and Visual QA

- New primary navigation entries are Bots, Sites, and Domains; `/integrations`, `/bot`, and `/site-builder` remain compatibility redirects.
- Async mutations use disabled, `aria-busy` pending controls, preventing duplicate clicks while webhook registration, binding saves, domain actions, publish operations, notification tests, and retries are in flight.
- Shared loading, empty, error, denied, and unavailable states are used without inventing records.
- Mobile content includes bottom safe-area clearance, and dense tab sets scroll horizontally rather than compressing or clipping labels.
- English light, English dark, and Farsi RTL light states were checked at desktop, tablet, and mobile sizes. Forty-two final captures are stored in `screenshots/`.
- Representative bot detail, builder RTL, notification mobile, and desktop dark renders were inspected after the final spacing fix for card padding, alignment, hierarchy, tab overflow, contrast, RTL mirroring, and bottom-navigation clearance.

## Verification

- `bash ./gradlew :tenant-service:test :bot-adapter-service:test :storefront-service:test :notification-service:test` — passed.
- `npm run lint` — passed with pre-existing non-fatal React hook/accessibility warnings.
- `npm run build` — passed; all 43 routes were generated or compiled successfully.
- Phase 9 interaction suite — 4 passed.
- Final Phase 1–9 interaction suite — 17 passed in Chromium.
- Phase 9 visual capture — 1 passed, producing 42 screenshots.
- Backend contract tests cover Telegram-to-Automation idempotency and Bale-to-BPM actor/scoping behavior.
- Browser tests cover Telegram command-to-Automation and Bale every-message-to-BPM binding, including disabled pending controls and exactly one save request.
- The minimum updated services were started locally on their fixed ports: storefront `9115`, notification `9122`, bot adapter `9126`, and tenant `9129`. The existing `cyan-admin` tenant membership resolved successfully through the authenticated tenant internal API.

## Truthful External Prerequisites and Operational States

- Telegram/Bale webhook registration and delivery still require provider-issued bot credentials stored behind configured secret references and an internet-reachable HTTPS callback.
- Domain verification requires public DNS ownership. Certificate provisioning requires a certificate/hosting provider contract and remains `NOT_CONFIGURED`.
- SMTP, SMS, push, and MQTT require provider configuration. HTTPS webhooks require explicit enablement and a real destination.
- Kafka was not running in the local minimum-service environment. Synchronous notification paths and tests pass; queued notification consumption remains unavailable until Kafka is started.
- The local `cyan-user` identity has no tenant membership in the selected tenant database, so a stale persisted tenant scope is rejected rather than accepted. The tenant-backed `cyan-admin` scope succeeds. No membership was fabricated for testing.

## Phase Boundary

Phase 10 and all later phases were not started.

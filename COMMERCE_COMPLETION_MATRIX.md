# Commerce Completion Matrix

This matrix reflects the current implementation state in `cyan_business` after the dynamic-commerce completion pass.

## Implemented Services

### `storefront-service`

Implemented:

- scoped route resolution
- scoped JSON render
- scoped HTML render
- public sitemap JSON
- public `sitemap.xml`
- public `robots.txt`
- canonical/meta/Open Graph/Twitter meta output
- fallback schema.org JSON-LD generation for `WebPage` and `Product`
- route binding to dynamic entities in other services

Still hardening:

- full theme/component engine
- visual section renderer
- draft preview workflow
- menu hierarchy rendering

### `media-service`

Implemented:

- dynamic `media-asset` and `media-folder`
- internal upload-preparation endpoint
- generated CDN-ready URLs
- generated responsive variants
- public asset lookup
- public variant lookup

Still hardening:

- real object storage
- binary upload stream handling
- image optimization workers
- signed/private delivery

### `cart-service`

Implemented:

- dynamic `shopping-cart`
- nested item, variant, attribute, pricing structures
- cart data usable by checkout

Still hardening:

- guest/user merge
- abandoned cart policies
- cart expiration and reservation rules

### `checkout-service`

Implemented:

- checkout snapshot from cart + pricing + payment methods
- payment initiation through payment orchestrator
- payment verification hook
- checkout lifecycle advance endpoint
- notification dispatch integration
- richer checkout static fields for order/payment lifecycle

Still hardening:

- shipping-provider integration
- commerce-service order creation hook
- refund/return lifecycle

### `payment-orchestrator-service`

Implemented:

- payment method aggregation from `payment-service`
- payment initiation bridge
- payment verification bridge

Still hardening:

- webhook reconciliation state
- retry policies
- multi-step payment session orchestration

### `payment-service`

Implemented:

- strategy-based provider model
- admin-managed payment methods/configs
- initiation/verification flow
- public callback entrypoints
- Iranian and international provider strategy classes

Still hardening:

- real upstream implementations per provider
- production signing/encryption details
- reconciliation and dispute flows

### `pricing-promotion-service`

Implemented:

- dynamic `promotion-rule` and `tax-rule`
- pricing evaluation endpoint
- cart/checkout pricing integration

Still hardening:

- stacked discounts
- advanced coupon conditions
- tax jurisdiction depth

### `search-index-service`

Implemented:

- dynamic `index-definition` and `search-document`
- projection sync from other dynamic services
- public search endpoint
- public suggestions endpoint
- internal search endpoint
- structured filters, sort values, SEO signal projection

Still hardening:

- background reindex jobs
- ranking tuning
- real Elasticsearch/OpenSearch adapter

### `notification-service`

Implemented:

- dynamic `notification-template` and `notification-message`
- internal send endpoint
- endpoint send endpoint
- rendered message persistence
- checkout lifecycle integration

Still hardening:

- real SMTP/SMS/push/webhook providers
- async retries
- template localization

## Shared Platform State

Implemented:

- strict dynamic nested validation
- missing-field rejection
- extra-field rejection
- tenant/site scoping
- `/endpoint/**` bearer surface
- `/internal/**` basic-auth surface
- structured relations
- Mongo unique record-key constraints

Still hardening:

- tenant quotas and domain binding
- schema migration/versioning
- full integration-test sweep
- provider-level production adapters

## Practical Status

You can now build:

- multi-client structured websites
- blog/page sites
- product catalog sites
- small ecommerce flows
- CRM/order/transaction portals
- search-backed storefronts

You should still treat these as needed before broad production rollout:

- real payment-provider implementations
- real media storage and optimization
- real notification-provider delivery
- deeper storefront rendering UX
- stronger automated runtime coverage

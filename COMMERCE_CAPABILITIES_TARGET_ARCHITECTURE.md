# Commerce Capabilities Target Architecture

## Goal

Add the missing commerce-capability services as structured dynamic microservices, not ad hoc CRUD systems.

Every service here must support:

- dynamic entity definitions
- dynamic nested field structures
- strict missing-field and extra-field rejection
- nested `object` and `list` validation
- validators and operators through shared runtime
- `/endpoint/**` bearer-token APIs
- `/internal/**` basic-auth APIs

This matches the `Cyan-core` style already being adopted in this repo.

## Added services

- `storefront-service`
- `media-service`
- `cart-service`
- `checkout-service`
- `payment-orchestrator-service`
- `pricing-promotion-service`
- `search-index-service`

## Service by service

### `storefront-service`

Purpose:

- public page routing
- theme layout metadata
- route-to-entity binding
- modern SEO controls
- sitemap generation inputs

Dynamic entities:

- `site-route`
- `theme-layout`

Key responsibility:

- own canonical route metadata, not content body itself
- connect route records to content/product/category entities
- manage route rendering metadata and search-facing SEO fields

Important modern SEO support:

- canonical URLs
- robots directives
- Open Graph and Twitter metadata
- JSON-LD blocks for schema.org
- sitemap priority and indexability flags
- render hints for page speed and preload decisions

### `media-service`

Purpose:

- files and image metadata
- asset variants
- alt text, captions, titles
- CDN-ready URL management

Dynamic entities:

- `media-asset`
- `media-folder`

Key responsibility:

- own asset metadata and responsive variants
- later connect to real object storage, image optimization, and signed delivery

### `cart-service`

Purpose:

- cart/session preparation before checkout
- cart line items and pricing snapshot
- customer/product relations

Dynamic entities:

- `shopping-cart`

Key responsibility:

- own mutable pre-order basket state
- preserve dynamic item attributes and product relations

### `checkout-service`

Purpose:

- addresses
- shipping selection
- totals snapshot
- checkout lifecycle
- order confirmation pre-state

Dynamic entities:

- `checkout-session`

Key responsibility:

- validate readiness for order creation
- bridge cart, pricing, payment orchestration, and final order write

### `payment-orchestrator-service`

Purpose:

- gateway abstraction at checkout boundary
- payment session coordination
- webhook subscription definitions
- callback delivery state

Dynamic entities:

- `payment-session`

Key responsibility:

- coordinate checkout-facing payment flow
- call `payment-service` internally
- hide raw provider details from checkout/storefront flows

### `pricing-promotion-service`

Purpose:

- coupon and promotion rules
- discount conditions
- tax rules
- future shipping-fee rules

Dynamic entities:

- `promotion-rule`
- `tax-rule`

Key responsibility:

- own reusable pricing logic inputs
- later expose calculation APIs and deterministic pricing projections

### `search-index-service`

Purpose:

- search/filter indexing
- query-optimized read models
- content/product/report projections
- future Elasticsearch/OpenSearch bridge

Dynamic entities:

- `index-definition`
- `search-document`

Key responsibility:

- own search-facing projection metadata
- define which fields are searchable, filterable, sortable, or facetable

## Focus areas

### 1. Search / filter indexing

Recommended target:

- keep source-of-truth records in dynamic services
- build search projections in `search-index-service`
- support:
  - full-text search
  - facets
  - typed filters
  - sort keys
  - autocomplete/suggest

Suggested progression:

1. Mongo/Postgres projection read models
2. background rebuild and incremental sync
3. Elasticsearch/OpenSearch adapter only when catalog/content scale justifies it

Minimum fields to mark per entity definition:

- `searchable`
- `filterable`
- `sortable`
- `facetable`
- `analyzer`
- `boost`

### 2. Modern SEO, routing, and sitemap

`storefront-service` should become the control plane for modern search optimization:

- canonical routing
- clean path ownership
- structured data JSON-LD
- freshness signals
- last-modified timestamps
- sitemap inclusion/exclusion
- robots control
- Open Graph and social cards
- internal linking metadata
- content type aware SEO:
  - article
  - product
  - category
  - FAQ
  - organization
  - local business

Important next implementation pieces:

- rendered `sitemap.xml`
- dynamic `robots.txt`
- route resolver API
- public preview/draft route support
- schema.org builders for product/article/organization/FAQ

### 3. Order lifecycle and notifications

Target ownership split:

- `cart-service`: pre-order mutable basket
- `checkout-service`: checkout progression and totals
- `commerce-service`: final order/invoice records
- `payment-orchestrator-service` + `payment-service`: payment coordination
- `finance-service`: financial transaction projection
- notification pipeline: email, SMS, push

Recommended order lifecycle:

1. `CART_ACTIVE`
2. `CHECKOUT_CREATED`
3. `CHECKOUT_PRICED`
4. `PAYMENT_PENDING`
5. `PAYMENT_VERIFIED`
6. `ORDER_CREATED`
7. `ORDER_CONFIRMED`
8. `FULFILLMENT_PENDING`
9. `FULFILLED`
10. `COMPLETED`
11. `CANCELLED` or `REFUNDED`

Notification events that should exist:

- cart abandoned
- checkout started
- payment pending
- payment failed
- payment verified
- order confirmed
- shipment created
- shipment delivered
- refund issued

Notification channels:

- email
- SMS
- push
- webhook

Notification payloads should reference structured records, not loose strings.

### 4. Implemented nested commerce conventions

The current dynamic templates now include explicit nested structures instead of loose JSON buckets:

- `catalog-product`
  - `seo`
  - `media[]`
  - `attributes[]`
  - `variants[]`
  - `routing`
  - `searchIndex.filterEntries[]`
  - `searchIndex.sortEntries[]`
- `shopping-cart`
  - `items[].variant`
  - `items[].attributes.optionValues[]`
  - `items[].attributes.customizations[]`
  - `items[].attributes.fulfillment`
  - `items[].lineTotals`
  - `pricing.breakdown[]`
  - `shippingPreference`
- `checkout-session`
  - `customer.customerRef`
  - `shippingOption.estimatedDelivery`
  - `totals.breakdown[]`
  - `paymentPreference.captureMode`
  - `orderLifecycle`
  - `notifications[]`
- `search-document`
  - `routing`
  - `filters[]`
  - `sortValues[]`
  - richer `seoSignals`

This is the preferred pattern for dynamic commerce entities in this repo:

- use typed nested lists/objects
- use relation objects for cross-service links
- use key/value entry lists when a field must stay extensible but still searchable

## Near-term architecture

These services are now added as dynamic-runtime-capable modules. The next implementation layer should be:

1. internal clients between:
   - `storefront-service` -> content/catalog/search-index
   - `checkout-service` -> cart/pricing-promotion/payment-orchestrator/commerce
   - `payment-orchestrator-service` -> payment-service
   - `search-index-service` -> content/catalog/commerce/report
2. event-driven projection updates through Kafka
3. service-specific processors/operators for:
   - SEO field normalization
   - pricing calculations
   - checkout readiness checks
   - payment session state transitions
   - search projection materialization

## Important principle

Do not turn these into fixed-schema only services.

They should stay definition-driven, with:

- default nested fields
- dynamic extension points
- relations between structured entities
- strong validation boundaries

That is what makes the platform AI-safe: AI generates structured entity definitions, not unbounded JSON blobs.

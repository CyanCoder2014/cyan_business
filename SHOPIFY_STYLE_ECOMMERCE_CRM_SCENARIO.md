# Shopify-Style E-Commerce + CRM Scenario

This scenario uses the dynamic entity runtime in this repo to build:

- landing pages
- blog content
- product catalog
- CRM contacts and leads
- shop orders
- finance transactions
- dynamic report definitions

Payment microservice is intentionally skipped. Payment is represented as a `finance-service` transaction record.

## Automated HTTP integration test

A lightweight live HTTP integration test was added here:

- [report-service/src/test/java/com/cyancoder/report/integration/DynamicServicesScenarioHttpIT.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/report-service/src/test/java/com/cyancoder/report/integration/DynamicServicesScenarioHttpIT.java:1)

It is opt-in and assumes the services are already running on localhost.

Example run:

```bash
bash ./gradlew :report-service:test --tests com.cyancoder.report.integration.DynamicServicesScenarioHttpIT -Ddynamic.http.it=true
```

It verifies:

- dynamic definition creation
- content record submission
- catalog record submission
- CRM record submission
- order submission
- finance transaction submission
- report-definition submission
- record lookup on `commerce-service`

## Runtime mode used for this test

Because PostgreSQL was not consistently available in this environment, the dynamic scenario services were started with the `localdemo` profile where needed:

- `content-service`
- `catalog-service`
- `crm-service`
- `commerce-service`
- `finance-service`
- `report-service`

That profile keeps Mongo for dynamic records and uses H2 for service metadata/JPA state.

## Ports and internal auth

These steps use `/internal/**` endpoints with basic auth for local integration testing.

| Service | Port | Basic auth |
|---|---:|---|
| `content-service` | `9101` | `content_internal:content_secret` |
| `catalog-service` | `9102` | `catalog_internal:catalog_secret` |
| `crm-service` | `9103` | `crm_internal:crm_secret` |
| `commerce-service` | `9104` | `commerce_internal:commerce_secret` |
| `finance-service` | `9105` | `finance_internal:finance_secret` |
| `report-service` | `9107` | `report_internal:report_secret` |

## Important behavior from the dynamic runtime

The dynamic runtime is strict:

- extra fields are rejected
- missing nested fields are rejected
- nested list/object items must include every declared child field

Example: the `landing-page.sections[]` structure required `ctaLabel` and `ctaUrl` even for a `FEATURES` block, so empty strings were sent explicitly.

## 1. Create dynamic entity definitions

### 1.1 Landing page

```bash
curl -s -u content_internal:content_secret \
  -H 'Content-Type: application/json' \
  -d '{"entityKey":"shop-landing-page"}' \
  http://127.0.0.1:9101/internal/entities/templates/landing-page/definitions
```

### 1.2 Blog post

```bash
curl -s -u content_internal:content_secret \
  -H 'Content-Type: application/json' \
  -d '{"entityKey":"shop-blog-post"}' \
  http://127.0.0.1:9101/internal/entities/templates/blog-page/definitions
```

### 1.3 Product

```bash
curl -s -u catalog_internal:catalog_secret \
  -H 'Content-Type: application/json' \
  -d '{"entityKey":"shop-product"}' \
  http://127.0.0.1:9102/internal/entities/templates/catalog-product/definitions
```

### 1.4 CRM contact

```bash
curl -s -u crm_internal:crm_secret \
  -H 'Content-Type: application/json' \
  -d '{"entityKey":"shop-contact"}' \
  http://127.0.0.1:9103/internal/entities/templates/crm-contact/definitions
```

### 1.5 CRM lead

```bash
curl -s -u crm_internal:crm_secret \
  -H 'Content-Type: application/json' \
  -d '{"entityKey":"shop-lead"}' \
  http://127.0.0.1:9103/internal/entities/templates/crm-lead/definitions
```

### 1.6 Order

```bash
curl -s -u commerce_internal:commerce_secret \
  -H 'Content-Type: application/json' \
  -d '{"entityKey":"shop-order"}' \
  http://127.0.0.1:9104/internal/entities/templates/sales-order/definitions
```

### 1.7 Transaction

```bash
curl -s -u finance_internal:finance_secret \
  -H 'Content-Type: application/json' \
  -d '{"entityKey":"shop-transaction"}' \
  http://127.0.0.1:9105/internal/entities/templates/finance-transaction/definitions
```

### 1.8 Report definition entity

```bash
curl -s -u report_internal:report_secret \
  -H 'Content-Type: application/json' \
  -d '{"entityKey":"shop-order-report"}' \
  http://127.0.0.1:9107/internal/entities/templates/dynamic-report/definitions
```

## 2. Create content records

### 2.1 Landing page

```bash
curl -s -u content_internal:content_secret \
  -H 'Content-Type: application/json' \
  -d '{
    "slug":"acme-store",
    "title":"Acme Store",
    "heroTitle":"Launch your next purchase",
    "heroSubtitle":"Curated gadgets with fast delivery.",
    "publicationStatus":"PUBLISHED",
    "sections":[
      {
        "blockType":"FEATURES",
        "title":"Why Acme",
        "body":"Fast shipping, curated products and live support.",
        "ctaLabel":"",
        "ctaUrl":""
      },
      {
        "blockType":"CTA",
        "title":"Shop now",
        "body":"",
        "ctaLabel":"Browse catalog",
        "ctaUrl":"/shop"
      }
    ]
  }' \
  'http://127.0.0.1:9101/internal/entities/submit/shop-landing-page?recordKey=landing-home'
```

Test result: succeeded.

### 2.2 Blog post

```bash
curl -s -u content_internal:content_secret \
  -H 'Content-Type: application/json' \
  -d '{
    "slug":"how-to-choose-a-smartwatch",
    "title":"How to choose a smartwatch",
    "summary":"A short guide for choosing the right smartwatch.",
    "body":"Choosing a smartwatch depends on battery life, display quality, comfort, fitness features and the apps you actually use every week.",
    "author":"Acme Editorial",
    "publicationStatus":"PUBLISHED",
    "tags":["wearables","guide"]
  }' \
  'http://127.0.0.1:9101/internal/entities/submit/shop-blog-post?recordKey=blog-smartwatch-guide'
```

Test result: succeeded.

## 3. Create catalog records

### 3.1 Product 1

```bash
curl -s -u catalog_internal:catalog_secret \
  -H 'Content-Type: application/json' \
  -d '{
    "name":"Acme Smartwatch X1",
    "sku":"WATCH-X1",
    "categoryKey":"wearables",
    "unit":"pcs",
    "defaultPrice":12500000,
    "currency":"IRR",
    "active":true,
    "details":{
      "brand":"Acme",
      "model":"X1",
      "shortDescription":"AMOLED smartwatch with health tracking"
    }
  }' \
  'http://127.0.0.1:9102/internal/entities/submit/shop-product?recordKey=product-watch-x1'
```

### 3.2 Product 2

```bash
curl -s -u catalog_internal:catalog_secret \
  -H 'Content-Type: application/json' \
  -d '{
    "name":"Acme Earbuds Pro",
    "sku":"EARBUDS-PRO",
    "categoryKey":"audio",
    "unit":"pcs",
    "defaultPrice":4800000,
    "currency":"IRR",
    "active":true,
    "details":{
      "brand":"Acme",
      "model":"Pro",
      "shortDescription":"Noise cancelling wireless earbuds"
    }
  }' \
  'http://127.0.0.1:9102/internal/entities/submit/shop-product?recordKey=product-earbuds-pro'
```

Test result: both succeeded.

## 4. Create CRM records

### 4.1 Contact

```bash
curl -s -u crm_internal:crm_secret \
  -H 'Content-Type: application/json' \
  -d '{
    "fullName":"Sara Ahmadi",
    "companyName":"Acme Retail",
    "email":"sara@acme.example",
    "mobile":"09121234567",
    "status":"ACTIVE",
    "source":"SHOP",
    "notes":"Primary e-commerce customer profile"
  }' \
  'http://127.0.0.1:9103/internal/entities/submit/shop-contact?recordKey=contact-sara-ahmadi'
```

### 4.2 Lead

```bash
curl -s -u crm_internal:crm_secret \
  -H 'Content-Type: application/json' \
  -d '{
    "fullName":"Sara Ahmadi",
    "companyName":"Acme Retail",
    "email":"sara@acme.example",
    "mobile":"09121234567",
    "status":"QUALIFIED",
    "source":"LANDING_PAGE",
    "ownerUserId":"sales-01",
    "notes":"Requested pricing for smartwatch bundle"
  }' \
  'http://127.0.0.1:9103/internal/entities/submit/shop-lead?recordKey=lead-sara-ahmadi'
```

Test result: both succeeded.

## 5. Create order record

```bash
curl -s -u commerce_internal:commerce_secret \
  -H 'Content-Type: application/json' \
  -d '{
    "customerKey":"contact-sara-ahmadi",
    "currency":"IRR",
    "documentStatus":"SUBMITTED",
    "subtotal":17300000,
    "discountTotal":0,
    "taxTotal":1730000,
    "grandTotal":19030000,
    "items":[
      {
        "itemKey":"product-watch-x1",
        "name":"Acme Smartwatch X1",
        "quantity":1,
        "unitPrice":12500000,
        "lineTotal":12500000
      },
      {
        "itemKey":"product-earbuds-pro",
        "name":"Acme Earbuds Pro",
        "quantity":1,
        "unitPrice":4800000,
        "lineTotal":4800000
      }
    ]
  }' \
  'http://127.0.0.1:9104/internal/entities/submit/shop-order?recordKey=order-1001'
```

Test result: succeeded.

## 6. Create finance transaction

```bash
curl -s -u finance_internal:finance_secret \
  -H 'Content-Type: application/json' \
  -d '{
    "transactionType":"PAYMENT",
    "referenceType":"ORDER",
    "referenceKey":"order-1001",
    "accountKey":"gateway-zarinpal",
    "currency":"IRR",
    "amount":19030000,
    "status":"CONFIRMED",
    "description":"Card payment captured for order-1001"
  }' \
  'http://127.0.0.1:9105/internal/entities/submit/shop-transaction?recordKey=txn-1001'
```

Test result: succeeded.

## 7. Create dynamic report record

```bash
curl -s -u report_internal:report_secret \
  -H 'Content-Type: application/json' \
  -d '{
    "reportKey":"shop-order-status-report",
    "title":"Shop Order Status Report",
    "sourceType":"DYNAMIC",
    "serviceKey":"commerce-service",
    "entityKey":"shop-order",
    "defaultFilterField":"",
    "defaultSumField":"grandTotal",
    "groupByField":"documentStatus",
    "filters":[]
  }' \
  'http://127.0.0.1:9107/internal/entities/submit/shop-order-report?recordKey=report-order-status'
```

Test result: succeeded.

## 8. Run dynamic report

```bash
curl -s -u report_internal:report_secret \
  -H 'Content-Type: application/json' \
  -d '{
    "filters":[],
    "sumField":"grandTotal",
    "groupByField":"documentStatus"
  }' \
  http://127.0.0.1:9107/internal/dynamic-reports/shop-order-report/report-order-status/run
```

## Current status of the report run step

The scenario data and report definition were created successfully, but the final report execution is not fully green in this environment yet.

What was done:

- `DynamicReportQueryService` was patched to prefer `localhost:<port>` instead of the Eureka machine IP for local service-to-service fetches.
- The source change is in:
  - [report-service/src/main/java/com/cyancoder/report/service/DynamicReportQueryService.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/report-service/src/main/java/com/cyancoder/report/service/DynamicReportQueryService.java:1)

What blocked full live verification:

- Gradle rebuilds for `report-service` were interrupted by the active Gradle mirror returning `402`.
- Manual direct-Java launches required classpath surgery because the sandbox/runtime environment and broad cached classpath introduced extra auto-configuration conflicts.

So the tested state is:

- definitions: passed
- content records: passed
- catalog records: passed
- CRM records: passed
- order record: passed
- finance transaction: passed
- report definition record: passed
- final report execution: source patched, live re-verification still pending

## Useful follow-up curls

### List shop orders

```bash
curl -s -u commerce_internal:commerce_secret \
  http://127.0.0.1:9104/internal/entities/records/shop-order
```

### Get one CRM contact

```bash
curl -s -u crm_internal:crm_secret \
  http://127.0.0.1:9103/internal/entities/records/shop-contact/contact-sara-ahmadi
```

### Validate a payload before submit

```bash
curl -s -u content_internal:content_secret \
  -H 'Content-Type: application/json' \
  -d '{
    "slug":"preview-page",
    "title":"Preview",
    "heroTitle":"Preview hero",
    "heroSubtitle":"test",
    "publicationStatus":"PUBLISHED",
    "sections":[
      {
        "blockType":"TEXT",
        "title":"Intro",
        "body":"hello",
        "ctaLabel":"",
        "ctaUrl":""
      }
    ]
  }' \
  http://127.0.0.1:9101/internal/entities/records/shop-landing-page/validate
```

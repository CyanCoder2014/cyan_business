# Storefront Tenant/Site Scenario

This scenario shows how to use `storefront-service` as a scoped public website layer for multiple clients.

It uses:

- `tenantKey = tenant-demo`
- `siteKey = site-shop-a`

All dynamic records and definitions are isolated with:

- `X-Tenant-Key`
- `X-Site-Key`

## 1. Create scoped definitions

Create `theme-layout`:

```bash
curl -s -u storefront_internal:storefront_secret \
  -H 'X-Tenant-Key: tenant-demo' \
  -H 'X-Site-Key: site-shop-a' \
  -X POST \
  -H 'Content-Type: application/json' \
  -d '{}' \
  http://localhost:9115/internal/entities/templates/theme-layout/definitions
```

Create `site-route`:

```bash
curl -s -u storefront_internal:storefront_secret \
  -H 'X-Tenant-Key: tenant-demo' \
  -H 'X-Site-Key: site-shop-a' \
  -X POST \
  -H 'Content-Type: application/json' \
  -d '{}' \
  http://localhost:9115/internal/entities/templates/site-route/definitions
```

## 2. Create scoped theme record

Use the raw-map submit endpoint:

```bash
curl -s -u storefront_internal:storefront_secret \
  -H 'X-Tenant-Key: tenant-demo' \
  -H 'X-Site-Key: site-shop-a' \
  -X POST \
  -H 'Content-Type: application/json' \
  'http://localhost:9115/internal/entities/submit/theme-layout?recordKey=theme-main' \
  -d '{
    "themeKey":"theme-main",
    "brandName":"Tenant Demo Shop",
    "status":"ACTIVE",
    "navigation":[
      {
        "label":"Home",
        "path":"/",
        "children":[]
      }
    ],
    "globalSeo":{
      "siteName":"Tenant Demo Shop",
      "defaultTitleTemplate":"%s | Tenant Demo Shop",
      "defaultDescription":"Structured storefront demo",
      "organizationJsonLd":"{}"
    },
    "blocks":[
      {
        "blockKey":"hero",
        "componentType":"hero-banner",
        "props":{}
      }
    ]
  }'
```

## 3. Create scoped content and catalog definitions

Create `landing-page` in `content-service`:

```bash
curl -s -u content_internal:content_secret \
  -H 'X-Tenant-Key: tenant-demo' \
  -H 'X-Site-Key: site-shop-a' \
  -X POST \
  -H 'Content-Type: application/json' \
  -d '{}' \
  http://localhost:9101/internal/entities/templates/landing-page/definitions
```

Create `catalog-product` in `catalog-service`:

```bash
curl -s -u catalog_internal:catalog_secret \
  -H 'X-Tenant-Key: tenant-demo' \
  -H 'X-Site-Key: site-shop-a' \
  -X POST \
  -H 'Content-Type: application/json' \
  -d '{}' \
  http://localhost:9102/internal/entities/templates/catalog-product/definitions
```

## 4. Create scoped content and product records

Create the home landing page:

```bash
curl -s -u content_internal:content_secret \
  -H 'X-Tenant-Key: tenant-demo' \
  -H 'X-Site-Key: site-shop-a' \
  -X POST \
  -H 'Content-Type: application/json' \
  'http://localhost:9101/internal/entities/submit/landing-page?recordKey=landing-home' \
  -d '{
    "slug":"home",
    "title":"Tenant Demo Shop",
    "heroTitle":"Launch Your Store Faster",
    "heroSubtitle":"Structured commerce, CRM, and content on one platform.",
    "publicationStatus":"PUBLISHED",
    "sections":[
      {
        "blockType":"FEATURES",
        "title":"Everything you need",
        "body":"Landing pages, catalog, checkout, CRM, and reporting.",
        "ctaLabel":"",
        "ctaUrl":""
      },
      {
        "blockType":"CTA",
        "title":"Start selling today",
        "body":"Build products and manage orders dynamically.",
        "ctaLabel":"Browse products",
        "ctaUrl":"/products/starter-pack"
      }
    ]
  }'
```

Create the product:

```bash
curl -s -u catalog_internal:catalog_secret \
  -H 'X-Tenant-Key: tenant-demo' \
  -H 'X-Site-Key: site-shop-a' \
  -X POST \
  -H 'Content-Type: application/json' \
  'http://localhost:9102/internal/entities/submit/catalog-product?recordKey=product-starter-pack' \
  -d '{
    "itemType":"PRODUCT",
    "name":"Starter Pack",
    "sku":"STARTER_PACK",
    "categoryKey":"platform",
    "unit":"package",
    "defaultPrice":2500000,
    "compareAtPrice":3000000,
    "currency":"IRR",
    "active":true,
    "slug":"starter-pack",
    "seo":{
      "title":"Starter Pack",
      "description":"Commerce starter bundle for modern online stores.",
      "canonicalUrl":"https://demo.example.com/products/starter-pack",
      "robots":"index,follow",
      "schemaType":"Product",
      "faqEntries":[
        {
          "question":"What is included?",
          "answer":"Dynamic storefront, product setup, and CRM foundations."
        }
      ]
    },
    "media":[
      {
        "assetRef":{
          "service":"media-service",
          "entityKey":"media-asset",
          "recordKey":""
        },
        "url":"https://cdn.example.com/products/starter-pack/cover.jpg",
        "alt":"Starter Pack cover",
        "sortOrder":1,
        "primary":true
      }
    ],
    "attributes":[
      {
        "attributeKey":"delivery",
        "label":"Delivery",
        "valueType":"TEXT",
        "stringValue":"Instant access",
        "numberValue":0,
        "booleanValue":false,
        "listValues":[]
      },
      {
        "attributeKey":"support",
        "label":"Support",
        "valueType":"TEXT",
        "stringValue":"30 days",
        "numberValue":0,
        "booleanValue":false,
        "listValues":[]
      }
    ],
    "variants":[
      {
        "variantKey":"starter-default",
        "title":"Starter Default",
        "sku":"STARTER_PACK_DEFAULT",
        "price":2500000,
        "compareAtPrice":3000000,
        "inventory":{
          "stockQuantity":100,
          "trackInventory":true,
          "allowBackorder":false
        },
        "optionValues":[
          {
            "optionKey":"license",
            "value":"single-store"
          }
        ]
      }
    ],
    "routing":{
      "primaryPath":"/products/starter-pack",
      "collectionPaths":["/products"],
      "sitemapPriority":0.7,
      "changeFrequency":"weekly"
    },
    "searchIndex":{
      "keywords":["starter","commerce","crm"],
      "filterEntries":[
        {
          "key":"category",
          "label":"Category",
          "valueType":"TEXT",
          "stringValue":"platform",
          "numberValue":0,
          "booleanValue":false,
          "listValues":[]
        },
        {
          "key":"delivery",
          "label":"Delivery",
          "valueType":"TEXT",
          "stringValue":"instant",
          "numberValue":0,
          "booleanValue":false,
          "listValues":[]
        }
      ],
      "sortEntries":[
        {
          "key":"price",
          "numberValue":2500000,
          "stringValue":""
        },
        {
          "key":"name",
          "numberValue":0,
          "stringValue":"Starter Pack"
        }
      ]
    },
    "details":{
      "brand":"Tenant Demo",
      "model":"Starter Pack",
      "shortDescription":"Launch a small online store quickly.",
      "longDescription":"A structured commerce starter pack with dynamic storefront, CRM, checkout preparation, and reporting support."
    }
  }'
```

## 5. Create scoped published routes

Home route to the scoped `landing-page` record:

```bash
curl -s -u storefront_internal:storefront_secret \
  -H 'X-Tenant-Key: tenant-demo' \
  -H 'X-Site-Key: site-shop-a' \
  -X POST \
  -H 'Content-Type: application/json' \
  'http://localhost:9115/internal/entities/submit/site-route?recordKey=home-route' \
  -d '{
    "routeKey":"home-route",
    "path":"/",
    "routeType":"LANDING",
    "entityRef":{
      "service":"content-service",
      "entityKey":"landing-page",
      "recordKey":"landing-home"
    },
    "navigation":{
      "label":"Home",
      "menuKey":"main",
      "sortOrder":1,
      "visible":"true"
    },
    "seo":{
      "title":"Tenant Demo Shop",
      "description":"Structured commerce and CRM storefront demo.",
      "canonicalUrl":"https://demo.example.com/",
      "robots":"index,follow",
      "ogImage":"",
      "twitterCard":"summary",
      "structuredDataBlocks":[]
    },
    "rendering":{
      "themeKey":"theme-main",
      "templateKey":"home",
      "cacheTtlSeconds":120,
      "preloadAssets":[],
      "hydrateTargetEntity":"true"
    },
    "indexingEnabled":"true",
    "sitemapPriority":"0.8",
    "publicationStatus":"PUBLISHED",
    "routeLifecycle":{
      "validFrom":"2026-01-01T00:00:00Z",
      "validTo":"2030-01-01T00:00:00Z",
      "redirectTo":"",
      "httpStatus":200
    }
  }'
```

Product route to the scoped `catalog-product` record:

```bash
curl -s -u storefront_internal:storefront_secret \
  -H 'X-Tenant-Key: tenant-demo' \
  -H 'X-Site-Key: site-shop-a' \
  -X POST \
  -H 'Content-Type: application/json' \
  'http://localhost:9115/internal/entities/submit/site-route?recordKey=product-route-starter-pack' \
  -d '{
    "routeKey":"product-route-starter-pack",
    "path":"/products/starter-pack",
    "routeType":"PRODUCT",
    "entityRef":{
      "service":"catalog-service",
      "entityKey":"catalog-product",
      "recordKey":"product-starter-pack"
    },
    "navigation":{
      "label":"Starter Pack",
      "menuKey":"catalog",
      "sortOrder":1,
      "visible":"true"
    },
    "seo":{
      "title":"Starter Pack",
      "description":"Commerce starter bundle for modern online stores.",
      "canonicalUrl":"https://demo.example.com/products/starter-pack",
      "robots":"index,follow",
      "ogImage":"https://cdn.example.com/products/starter-pack/cover.jpg",
      "twitterCard":"summary_large_image",
      "structuredDataBlocks":[]
    },
    "rendering":{
      "themeKey":"theme-main",
      "templateKey":"product",
      "cacheTtlSeconds":120,
      "preloadAssets":["https://cdn.example.com/products/starter-pack/cover.jpg"],
      "hydrateTargetEntity":"true"
    },
    "indexingEnabled":"true",
    "sitemapPriority":"0.7",
    "publicationStatus":"PUBLISHED",
    "routeLifecycle":{
      "validFrom":"2026-01-01T00:00:00Z",
      "validTo":"2030-01-01T00:00:00Z",
      "redirectTo":"",
      "httpStatus":200
    }
  }'
```

## 6. Resolve the routes publicly

```bash
curl -s \
  -H 'X-Tenant-Key: tenant-demo' \
  -H 'X-Site-Key: site-shop-a' \
  'http://localhost:9115/public/storefront/resolve?path=/'
```

```bash
curl -s \
  -H 'X-Tenant-Key: tenant-demo' \
  -H 'X-Site-Key: site-shop-a' \
  'http://localhost:9115/public/storefront/resolve?path=/products/starter-pack'
```

Expected result:

- scoped route is found
- scoped target record is hydrated from `content-service` or `catalog-service`
- scoped theme is hydrated from `storefront-service`

## 7. Get JSON render output

```bash
curl -s \
  -H 'X-Tenant-Key: tenant-demo' \
  -H 'X-Site-Key: site-shop-a' \
  'http://localhost:9115/public/storefront/render?path=/'
```

```bash
curl -s \
  -H 'X-Tenant-Key: tenant-demo' \
  -H 'X-Site-Key: site-shop-a' \
  'http://localhost:9115/public/storefront/render?path=/products/starter-pack'
```

Expected result:

- all route data
- hydrated target from the referenced microservice
- hydrated theme
- generated `html`

## 8. Get HTML page output

```bash
curl -s \
  -H 'X-Tenant-Key: tenant-demo' \
  -H 'X-Site-Key: site-shop-a' \
  'http://localhost:9115/public/storefront/page?path=/'
```

```bash
curl -s \
  -H 'X-Tenant-Key: tenant-demo' \
  -H 'X-Site-Key: site-shop-a' \
  'http://localhost:9115/public/storefront/page?path=/products/starter-pack'
```

Expected result:

- HTML document with:
  - `<title>`
  - meta description
  - brand header
  - basic page body

## 9. Get sitemap for the scoped site

```bash
curl -s \
  -H 'X-Tenant-Key: tenant-demo' \
  -H 'X-Site-Key: site-shop-a' \
  'http://localhost:9115/public/storefront/sitemap'
```

Expected result:

- only `PUBLISHED` and `indexingEnabled=true` routes
- scoped to the selected tenant/site

## Notes

- Use `submit/{entityKey}` when you want the simplest raw-map flow, closest to `naviya-core`.
- If another client uses a different tenant/site pair, their definitions and records stay isolated.
- The route target can point to `content-service`, `catalog-service`, or another dynamic service as long as the target records use the same `X-Tenant-Key` and `X-Site-Key` values.

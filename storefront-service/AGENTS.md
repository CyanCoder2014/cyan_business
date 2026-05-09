# storefront-service Agent Guide

## Purpose
`storefront-service` is the public website composition layer. It resolves tenant/site routes, theme data, SEO metadata, sitemap output, and rendered page responses.

## Owns
- templates `site-route` and `theme-layout`
- public route resolution and render endpoints

## Main APIs
- `GET /public/storefront/resolve`
- `GET /public/storefront/render`
- `GET /public/storefront/page`
- `GET /public/storefront/sitemap`
- `GET /public/storefront/sitemap.xml`
- `GET /public/storefront/robots.txt`

## Dependencies
- `content-service`
- `catalog-service`
- `media-service`
- tenant/site scoped definitions and records

## Flow Role
1. Resolve incoming path to a route record.
2. Fetch referenced entity data.
3. Merge route, theme, and target record into response or HTML.

## Change Rules
- Preserve `X-Tenant-Key` and `X-Site-Key` scoping.
- SEO fields here affect indexing and public correctness.

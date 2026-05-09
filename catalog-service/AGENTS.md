# catalog-service Agent Guide

## Purpose
`catalog-service` owns structured product and service catalog data.

## Owns
- product-like definitions and records
- templates such as `catalog-product` and `catalog-service-offer`

## Main APIs
- dynamic runtime endpoint/internal entity APIs

## Dependencies
- `dynamic-entity-core`
- consumed by storefront, cart, pricing, inventory, search, reporting, and AI provisioning

## Flow Role
1. Define product schema from a template.
2. Store product, variant, media, SEO, and routing data.
3. Feed downstream cart, search, storefront, and reporting features.

## Change Rules
- Product references are used by multiple services; keep relation fields stable.

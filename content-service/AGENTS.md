# content-service Agent Guide

## Purpose
`content-service` owns dynamic website content such as landing pages, blog pages, and CMS-style records.

## Owns
- structured content definitions and records
- templates such as `blog-page` and `landing-page`

## Main APIs
- dynamic runtime endpoint/internal entity APIs

## Dependencies
- `dynamic-entity-core`
- consumed by `storefront-service`, `report-service`, `search-index-service`, and AI provisioning

## Flow Role
1. Create a content definition from a template.
2. Accept content records under strict validation.
3. Serve content to storefront, search, and reporting layers.

## Change Rules
- Keep content SEO fields and route references compatible with storefront resolution.

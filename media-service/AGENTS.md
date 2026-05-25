# media-service Agent Guide

## Purpose
`media-service` owns media asset metadata and public asset lookup behavior.

## Owns
- media definitions and records
- internal upload-preparation API
- public asset and variant metadata APIs

## Main APIs
- `POST /internal/media/assets/prepare-upload`
- `GET /internal/media/assets/{assetKey}`
- `GET /public/media/assets/{assetKey}`
- `GET /public/media/assets/{assetKey}/variants/{variantKey}`

## Dependencies
- referenced by catalog and storefront records

## Change Rules
- Keep public asset contracts stable because storefront and SEO output depend on them.

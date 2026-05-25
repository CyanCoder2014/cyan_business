# search-index-service Agent Guide

## Purpose
`search-index-service` provides public search and suggestion APIs plus internal index sync endpoints.

## Owns
- search index definitions/records
- sync and query behavior

## Main APIs
- `GET /public/search-index/search`
- `GET /public/search-index/suggest`
- `POST /internal/search-index/sync/{sourceServiceKey}/{sourceEntityKey}`
- `GET /internal/search-index/search`

## Dependencies
- source services such as content and catalog
- storefront/public discovery use cases

## Change Rules
- Keep source-service reference fields explicit so reindexing remains possible.

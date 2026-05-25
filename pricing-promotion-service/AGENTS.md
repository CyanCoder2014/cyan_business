# pricing-promotion-service Agent Guide

## Purpose
`pricing-promotion-service` evaluates cart or checkout pricing, including totals and promotion effects.

## Owns
- pricing/promotion definitions and records
- internal pricing evaluation API

## Main APIs
- `POST /internal/pricing-promotions/evaluate`

## Dependencies
- `cart-service`
- `checkout-service`
- catalog/product-linked data

## Change Rules
- Pricing outputs feed checkout state directly; keep totals deterministic and backward compatible.

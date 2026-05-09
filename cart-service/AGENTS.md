# cart-service Agent Guide

## Purpose
`cart-service` owns shopping-cart state for sessions and customers using structured dynamic records.

## Owns
- template `shopping-cart`
- cart item, pricing snapshot, promotion, and shipping-preference data

## Dependencies
- `dynamic-entity-core`
- `catalog-service` references
- `checkout-service`

## Flow Role
1. Store cart as a dynamic record.
2. Carry product refs, quantities, and line totals.
3. Hand cart reference into checkout preparation.

## Change Rules
- Cart record shape is used directly by `checkout-service`; coordinate schema changes.

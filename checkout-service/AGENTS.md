# checkout-service Agent Guide

## Purpose
`checkout-service` prepares and advances checkout lifecycle state by combining cart, pricing, payment orchestration, and notifications.

## Owns
- checkout definitions and records
- lifecycle transitions such as payment pending, verified, failed, confirmed

## Main APIs
- `/internal/checkout/**` preparation and payment lifecycle endpoints
- dynamic runtime endpoint/internal entity APIs

## Dependencies
- `cart-service`
- `pricing-promotion-service`
- `payment-orchestrator-service`
- `notification-service`

## Flow Role
1. Load checkout record and referenced cart.
2. Evaluate pricing and payment methods.
3. Initiate payment session.
4. Verify payment result.
5. Update checkout lifecycle and optionally send notifications.

## Change Rules
- This service is orchestration-heavy; keep source-of-truth ownership clear between cart, payment, and order systems.

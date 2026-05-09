# payment-service Agent Guide

## Purpose
`payment-service` owns payment methods, transaction initiation, verification, and public callback handling.

## Owns
- payment method administration
- payment transaction records
- provider callback surface

## Main APIs
- `/endpoint/payment/methods`
- `/endpoint/payment/admin/methods`
- `/endpoint/payment/transactions/**`
- matching `/internal/payment/**`
- `/public/payment/callback/{providerCode}/{transactionKey}`

## Dependencies
- used by `payment-orchestrator-service`

## Change Rules
- Public callback behavior is externally sensitive; preserve provider contract stability.

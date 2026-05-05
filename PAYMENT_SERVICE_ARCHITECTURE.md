# Payment Service Architecture

## Why this service exists

`../../../vpg` already proves the payment-provider split is needed, but it is not enough as a reusable commerce payment microservice for this repo.

Useful ideas from `vpg`:

- provider-specific payment implementations
- one selector/factory to choose the implementation
- redirect + callback + verify lifecycle

Weak parts in `vpg` that were not copied directly:

- `BankGateway` config model is too thin for admin-managed commerce
- transaction model is Mongo-first and too loosely structured
- callback logic is bank-specific at controller level
- admin management of available payment methods and configs is incomplete

## New service design

The new module is `payment-service`.

It uses:

- `PaymentProviderStrategy`
- `PaymentProviderRegistry`
- one strategy per provider
- PostgreSQL for method config and transaction history
- `/endpoint/**` bearer-token APIs
- `/internal/**` basic-auth APIs
- `/public/**` callback and simulator APIs

## Supported payment methods

Seeded methods:

- `tejarat-default`
- `sep-default`
- `zarinpal-default`
- `payir-default`
- `paypal-default`
- `visa-default`
- `mastercard-default`

These are seeded as enabled and active in local mock mode so the service is usable immediately. Admin can still disable, reprioritize, or replace configs.

## Admin responsibilities

Admin can manage:

- available payment methods
- activation/enabling
- sort priority
- supported currencies
- provider config JSON

## Main APIs

### End-user and admin APIs

- `GET /endpoint/payment/methods`
- `GET /endpoint/payment/admin/methods`
- `GET /endpoint/payment/admin/methods/{methodKey}`
- `POST /endpoint/payment/admin/methods`
- `PUT /endpoint/payment/admin/methods/{methodKey}`
- `DELETE /endpoint/payment/admin/methods/{methodKey}`
- `POST /endpoint/payment/transactions/initiate`
- `POST /endpoint/payment/transactions/{transactionKey}/verify`
- `GET /endpoint/payment/transactions/{transactionKey}`
- `GET /endpoint/payment/transactions`

### Internal service APIs

- `GET /internal/payment/methods`
- `GET /internal/payment/admin/methods`
- `GET /internal/payment/admin/methods/{methodKey}`
- `POST /internal/payment/admin/methods`
- `PUT /internal/payment/admin/methods/{methodKey}`
- `POST /internal/payment/transactions/initiate`
- `POST /internal/payment/transactions/{transactionKey}/verify`
- `GET /internal/payment/transactions/{transactionKey}`
- `GET /internal/payment/transactions`

### Public callback APIs

- `GET /public/payment/callback/{providerCode}/{transactionKey}`
- `POST /public/payment/callback/{providerCode}/{transactionKey}`
- `GET /public/payment/simulate/{providerCode}/{transactionKey}`

## Important model choices

- `PaymentMethodEntity` stores provider config and availability state
- `PaymentTransactionEntity` stores order/invoice/customer relation fields
- relations are explicit:
  - `relatedService`
  - `relatedEntityType`
  - `relatedEntityKey`
- this allows payment to connect to `commerce-service`, `finance-service`, CRM, or future BPM flows

## Production note

The current provider strategies are integration-ready strategy shells with seeded config templates and deterministic local simulation.

They are enough to:

- let admin choose and configure methods
- let the shopper select a method
- create payment transactions
- handle redirect-style callback verification
- support local development and future real API integrations

Before production use per provider, each strategy should be extended with the real upstream API client and signature/error mapping for that provider.

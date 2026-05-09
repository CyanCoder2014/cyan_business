# AI Call Integration Test Guide

## Purpose
This document explains how to test the AI generation and provisioning path from an API call into `ai-orchestrator-service`, and how to extend that flow to a real client project.

## Automated Coverage
The repository now includes an integration-style Spring test:

- `AiPlatformGenerationControllerIntegrationTest`

It verifies two critical paths:

1. authenticated call to `POST /endpoint/ai-orchestrator/generate/app` returns a DSL and executes provisioning when no follow-up questions remain
2. authenticated call to the same endpoint returns follow-up questions and does not provision when required answers are missing

The test runs locally without live downstream services because platform metadata, LLM generation, and provisioning clients are mocked.

## Run The Automated Test
From repository root:

```bash
./gradlew :ai-orchestrator-service:test --tests com.cyancoder.aiorchestrator.api.AiPlatformGenerationControllerIntegrationTest
```

## What The Automated Test Covers
- endpoint security with JWT-authenticated access
- controller to service wiring
- prompt build and DSL generation handoff
- DSL validation against platform service metadata
- provisioning orchestration through `PlatformProvisioningService`
- skip-provision behavior when AI follow-up questions remain

## Real Local End-To-End Flow
Use this when you want to test a real client project bootstrap instead of the mocked integration test.

### 1. Start required infrastructure
At minimum you need:

- `discovery-server`
- `api-gateway`
- `sso-auth-service`
- `ai-orchestrator-service`
- target services you want provisioned, usually:
  - `content-service`
  - `catalog-service`
  - `storefront-service`
  - `bpm-service`
  - `notification-service`
  - `checkout-service`
  - `payment-orchestrator-service`

For dynamic services you also need their backing PostgreSQL and MongoDB instances.

### 2. Get a bearer token
Use the SSO flow already configured in the platform and obtain a JWT issued by `sso-auth-service`.

### 3. Call the AI endpoint in planning mode
This creates a generated DSL without applying it yet.

Direct service:

```bash
curl -s \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -X POST http://localhost:9121/endpoint/ai-orchestrator/generate/app \
  -d '{
    "prompt": "Create a storefront for Demo Shop with one starter product and checkout",
    "tenantKey": "tenant-demo",
    "siteKey": "site-demo",
    "clientKey": "client-demo",
    "execute": false,
    "answers": {
      "brandName": "Demo Shop",
      "homePageTitle": "Launch Faster",
      "starterProductName": "Starter Product",
      "starterProductSku": "STARTER-001",
      "paymentProvider": "zarinpal-default"
    }
  }'
```

Gateway route:

```bash
curl -s \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -X POST http://localhost:8001/endpoint/ai-orchestrator/generate/app \
  -d '{
    "prompt": "Create a storefront for Demo Shop with one starter product and checkout",
    "tenantKey": "tenant-demo",
    "siteKey": "site-demo",
    "clientKey": "client-demo",
    "execute": false
  }'
```

Expected result:

- `dsl` is present
- `nextQuestions` is either empty or contains missing business decisions
- `provisioningResult` is `null`

### 4. Resolve follow-up questions
If `nextQuestions` is not empty, call again with the required `answers` until they are resolved.

Typical examples:

- `brandName`
- `homePageTitle`
- `starterProductName`
- `starterProductSku`
- `paymentProvider`
- `subdomainPrefix`

### 5. Execute provisioning
When no follow-up questions remain, call with `execute=true`.

```bash
curl -s \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -X POST http://localhost:8001/endpoint/ai-orchestrator/generate/app \
  -d '{
    "prompt": "Create a storefront for Demo Shop with one starter product and checkout",
    "tenantKey": "tenant-demo",
    "siteKey": "site-demo",
    "clientKey": "client-demo",
    "execute": true,
    "answers": {
      "brandName": "Demo Shop",
      "homePageTitle": "Launch Faster",
      "starterProductName": "Starter Product",
      "starterProductSku": "STARTER-001",
      "paymentProvider": "zarinpal-default",
      "subdomainPrefix": "demo-shop"
    }
  }'
```

Expected result:

- `provisioningResult.status = PROVISIONED`
- `createdDefinitions` contains service-owned definitions created from templates
- `createdRecords` contains seed records such as content, product, theme, and routes
- `deliveryEndpoints` contains public and bot-facing entry points

### 6. Validate the created client project
After provisioning, verify:

- storefront route:
  - `GET /public/storefront/render?path=/`
- sitemap:
  - `GET /public/storefront/sitemap`
- seeded content and catalog records through their service APIs
- if BPM was provisioned, verify `bpm-service` flow availability

## Recommended Expansion
If you want a deeper live integration test later, the next step is a profile-based test that boots `ai-orchestrator-service` plus WireMock-backed downstream service stubs for:

- `content-service`
- `catalog-service`
- `storefront-service`
- `bpm-service`

That would let CI validate actual internal HTTP requests while still staying deterministic.

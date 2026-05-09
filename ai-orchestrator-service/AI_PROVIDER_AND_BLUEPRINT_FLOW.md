# AI Provider And Blueprint Flow

## Purpose
This document explains whether a provider such as GapGPT can support this platform, and how the platform should handle a user request like:

`want a e-commerce site like the one have before with spiffy, with CRM, notify user and connect to zarinpal for payment then user orders`

## What The Platform Needs From An AI API
For this platform, the AI layer is not just chat. It must support:

1. intent detection
2. blueprint or draft selection
3. follow-up question generation
4. strict structured output for `PlatformAppDslDefinition`
5. deterministic provisioning into multiple microservices through `dynamic-entity-core`

That means a provider is only a good fit if it can reliably return machine-parseable JSON for a fairly large orchestration DSL.

## Current Platform Design
The current implementation already separates responsibilities correctly:

- `ai-orchestrator-service` uses blueprint-first behavior for known app types
- if a known draft exists, it reuses that draft instead of inventing a new schema
- the dynamic entity definitions are created by calling each service template through internal APIs
- provisioning is deterministic after the draft is resolved

This is the right architecture for your platform. The AI provider should not directly invent arbitrary microservice schemas for known scenarios when a blueprint exists.

## GapGPT Fit
### What is already supported in code
`ai-orchestrator-service` already has a GapGPT provider slot:

- base URL: `https://api.gapgpt.app`
- path: `/v1/chat/completions`
- provider enum: `GAPGPT`
- routing path: `RoutingLlmClient -> OpenAiCompatibleLlmClient`

That means the code assumes GapGPT is OpenAI-chat-compatible.

### What this means in practice
If GapGPT accepts OpenAI-style `POST /v1/chat/completions` requests and returns:

- `choices[0].message.content`

with JSON content, then the current client can consume it.

### What is still risky
The current implementation does not use:

- JSON schema enforcement
- tool calling
- structured output mode with guaranteed schema compliance

Instead it:

1. sends a prompt asking for strict JSON
2. reads `choices[0].message.content`
3. retries parsing if the provider returns invalid JSON

So GapGPT may be usable, but it is only safe enough for your platform if its JSON obedience is high.

## Best Strategy For Your Use Case
For the Spiffy-like ecommerce case, use AI in this order:

1. infer or accept `appType = e-commerce`
2. resolve an existing blueprint draft such as `ecommerce-crm-zarinpal-v1`
3. ask missing follow-up questions
4. enrich the resolved DSL with deterministic answers
5. provision each service through internal template-based APIs

That is better than asking a provider to design the full microservice schema from scratch every time.

## How The Spiffy Scenario Should Flow
### User says
`want a e-commerce site like the one have before with spiffy, with CRM, notify user and connect to zarinpal for payment then user orders`

### Expected orchestrator behavior
1. infer `e-commerce`
2. load or create a known draft from `ecommerce-crm-zarinpal-v1`
3. keep `paymentProvider = zarinpal-default`
4. ask for missing details such as:
   - `brandName`
   - `homePageTitle`
   - `starterProductName`
   - `subdomainPrefix`
   - `pageContentSummary`
5. after answers are complete, set draft status to `READY`
6. provision service-owned definitions and records across:
   - `content-service`
   - `catalog-service`
   - `crm-service`
   - `commerce-service`
   - `finance-service`
   - `checkout-service`
   - `payment-orchestrator-service`
   - `notification-service`
   - `report-service`
   - `storefront-service`

## Test Coverage Added
Two tests were added for this:

1. `MongoAppDraftServiceEcommerceScenarioTest`
   - proves the Spiffy-like prompt resolves to the ecommerce blueprint draft
   - proves the orchestrator asks the right follow-up questions
   - proves the draft becomes ready and enriches entity data after answers are provided

2. `OpenAiCompatibleLlmClientGapGptContractTest`
   - proves the current OpenAI-compatible client can parse a GapGPT-style chat completion response
   - validates the request path and auth header contract expected by the current implementation

An additional opt-in live smoke test is also available:

3. `GapGptLiveSmokeTest`
   - runs only when `GAPGPT_API_KEY` is present
   - performs a real call to `https://api.gapgpt.app/v1/chat/completions`
   - verifies that the live provider can return parseable `PlatformAppDslDefinition` JSON for a minimal case

## Live Validation Result
A live GapGPT call was verified against the configured `chat/completions` endpoint.

Observed result:

- transport compatibility: good
- auth/header compatibility: good
- simple strict JSON reply: good
- full ecommerce DSL semantic accuracy without blueprint control: only moderate

In practice this means:

- GapGPT can be used as a provider behind the current OpenAI-compatible client
- it should not be trusted to invent final production microservice structures for known flows without blueprint constraints
- for your platform, blueprint-first orchestration is still the correct control mechanism

## Recommendation
GapGPT can be a candidate provider for this platform if it is truly OpenAI-compatible and reliable at returning strict JSON.

For production behavior:

1. use blueprint/draft-first provisioning for known business scenarios
2. use the external LLM mainly for intent understanding, copy generation, and missing-answer guidance
3. do not rely on raw LLM output alone for final entity structure in known flows
4. if you want stronger safety, upgrade the OpenAI-compatible client to optional schema-enforced output for providers that support it

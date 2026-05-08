# AI Orchestrator Control Plane Plan

## Purpose

This document defines the concrete redesign of `ai-orchestrator-service` from a one-shot generation endpoint into the **persistent control plane** for the platform.

This plan assumes:

- most required business microservices already exist
- the main missing backend capability is persistent orchestration state
- the panel and bot should both consume the same orchestrator APIs
- external AI providers should be a fallback, not the default path, for known app types

## Current Problem

The current implementation of `ai-orchestrator-service`:

- accepts a prompt
- fetches metadata
- optionally calls external or local LLMs
- generates `PlatformAppDslDefinition`
- optionally provisions immediately
- returns the result in-memory for that request only

Current references:

- [AI_ORCHESTRATOR_SERVICE_ARCHITECTURE.md](/Users/farid/Projects/naviya/old-cyan/cyan_business/AI_ORCHESTRATOR_SERVICE_ARCHITECTURE.md)
- [AiPlatformGenerationServiceImpl.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/ai-orchestrator-service/src/main/java/com/cyancoder/aiorchestrator/service/impl/AiPlatformGenerationServiceImpl.java)
- [PlatformProvisioningService.java](/Users/farid/Projects/naviya/old-cyan/cyan_business/ai-orchestrator-service/src/main/java/com/cyancoder/aiorchestrator/service/impl/PlatformProvisioningService.java)

That is not enough for:

- known app-type reuse
- client-specific long-lived drafts
- panel editing over time
- bot session resume
- deterministic non-AI provisioning for known archetypes
- release/version management

## Main Architectural Decision

`ai-orchestrator-service` should become the owner of:

- app blueprints
- client app drafts
- client app releases
- provisioning runs
- panel/bot conversation sessions

Do **not** introduce a new microservice first.

Reason:

- orchestration logic, blueprint resolution, and session state all belong to the same control plane
- separating early increases distributed complexity without solving a current product bottleneck
- a split can happen later if operational ownership or scale requires it

## New Responsibilities

`ai-orchestrator-service` should own these responsibilities:

1. resolve app intent into a known `AppBlueprint`
2. create and update tenant-specific `ClientAppDraft`
3. manage `ConversationSession` for panel and bot
4. decide whether external AI is needed
5. execute deterministic internal provisioning steps
6. store `ProvisioningRun` and outcome details
7. create `ClientAppRelease` snapshots for publish/versioning

## Decision Rules

### Rule 1: Known app type should not call external AI by default

If the request matches a known blueprint family such as:

- `blog`
- `company-site`
- `shop`
- `crm`
- `mixed`

then the orchestrator should:

- load blueprint
- merge user answers and existing draft state
- compute provisioning plan
- execute internal API calls if requested

without calling:

- Ollama
- OpenAI
- OpenRouter
- GapGPT

### Rule 2: External AI is only a fallback

External AI should be used only if:

- no matching blueprint exists
- user asks for a structurally new app type
- user asks for a major redesign outside blueprint rules
- user prompt is too ambiguous to map safely to a known blueprint
- user asks to generate a new reusable blueprint candidate

### Rule 3: Panel and bot must use the same orchestration state

The panel and bot are delivery surfaces only.

They should not own:

- long-lived draft state
- microservice topology decisions
- provisioning step ordering

They should call orchestrator APIs and read/write:

- `ConversationSession`
- `ClientAppDraft`
- `ProvisioningRun`
- `ClientAppRelease`

## Core Domain Model

## 1. `AppBlueprint`

Canonical app-type template used for deterministic provisioning.

Suggested fields:

```java
public class AppBlueprint {
    private String blueprintKey;
    private String appType; // blog, company-site, shop, crm, mixed
    private Integer version;
    private String title;
    private String description;
    private boolean active;
    private List<String> tags;
    private BlueprintMatchRules matchRules;
    private BlueprintDefaultAnswers defaultAnswers;
    private PlatformAppDslDefinition baseDsl;
    private List<ProvisioningStepDefinition> provisioningSteps;
    private List<BlueprintQuestionDefinition> requiredQuestions;
    private List<String> capabilities;
    private DeliveryBlueprint delivery;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;
}
```

Purpose:

- reusable canonical source for known app families
- avoids repeated AI routing work
- defines deterministic service/template call sequences

Examples:

- `blog-basic-v1`
- `company-site-v1`
- `shop-basic-v1`
- `crm-basic-v1`

## 2. `ClientAppDraft`

Tenant/site-specific mutable working state.

Suggested fields:

```java
public class ClientAppDraft {
    private String draftId;
    private String tenantKey;
    private String siteKey;
    private String clientKey;
    private String blueprintKey;
    private Integer blueprintVersion;
    private DraftStatus status; // DRAFT, READY, PROVISIONING, PROVISIONED, RELEASED, FAILED
    private String title;
    private String appType;
    private String latestIntent;
    private Map<String, Object> answers;
    private PlatformAppDslDefinition resolvedDsl;
    private List<String> pendingQuestions;
    private List<String> manualActions;
    private String latestSessionId;
    private Integer revision;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
```

Purpose:

- panel editing target
- bot resume target
- source of truth for mutable client app state over time

## 3. `ClientAppRelease`

Immutable publishable snapshot.

Suggested fields:

```java
public class ClientAppRelease {
    private String releaseId;
    private String draftId;
    private String tenantKey;
    private String siteKey;
    private Integer draftRevision;
    private ReleaseStatus status; // ACTIVE, ROLLED_BACK, ARCHIVED
    private PlatformAppDslDefinition releasedDsl;
    private List<String> publishedEndpoints;
    private String publishedBy;
    private Instant publishedAt;
}
```

Purpose:

- separate live release from mutable draft
- support rollback and history

## 4. `ProvisioningRun`

Execution audit for internal platform calls.

Suggested fields:

```java
public class ProvisioningRun {
    private String runId;
    private String draftId;
    private String releaseId;
    private String tenantKey;
    private String siteKey;
    private ProvisioningRunStatus status; // PLANNED, RUNNING, PARTIAL_SUCCESS, SUCCESS, FAILED
    private List<ProvisioningStepResult> stepResults;
    private String triggerType; // PANEL, BOT, API, SYSTEM
    private String triggeredBy;
    private Instant startedAt;
    private Instant finishedAt;
}
```

Each step result should store:

- service key
- endpoint path
- request summary
- idempotency key
- response summary
- duration
- success/failure
- retry count

Purpose:

- idempotent audit trail
- operational troubleshooting
- user-facing provisioning history

## 5. `ConversationSession`

Persistent interaction thread for panel or bot.

Suggested fields:

```java
public class ConversationSession {
    private String sessionId;
    private String channelType; // PANEL, TELEGRAM, BALE
    private String tenantKey;
    private String siteKey;
    private String clientKey;
    private String draftId;
    private String appTypeHint;
    private SessionStatus status; // OPEN, WAITING_FOR_ANSWERS, RESOLVED, CLOSED, FAILED
    private List<SessionMessage> messages;
    private Map<String, Object> extractedAnswers;
    private String latestPrompt;
    private String latestQuestion;
    private Instant createdAt;
    private Instant updatedAt;
}
```

Purpose:

- resume bot chat
- resume panel assisted flow
- keep conversation linked to the same draft

## Storage Recommendation

Use MongoDB for the control-plane state first.

Why:

- `PlatformAppDslDefinition` is document-shaped
- `answers` and session messages are dynamic JSON-like structures
- draft and release snapshots fit document storage well

Suggested collections:

- `app_blueprint`
- `client_app_draft`
- `client_app_release`
- `provisioning_run`
- `conversation_session`

If relational reporting becomes important later, mirror key metadata into PostgreSQL or add projections.

## Blueprint Structure

An `AppBlueprint` should contain:

1. matching metadata
2. required question definitions
3. base DSL skeleton
4. deterministic provisioning steps
5. optional transformation rules from answers -> DSL

### Example: `blog-basic-v1`

Base services:

- `content-service`
- `storefront-service`
- optionally `media-service`

Provisioning steps:

1. create `landing-page` definition in `content-service`
2. create `blog-page` definition in `content-service`
3. create `theme-layout` definition in `storefront-service`
4. create `site-route` definition in `storefront-service`
5. create starter theme record
6. create starter landing page record
7. create starter blog index record
8. create `/` and `/blog` routes

No LLM is needed if the user simply wants a blog site.

## Orchestration Flow

## A. Create Draft From Known App Type

Input:

- `appType = blog`
- tenant/site/client identity
- optional answers

Flow:

1. resolve active blueprint by `appType`
2. create or reuse draft for `tenantKey + siteKey + blueprintKey`
3. merge answers into blueprint defaults
4. compute resolved DSL
5. compute missing questions
6. persist draft
7. return draft

## B. Update Draft Over Time

Input:

- `draftId`
- new prompt or structured answer patch

Flow:

1. load draft
2. load blueprint
3. merge answer patch
4. if the request still fits blueprint rules:
   - recompute deterministic DSL
   - do not call external AI
5. if the request exceeds blueprint rules:
   - use AI fallback to propose updated DSL or blueprint extension
6. persist new draft revision
7. return updated draft

## C. Provision Draft

Flow:

1. load draft
2. validate required questions complete
3. generate step plan from blueprint and resolved DSL
4. create `ProvisioningRun`
5. execute internal service calls in order
6. persist step results
7. update draft status
8. return provisioning run summary

## D. Publish Draft

Flow:

1. load draft
2. verify draft is provisioned or provision successfully first
3. create immutable `ClientAppRelease`
4. mark previous active release as archived if needed
5. return release summary

## API Redesign

The current endpoint:

- `POST /endpoint/ai-orchestrator/generate/app`

should remain for backward compatibility, but it should no longer be the main product API.

The main APIs should be:

## Blueprint APIs

### `GET /endpoint/ai-orchestrator/blueprints`

List active app blueprints.

Query examples:

- `?appType=shop`
- `?active=true`

### `GET /endpoint/ai-orchestrator/blueprints/{blueprintKey}`

Return full blueprint definition.

### `POST /internal/ai-orchestrator/blueprints`

Create blueprint.

Internal/admin only.

### `PUT /internal/ai-orchestrator/blueprints/{blueprintKey}`

Update blueprint version or metadata.

## Draft APIs

### `POST /endpoint/ai-orchestrator/drafts`

Create draft from known blueprint or explicit app type.

Request example:

```json
{
  "appType": "shop",
  "tenantKey": "tenant-demo",
  "siteKey": "site-shop-a",
  "clientKey": "client-100",
  "title": "Demo shop",
  "answers": {
    "brandName": "Demo Shop",
    "catalogMode": "PRODUCT"
  }
}
```

Behavior:

- should resolve existing active blueprint
- should not call LLM for known app types

### `GET /endpoint/ai-orchestrator/drafts`

List drafts.

Query examples:

- `?tenantKey=tenant-demo`
- `?siteKey=site-shop-a`
- `?clientKey=client-100`
- `?status=DRAFT`

### `GET /endpoint/ai-orchestrator/drafts/{draftId}`

Return draft details, resolved DSL, and pending questions.

### `PATCH /endpoint/ai-orchestrator/drafts/{draftId}`

Update prompt or answers over time.

Request example:

```json
{
  "prompt": "Add a blog and FAQ page too",
  "answersPatch": {
    "hasBlog": true,
    "hasFaq": true
  }
}
```

Behavior:

- if still within blueprint scope, recompute deterministically
- otherwise mark `aiFallbackUsed=true` in response and use AI proposal flow

### `POST /endpoint/ai-orchestrator/drafts/{draftId}/resolve`

Force recomputation of resolved DSL from blueprint + answers.

Useful after manual edits or migrations.

## Provisioning APIs

### `POST /endpoint/ai-orchestrator/drafts/{draftId}/provision`

Execute internal API calls.

Request example:

```json
{
  "mode": "APPLY",
  "idempotencyKey": "draft-123-rev-5"
}
```

Modes:

- `PLAN`
- `APPLY`

### `GET /endpoint/ai-orchestrator/drafts/{draftId}/runs`

List provisioning runs for the draft.

### `GET /endpoint/ai-orchestrator/runs/{runId}`

Return step-by-step provisioning run details.

## Release APIs

### `POST /endpoint/ai-orchestrator/drafts/{draftId}/publish`

Create release from draft.

### `GET /endpoint/ai-orchestrator/releases`

List releases.

### `GET /endpoint/ai-orchestrator/releases/{releaseId}`

Return release snapshot.

### `POST /endpoint/ai-orchestrator/releases/{releaseId}/rollback`

Roll back to a previous release.

## Session APIs

### `POST /endpoint/ai-orchestrator/sessions`

Create panel or bot session.

Request example:

```json
{
  "channelType": "TELEGRAM",
  "tenantKey": "tenant-demo",
  "siteKey": "site-shop-a",
  "clientKey": "client-100",
  "draftId": null,
  "appTypeHint": "shop"
}
```

### `GET /endpoint/ai-orchestrator/sessions/{sessionId}`

Return full session.

### `POST /endpoint/ai-orchestrator/sessions/{sessionId}/message`

Append message and advance orchestration state.

Request example:

```json
{
  "role": "USER",
  "content": "I also need a blog section and payment page"
}
```

Behavior:

- load linked draft if present
- extract answers
- update draft
- return next question or updated draft summary

### `POST /endpoint/ai-orchestrator/sessions/{sessionId}/close`

Close session.

## Backward Compatibility Strategy

Keep:

- `POST /endpoint/ai-orchestrator/generate/app`

But change behavior:

1. if request includes explicit known `appType` in answers, route through blueprint resolution first
2. if known app type can be handled deterministically, do not call external AI
3. return generated draft summary along with current DSL

Over time, panel and bot should migrate to the draft/session APIs instead.

## Provisioning Engine Changes

`PlatformProvisioningService` should evolve from:

- immediate DSL executor

into:

- idempotent provisioning engine with step planning and run tracking

Required additions:

- `planProvisioning(draft)` -> list of `ProvisioningStepDefinition`
- idempotency key support per step
- step result persistence
- retry-safe execution
- partial reprovision support

Provisioning step types should include:

- `CREATE_DEFINITION_FROM_TEMPLATE`
- `UPSERT_RECORD`
- `CREATE_BPM_FLOW`
- `UPSERT_STOREFRONT_ROUTE`
- `ENSURE_THEME`
- `CALL_INTERNAL_API`

## Panel Integration Contract

Panel should support two modes:

### Manual mode

User selects app type and edits structure directly.

Flow:

1. panel creates draft from blueprint
2. panel edits answers and optional DSL sections
3. panel requests resolve/provision/publish

### Assisted mode

User starts with natural language.

Flow:

1. panel opens session
2. session updates draft
3. orchestrator asks follow-up questions
4. panel shows draft preview and manual edit UI

The panel should never own the final truth of draft state.

## Bot Integration Contract

Bot should support:

### Guided mode

- no external AI needed
- bot asks blueprint-defined questions
- orchestrator session stores state

### Free-text assisted mode

- orchestrator decides whether blueprint is enough
- AI fallback only when required

Bot runtime should remain thin:

- receive webhook
- map user/chat identity to session
- call orchestrator session APIs
- send reply back to channel

## Implementation Order

## Phase 1

Add persistence and deterministic blueprint resolution to `ai-orchestrator-service`.

Deliverables:

- `AppBlueprint`
- `ClientAppDraft`
- `ConversationSession`
- `ProvisioningRun`
- Mongo repositories
- draft/session/blueprint APIs

## Phase 2

Refactor provisioning engine.

Deliverables:

- planned vs applied provisioning
- step persistence
- idempotency and retry tracking

## Phase 3

Add release management.

Deliverables:

- `ClientAppRelease`
- publish endpoint
- rollback endpoint

## Phase 4

Migrate panel and bot to draft/session APIs as primary path.

## What Not To Build Yet

Do not build first:

- a separate `app-registry-service`
- direct bot-to-microservice orchestration
- a new domain microservice for app type routing
- mandatory AI invocation for every prompt

These would increase complexity while avoiding the real missing capability: persistent orchestration state.

## Final Recommendation

The platform should be completed by making `ai-orchestrator-service` the persistent control plane for:

- blueprint resolution
- draft lifecycle
- session continuity
- deterministic internal API orchestration
- release/version history

Known app types should be provisioned from saved blueprints first.
External AI should be used only when deterministic blueprint routing is no longer enough.

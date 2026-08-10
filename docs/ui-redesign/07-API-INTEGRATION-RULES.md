# API Integration Rules

## 1. Non-negotiable rule

Never fill a service-backed screen with fixtures to make the UI resemble a design.

Permitted:
- skeletons
- empty-state examples in Storybook/tests
- explicit demo environment data returned by backend

Not permitted:
- seeded chat messages in production UI
- hardcoded KPI values
- hardcoded record create/update payloads
- fake preview URLs
- swallowed provisioning errors
- fake workspace/site values used for API scope

## 2. Typed client layers

Keep transport separate from domain clients:

```text
lib/http/platform-fetch.ts
lib/auth/session.ts
lib/scope/scope-context.ts
lib/access/access-context.ts

lib/api/ai-orchestrator.ts
lib/api/dynamic-entities.ts
lib/api/automation.ts
lib/api/bpm.ts
lib/api/iam.ts
lib/api/billing.ts
lib/api/bots.ts
lib/api/notifications.ts
lib/api/reports.ts
lib/api/media.ts
lib/api/sites.ts
lib/api/domains.ts
```

No route page should assemble service URLs directly.

## 3. Scope

Every scoped call gets stable IDs from active context:

```ts
type ActiveScope = {
  tenantKey: string;
  siteKey?: string;
  clientKey?: string;
  environment?: string;
};
```

Send:
- `X-Tenant-Key`
- `X-Site-Key` when applicable
- query/body scope only when required by the backend contract

The visible workspace/site selector must update the same active scope used by API clients.

## 4. Query behavior

Use a query abstraction with:
- stable query keys including tenant/site
- request cancellation
- deduplication
- stale time by resource
- background refresh indicator
- explicit partial-error handling

Do not use one `isLoading` flag for unrelated resources.

## 5. Mutation behavior

Every mutation defines:
- confirmation requirement
- optimistic or pessimistic strategy
- idempotency behavior
- success invalidations
- rollback behavior
- user-facing error mapping

High-impact actions are pessimistic:
- publish
- activate
- cancel execution
- delete
- webhook registration
- domain change
- role grant
- plan change

## 6. Error model

Normalize backend errors:

```ts
type UiError = {
  code: string;
  message: string;
  fieldErrors?: Record<string, string[]>;
  correlationId?: string;
  retryable: boolean;
  status?: number;
};
```

Never display raw backend HTML or serialized exception bodies directly.

## 7. Validation

- Use backend definition/metadata as the primary validation source.
- Client validation improves feedback but does not replace backend validation.
- Map strict dynamic-entity validation errors to exact nested paths.
- For nested lists/objects, show missing child fields at the correct row/item.

## 8. Loading and partial failures

Dashboard widgets load independently.
Builder metadata and resource data have separate states.
A failed secondary panel must not erase a loaded canvas.

## 9. Pagination/filter/sort

Use server-side contracts when available.

Generic grid query model:

```ts
type GridQuery = {
  page: number;
  pageSize: number;
  sort?: Array<{ field: string; direction: "asc" | "desc" }>;
  filters?: FilterGroup;
  search?: string;
};
```

Do not present pagination controls that only slice a fixed client array.

## 10. Realtime and polling

Preferred:
1. WebSocket/SSE
2. backoff polling
3. manual refresh

Use realtime for:
- AI generation progress
- provisioning run steps
- automation execution
- notification delivery
- domain/certificate verification

## 11. Offline/PWA

- Cache shell and static assets.
- Do not cache mutation responses as successful offline operations unless an explicit queue exists.
- Read pages may show last-synced data with a clear stale badge.
- Builders should preserve unsaved local drafts, but label them local and reconcile intentionally.
- Display offline state globally.

## 12. Security

- Do not display secret values.
- Use secret/credential references.
- Avoid access tokens in local storage when backend migration can support secure cookies; until then, preserve current behavior without leaking tokens to logs.
- Sanitize execution diagnostics.
- Do not render backend HTML outside a sandboxed iframe.

# Dynamic OpenAPI Platform

## Outcome

Every Spring Boot microservice now derives OpenAPI from its real controllers,
DTOs, request parameters, and validation annotations. The shared
`platform-openapi-core` module adds consistent platform metadata and security:

| Controller path | OpenAPI security |
|---|---|
| `/public/**` | none |
| `/internal/**` | HTTP Basic |
| `/endpoint/**` | Bearer JWT |
| `/api/{service}/endpoint/**` | Bearer JWT |
| other controller paths | configurable; Bearer by default |

Mixed controllers can override the convention at class or method level:

```java
@PlatformOpenApiAuth(PlatformApiSecurity.NONE)
class AuthenticationController {

    @PlatformOpenApiAuth(PlatformApiSecurity.BEARER)
    public void authenticatedOperation() {
    }
}
```

The method annotation wins over the class annotation, and both win over the
path/default convention. Use the override when a controller mixes public,
Bearer, and Basic operations under the same URL prefix.

The specifications never contain usernames, passwords, tokens, or example
secrets. Clients inject authentication at runtime.

## Per-service endpoints

Each servlet or WebFlux microservice exposes:

```text
GET /v3/api-docs
GET /v3/api-docs.yaml
GET /swagger-ui.html
```

The documentation endpoints themselves use Basic authentication by default.
They use `platform.openapi.docs-username` and
`platform.openapi.docs-password`, falling back to the service's internal
credentials. In Kubernetes, use shared documentation-only credentials:

```properties
PLATFORM_OPENAPI_DOCS_USERNAME=platform_docs
PLATFORM_OPENAPI_DOCS_PASSWORD=<kubernetes-secret>
```

Access policy is configurable:

```properties
platform.openapi.docs-access=BASIC
# BASIC | PUBLIC | DISABLED
```

Use `PUBLIC` only for local development. Production service networking or
NetworkPolicy should restrict `/v3/api-docs` and `/swagger-ui/**` to the panel,
catalog, CI, and trusted administration workloads.

Example:

```bash
curl -fSs \
  -u "$PLATFORM_OPENAPI_DOCS_USERNAME:$PLATFORM_OPENAPI_DOCS_PASSWORD" \
  http://localhost:9104/v3/api-docs |
jq '.paths | keys'
```

## Dynamic entity specifications

Controller reflection cannot infer a tenant's dynamic record fields from
`Map<String,Object>`. Dynamic services therefore expose a specification generated
from the stored entity definition:

```text
GET /endpoint/entities/definitions/{entityKey}/openapi
GET /api/{serviceKey}/endpoint/entities/definitions/{entityKey}/openapi
GET /internal/entities/definitions/{entityKey}/openapi
```

Endpoint routes use Bearer authentication; internal routes use Basic
authentication. `X-Tenant-Key` and `X-Site-Key` select the definition.

The generated specification includes:

- strict nested object and list schemas
- required fields
- regular expressions
- minimum/maximum string lengths
- numeric minimums/maximums
- enum values
- record create/list/get/replace/patch/delete operations
- record validation
- pagination and tenant/site headers

Example:

```bash
curl -fSs \
  -u commerce_internal:commerce_secret \
  -H 'X-Tenant-Key: importer-demo' \
  -H 'X-Site-Key: main-site' \
  http://localhost:9104/internal/entities/definitions/importer-order/openapi |
jq '.components.schemas.ImporterOrderData'
```

## Kubernetes API catalog

`api-docs-service` runs on port `9128` and does not depend on Eureka or the local
gateway. Its configured targets use Kubernetes Service DNS directly.

Main APIs:

```text
GET /endpoint/api-docs/services
GET /endpoint/api-docs/services/{serviceKey}
GET /endpoint/api-docs/aggregate

GET /internal/api-docs/services
GET /internal/api-docs/services/{serviceKey}
GET /internal/api-docs/aggregate
```

The endpoint routes use Bearer authentication. Internal routes use the
`api_docs_internal` Basic user.

Targets are provided through `PLATFORM_API_DOCS_TARGETS_JSON`. Passwords can be
referenced by environment-variable name:

```json
[
  {
    "serviceKey": "commerce-service",
    "baseUrl": "http://commerce-service:9104",
    "username": "platform_docs",
    "passwordEnvironmentVariable": "PLATFORM_OPENAPI_DOCS_PASSWORD"
  },
  {
    "serviceKey": "bpm-service",
    "baseUrl": "http://bpm-service:9119",
    "username": "platform_docs",
    "passwordEnvironmentVariable": "PLATFORM_OPENAPI_DOCS_PASSWORD"
  }
]
```

The service caches specifications for 60 seconds by default. `?refresh=true`
forces a controller refresh. Catalog responses never return target credentials.

The aggregate document prefixes paths with `/services/{serviceKey}` and reusable
components with a service prefix to prevent collisions. If a target pod is down,
healthy services remain in the aggregate and the failure appears under
`x-platform-unavailable-services`. The aggregate is intended for discovery and
documentation. Generate executable clients from each per-service specification,
which retains the real service path and base URL.

## Panel

The web panel has a `/api-docs` page. It shows:

- currently reachable services
- controller path and method
- derived Basic/Bearer/public authentication
- path and operation counts
- live refresh
- per-service OpenAPI download

Configure the panel backend:

```properties
API_DOCS_SERVICE_BASE_URL=http://api-docs-service:9128
```

## Live Postman export

The primary exporter fetches live controller specifications and creates
per-service OpenAPI plus a Postman collection:

```bash
export API_DOCS_CATALOG_URL=http://localhost:9128/internal/api-docs
export API_DOCS_USERNAME=api_docs_internal
export API_DOCS_PASSWORD=api_docs_secret

python3 scripts/export_live_api_docs.py --refresh
```

Outputs:

```text
docs/runtime-api/service-catalog.json
docs/runtime-api/openapi/platform.openapi.json
docs/runtime-api/openapi/services/{serviceKey}.openapi.json
docs/runtime-api/postman/cyan-business-platform.live.postman_collection.json
```

The collection is derived from OpenAPI operations. Basic operations use
service-specific `*_internal_username` and `*_internal_password` variables;
Bearer operations use `access_token`.

## Client SDK generation

Generate clients from the exported per-service specifications. For example:

```bash
npx @openapitools/openapi-generator-cli generate \
  -i docs/runtime-api/openapi/services/commerce-service.openapi.json \
  -g typescript-fetch \
  -o generated-clients/commerce-typescript

npx @openapitools/openapi-generator-cli generate \
  -i docs/runtime-api/openapi/services/bpm-service.openapi.json \
  -g java \
  -o generated-clients/bpm-java
```

Generated clients expose Basic/Bearer configuration because those schemes come
from the controller-derived operation contract. Credentials remain runtime
configuration and must not be committed.

## AI orchestration

When `api-docs-service` appears in `availableServiceKeys`,
`ai-orchestrator-service` adds a compact `controllerApis` list to metadata for
each available service. Each entry contains method, path, operation ID, summary,
and authentication. The AI prompt treats this list as authoritative and must not
invent routes or change authentication.

The catalog limits AI metadata to 500 operations per service and does not include
schemas or credentials, keeping prompt size bounded.

## Static artifacts

`scripts/generate_api_docs.py` and the checked-in `docs/swagger` collection are
legacy offline snapshots. They remain available for environments where services
cannot be started, but they are not authoritative. CI and production exports
should use `scripts/export_live_api_docs.py`.

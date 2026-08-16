# Cyan Kubernetes deployment profiles

The panel is a server-side BFF. Every `*_SERVICE_BASE_URL` used by `panel-web`
must resolve to a Kubernetes Service; browser access to `api.cyancoder.com` does
not replace this internal routing.

## Common client profile

The service groups used by build/deploy scripts are declared in
`common-client-profile.sh`.

The smallest usable authenticated panel consists of PostgreSQL, MongoDB,
`panel-web`, the five SSO services, `tenant-service`, `billing-service`, and
`storefront-service`. Billing is required when a tenant is created. Storefront
owns sites and is required when a workspace selects or creates a site.

The normal Cyan business profile additionally runs content, catalog, CRM,
report, processor, BPM, automation orchestration, AI orchestration,
notification, and media. To exercise every Phase 1-11 panel page also run:

- `event-service` plus Kafka for event-triggered automation;
- `bot-adapter-service` for Telegram/Bale connections and BPM/automation bot actions;
- `search-index-service` for search administration;
- `batch-worker-service` for queued/batch automation;
- `api-docs-service` for the API catalog.

Commerce, finance, inventory, cart, checkout, pricing, and payment services are
separate optional bundles. Do not advertise their capabilities to a tenant
unless the corresponding services and provider configuration are deployed.

## Manifests

- `panel-web.yaml` defines the panel Deployment, Service, public HTTPS route,
  and all internal BFF service URLs.
- `tenant-billing.yaml` defines tenant and billing Deployments/Services and the
  required SSO, billing, notification, and invitation URL wiring.
- `apps.yaml` contains the wider backend service catalog.
- `secret.template.yaml` is a template only. Replace every `change-me` and do
  not commit the resulting Secret.

Create the PostgreSQL databases before first startup. The canonical list is in
`docker/databases/init/01-create-databases.sql`; the common panel requires at
least `tenant_service`, `billing_service`, `storefront_service`, and
`report_service` in addition to the SSO/business databases being deployed.

Apply a locally tagged image by setting it in the relevant manifest or after
apply with `kubectl set image`. The checked-in `ghcr.io/your-org/...:develop`
values are intentionally registry placeholders.

## Required panel environment

At minimum, the live panel Deployment must contain:

```text
SSO_AUTH_SERVICE_BASE_URL=http://sso-auth-service:9001
SSO_USER_SERVICE_BASE_URL=http://sso-user-service:9002
SSO_CAPTCHA_SERVICE_BASE_URL=http://sso-captcha-service:9003
SSO_OTP_SERVICE_BASE_URL=http://sso-otp-service:9004
SSO_SESSION_SERVICE_BASE_URL=http://sso-session-service:9005
TENANT_SERVICE_BASE_URL=http://tenant-service:9129
BILLING_SERVICE_BASE_URL=http://billing-service:9130
STOREFRONT_SERVICE_BASE_URL=http://storefront-service:9115
```

Use the complete env list in `panel-web.yaml` for the common Phase 1-11 profile.
An unset server-side variable falls back to `localhost`, which is the panel
container itself and will produce `BFF_UPSTREAM_UNAVAILABLE`.

The tenant Deployment must also set:

```text
SSO_USER_SERVICE_BASE_URL=http://sso-user-service:9002
BILLING_SERVICE_BASE_URL=http://billing-service:9130
NOTIFICATION_SERVICE_BASE_URL=http://notification-service:9122
TENANT_INVITATION_ACCEPT_BASE_URL=https://cyancoder.com/auth/invitation
```

The shared Secret template caps Hikari at three connections with one minimum
idle connection. All generated backend Deployments consume that Secret through
`envFrom`; services without a JDBC datasource ignore these settings. Keep the
same limits in the live `cyan-platform-secrets` Secret for small staging nodes.

Spring Boot 4 Mongo services use `spring.mongodb.uri`. Service server profiles
bind their existing `<SERVICE>_MONGODB_URI` variable to that property, with a
Kubernetes DNS default such as `mongodb://mongo:27017/search_index_service`.

## Verification

After applying images and configuration:

```bash
kubectl -n cyan-staging rollout status deployment/panel-web --timeout=180s
kubectl -n cyan-staging get pods
kubectl -n cyan-staging get services
kubectl -n cyan-staging get httproute cyancoder-panel-https
```

A successful unauthenticated `GET /api/panel/bootstrap` returns `401
AUTH_REQUIRED`. After login it must return identity, access, and tenancy rather
than `503 BOOTSTRAP_UNAVAILABLE`. A BFF probe returning `502
BFF_UPSTREAM_UNAVAILABLE` means the corresponding Kubernetes Service is absent,
has no ready endpoints, or its panel env URL is wrong.

When building interactively over SSH, run build loops inside a script or use
`return 1` from a sourced function. Do not append `|| exit 1` directly in the
interactive shell: it closes the SSH session on the first failed image build.

## Migration recovery checks

Do not create application tables manually. After deploying a migration-bearing
image, inspect its startup log before changing database state:

```bash
kubectl -n cyan-staging logs deployment/billing-service --tail=200
kubectl -n cyan-staging logs deployment/notification-service --tail=200
kubectl -n cyan-staging logs deployment/storefront-service --tail=200
```

Billing safely supports fresh databases, an existing Flyway V1 database, and a
legacy non-empty database without Flyway history. The shared dynamic migration
chain creates `dynamic_entity_definitions` in V199 before V200 adds definition
versioning. If Flyway reports a failed schema-history entry rather than applying
these migrations, retain the log and run a Flyway repair through the service
tooling; do not delete history rows or create tables by hand.

# Cyan Business Server Deployment Guide

This guide deploys the platform on a server-backed Kubernetes cluster using Kubernetes service discovery instead of the local Eureka `discovery-server`, and Envoy Gateway plus Gateway API `HTTPRoute` resources instead of the Spring Cloud `api-gateway`.

The word "Huber" is treated here as Kubernetes/Kuber service discovery. If a different Huber product is intended, keep the same service contract but replace the discovery section with that product's service registry.

## Target Architecture

- Kubernetes `Service` DNS is the runtime service discovery mechanism.
- `discovery-server` is not deployed on the server.
- `api-gateway` is not deployed on the server.
- Envoy Gateway owns public ingress through `GatewayClass`, `Gateway`, and `HTTPRoute`.
- Every Spring Boot service runs with `SPRING_PROFILES_ACTIVE=server`.
- Dynamic services use PostgreSQL for definitions and MongoDB for records.
- Event fan-out still goes through `event-service`, then Kafka.
- Legacy Axon services still require Axon Server unless those flows are retired.

Official references:

- Envoy Gateway YAML install: https://gateway.envoyproxy.io/docs/install/install-yaml/
- Envoy Gateway deployment modes: https://gateway.envoyproxy.io/v1.5/tasks/operations/deployment-mode/
- Gateway API and `HTTPRoute`: https://gateway-api.sigs.k8s.io/reference/api-types/httproute/
- Kubernetes Gateway API overview: https://kubernetes.io/docs/concepts/services-networking/gateway/

## Files Added Or Changed

- `*/Dockerfile`: one runtime Dockerfile per deployable microservice.
- `*/src/main/resources/application-server.properties`: server profile per deployable microservice.
- `deploy/kubernetes/apps.yaml`: Kubernetes Deployments and Services.
- `deploy/kubernetes/envoy-gateway.yaml`: Envoy Gateway `GatewayClass`, `Gateway`, and all platform `HTTPRoute` rules.
- `deploy/kubernetes/kustomization.yaml`: applies the Kubernetes app and gateway manifests.
- `deploy/kubernetes/secret.template.yaml`: non-production secret template. Edit values before applying.
- `.github/workflows/deploy.yml`: GitHub Actions build, push, and deploy flow for `develop` and `main`.
- `Jenkinsfile`: Jenkins alternative for the same build, push, and deploy flow.
- `scripts/generate_server_deployment_assets.js`: generator for the repeated service deployment assets.

The local `api-gateway` and `discovery-server` remain available for local development.

## Service Discovery

On the server profile, Eureka is disabled:

```properties
eureka.client.enabled=false
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

Services that call other services through `DiscoveryClient` or OpenFeign use Kubernetes DNS names such as:

```properties
spring.cloud.discovery.client.simple.instances.content-service[0].uri=http://content-service:9101
```

The fixed local `server.port` values are preserved. Kubernetes `Service` objects expose those same ports, so the service DNS name and application port stay aligned.

## Server Prerequisites

Install or provide:

- Kubernetes cluster with `kubectl` access.
- Envoy Gateway installed in the cluster.
- Container registry access, for example GitHub Container Registry.
- PostgreSQL reachable from the cluster.
- MongoDB reachable from the cluster.
- Kafka reachable from the cluster.
- Axon Server reachable from the cluster for `tax-pay-sys`, `factor-service`, `buyer-service`, `product-service`, and `client-service`.
- DNS for the public API domain, for example `api.example.com`.
- TLS termination plan. The included manifest starts with HTTP port `80`; add an HTTPS listener and certificate config for production.

## Infrastructure Databases

Create one PostgreSQL database per service that has a datasource. The default names are:

```text
tax_pay_sys
factor_service
buyer_service
product_service
client_service
sso_auth_service
sso_user_service
sso_otp_service
sso_session_service
content_service
catalog_service
crm_service
commerce_service
finance_service
inventory_service
report_service
processor_service
event_service
crm_automation_service
finance_automation_service
inventory_automation_service
report_automation_service
payment_service
storefront_service
media_service
cart_service
checkout_service
bpm_service
notification_service
payment_orchestrator_service
pricing_promotion_service
search_index_service
```

MongoDB database names follow the same service naming pattern, for example `content_service`, `ai_orchestrator_service`, and `bot_adapter_service`.

For production, do not use the default `postgres/postgres` credentials. Set either global datasource credentials:

```text
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
```

or service-specific credentials:

```text
CONTENT_SERVICE_DATASOURCE_USERNAME
CONTENT_SERVICE_DATASOURCE_PASSWORD
CONTENT_SERVICE_DATASOURCE_URL
CONTENT_SERVICE_MONGODB_URI
```

Service-specific keys win over global keys.

## Install Envoy Gateway

Install Envoy Gateway before applying this repository's Gateway resources:

```bash
kubectl apply --server-side -f https://github.com/envoyproxy/gateway/releases/download/v1.8.0/install.yaml
kubectl wait --timeout=5m -n envoy-gateway-system deployment/envoy-gateway --for=condition=Available
```

The repository defines:

```yaml
controllerName: gateway.envoyproxy.io/gatewayclass-controller
gatewayClassName: cyan-envoy
```

If your cluster uses a custom Envoy Gateway controller name, update `deploy/kubernetes/envoy-gateway.yaml`.

## Create Secrets

Prepare a real secret manifest from `deploy/kubernetes/secret.template.yaml`. Do not commit production secret values.

Required high-level keys:

```text
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
KAFKA_BOOTSTRAP_SERVERS
JWKS_URI
SSO_JWT_ISSUER
SSO_JWT_AUDIENCE
PUBLIC_PLATFORM_BASE_URL
AXON_SERVER_SERVERS
AXON_SERVER_TOKEN
AUTOMATION_CALLBACK_SECRET
```

Add service-specific secret keys where needed, especially internal API passwords:

```text
CONTENT_SERVICE_INTERNAL_PASSWORD
AI_ORCHESTRATOR_SERVICE_INTERNAL_PASSWORD
BPM_SERVICE_INTERNAL_PASSWORD
NOTIFICATION_SERVICE_INTERNAL_PASSWORD
```

Apply the edited secret once per namespace:

```bash
kubectl create namespace cyan-staging --dry-run=client -o yaml | kubectl apply -f -
kubectl -n cyan-staging apply -f /path/to/edited-cyan-platform-secret.yaml
```

The CI pipelines intentionally do not apply `secret.template.yaml`, so a deployment cannot overwrite real secrets with placeholders.

## Build Images Manually

Build a service jar, then build the image from that service directory:

```bash
./gradlew :content-service:bootJar
docker build -t ghcr.io/your-org/cyan-business/content-service:develop content-service
docker push ghcr.io/your-org/cyan-business/content-service:develop
```

Repeat for all deployable services or use CI.

The generated Dockerfiles expect the service jar at:

```text
<service>/build/libs/*.jar
```

## Deploy Manually

Apply manifests into the target namespace:

```bash
kubectl create namespace cyan-staging --dry-run=client -o yaml | kubectl apply -f -
kubectl -n cyan-staging apply -k deploy/kubernetes
```

Set the real image registry and tag:

```bash
kubectl -n cyan-staging set image deployment/content-service \
  content-service=ghcr.io/your-org/cyan-business/content-service:develop
```

Repeat for each service or let CI do it.

Check rollout:

```bash
kubectl -n cyan-staging get pods
kubectl -n cyan-staging get gateway,httproute
kubectl -n cyan-staging rollout status deployment/content-service --timeout=180s
```

Get the Envoy Gateway address:

```bash
kubectl -n cyan-staging get gateway cyan-gateway -o jsonpath='{.status.addresses[0].value}'
```

Point your DNS record, for example `api.example.com`, to that address.

## Envoy Routes

`deploy/kubernetes/envoy-gateway.yaml` mirrors the current Spring Cloud Gateway route surface.

Examples:

- `/api/content-service/**` -> `content-service:9101`
- `/v2/api/tax-service/**` and `/v2/api/tax/**` -> `tax-pay-sys:8002`
- `/api/sso/auth/**` and `/.well-known/**` -> `sso-auth-service:9001`
- `/public/storefront/**` -> `storefront-service:9115`
- `/endpoint/ai-orchestrator/**` -> `ai-orchestrator-service:9121`
- `/internal/bpm/**` -> `bpm-service:9119`

The routes preserve paths. There is no prefix rewrite, because the existing service controllers and dynamic runtime APIs already expect these paths.

Security note: the historical gateway exposed selected `/internal/**` paths. The Envoy manifest preserves only the same selected routes. For stricter production security, add Gateway policy, network policy, or authentication policy so internal routes are reachable only from trusted clients or private networks.

## GitHub Actions Deployment

The workflow runs on pushes to:

- `develop` -> namespace `cyan-staging`
- `main` -> namespace `cyan-production`

Required GitHub secret:

```text
KUBE_CONFIG
```

`KUBE_CONFIG` must be a base64-encoded kubeconfig:

```bash
base64 < ~/.kube/config | tr -d '\n'
```

The workflow:

1. Builds each service jar with Gradle.
2. Builds one Docker image per service.
3. Pushes images to `ghcr.io/${{ github.repository_owner }}/cyan-business/<service>`.
4. Applies Kubernetes and Envoy manifests.
5. Updates Deployments to the branch/SHA image tag.
6. Waits for every rollout.

Before first use:

1. Ensure the repository has package write permission.
2. Create the target namespaces and real `cyan-platform-secrets`.
3. Install Envoy Gateway.
4. Update image registry naming if you do not use GitHub Container Registry.

## Jenkins Deployment

The `Jenkinsfile` supports multibranch pipelines for `develop` and `main`.

Required Jenkins credentials:

```text
cyan-container-registry
cyan-kubeconfig
```

`cyan-container-registry` should be username/password credentials for `ghcr.io` or your registry.

`cyan-kubeconfig` should be a file credential containing kubeconfig.

Update this value if your registry path is different:

```groovy
REGISTRY_HOST = 'ghcr.io/your-org/cyan-business'
```

The Jenkins flow builds jars, builds/pushes service images, applies manifests, sets image tags, and waits for rollout.

## Service Groups

Deploy infrastructure first:

```text
PostgreSQL
MongoDB
Kafka
Axon Server
Envoy Gateway
```

Recommended service rollout order:

```text
sso-auth-service
sso-user-service
sso-captcha-service
sso-otp-service
sso-session-service
sso-fido-service
processor-service
event-service
content-service
catalog-service
crm-service
commerce-service
finance-service
inventory-service
report-service
payment-service
storefront-service
media-service
cart-service
checkout-service
bpm-service
automation-orchestrator-service
ai-orchestrator-service
notification-service
payment-orchestrator-service
pricing-promotion-service
search-index-service
bot-adapter-service
legacy services as needed
```

Kubernetes can start them all at once, but staged rollout makes troubleshooting easier.

## Smoke Tests

After rollout, test through Envoy Gateway:

```bash
curl -i https://api.example.com/.well-known/jwks.json
curl -i https://api.example.com/public/storefront/render?path=/
curl -i https://api.example.com/public/search-index/suggest?q=test
curl -i https://api.example.com/public/bot-adapter/health
```

Check pods and events:

```bash
kubectl -n cyan-staging get pods
kubectl -n cyan-staging describe gateway cyan-gateway
kubectl -n cyan-staging describe httproute cyan-platform-routes
kubectl -n cyan-staging logs deployment/content-service
```

## Regenerating Deployment Assets

When adding or removing a service, update `scripts/generate_server_deployment_assets.js`, then run:

```bash
node scripts/generate_server_deployment_assets.js
```

Review the generated changes before committing:

```bash
git diff -- deploy/kubernetes .github/workflows Jenkinsfile '*application-server.properties' '*Dockerfile'
```

Keep `server.port`, Kubernetes `Service.port`, simple-discovery URI port, and Envoy `backendRefs.port` aligned.

## Production Hardening Checklist

- Add HTTPS listener and certificate integration to `cyan-gateway`.
- Replace all `change-me` values in secrets.
- Use per-service internal API credentials.
- Add Kubernetes `NetworkPolicy` rules around `/internal/**` services.
- Add resource requests and limits per service.
- Add persistent storage or managed service endpoints for PostgreSQL, MongoDB, Kafka, and Axon.
- Add centralized logging and metrics.
- Add backup and restore plans for PostgreSQL and MongoDB.
- Add image vulnerability scanning in CI.
- Gate `main` deployment behind protected GitHub/Jenkins approvals.
- Confirm tenant/site headers are preserved by Envoy for storefront and scoped flows.

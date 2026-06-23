# GCP GKE Deployment Runbook

This runbook bootstraps Cyan Business on Google Kubernetes Engine and then reuses the repository's existing Kubernetes assets in `deploy/kubernetes`.

Use this file with:

- `SERVER_DEPLOYMENT_GUIDE.md` for the shared platform deployment model
- `SERVER_KUBERNETES_BOOTSTRAP_RUNBOOK.md` for the generic Kubernetes bootstrap flow

This GCP version assumes:

- Google Kubernetes Engine for Kubernetes
- Artifact Registry for container images
- Cloud SQL for PostgreSQL
- managed or external Kafka
- self-managed MongoDB or MongoDB Atlas
- self-managed or external Axon Server

There is no first-party Google Cloud managed MongoDB service that should be treated as a direct default here. Keep MongoDB compatibility explicit.

Replace every value wrapped in `<...>`.

## 1. Target Shape

Recommended production shape:

- GKE cluster with nodes in more than one zone
- one `cyan-staging` namespace and one `cyan-production` namespace
- Cloud SQL for PostgreSQL
- Kafka outside the application namespace
- MongoDB outside the application namespace
- Axon Server outside the application namespace unless the legacy services are retired
- Envoy Gateway inside the cluster with a public `LoadBalancer`
- Artifact Registry as the image source for every microservice

The repository still expects:

- Kubernetes DNS service discovery
- `SPRING_PROFILES_ACTIVE=server`
- Envoy Gateway `GatewayClass` name `cyan-envoy`
- one PostgreSQL database per service that owns relational definitions

## 2. GCP Prerequisites

Install and configure:

- `gcloud`
- `kubectl`
- `docker`
- `jq`

Authenticate and select the target project:

```bash
gcloud auth login
gcloud config set project <project-id>
gcloud config list
```

Enable core services:

```bash
gcloud services enable \
  container.googleapis.com \
  artifactregistry.googleapis.com \
  compute.googleapis.com
```

Set shared variables:

```bash
export PROJECT_ID=<project-id>
export GKE_LOCATION=<gke-location>
export GAR_LOCATION=<artifact-registry-location>
export CLUSTER_NAME=cyan-business
export KUBE_NAMESPACE=cyan-staging
export IMAGE_TAG=develop
```

## 3. Provision Managed Dependencies

Before deploying application workloads, provision:

- PostgreSQL: Cloud SQL for PostgreSQL
- Kafka: managed Kafka provider or self-managed Kafka
- MongoDB: MongoDB Atlas or self-managed MongoDB
- Axon Server: self-managed on Compute Engine, another Kubernetes cluster, or an external environment

Create one PostgreSQL database per service listed in `SERVER_DEPLOYMENT_GUIDE.md`.

Collect these values for the Kubernetes secret:

```text
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
KAFKA_BOOTSTRAP_SERVERS
AXON_SERVER_SERVERS
AXON_SERVER_TOKEN
JWKS_URI
SSO_JWT_ISSUER
SSO_JWT_AUDIENCE
PUBLIC_PLATFORM_BASE_URL
AUTOMATION_CALLBACK_SECRET
```

Keep service-specific overrides only for the services that really need separate endpoints or credentials.

## 4. Create The GKE Cluster

For a straightforward baseline, create the cluster with `gcloud`:

```bash
gcloud container clusters create "$CLUSTER_NAME" \
  --location "$GKE_LOCATION" \
  --machine-type e2-standard-4 \
  --num-nodes 3 \
  --release-channel regular
```

Fetch kubeconfig credentials:

```bash
gcloud container clusters get-credentials "$CLUSTER_NAME" --location "$GKE_LOCATION"
```

Verify access:

```bash
kubectl config current-context
kubectl get nodes -o wide
kubectl get pods -A
```

For production, move beyond this baseline before first traffic:

- prefer multi-zone or regional topology
- define node service accounts intentionally
- enable logging, metrics, and backups explicitly
- use private networking where required
- control cluster upgrades and maintenance windows

## 5. Create Artifact Registry

Create a Docker repository:

```bash
gcloud artifacts repositories create cyan-business \
  --repository-format=docker \
  --location="$GAR_LOCATION"
```

Authenticate Docker:

```bash
gcloud auth configure-docker "$GAR_LOCATION-docker.pkg.dev"
```

Make sure the GKE node service account can pull images. At minimum, it needs Artifact Registry read access for the target repository or project.

## 6. Install Envoy Gateway

Install Envoy Gateway before applying the repo manifests:

```bash
kubectl apply --server-side -f https://github.com/envoyproxy/gateway/releases/download/v1.8.0/install.yaml
kubectl wait --timeout=5m -n envoy-gateway-system deployment/envoy-gateway --for=condition=Available
kubectl get crd gateways.gateway.networking.k8s.io httproutes.gateway.networking.k8s.io
```

The repository expects:

```text
GatewayClass: cyan-envoy
controllerName: gateway.envoyproxy.io/gatewayclass-controller
```

## 7. Create Namespace And Secrets

Create the namespace:

```bash
kubectl create namespace "$KUBE_NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -
```

Prepare a real secret manifest from `deploy/kubernetes/secret.template.yaml` and apply it:

```bash
kubectl -n "$KUBE_NAMESPACE" apply -f /path/to/edited-cyan-platform-secret.yaml
kubectl -n "$KUBE_NAMESPACE" get secret cyan-platform-secrets
```

Do not commit production secrets into the repository.

## 8. Build And Push Service Images

Build the jars first:

```bash
export SERVICES="tax-pay-sys factor-service buyer-service product-service client-service sso-auth-service sso-user-service sso-captcha-service sso-otp-service sso-session-service sso-fido-service processor-service event-service content-service catalog-service crm-service commerce-service finance-service inventory-service report-service crm-automation-service finance-automation-service inventory-automation-service report-automation-service payment-service storefront-service media-service cart-service checkout-service bpm-service automation-orchestrator-service ai-orchestrator-service notification-service payment-orchestrator-service pricing-promotion-service search-index-service bot-adapter-service"

./gradlew $(for service in $SERVICES; do printf ":%s:bootJar " "$service"; done)
```

Set the registry host:

```bash
export REGISTRY_HOST="$GAR_LOCATION-docker.pkg.dev/$PROJECT_ID/cyan-business"
```

Build and push:

```bash
for service in $SERVICES; do
  docker build -t "$REGISTRY_HOST/$service:$IMAGE_TAG" "$service"
  docker push "$REGISTRY_HOST/$service:$IMAGE_TAG"
done
```

Confirm one image exists before continuing:

```bash
gcloud artifacts docker images list \
  "$REGISTRY_HOST/content-service" \
  --include-tags
```

## 9. Deploy The Kubernetes Manifests

Apply the repository manifests:

```bash
kubectl -n "$KUBE_NAMESPACE" apply -k deploy/kubernetes
```

Set the real image for every Deployment:

```bash
for service in $SERVICES; do
  kubectl -n "$KUBE_NAMESPACE" set image "deployment/$service" \
    "$service=$REGISTRY_HOST/$service:$IMAGE_TAG"
done
```

Watch rollout:

```bash
kubectl -n "$KUBE_NAMESPACE" get pods -w
```

Check key resources:

```bash
kubectl -n "$KUBE_NAMESPACE" get gateway,httproute
kubectl -n "$KUBE_NAMESPACE" rollout status deployment/content-service --timeout=240s
kubectl -n "$KUBE_NAMESPACE" rollout status deployment/sso-auth-service --timeout=240s
kubectl -n "$KUBE_NAMESPACE" rollout status deployment/event-service --timeout=240s
```

## 10. Publish DNS

Get the Envoy Gateway address:

```bash
kubectl -n "$KUBE_NAMESPACE" get gateway cyan-gateway -o jsonpath='{.status.addresses[0].value}{"\n"}'
```

Point your Cloud DNS record to that address. Use the real public API host in:

```text
PUBLIC_PLATFORM_BASE_URL
JWKS_URI
SSO_JWT_ISSUER
```

For production, add TLS on the Envoy Gateway listener before public launch. The shared manifests start from HTTP and need certificate integration for a real production edge.

## 11. Smoke Test

Run basic gateway checks:

```bash
curl -i http://<public-api-host>/.well-known/jwks.json
curl -i http://<public-api-host>/public/storefront/render?path=/
curl -i http://<public-api-host>/public/search-index/suggest?q=test
curl -i http://<public-api-host>/public/bot-adapter/health
```

Inspect failures with:

```bash
kubectl -n "$KUBE_NAMESPACE" get pods
kubectl -n "$KUBE_NAMESPACE" describe pod <pod-name>
kubectl -n "$KUBE_NAMESPACE" logs deployment/<service-name> --tail=200
kubectl -n envoy-gateway-system logs deployment/envoy-gateway --tail=200
```

## 12. CI/CD Notes

The existing GitHub Actions and Jenkins flows can target GKE if:

- kubeconfig points at this cluster
- namespaces already exist
- `cyan-platform-secrets` already exist
- registry host is updated from GHCR to Artifact Registry

For GitHub Actions, prefer:

- GitHub OIDC to Google Cloud instead of long-lived JSON keys
- a generated kubeconfig during the workflow instead of storing broad cluster-admin credentials

For Jenkins, prefer:

- a dedicated service account for Artifact Registry and cluster deployment
- a restricted kubeconfig bound to the deployment namespace

## 13. GCP-Specific Hardening

- Use separate GCP projects or at least separate clusters for staging and production.
- Make node service-account permissions explicit; do not rely on overly broad defaults.
- Put PostgreSQL, MongoDB, Kafka, and Axon on private networking reachable from GKE.
- Back up Cloud SQL and MongoDB on an explicit schedule.
- Enable centralized logging and metrics for both cluster and application layers.
- Restrict public exposure of `/internal/**` routes with additional gateway or network controls.
- Confirm Artifact Registry pull permissions before the first rollout.

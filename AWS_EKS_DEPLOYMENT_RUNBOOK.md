# AWS EKS Deployment Runbook

This runbook bootstraps Cyan Business on Amazon EKS and then hands off to the repository's existing Kubernetes manifests in `deploy/kubernetes`.

Use this file with:

- `SERVER_DEPLOYMENT_GUIDE.md` for the shared platform deployment model
- `SERVER_KUBERNETES_BOOTSTRAP_RUNBOOK.md` for the generic single-server Kubernetes flow

This AWS version assumes:

- Amazon EKS for Kubernetes
- Amazon ECR for container images
- Amazon RDS for PostgreSQL
- Amazon MSK for Kafka
- self-managed MongoDB or MongoDB Atlas
- self-managed or external Axon Server

Do not assume Amazon DocumentDB is a drop-in MongoDB replacement for this platform unless you have already verified feature compatibility for your record and query patterns.

Replace every value wrapped in `<...>`.

## 1. Target Shape

Recommended production shape:

- EKS managed node group across at least 2 availability zones
- one `cyan-staging` namespace and one `cyan-production` namespace
- RDS PostgreSQL outside the cluster
- MongoDB outside the cluster
- MSK outside the cluster
- Axon Server outside the cluster unless the legacy services are retired
- Envoy Gateway inside the cluster with a public `LoadBalancer`
- ECR as the image source for every microservice

The repository still expects:

- Kubernetes DNS service discovery
- `SPRING_PROFILES_ACTIVE=server`
- Envoy Gateway `GatewayClass` name `cyan-envoy`
- one PostgreSQL database per service that owns relational definitions

## 2. AWS Prerequisites

Install and configure:

- `aws`
- `kubectl`
- `eksctl`
- `docker`
- `jq`

Authenticate AWS CLI and select the target account first:

```bash
aws sts get-caller-identity
aws configure list
```

Set shared variables:

```bash
export AWS_REGION=<aws-region>
export AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
export CLUSTER_NAME=cyan-business
export KUBE_NAMESPACE=cyan-staging
export IMAGE_TAG=develop
```

## 3. Provision Managed Dependencies

Before deploying application workloads, provision:

- PostgreSQL: Amazon RDS for PostgreSQL
- Kafka: Amazon MSK
- MongoDB: MongoDB Atlas or self-managed MongoDB
- Axon Server: self-managed on EC2, another Kubernetes cluster, or an external environment

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

Use service-specific overrides only where a service truly needs a different database, MongoDB URI, or credential.

## 4. Create The EKS Cluster

For staging or an initial production baseline, create the cluster with `eksctl`:

```bash
eksctl create cluster \
  --name "$CLUSTER_NAME" \
  --region "$AWS_REGION" \
  --nodes 3 \
  --node-type m6i.xlarge
```

Verify access:

```bash
kubectl config current-context
kubectl get nodes -o wide
kubectl get pods -A
```

For production, tighten this baseline before first traffic:

- move to private subnets where appropriate
- define node groups explicitly
- set cluster version intentionally
- add cluster logging, metrics, and backup policies
- define IAM access for operators and CI before rollout

## 5. Create ECR Repositories

Create one ECR repository per service image namespace. Example:

```bash
aws ecr create-repository --region "$AWS_REGION" --repository-name cyan-business/content-service
```

Authenticate Docker to ECR:

```bash
aws ecr get-login-password --region "$AWS_REGION" | \
  docker login --username AWS --password-stdin "$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com"
```

If you use a separate ECR account, make sure the EKS worker-node IAM role can pull images from that account.

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

Do not commit real AWS, database, JWT, or internal API secrets into the repository.

## 8. Build And Push Service Images

Build the jars first:

```bash
export SERVICES="tax-pay-sys factor-service buyer-service product-service client-service sso-auth-service sso-user-service sso-captcha-service sso-otp-service sso-session-service sso-fido-service processor-service event-service content-service catalog-service crm-service commerce-service finance-service inventory-service report-service crm-automation-service finance-automation-service inventory-automation-service report-automation-service payment-service storefront-service media-service cart-service checkout-service bpm-service automation-orchestrator-service ai-orchestrator-service notification-service payment-orchestrator-service pricing-promotion-service search-index-service bot-adapter-service"

./gradlew $(for service in $SERVICES; do printf ":%s:bootJar " "$service"; done)
```

Set the registry host:

```bash
export REGISTRY_HOST="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/cyan-business"
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
aws ecr describe-images \
  --region "$AWS_REGION" \
  --repository-name cyan-business/content-service \
  --query 'imageDetails[].imageTags'
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

Point your Route 53 record to that address. Use the real public API host in:

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

The existing GitHub Actions and Jenkins flows can target EKS if:

- kubeconfig points at this cluster
- namespaces already exist
- `cyan-platform-secrets` already exist
- registry host is updated from GHCR to ECR

For GitHub Actions, prefer:

- GitHub OIDC to AWS IAM instead of static AWS keys
- a generated kubeconfig during the workflow instead of storing long-lived admin credentials

For Jenkins, prefer:

- an IAM role or short-lived credentials for ECR push
- a restricted kubeconfig bound to the deployment namespace

## 13. AWS-Specific Hardening

- Use separate AWS accounts or at least separate EKS clusters for staging and production.
- Put PostgreSQL, MongoDB, Kafka, and Axon on private networking reachable from EKS.
- Use security groups and Kubernetes `NetworkPolicy` together; do not rely on only one layer.
- Make sure worker nodes can pull from ECR before the first rollout.
- Back up RDS and MongoDB on an explicit schedule.
- Enable CloudWatch or another central log sink for both cluster and application logs.
- Restrict public exposure of `/internal/**` routes with additional gateway or network controls.

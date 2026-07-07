# Cyan Business Production Deployment Guide

This runbook documents the working deployment model for Cyan Business on a single-node K3s server using Docker for image builds, K3s/containerd for workloads, Envoy Gateway as the single public entry point, and cert-manager with Let's Encrypt for TLS.

Public endpoints:

```text
Panel:        https://cyancoder.com
Platform API: https://api.cyancoder.com
JWKS:         https://api.cyancoder.com/.well-known/jwks.json
Hockey UI:    https://hockey.cyancoder.net
Hockey API:   https://hockeyapi.cyancoder.net
```

## 1. Target architecture

```text
Internet
   |
   v
23.88.112.248
   |
   v
K3s ServiceLB
   |
   v
cyan-staging/cyan-gateway
   |
   +--> cyancoder.com
   |      \--> panel-web:3000
   |
   +--> api.cyancoder.com
   |      +--> sso-auth-service:9001
   |      +--> sso-user-service:9002
   |      +--> sso-captcha-service:9003
   |      +--> sso-otp-service:9004
   |      +--> sso-session-service:9005
   |      +--> sso-fido-service:9006
   |      +--> tax-pay-sys:8002
   |      +--> content-service:9101
   |      +--> ...
   |      \--> bot-adapter-service:9126
   |
   +--> hockey.cyancoder.net
   |      \--> selectorless Service/EndpointSlice --> Docker host port 3000
   |
   \--> hockeyapi.cyancoder.net
          \--> selectorless Service/EndpointSlice --> Docker host port 8090
```

## 2. Critical rule: one public Gateway only

Keep only this public Gateway:

```text
cyan-staging/cyan-gateway
```

Do not create another public `Gateway` or `LoadBalancer` Service on this single-node K3s server. K3s ServiceLB reserves host ports `80` and `443`. Two Gateways competing for those ports cause one `svclb-*` pod to remain `Pending` with an event such as:

```text
0/1 nodes are available: 1 node(s) didn't have free ports for the requested pod ports
```

Check the healthy state:

```bash
kubectl get gateway -A
kubectl get svc -n envoy-gateway-system
kubectl get pods -n kube-system | grep svclb
```

Expected:

```text
Only cyan-staging/cyan-gateway is public.
Only one staging Envoy ServiceLB pod owns ports 80 and 443.
```

Never recreate `default/cyan-gateway`.

## 3. Repository and environment

Expected repository structure:

```text
cyan_business/
├── panel-web/
├── sso-auth-service/
├── sso-user-service/
├── sso-captcha-service/
├── content-service/
├── ...
└── deploy/
    └── kubernetes/
        ├── apps.yaml
        ├── envoy-gateway.yaml
        ├── kustomization.yaml
        └── secret.template.yaml
```

Set reusable variables:

```bash
export APP_DIR=/root/cyan_business
export NS=cyan-staging
cd "$APP_DIR"
```

## 4. Preflight checks

```bash
kubectl get nodes -o wide
kubectl get pods -A
kubectl get gateway -A
kubectl get svc -n envoy-gateway-system
kubectl -n "$NS" get pods
kubectl -n "$NS" get svc
nproc
free -h
df -h /
```

For the complete platform on one node, use approximately:

```text
CPU: 8 cores or more
RAM: 24 GB or more
Disk: 150 GB or more
```

Verify infrastructure dependencies before starting services that require them:

```bash
kubectl -n "$NS" get statefulset
kubectl -n "$NS" get svc postgres mongo kafka axon-server
```

## 5. DNS

Required records:

```dns
cyancoder.com.       300 IN A 23.88.112.248
api.cyancoder.com.   300 IN A 23.88.112.248
```

If BIND is authoritative, it must contain a `cyancoder.com` zone. Example:

```conf
zone "cyancoder.com" IN {
    type primary;
    file "/var/named/cyancoder.com.zone";
    allow-query { any; };
};
```

Example zone file:

```dns
$TTL 300
$ORIGIN cyancoder.com.

@ IN SOA ns1.cyancoder.net. hostmaster.cyancoder.com. (
    2026070501
    3600
    600
    1209600
    300
)

@       IN NS      ns1.cyancoder.net.
@       IN NS      ns2.cyancoder.net.
@       IN A       23.88.112.248
www     IN CNAME   cyancoder.com.
api     IN A       23.88.112.248
```

Validate and reload:

```bash
named-checkconf
named-checkzone cyancoder.com /var/named/cyancoder.com.zone
rndc reload cyancoder.com
```

Verify:

```bash
dig @ns1.cyancoder.net cyancoder.com SOA +norecurse
dig @8.8.8.8 +short cyancoder.com A
dig @8.8.8.8 +short api.cyancoder.com A
```

Both A records must return `23.88.112.248`.

## 6. Back up the working edge

Before changing listeners or routes:

```bash
kubectl get gateway cyan-gateway \
  -n "$NS" \
  -o yaml \
  > /root/cyan-gateway-working-backup.yaml

kubectl get httproute \
  -n "$NS" \
  -o yaml \
  > /root/cyan-routes-working-backup.yaml
```

Restore if required:

```bash
kubectl apply -f /root/cyan-gateway-working-backup.yaml
kubectl apply -f /root/cyan-routes-working-backup.yaml
```

## 7. Platform secrets

Start from the repository template:

```bash
cp deploy/kubernetes/secret.template.yaml \
  /root/cyan-platform-secrets.yaml
```

Set at least:

```text
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
KAFKA_BOOTSTRAP_SERVERS
JWKS_URI=https://api.cyancoder.com/.well-known/jwks.json
SSO_JWT_ISSUER=https://api.cyancoder.com
SSO_JWT_AUDIENCE=cyan-business
PUBLIC_PLATFORM_BASE_URL=https://api.cyancoder.com
AXON_SERVER_SERVERS
AXON_SERVER_TOKEN
AUTOMATION_CALLBACK_SECRET
CONTENT_SERVICE_INTERNAL_PASSWORD
AI_ORCHESTRATOR_SERVICE_INTERNAL_PASSWORD
BPM_SERVICE_INTERNAL_PASSWORD
NOTIFICATION_SERVICE_INTERNAL_PASSWORD
GAPGPT_API_KEY
```

Apply:

```bash
kubectl -n "$NS" apply -f /root/cyan-platform-secrets.yaml
```

Do not commit live secrets.

## 8. Docker images and K3s containerd

Docker and K3s use different image stores. Building an image with Docker does not make it available to K3s automatically.

Build:

```bash
docker build -t IMAGE:TAG DIRECTORY
```

Import into K3s:

```bash
docker save IMAGE:TAG |
  k3s ctr -n k8s.io images import -
```

Verify:

```bash
k3s ctr -n k8s.io images list | grep IMAGE_NAME
```

Use unique image tags:

```bash
TAG="$(git rev-parse --short HEAD)-$(date +%Y%m%d%H%M%S)"
```

Avoid repeatedly reusing `latest` or `local`.

## 9. Deploy `panel-web`

### 9.1 Dockerfile

Use `panel-web/Dockerfile`:

```dockerfile
FROM node:20-alpine AS deps
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci

FROM node:20-alpine AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
ARG NEXT_PUBLIC_PLATFORM_API_BASE_URL
ENV NEXT_PUBLIC_PLATFORM_API_BASE_URL=${NEXT_PUBLIC_PLATFORM_API_BASE_URL}
RUN npm run build

FROM node:20-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production
ENV HOSTNAME=0.0.0.0
ENV PORT=3000
COPY --from=builder /app ./
EXPOSE 3000
CMD ["npm", "run", "start"]
```

### 9.2 Build and import

```bash
cd "$APP_DIR"

TAG="$(git rev-parse --short HEAD)-$(date +%Y%m%d%H%M%S)"
PANEL_IMAGE="docker.io/cyanlocal/panel-web:${TAG}"

docker build \
  --no-cache \
  --build-arg NEXT_PUBLIC_PLATFORM_API_BASE_URL=https://api.cyancoder.com \
  -t "$PANEL_IMAGE" \
  ./panel-web

docker save "$PANEL_IMAGE" |
  k3s ctr -n k8s.io images import -
```

`NEXT_PUBLIC_*` variables are compiled into the browser bundle during `npm run build`. Setting the value only on the Kubernetes Deployment does not update already-built JavaScript.

### 9.3 Deployment and Service

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: panel-web
  namespace: cyan-staging
spec:
  replicas: 1
  selector:
    matchLabels:
      app.kubernetes.io/name: panel-web
  template:
    metadata:
      labels:
        app.kubernetes.io/name: panel-web
    spec:
      containers:
        - name: panel-web
          image: docker.io/cyanlocal/panel-web:REPLACE_TAG
          imagePullPolicy: Never
          ports:
            - name: http
              containerPort: 3000
          env:
            - name: NODE_ENV
              value: production
            - name: HOSTNAME
              value: 0.0.0.0
            - name: PORT
              value: "3000"
            - name: NEXT_PUBLIC_PLATFORM_API_BASE_URL
              value: https://api.cyancoder.com
          readinessProbe:
            tcpSocket:
              port: http
            initialDelaySeconds: 15
            periodSeconds: 10
          livenessProbe:
            tcpSocket:
              port: http
            initialDelaySeconds: 45
            periodSeconds: 20
---
apiVersion: v1
kind: Service
metadata:
  name: panel-web
  namespace: cyan-staging
spec:
  selector:
    app.kubernetes.io/name: panel-web
  ports:
    - name: http
      port: 3000
      targetPort: http
```

Apply and verify:

```bash
kubectl apply -f deploy/kubernetes/panel-web.yaml

kubectl rollout status deployment/panel-web \
  -n "$NS" \
  --timeout=180s

kubectl run panel-test \
  -n "$NS" \
  --rm -i \
  --restart=Never \
  --image=curlimages/curl \
  -- curl -I http://panel-web:3000/
```

## 10. TLS and Gateway listeners

Use the existing `letsencrypt-production` ClusterIssuer.

Certificate example:

```yaml
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: cyancoder-public
  namespace: cyan-staging
spec:
  secretName: cyancoder-public-tls
  issuerRef:
    name: letsencrypt-production
    kind: ClusterIssuer
  dnsNames:
    - cyancoder.com
    - api.cyancoder.com
```

Apply and wait:

```bash
kubectl apply -f deploy/kubernetes/cyancoder-certificate.yaml

kubectl wait \
  --for=condition=Ready \
  certificate/cyancoder-public \
  -n "$NS" \
  --timeout=300s
```

The existing shared Gateway should include listeners equivalent to:

```yaml
- name: cyancoder-https
  hostname: cyancoder.com
  protocol: HTTPS
  port: 443
  tls:
    mode: Terminate
    certificateRefs:
      - group: ""
        kind: Secret
        name: cyancoder-public-tls
  allowedRoutes:
    namespaces:
      from: Same

- name: api-cyancoder-https
  hostname: api.cyancoder.com
  protocol: HTTPS
  port: 443
  tls:
    mode: Terminate
    certificateRefs:
      - group: ""
        kind: Secret
        name: cyancoder-public-tls
  allowedRoutes:
    namespaces:
      from: Same
```

Do not replace the entire Gateway with an old HTTP-only manifest.

Verify live listeners:

```bash
kubectl get gateway cyan-gateway \
  -n "$NS" \
  -o jsonpath='{range .spec.listeners[*]}{.name}{" "}{.hostname}{" "}{.protocol}{":"}{.port}{"\n"}{end}'
```

Expected listener set includes:

```text
http
hockey-https
hockeyapi-https
cyancoder-https
api-cyancoder-https
```

## 11. Panel route

```yaml
apiVersion: gateway.networking.k8s.io/v1
kind: HTTPRoute
metadata:
  name: cyancoder-panel-https
  namespace: cyan-staging
spec:
  parentRefs:
    - name: cyan-gateway
      namespace: cyan-staging
      sectionName: cyancoder-https
  hostnames:
    - cyancoder.com
  rules:
    - matches:
        - path:
            type: PathPrefix
            value: /
      backendRefs:
        - name: panel-web
          port: 3000
```

HTTP redirect:

```yaml
apiVersion: gateway.networking.k8s.io/v1
kind: HTTPRoute
metadata:
  name: cyancoder-panel-http-redirect
  namespace: cyan-staging
spec:
  parentRefs:
    - name: cyan-gateway
      namespace: cyan-staging
      sectionName: http
  hostnames:
    - cyancoder.com
  rules:
    - filters:
        - type: RequestRedirect
          requestRedirect:
            scheme: https
            statusCode: 301
```

## 12. Cyan API route file

`deploy/kubernetes/envoy-gateway.yaml` must contain **HTTPRoute objects only**.

It must not contain:

```yaml
kind: Gateway
```

or:

```yaml
kind: GatewayClass
```

Check:

```bash
grep -nE '^kind: (Gateway|GatewayClass)$' \
  deploy/kubernetes/envoy-gateway.yaml
```

Expected: no output.

Every Cyan API route must explicitly include:

```yaml
metadata:
  namespace: cyan-staging

spec:
  parentRefs:
    - name: cyan-gateway
      namespace: cyan-staging
      sectionName: api-cyancoder-https
  hostnames:
    - api.cyancoder.com
```

Example route header:

```yaml
apiVersion: gateway.networking.k8s.io/v1
kind: HTTPRoute
metadata:
  name: cyan-platform-routes
  namespace: cyan-staging
spec:
  parentRefs:
    - name: cyan-gateway
      namespace: cyan-staging
      sectionName: api-cyancoder-https
  hostnames:
    - api.cyancoder.com
  rules:
```

### 12.1 Maximum rules per route

Gateway API accepts at most **16 `rules` entries per `HTTPRoute`**.

If validation reports:

```text
spec.rules: Too many: 17: must have at most 16 items
```

split the rules into another route, for example:

```text
cyan-platform-routes
cyan-platform-routes-2
cyan-platform-routes-3
cyan-platform-routes-4
```

Every route object must use the same hostname and listener attachment.

### 12.2 Validate YAML formatting

Check for tabs:

```bash
grep -nP '\t' deploy/kubernetes/envoy-gateway.yaml
```

Expected: no output.

Correct indentation:

```yaml
spec:
  parentRefs:
    - name: cyan-gateway
      namespace: cyan-staging
      sectionName: api-cyancoder-https
  hostnames:
    - api.cyancoder.com
  rules:
```

`hostnames:`, `parentRefs:`, and `rules:` must align.

Print numbered sections while debugging:

```bash
nl -ba deploy/kubernetes/envoy-gateway.yaml |
  sed -n '1,60p'
```

### 12.3 Dry-run and apply

```bash
kubectl apply \
  --dry-run=server \
  -f deploy/kubernetes/envoy-gateway.yaml

kubectl apply \
  -f deploy/kubernetes/envoy-gateway.yaml
```

### 12.4 Verify namespace

```bash
kubectl get httproute -A |
  grep cyan-platform
```

All Cyan platform routes must be in `cyan-staging`.

Delete accidental default-namespace copies:

```bash
kubectl delete httproute \
  cyan-platform-routes \
  cyan-platform-routes-2 \
  cyan-platform-routes-3 \
  -n default \
  --ignore-not-found
```

## 13. Route map

### 13.1 Legacy services

| Public path prefix | Backend Service | Port |
|---|---|---:|
| `/v2/api/tax-service/` | `tax-pay-sys` | 8002 |
| `/v2/api/tax/` | `tax-pay-sys` | 8002 |
| `/v2/api/factor-service/` | `factor-service` | 8003 |
| `/v2/api/buyer-service/` | `buyer-service` | 8004 |
| `/v2/api/product-service/` | `product-service` | 8005 |
| `/v2/api/client-service/` | `client-service` | 8010 |

### 13.2 SSO services

| Public path prefix | Backend Service | Port |
|---|---|---:|
| `/realms/` | `sso-auth-service` | 9001 |
| `/api/sso/auth/` | `sso-auth-service` | 9001 |
| `/.well-known/` | `sso-auth-service` | 9001 |
| `/api/sso/users/` | `sso-user-service` | 9002 |
| `/api/sso/iam/` | `sso-user-service` | 9002 |
| `/api/sso/captcha/` | `sso-captcha-service` | 9003 |
| `/api/sso/otp/` | `sso-otp-service` | 9004 |
| `/api/sso/sessions/` | `sso-session-service` | 9005 |
| `/api/sso/fido/` | `sso-fido-service` | 9006 |

### 13.3 Core services

| Public path prefix | Backend Service | Port |
|---|---|---:|
| `/api/content-service/` | `content-service` | 9101 |
| `/api/catalog-service/` | `catalog-service` | 9102 |
| `/api/crm-service/` | `crm-service` | 9103 |
| `/api/commerce-service/` | `commerce-service` | 9104 |
| `/api/finance-service/` | `finance-service` | 9105 |
| `/api/inventory-service/` | `inventory-service` | 9106 |
| `/api/report-service/` | `report-service` | 9107 |
| `/api/processor-service/` | `processor-service` | 9108 |
| `/api/event-service/` | `event-service` | 9109 |

### 13.4 Automation and commerce services

| Public path prefix | Backend Service | Port |
|---|---|---:|
| `/api/crm-automation-service/` | `crm-automation-service` | 9110 |
| `/api/finance-automation-service/` | `finance-automation-service` | 9111 |
| `/api/inventory-automation-service/` | `inventory-automation-service` | 9112 |
| `/api/report-automation-service/` | `report-automation-service` | 9113 |
| `/api/payment-service/` | `payment-service` | 9114 |
| `/endpoint/payment/` | `payment-service` | 9114 |
| `/internal/payment/` | `payment-service` | 9114 |
| `/public/payment/` | `payment-service` | 9114 |
| `/api/storefront-service/` | `storefront-service` | 9115 |
| `/public/storefront/` | `storefront-service` | 9115 |
| `/api/media-service/` | `media-service` | 9116 |
| `/public/media/` | `media-service` | 9116 |
| `/internal/media/` | `media-service` | 9116 |
| `/api/cart-service/` | `cart-service` | 9117 |
| `/api/checkout-service/` | `checkout-service` | 9118 |

### 13.5 Orchestration and integration services

| Public path prefix | Backend Service | Port |
|---|---|---:|
| `/api/bpm-service/` | `bpm-service` | 9119 |
| `/endpoint/bpm/` | `bpm-service` | 9119 |
| `/internal/bpm/` | `bpm-service` | 9119 |
| `/public/bpm/` | `bpm-service` | 9119 |
| `/api/automation-orchestrator-service/` | `automation-orchestrator-service` | 9120 |
| `/internal/automation-orchestrator/` | `automation-orchestrator-service` | 9120 |
| `/api/ai-orchestrator-service/` | `ai-orchestrator-service` | 9121 |
| `/endpoint/ai-orchestrator/` | `ai-orchestrator-service` | 9121 |
| `/internal/ai-orchestrator/` | `ai-orchestrator-service` | 9121 |
| `/api/notification-service/` | `notification-service` | 9122 |
| `/endpoint/notifications/` | `notification-service` | 9122 |
| `/internal/notifications/` | `notification-service` | 9122 |
| `/api/payment-orchestrator-service/` | `payment-orchestrator-service` | 9123 |
| `/api/pricing-promotion-service/` | `pricing-promotion-service` | 9124 |
| `/api/search-index-service/` | `search-index-service` | 9125 |
| `/public/search-index/` | `search-index-service` | 9125 |
| `/internal/search-index/` | `search-index-service` | 9125 |
| `/api/bot-adapter-service/` | `bot-adapter-service` | 9126 |
| `/endpoint/bot-adapter/` | `bot-adapter-service` | 9126 |
| `/public/bot-adapter/` | `bot-adapter-service` | 9126 |

## 14. Bring services up one by one

Recommended order:

```text
1. sso-auth-service
2. sso-user-service
3. sso-captcha-service
4. sso-otp-service
5. sso-session-service
6. sso-fido-service
7. content-service
8. catalog-service
9. remaining services
```

Reusable build and deployment sequence:

```bash
cd "$APP_DIR"

SERVICE=sso-captcha-service
TAG="$(git rev-parse --short HEAD)-$(date +%Y%m%d%H%M%S)"
IMAGE="docker.io/cyanlocal/${SERVICE}:${TAG}"

./gradlew ":${SERVICE}:bootJar"

docker build \
  --no-cache \
  -t "$IMAGE" \
  "./${SERVICE}"

docker save "$IMAGE" |
  k3s ctr -n k8s.io images import -
```

If the Deployment exists:

```bash
CONTAINER=$(kubectl get deployment "$SERVICE" \
  -n "$NS" \
  -o jsonpath='{.spec.template.spec.containers[0].name}')

kubectl set image deployment/"$SERVICE" \
  "${CONTAINER}=${IMAGE}" \
  -n "$NS"

kubectl rollout status deployment/"$SERVICE" \
  -n "$NS" \
  --timeout=180s

kubectl logs deployment/"$SERVICE" \
  -n "$NS" \
  --tail=200
```

### 14.1 `ErrImageNeverPull`

Cause:

```text
The image is referenced by the Deployment but is absent from K3s containerd.
```

Fix:

```bash
docker save "$IMAGE" |
  k3s ctr -n k8s.io images import -

kubectl rollout restart deployment/"$SERVICE" \
  -n "$NS"
```

### 14.2 `CrashLoopBackOff`

Inspect current and previous logs:

```bash
kubectl logs deployment/"$SERVICE" \
  -n "$NS" \
  --all-containers \
  --tail=250

kubectl logs deployment/"$SERVICE" \
  -n "$NS" \
  --all-containers \
  --previous \
  --tail=250

kubectl describe deployment "$SERVICE" \
  -n "$NS"
```

Common causes:

```text
Database does not exist
Invalid database credentials
Missing Secret or ConfigMap
Kafka unavailable
Axon Server unavailable
Migration failure
Incorrect JWKS URL
Missing dependent SSO service
Old image
```

## 15. Service verification

### Pod

```bash
kubectl get pods -n "$NS" | grep SERVICE_NAME
```

### Service and endpoints

```bash
kubectl get svc SERVICE_NAME \
  -n "$NS" \
  -o wide

kubectl get endpoints SERVICE_NAME \
  -n "$NS" \
  -o wide
```

A Service with no endpoints cannot receive traffic.

### TCP test inside Kubernetes

```bash
kubectl run port-test \
  -n "$NS" \
  --rm -i \
  --restart=Never \
  --image=busybox:1.36 \
  -- nc -vz SERVICE_NAME PORT
```

### HTTP test inside Kubernetes

```bash
kubectl run curl-test \
  -n "$NS" \
  --rm -i \
  --restart=Never \
  --image=curlimages/curl \
  -- curl -i http://SERVICE_NAME:PORT/REAL_ENDPOINT
```

Do not assume `/actuator/health` exists. Some services do not include Spring Boot Actuator.

## 16. SSO verification

Internal JWKS:

```bash
kubectl run sso-jwks-test \
  -n "$NS" \
  --rm -i \
  --restart=Never \
  --image=curlimages/curl \
  -- curl -i \
  http://sso-auth-service:9001/.well-known/jwks.json
```

Public JWKS:

```bash
curl -vk -i \
  https://api.cyancoder.com/.well-known/jwks.json
```

Captcha is handled by `sso-captcha-service`, not `sso-auth-service`:

```text
/api/sso/captcha/* --> sso-captcha-service:9003
```

Verify the backend exists:

```bash
kubectl get deployment,svc,endpoints \
  -n "$NS" |
  grep sso-captcha-service
```

Then test:

```bash
curl -vk -i \
  -X POST \
  'https://api.cyancoder.com/api/sso/captcha/challenges?clientId=cyan-panel' \
  -H 'Content-Type: application/json' \
  -H 'Origin: https://cyancoder.com' \
  --data '{}'
```

## 17. Route status

```bash
for ROUTE in \
  cyan-platform-routes \
  cyan-platform-routes-2 \
  cyan-platform-routes-3
do
  echo "===== $ROUTE ====="

  kubectl get httproute "$ROUTE" \
    -n "$NS" \
    -o jsonpath='hosts={.spec.hostnames}{"\n"}listener={.spec.parentRefs[0].sectionName}{"\n"}'

  kubectl get httproute "$ROUTE" \
    -n "$NS" \
    -o jsonpath='{range .status.parents[*].conditions[*]}{.type}={.status}{" reason="}{.reason}{" message="}{.message}{"\n"}{end}'

  echo
done
```

Healthy attachment:

```text
hosts=["api.cyancoder.com"]
listener=api-cyancoder-https
Accepted=True
```

During staged rollout, a route can report:

```text
ResolvedRefs=False
```

when one or more referenced Services do not yet exist.

## 18. HTTP status interpretation

### `404 Not Found`

Possible causes:

```text
No matching HTTPRoute
Wrong hostname
Wrong backend path
Request sent to cyancoder.com instead of api.cyancoder.com
Next.js catch-all route handled the request
```

If response headers contain:

```text
vary: RSC, Next-Router-State-Tree
```

then the request reached `panel-web`, not the API.

### `500 Internal Server Error`

Possible causes:

```text
Invalid backend reference
Missing Service
Application exception
A global Spring exception handler converted a missing route into 500
```

### `503 Service Unavailable`

Possible causes:

```text
Service exists but has no ready endpoints
Pod is unready
Service targetPort mismatch
```

### Connection refused on port 443

Check:

```bash
kubectl get svc -n envoy-gateway-system
kubectl get pods -n kube-system | grep svclb
kubectl get gateway -A
```

If the staging Envoy Service has:

```text
EXTERNAL-IP: <pending>
```

look for a duplicate Gateway or another ServiceLB pod occupying ports `80/443`.

## 19. Panel API base URL

The panel must call:

```text
https://api.cyancoder.com
```

not:

```text
https://cyancoder.com/api/...
```

Search the source:

```bash
grep -RniE \
'NEXT_PUBLIC_PLATFORM_API_BASE_URL|/api/sso|localhost|127\.0\.0\.1' \
panel-web \
--exclude-dir=node_modules \
--exclude-dir=.next
```

Correct usage:

```typescript
const apiBase =
  process.env.NEXT_PUBLIC_PLATFORM_API_BASE_URL ??
  "https://api.cyancoder.com";

await fetch(`${apiBase}/api/sso/captcha/challenges?clientId=cyan-panel`, {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
  },
  body: "{}",
});
```

Rebuild the panel after changing any `NEXT_PUBLIC_*` variable.

## 20. Update after `git pull`

### 20.1 Pull

```bash
cd "$APP_DIR"

git pull
git status
git log -1 --oneline

TAG="$(git rev-parse --short HEAD)-$(date +%Y%m%d%H%M%S)"
```

### 20.2 Update only `panel-web`

```bash
PANEL_IMAGE="docker.io/cyanlocal/panel-web:${TAG}"

docker build \
  --no-cache \
  --build-arg NEXT_PUBLIC_PLATFORM_API_BASE_URL=https://api.cyancoder.com \
  -t "$PANEL_IMAGE" \
  ./panel-web

docker save "$PANEL_IMAGE" |
  k3s ctr -n k8s.io images import -

kubectl set image deployment/panel-web \
  panel-web="$PANEL_IMAGE" \
  -n "$NS"

kubectl rollout status deployment/panel-web \
  -n "$NS" \
  --timeout=180s
```

### 20.3 Update one backend service

```bash
SERVICE=content-service
IMAGE="docker.io/cyanlocal/${SERVICE}:${TAG}"

./gradlew ":${SERVICE}:bootJar"

docker build \
  --no-cache \
  -t "$IMAGE" \
  "./${SERVICE}"

docker save "$IMAGE" |
  k3s ctr -n k8s.io images import -

CONTAINER=$(kubectl get deployment "$SERVICE" \
  -n "$NS" \
  -o jsonpath='{.spec.template.spec.containers[0].name}')

kubectl set image deployment/"$SERVICE" \
  "${CONTAINER}=${IMAGE}" \
  -n "$NS"

kubectl rollout status deployment/"$SERVICE" \
  -n "$NS" \
  --timeout=180s
```

A rollout restart by itself does not build or load new code.

Correct sequence:

```text
git pull
build artifact
build image
import image into K3s
update Deployment image
wait for rollout
verify
```

## 21. Rollback

History:

```bash
kubectl rollout history deployment/SERVICE_NAME \
  -n "$NS"
```

Rollback:

```bash
kubectl rollout undo deployment/SERVICE_NAME \
  -n "$NS"

kubectl rollout status deployment/SERVICE_NAME \
  -n "$NS" \
  --timeout=180s
```

Inspect the deployed image:

```bash
kubectl get deployment SERVICE_NAME \
  -n "$NS" \
  -o jsonpath='{.spec.template.spec.containers[0].image}{"\n"}'
```

## 22. Full smoke test

DNS:

```bash
dig +short cyancoder.com
dig +short api.cyancoder.com
```

Gateway and routes:

```bash
kubectl get gateway cyan-gateway -n "$NS"
kubectl get httproute -n "$NS"
```

Panel:

```bash
curl -Iv https://cyancoder.com/
```

JWKS:

```bash
curl -sS \
  https://api.cyancoder.com/.well-known/jwks.json |
  jq
```

Hockey:

```bash
curl -I https://hockey.cyancoder.net/

curl -sS \
  https://hockeyapi.cyancoder.net/actuator/health
```

Force a public-IP test while preserving the Host/SNI value:

```bash
curl -vk \
  --resolve api.cyancoder.com:443:23.88.112.248 \
  https://api.cyancoder.com/.well-known/jwks.json
```

## 23. Security and production notes

1. Do not expose PostgreSQL, MongoDB, Kafka, or Axon ports publicly.
2. Do not commit live Secrets.
3. Use unique high-entropy JWT and internal service secrets.
4. Change all default passwords.
5. Restrict public access to `/internal/**` routes.
6. Add NetworkPolicies after the service topology stabilizes.
7. Add Actuator health endpoints and Kubernetes probes to each Spring service.
8. Use reliable persistent volumes or managed databases.
9. Move to a registry and CI/CD after manual rollout is stable.
10. Keep hockey selectorless Services/EndpointSlices separate from Cyan Business Services.
11. Do not recreate `default/cyan-gateway`.
12. Never replace the shared working Gateway with an older HTTP-only manifest.

## 24. Recommended CI/CD order

```text
1. Run tests
2. Build Spring Boot JARs
3. Build versioned backend images
4. Build panel-web with the production API URL
5. Push images to a registry
6. Apply namespace-scoped manifests
7. Update Deployment images
8. Wait for rollouts
9. Verify HTTPRoute status
10. Run public smoke tests
11. Roll back automatically on failure
```

## 25. Fast troubleshooting commands

```bash
kubectl -n cyan-staging get pods
kubectl -n cyan-staging get svc
kubectl -n cyan-staging get endpoints
kubectl -n cyan-staging get httproute
kubectl -n cyan-staging describe gateway cyan-gateway
kubectl -n envoy-gateway-system get pods
kubectl -n kube-system get pods | grep svclb
```

Logs:

```bash
kubectl logs deployment/SERVICE_NAME \
  -n cyan-staging \
  --tail=200

kubectl logs deployment/SERVICE_NAME \
  -n cyan-staging \
  --previous \
  --tail=200
```

Route validation:

```bash
kubectl apply \
  --dry-run=server \
  -f deploy/kubernetes/envoy-gateway.yaml
```

## Final operational principles

```text
One public Gateway.
Many hostname listeners.
Many HTTPRoutes.
No more than 16 rules per HTTPRoute.
Every route explicitly uses namespace cyan-staging.
Every Cyan API route explicitly uses api.cyancoder.com.
Build with Docker.
Import images into K3s containerd.
Deploy microservices one by one.
Use unique image tags.
Verify Services and endpoints before testing public routes.
```

# Cyan Business Kubernetes Bootstrap Runbook

This runbook starts from a fresh Linux server and ends with:

- `panel-web` on `https://example.com`
- the platform API and microservices behind Envoy Gateway on `https://api.example.com`
- Docker used to build images
- Kubernetes used to run the platform

It is based on the repository bootstrap/server runbooks, but it closes the missing production gap: the repo already contains backend Kubernetes assets, while `panel-web`, TLS, and domain split still need explicit deployment steps.

Replace every value wrapped in `<...>`.

## Target Architecture

```text
https://example.com
        |
        v
Envoy Gateway :443
        |
        v
panel-web :3000
        |
        v
https://api.example.com
        |
        v
Envoy Gateway :443
        |
        v
HTTPRoute host=api.example.com
        |
        +--> sso-auth-service :9001
        +--> sso-user-service :9002
        +--> content-service :9101
        +--> catalog-service :9102
        +--> ...
        +--> bpm-service :9119
        +--> automation-orchestrator-service :9120
        +--> ai-orchestrator-service :9121
        +--> notification-service :9122
        +--> payment-orchestrator-service :9123
        +--> pricing-promotion-service :9124
        +--> search-index-service :9125
        +--> bot-adapter-service :9126
```

Public URLs:

```text
Panel:
https://example.com

API edge:
https://api.example.com

API smoke example:
https://api.example.com/public/bot-adapter/health

JWKS:
https://api.example.com/.well-known/jwks.json
```

Do not use raw node ports or temporary IP URLs in production:

```text
http://<server-ip>:3000
http://<server-ip>:8001
http://<server-ip>:9001
...
```

## 1. Server Assumptions

Recommended minimum for a single-node staging or small production bootstrap:

```text
CPU: 8 cores
RAM: 24 GB or more
Disk: 150 GB or more
OS: Ubuntu 22.04+ or Rocky/Alma/RHEL 8+
Ports: 80, 443, 6443
DNS: example.com and api.example.com must be under your control
```

For serious production, keep PostgreSQL, MongoDB, Kafka, and Axon Server outside the application node or run them as separately managed stateful infrastructure.

## 2. Install Base Packages

Ubuntu/Debian:

```bash
apt-get update
apt-get install -y curl ca-certificates git jq tar gzip unzip openssl dnsutils
```

RHEL/Rocky/Alma:

```bash
dnf update -y
dnf install -y curl ca-certificates git jq tar gzip unzip openssl bind-utils
```

If a firewall is enabled, open the required ports:

```bash
firewall-cmd --add-port=80/tcp --permanent || true
firewall-cmd --add-port=443/tcp --permanent || true
firewall-cmd --add-port=6443/tcp --permanent || true
firewall-cmd --reload || true
```

## 3. Install k3s

Download the installer first:

```bash
curl -fL https://get.k3s.io -o /tmp/install-k3s.sh
chmod +x /tmp/install-k3s.sh
test -s /tmp/install-k3s.sh
```

Install a single-node cluster. Disable Traefik because this stack uses Envoy Gateway:

```bash
INSTALL_K3S_EXEC="server --disable traefik --write-kubeconfig-mode=600" /tmp/install-k3s.sh
```

Set up `kubectl` for root:

```bash
mkdir -p /root/.kube
cp -f /etc/rancher/k3s/k3s.yaml /root/.kube/config
chmod 600 /root/.kube/config
unset KUBECONFIG
```

Verify the cluster before continuing:

```bash
kubectl config current-context
kubectl get nodes -o wide
kubectl get pods -A
kubectl get --raw=/version
```

## 4. Install Docker

Kubernetes runs workloads with containerd, but Docker is still the simplest way to build the images.

Ubuntu/Debian:

```bash
apt-get install -y docker.io
systemctl enable --now docker
docker version
```

RHEL/Rocky/Alma:

```bash
dnf install -y dnf-plugins-core
dnf config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
dnf install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin
systemctl enable --now docker
docker version
```

## 5. Install Envoy Gateway

```bash
kubectl apply --server-side -f https://github.com/envoyproxy/gateway/releases/download/v1.8.0/install.yaml
kubectl wait --timeout=5m -n envoy-gateway-system deployment/envoy-gateway --for=condition=Available
kubectl get crd gateways.gateway.networking.k8s.io httproutes.gateway.networking.k8s.io
```

This repo uses:

```text
GatewayClass: cyan-envoy
controllerName: gateway.envoyproxy.io/gatewayclass-controller
```

## 6. Install cert-manager

You need this before creating public HTTPS certificates.

```bash
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.16.2/cert-manager.yaml
kubectl wait --timeout=5m -n cert-manager deployment/cert-manager --for=condition=Available
kubectl wait --timeout=5m -n cert-manager deployment/cert-manager-webhook --for=condition=Available
kubectl wait --timeout=5m -n cert-manager deployment/cert-manager-cainjector --for=condition=Available
```

Create a Let's Encrypt issuer:

```bash
cat > /root/letsencrypt-production.yaml <<'EOF'
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-production
spec:
  acme:
    email: <ops@example.com>
    server: https://acme-v02.api.letsencrypt.org/directory
    privateKeySecretRef:
      name: letsencrypt-production
    solvers:
      - http01:
          gatewayHTTPRoute:
            parentRefs:
              - name: cyan-gateway
                namespace: cyan-staging
                kind: Gateway
EOF

kubectl apply -f /root/letsencrypt-production.yaml
kubectl get clusterissuer letsencrypt-production
```

## 7. Create Namespace

Use staging first:

```bash
kubectl create namespace cyan-staging --dry-run=client -o yaml | kubectl apply -f -
```

## 8. Provide Infrastructure Dependencies

The Spring Boot services expect these defaults unless you override them with secrets:

```text
postgres:5432
mongo:27017
kafka:9092
axon-server:8124
```

### Option A: Managed or external infrastructure

Preferred production shape:

- managed PostgreSQL
- managed MongoDB
- managed Kafka
- external Axon Server for the legacy Axon services

Set the real values later in `cyan-platform-secrets`.

### Option B: Single-node bootstrap infrastructure

For a quick server bootstrap, you can run simple in-cluster stateful services. Save this as `/root/cyan-dev-infra.yaml`.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: postgres
  namespace: cyan-staging
spec:
  selector:
    app: postgres
  ports:
    - name: postgres
      port: 5432
      targetPort: 5432
---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: postgres
  namespace: cyan-staging
spec:
  serviceName: postgres
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
        - name: postgres
          image: postgres:16
          ports:
            - containerPort: 5432
          env:
            - name: POSTGRES_USER
              value: postgres
            - name: POSTGRES_PASSWORD
              value: postgres
            - name: POSTGRES_DB
              value: postgres
          volumeMounts:
            - name: data
              mountPath: /var/lib/postgresql/data
  volumeClaimTemplates:
    - metadata:
        name: data
      spec:
        accessModes: [ReadWriteOnce]
        resources:
          requests:
            storage: 20Gi
---
apiVersion: v1
kind: Service
metadata:
  name: mongo
  namespace: cyan-staging
spec:
  selector:
    app: mongo
  ports:
    - name: mongo
      port: 27017
      targetPort: 27017
---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: mongo
  namespace: cyan-staging
spec:
  serviceName: mongo
  replicas: 1
  selector:
    matchLabels:
      app: mongo
  template:
    metadata:
      labels:
        app: mongo
    spec:
      containers:
        - name: mongo
          image: mongo:7
          ports:
            - containerPort: 27017
          volumeMounts:
            - name: data
              mountPath: /data/db
  volumeClaimTemplates:
    - metadata:
        name: data
      spec:
        accessModes: [ReadWriteOnce]
        resources:
          requests:
            storage: 20Gi
---
apiVersion: v1
kind: Service
metadata:
  name: kafka
  namespace: cyan-staging
spec:
  selector:
    app: kafka
  ports:
    - name: kafka
      port: 9092
      targetPort: 9092
---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: kafka
  namespace: cyan-staging
spec:
  serviceName: kafka
  replicas: 1
  selector:
    matchLabels:
      app: kafka
  template:
    metadata:
      labels:
        app: kafka
    spec:
      containers:
        - name: kafka
          image: apache/kafka:3.8.0
          ports:
            - containerPort: 9092
            - containerPort: 9093
          env:
            - name: KAFKA_NODE_ID
              value: "1"
            - name: KAFKA_PROCESS_ROLES
              value: broker,controller
            - name: KAFKA_LISTENERS
              value: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093
            - name: KAFKA_ADVERTISED_LISTENERS
              value: PLAINTEXT://kafka:9092
            - name: KAFKA_LISTENER_SECURITY_PROTOCOL_MAP
              value: PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT
            - name: KAFKA_CONTROLLER_LISTENER_NAMES
              value: CONTROLLER
            - name: KAFKA_CONTROLLER_QUORUM_VOTERS
              value: 1@localhost:9093
            - name: KAFKA_INTER_BROKER_LISTENER_NAME
              value: PLAINTEXT
            - name: KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR
              value: "1"
            - name: KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR
              value: "1"
            - name: KAFKA_TRANSACTION_STATE_LOG_MIN_ISR
              value: "1"
            - name: CLUSTER_ID
              value: MkU3OEVBNTcwNTJENDM2Qk
          volumeMounts:
            - name: data
              mountPath: /var/lib/kafka/data
  volumeClaimTemplates:
    - metadata:
        name: data
      spec:
        accessModes: [ReadWriteOnce]
        resources:
          requests:
            storage: 20Gi
---
apiVersion: v1
kind: Service
metadata:
  name: axon-server
  namespace: cyan-staging
spec:
  selector:
    app: axon-server
  ports:
    - name: grpc
      port: 8124
      targetPort: 8124
    - name: http
      port: 8024
      targetPort: 8024
---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: axon-server
  namespace: cyan-staging
spec:
  serviceName: axon-server
  replicas: 1
  selector:
    matchLabels:
      app: axon-server
  template:
    metadata:
      labels:
        app: axon-server
    spec:
      containers:
        - name: axon-server
          image: axoniq/axonserver:latest
          ports:
            - containerPort: 8124
            - containerPort: 8024
          volumeMounts:
            - name: data
              mountPath: /data
            - name: events
              mountPath: /eventdata
  volumeClaimTemplates:
    - metadata:
        name: data
      spec:
        accessModes: [ReadWriteOnce]
        resources:
          requests:
            storage: 10Gi
    - metadata:
        name: events
      spec:
        accessModes: [ReadWriteOnce]
        resources:
          requests:
            storage: 20Gi
```

Apply it:

```bash
kubectl apply -f /root/cyan-dev-infra.yaml
kubectl -n cyan-staging rollout status statefulset/postgres --timeout=180s
kubectl -n cyan-staging rollout status statefulset/mongo --timeout=180s
kubectl -n cyan-staging rollout status statefulset/kafka --timeout=240s
kubectl -n cyan-staging rollout status statefulset/axon-server --timeout=240s
```

Create the PostgreSQL databases required by the services:

```bash
cat > /root/cyan-create-databases.sql <<'SQL'
SELECT 'CREATE DATABASE tax_pay_sys' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'tax_pay_sys')\gexec;
SELECT 'CREATE DATABASE factor_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'factor_service')\gexec;
SELECT 'CREATE DATABASE buyer_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'buyer_service')\gexec;
SELECT 'CREATE DATABASE product_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'product_service')\gexec;
SELECT 'CREATE DATABASE client_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'client_service')\gexec;
SELECT 'CREATE DATABASE sso_auth_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'sso_auth_service')\gexec;
SELECT 'CREATE DATABASE sso_user_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'sso_user_service')\gexec;
SELECT 'CREATE DATABASE sso_otp_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'sso_otp_service')\gexec;
SELECT 'CREATE DATABASE sso_session_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'sso_session_service')\gexec;
SELECT 'CREATE DATABASE content_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'content_service')\gexec;
SELECT 'CREATE DATABASE catalog_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'catalog_service')\gexec;
SELECT 'CREATE DATABASE crm_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'crm_service')\gexec;
SELECT 'CREATE DATABASE commerce_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'commerce_service')\gexec;
SELECT 'CREATE DATABASE finance_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'finance_service')\gexec;
SELECT 'CREATE DATABASE inventory_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'inventory_service')\gexec;
SELECT 'CREATE DATABASE report_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'report_service')\gexec;
SELECT 'CREATE DATABASE processor_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'processor_service')\gexec;
SELECT 'CREATE DATABASE event_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'event_service')\gexec;
SELECT 'CREATE DATABASE crm_automation_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'crm_automation_service')\gexec;
SELECT 'CREATE DATABASE finance_automation_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'finance_automation_service')\gexec;
SELECT 'CREATE DATABASE inventory_automation_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'inventory_automation_service')\gexec;
SELECT 'CREATE DATABASE report_automation_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'report_automation_service')\gexec;
SELECT 'CREATE DATABASE payment_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'payment_service')\gexec;
SELECT 'CREATE DATABASE storefront_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'storefront_service')\gexec;
SELECT 'CREATE DATABASE media_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'media_service')\gexec;
SELECT 'CREATE DATABASE cart_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'cart_service')\gexec;
SELECT 'CREATE DATABASE checkout_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'checkout_service')\gexec;
SELECT 'CREATE DATABASE bpm_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'bpm_service')\gexec;
SELECT 'CREATE DATABASE notification_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'notification_service')\gexec;
SELECT 'CREATE DATABASE payment_orchestrator_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'payment_orchestrator_service')\gexec;
SELECT 'CREATE DATABASE pricing_promotion_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'pricing_promotion_service')\gexec;
SELECT 'CREATE DATABASE search_index_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'search_index_service')\gexec;
SQL

kubectl -n cyan-staging run pg-client --rm -i --restart=Never \
  --image=postgres:16 \
  --env PGPASSWORD=postgres \
  -- psql -h postgres -U postgres -d postgres < /root/cyan-create-databases.sql
```

MongoDB databases are created automatically on first write.

## 9. Clone the Repository

```bash
cd /root
git clone <repo-url> cyan_business
cd cyan_business
```

If the repository is private, use a deploy key or GitHub token first.

## 10. Create Platform Secrets

Start from the repo template:

```bash
cp deploy/kubernetes/secret.template.yaml /root/cyan-platform-secrets.yaml
```

Set at least these values:

```text
SPRING_DATASOURCE_USERNAME=<postgres-user>
SPRING_DATASOURCE_PASSWORD=<postgres-password>
KAFKA_BOOTSTRAP_SERVERS=<kafka-bootstrap>
JWKS_URI=https://api.example.com/.well-known/jwks.json
SSO_JWT_ISSUER=https://api.example.com
SSO_JWT_AUDIENCE=cyan-business
PUBLIC_PLATFORM_BASE_URL=https://api.example.com
AXON_SERVER_SERVERS=<axon-host>:8124
AXON_SERVER_TOKEN=<axon-token>
AUTOMATION_CALLBACK_SECRET=<long-random-secret>
CONTENT_SERVICE_INTERNAL_PASSWORD=<long-random-secret>
AI_ORCHESTRATOR_SERVICE_INTERNAL_PASSWORD=<long-random-secret>
BPM_SERVICE_INTERNAL_PASSWORD=<long-random-secret>
NOTIFICATION_SERVICE_INTERNAL_PASSWORD=<long-random-secret>
GAPGPT_API_KEY=<your-gapgpt-key>
```

Apply the secret:

```bash
kubectl -n cyan-staging apply -f /root/cyan-platform-secrets.yaml
```

## 11. Build Backend Images

The repo already has backend Dockerfiles and Kubernetes manifests. Build and push the backend service images through Docker.

Example for one service:

```bash
./gradlew :content-service:bootJar
docker build -t ghcr.io/<org>/cyan-business/content-service:<tag> content-service
docker push ghcr.io/<org>/cyan-business/content-service:<tag>
```

Repeat for every deployable backend service in `deploy/kubernetes/apps.yaml`, or use the existing CI pipeline later.

## 12. Build the Panel Image

The repo currently does not include a `panel-web/Dockerfile`. Create one on the server or commit it to the repo before production deployment.

Example Dockerfile:

```dockerfile
FROM node:20-alpine AS deps
WORKDIR /app
COPY package*.json ./
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

Build and push it:

```bash
cd panel-web
docker build \
  --build-arg NEXT_PUBLIC_PLATFORM_API_BASE_URL=https://api.example.com \
  -t ghcr.io/<org>/cyan-business/panel-web:<tag> .
docker push ghcr.io/<org>/cyan-business/panel-web:<tag>
cd ..
```

## 13. Deploy Backend Manifests

Apply the repo manifests first:

```bash
kubectl -n cyan-staging apply -k deploy/kubernetes
kubectl -n cyan-staging get pods
kubectl -n cyan-staging get svc
```

Point deployments to your real image registry:

```bash
kubectl -n cyan-staging set image deployment/content-service \
  content-service=ghcr.io/<org>/cyan-business/content-service:<tag>
```

Repeat for all backend deployments in `deploy/kubernetes/apps.yaml`.

## 14. Deploy panel-web in Kubernetes

Create `/root/panel-web.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: panel-web
  namespace: cyan-staging
  labels:
    app.kubernetes.io/name: panel-web
    app.kubernetes.io/part-of: cyan-business
spec:
  replicas: 1
  selector:
    matchLabels:
      app.kubernetes.io/name: panel-web
  template:
    metadata:
      labels:
        app.kubernetes.io/name: panel-web
        app.kubernetes.io/part-of: cyan-business
    spec:
      containers:
        - name: panel-web
          image: ghcr.io/<org>/cyan-business/panel-web:<tag>
          imagePullPolicy: IfNotPresent
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
              value: https://api.example.com
          readinessProbe:
            tcpSocket:
              port: http
            initialDelaySeconds: 20
            periodSeconds: 10
          livenessProbe:
            tcpSocket:
              port: http
            initialDelaySeconds: 60
            periodSeconds: 20
---
apiVersion: v1
kind: Service
metadata:
  name: panel-web
  namespace: cyan-staging
  labels:
    app.kubernetes.io/name: panel-web
    app.kubernetes.io/part-of: cyan-business
spec:
  selector:
    app.kubernetes.io/name: panel-web
  ports:
    - name: http
      port: 3000
      targetPort: http
```

Apply it:

```bash
kubectl apply -f /root/panel-web.yaml
kubectl -n cyan-staging rollout status deployment/panel-web --timeout=180s
```

## 15. Replace the HTTP-only Gateway with TLS and hostnames

The checked-in `deploy/kubernetes/envoy-gateway.yaml` is not enough for production because:

- it exposes HTTP only
- it has no TLS listeners
- it has no hostname split between panel and API
- it does not route `panel-web`

Create `/root/cyan-gateway-production.yaml`:

```yaml
apiVersion: gateway.networking.k8s.io/v1
kind: GatewayClass
metadata:
  name: cyan-envoy
spec:
  controllerName: gateway.envoyproxy.io/gatewayclass-controller
---
apiVersion: gateway.networking.k8s.io/v1
kind: Gateway
metadata:
  name: cyan-gateway
  namespace: cyan-staging
spec:
  gatewayClassName: cyan-envoy
  listeners:
    - name: http-panel
      hostname: example.com
      protocol: HTTP
      port: 80
      allowedRoutes:
        namespaces:
          from: Same
    - name: https-panel
      hostname: example.com
      protocol: HTTPS
      port: 443
      tls:
        mode: Terminate
        certificateRefs:
          - kind: Secret
            name: example-com-tls
      allowedRoutes:
        namespaces:
          from: Same
    - name: http-api
      hostname: api.example.com
      protocol: HTTP
      port: 80
      allowedRoutes:
        namespaces:
          from: Same
    - name: https-api
      hostname: api.example.com
      protocol: HTTPS
      port: 443
      tls:
        mode: Terminate
        certificateRefs:
          - kind: Secret
            name: api-example-com-tls
      allowedRoutes:
        namespaces:
          from: Same
---
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: example-com
  namespace: cyan-staging
spec:
  secretName: example-com-tls
  issuerRef:
    name: letsencrypt-production
    kind: ClusterIssuer
  dnsNames:
    - example.com
---
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: api-example-com
  namespace: cyan-staging
spec:
  secretName: api-example-com-tls
  issuerRef:
    name: letsencrypt-production
    kind: ClusterIssuer
  dnsNames:
    - api.example.com
---
apiVersion: gateway.networking.k8s.io/v1
kind: HTTPRoute
metadata:
  name: panel-web-route
  namespace: cyan-staging
spec:
  parentRefs:
    - name: cyan-gateway
      sectionName: http-panel
    - name: cyan-gateway
      sectionName: https-panel
  hostnames:
    - example.com
  rules:
    - matches:
        - path:
            type: PathPrefix
            value: /
      backendRefs:
        - name: panel-web
          port: 3000
```

For the API routes, reuse the backend route definitions already present in `deploy/kubernetes/envoy-gateway.yaml`, but add:

- `namespace: cyan-staging`
- `hostnames: [api.example.com]`
- `parentRefs` pointing at `http-api` and `https-api`

A route example:

```yaml
apiVersion: gateway.networking.k8s.io/v1
kind: HTTPRoute
metadata:
  name: cyan-platform-routes
  namespace: cyan-staging
spec:
  parentRefs:
    - name: cyan-gateway
      sectionName: http-api
    - name: cyan-gateway
      sectionName: https-api
  hostnames:
    - api.example.com
  rules:
    - matches:
        - path:
            type: PathPrefix
            value: /api/content-service/
      backendRefs:
        - name: content-service
          port: 9101
    - matches:
        - path:
            type: PathPrefix
            value: /endpoint/ai-orchestrator/
      backendRefs:
        - name: ai-orchestrator-service
          port: 9121
    - matches:
        - path:
            type: PathPrefix
            value: /api/bpm-service/
      backendRefs:
        - name: bpm-service
          port: 9119
```

Apply the production edge resources:

```bash
kubectl apply -f /root/cyan-gateway-production.yaml
```

Then apply the API route resources after adapting the checked-in route file:

```bash
cp deploy/kubernetes/envoy-gateway.yaml /root/cyan-api-routes.yaml
```

Edit that copy so it contains only the API `HTTPRoute` objects with:

- `namespace: cyan-staging`
- `hostnames: [api.example.com]`
- `parentRefs.sectionName: http-api` and `https-api`

Then apply it:

```bash
kubectl apply -f /root/cyan-api-routes.yaml
```

## 16. Point DNS to Envoy Gateway

Get the external address:

```bash
kubectl -n cyan-staging get gateway cyan-gateway -o jsonpath='{.status.addresses[0].value}{"\n"}'
```

Create DNS records:

```dns
example.com.      300 IN A     <gateway-ip>
api.example.com.  300 IN A     <gateway-ip>
```

If your provider uses CNAMEs and the gateway returns a hostname, use that instead.

Verify DNS:

```bash
dig +short example.com
dig +short api.example.com
```

## 17. Verify TLS and Routing

Certificates:

```bash
kubectl -n cyan-staging get certificates
kubectl -n cyan-staging describe certificate example-com
kubectl -n cyan-staging describe certificate api-example-com
```

Gateway and routes:

```bash
kubectl -n cyan-staging get gateway
kubectl -n cyan-staging get httproute
kubectl -n cyan-staging describe gateway cyan-gateway
```

Health checks:

```bash
curl -I https://example.com
curl -sS https://api.example.com/.well-known/jwks.json | head
curl -sS https://api.example.com/public/storefront/render?path=/ | head
curl -sS https://api.example.com/public/search-index/suggest?q=test
curl -sS https://api.example.com/public/bot-adapter/health
```

Panel check:

```bash
curl -I https://example.com
```

The panel should load from `example.com`, and its browser calls should target `https://api.example.com`.

## 18. Production Notes

1. The repo's current backend manifests are usable as a base, but they are not a complete production edge on their own.
2. `panel-web` is not yet represented in `deploy/kubernetes/`.
3. The checked-in Envoy manifest mirrors backend paths, but it is HTTP-only and hostname-agnostic.
4. Selected `/internal/**` routes are still publicly routed in the current API surface. For production, restrict them with network policy, auth policy, or private ingress.
5. `api-gateway` and `discovery-server` are local-development components. On Kubernetes, runtime discovery is done with cluster DNS plus Gateway API routing.

## 19. CI/CD Direction

Once manual deployment works, automate this order:

1. build Spring Boot jars
2. build backend Docker images
3. build `panel-web` image with `NEXT_PUBLIC_PLATFORM_API_BASE_URL=https://api.example.com`
4. push images to the registry
5. apply namespace-scoped manifests
6. update deployment images
7. wait for rollout
8. run smoke checks against `https://example.com` and `https://api.example.com`

## 20. Fast Troubleshooting

Check pods:

```bash
kubectl -n cyan-staging get pods
kubectl -n cyan-staging describe pod <pod-name>
kubectl -n cyan-staging logs deployment/<deployment-name> --tail=200
```

Check Envoy Gateway:

```bash
kubectl -n envoy-gateway-system get pods
kubectl -n envoy-gateway-system logs deployment/envoy-gateway --tail=200
```

Check certificate issues:

```bash
kubectl -n cyan-staging describe challenge
kubectl -n cyan-staging describe order
kubectl -n cert-manager logs deployment/cert-manager --tail=200
```

Check service reachability from inside the cluster:

```bash
kubectl -n cyan-staging run curlbox --rm -it --restart=Never \
  --image=curlimages/curl -- \
  curl -sS http://content-service:9101/actuator/health
```

Check panel reachability inside the cluster:

```bash
kubectl -n cyan-staging run curlbox --rm -it --restart=Never \
  --image=curlimages/curl -- \
  curl -I http://panel-web:3000
```

If you want this fully codified in-repo next, the right follow-up is to add:

- `panel-web/Dockerfile`
- `deploy/kubernetes/panel-web.yaml`
- a production `envoy-gateway` manifest with `example.com` and `api.example.com`
- CI wiring that builds and ships the panel image alongside the backend images

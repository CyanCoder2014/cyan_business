# Kubernetes Bootstrap Runbook

This runbook starts from a fresh Linux server and ends with the Cyan Business services running behind Envoy Gateway. It complements `SERVER_DEPLOYMENT_GUIDE.md`, which assumes a working Kubernetes cluster already exists.

Use this order:

1. Install operating system tools.
2. Install and verify k3s Kubernetes.
3. Install Envoy Gateway.
4. Create the namespace and platform secrets.
5. Provide PostgreSQL, MongoDB, Kafka, and Axon Server.
6. Clone the source.
7. Build and push service images.
8. Deploy Kubernetes manifests.
9. Verify the rollout.
10. Configure CI/CD.

Replace every value wrapped in `<...>`.

## 1. Server Assumptions

Recommended minimum for a single-server staging environment:

```text
CPU: 8 cores
RAM: 24 GB or more
Disk: 150 GB or more
OS: RHEL/Rocky/Alma/CentOS 8+ or Ubuntu 22.04+
Ports: 80, 443, and 6443 reachable as needed
```

For production, use managed or separately operated PostgreSQL, MongoDB, Kafka, and Axon Server instead of running all dependencies on the same node.

## 2. Install Base Packages

On RHEL/Rocky/Alma/CentOS:

```bash
dnf update -y
dnf install -y curl ca-certificates git jq tar gzip unzip openssl bind-utils
```

If `dnf` fails with `rpmdb open failed`, `disk I/O error`, or `cannot open Packages database in /var/lib/rpm`, stop and check the host before retrying package installs:

```bash
df -h
df -ih
mount | grep ' / '
mount | grep ' /var '
dmesg -T | tail -100
ls -ld /var/lib/rpm
```

If `/` or `/var` is full, clear space first. If the mount is read-only or `dmesg` shows real storage I/O errors, fix the disk/filesystem problem before rebuilding RPM metadata.

After the disk is writable and has free space, back up and rebuild the RPM database:

```bash
mkdir -p /root/rpmdb-backup
cp -a /var/lib/rpm "/root/rpmdb-backup/rpm-$(date +%F-%H%M%S)"
rpm --rebuilddb
rpm -qa >/dev/null
dnf clean all
dnf makecache
dnf update -y
```

On Ubuntu/Debian:

```bash
apt-get update
apt-get install -y curl ca-certificates git jq tar gzip unzip openssl dnsutils
```

If a firewall is enabled, open HTTP/HTTPS and the Kubernetes API port:

```bash
firewall-cmd --add-port=80/tcp --permanent || true
firewall-cmd --add-port=443/tcp --permanent || true
firewall-cmd --add-port=6443/tcp --permanent || true
firewall-cmd --reload || true
```

For a multi-node k3s cluster, also open the required k3s node-to-node ports. For single-node staging, the commands above are usually enough.

## 3. Install k3s Kubernetes

Do not use a silent pipe first. Download the installer and confirm it exists. This catches the failure where `curl -sfL https://get.k3s.io | sh -` exits without creating `/etc/rancher/k3s/k3s.yaml`.

```bash
curl -fL https://get.k3s.io -o /tmp/install-k3s.sh
test -s /tmp/install-k3s.sh
chmod +x /tmp/install-k3s.sh
```

Install a single-node server. Traefik is disabled because this project uses Envoy Gateway.

```bash
INSTALL_K3S_EXEC="server --disable traefik --write-kubeconfig-mode=600" /tmp/install-k3s.sh
```

Check the service:

```bash
systemctl status k3s --no-pager
journalctl -u k3s -n 100 --no-pager
```

Configure root's `kubectl`:

```bash
mkdir -p /root/.kube
cp -f /etc/rancher/k3s/k3s.yaml /root/.kube/config
chmod 600 /root/.kube/config
unset KUBECONFIG
```

Verify the cluster. Do not continue until these commands work:

```bash
kubectl config current-context
kubectl config get-contexts
kubectl get --raw=/version
kubectl get nodes -o wide
kubectl get pods -A
```

Expected result:

```text
kubectl config current-context -> default
kubectl get nodes -> one Ready node
```

If `/etc/rancher/k3s/k3s.yaml` does not exist, k3s did not install or did not start. Run:

```bash
curl -v https://get.k3s.io/
systemctl status k3s --no-pager
journalctl -u k3s -xe --no-pager
ls -l /etc/rancher/k3s /var/lib/rancher/k3s
```

Common causes are blocked outbound network access, DNS failure, package-manager failure, a previous broken Kubernetes install, or a systemd service start failure.

## 4. Install Docker For Building Images

k3s runs containers through containerd, but Docker is convenient for building and pushing the service images.

On RHEL/Rocky/Alma/CentOS:

```bash
dnf install -y dnf-plugins-core
dnf config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
dnf install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin
systemctl enable --now docker
docker version
```

On Ubuntu/Debian, use Docker's official repository or your OS package repository, then verify:

```bash
docker version
```

## 5. Install Envoy Gateway

Install Envoy Gateway before applying this repository's Gateway and HTTPRoute resources:

```bash
kubectl apply --server-side -f https://github.com/envoyproxy/gateway/releases/download/v1.8.0/install.yaml
kubectl wait --timeout=5m -n envoy-gateway-system deployment/envoy-gateway --for=condition=Available
kubectl get crd gateways.gateway.networking.k8s.io httproutes.gateway.networking.k8s.io
```

The repository uses:

```text
GatewayClass: cyan-envoy
controllerName: gateway.envoyproxy.io/gatewayclass-controller
```

## 6. Create Namespace

Use `cyan-staging` first. Create production only after staging works.

```bash
kubectl create namespace cyan-staging --dry-run=client -o yaml | kubectl apply -f -
```

## 7. Provide Infrastructure Dependencies

The server profile expects these default Kubernetes DNS names unless you override them in secrets:

```text
postgres:5432
mongo:27017
kafka:9092
axon-server:8124
```

### Option A: Production

Use external or managed PostgreSQL, MongoDB, Kafka, and Axon Server. Put the real connection values in `cyan-platform-secrets`.

Examples:

```text
SPRING_DATASOURCE_USERNAME=<postgres-user>
SPRING_DATASOURCE_PASSWORD=<postgres-password>
KAFKA_BOOTSTRAP_SERVERS=<kafka-bootstrap-hosts>
AXON_SERVER_SERVERS=<axon-host>:8124
AXON_SERVER_TOKEN=<axon-token>
CONTENT_SERVICE_DATASOURCE_URL=jdbc:postgresql://<postgres-host>:5432/content_service
CONTENT_SERVICE_MONGODB_URI=mongodb://<mongo-host>:27017/content_service
```

Repeat service-specific values only where a service needs a different endpoint or credential.

### Option B: Single-Server Staging

For staging, you can run simple in-cluster dependencies using service names that match the application defaults. Save the following as `/root/cyan-dev-infra.yaml`.

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
        accessModes:
          - ReadWriteOnce
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
        accessModes:
          - ReadWriteOnce
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
        accessModes:
          - ReadWriteOnce
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
        accessModes:
          - ReadWriteOnce
        resources:
          requests:
            storage: 10Gi
    - metadata:
        name: events
      spec:
        accessModes:
          - ReadWriteOnce
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

Create the PostgreSQL databases:

```bash
cat >/root/cyan-create-databases.sql <<'SQL'
SELECT 'CREATE DATABASE tax_pay_sys' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'tax_pay_sys')\gexec
SELECT 'CREATE DATABASE factor_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'factor_service')\gexec
SELECT 'CREATE DATABASE buyer_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'buyer_service')\gexec
SELECT 'CREATE DATABASE product_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'product_service')\gexec
SELECT 'CREATE DATABASE client_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'client_service')\gexec
SELECT 'CREATE DATABASE sso_auth_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'sso_auth_service')\gexec
SELECT 'CREATE DATABASE sso_user_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'sso_user_service')\gexec
SELECT 'CREATE DATABASE sso_otp_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'sso_otp_service')\gexec
SELECT 'CREATE DATABASE sso_session_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'sso_session_service')\gexec
SELECT 'CREATE DATABASE content_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'content_service')\gexec
SELECT 'CREATE DATABASE catalog_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'catalog_service')\gexec
SELECT 'CREATE DATABASE crm_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'crm_service')\gexec
SELECT 'CREATE DATABASE commerce_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'commerce_service')\gexec
SELECT 'CREATE DATABASE finance_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'finance_service')\gexec
SELECT 'CREATE DATABASE inventory_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'inventory_service')\gexec
SELECT 'CREATE DATABASE report_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'report_service')\gexec
SELECT 'CREATE DATABASE processor_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'processor_service')\gexec
SELECT 'CREATE DATABASE event_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'event_service')\gexec
SELECT 'CREATE DATABASE crm_automation_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'crm_automation_service')\gexec
SELECT 'CREATE DATABASE finance_automation_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'finance_automation_service')\gexec
SELECT 'CREATE DATABASE inventory_automation_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'inventory_automation_service')\gexec
SELECT 'CREATE DATABASE report_automation_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'report_automation_service')\gexec
SELECT 'CREATE DATABASE payment_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'payment_service')\gexec
SELECT 'CREATE DATABASE storefront_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'storefront_service')\gexec
SELECT 'CREATE DATABASE media_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'media_service')\gexec
SELECT 'CREATE DATABASE cart_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'cart_service')\gexec
SELECT 'CREATE DATABASE checkout_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'checkout_service')\gexec
SELECT 'CREATE DATABASE bpm_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'bpm_service')\gexec
SELECT 'CREATE DATABASE notification_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'notification_service')\gexec
SELECT 'CREATE DATABASE payment_orchestrator_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'payment_orchestrator_service')\gexec
SELECT 'CREATE DATABASE pricing_promotion_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'pricing_promotion_service')\gexec
SELECT 'CREATE DATABASE search_index_service' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'search_index_service')\gexec
SQL

kubectl -n cyan-staging run pg-client --rm -i --restart=Never \
  --image=postgres:16 \
  --env PGPASSWORD=postgres \
  -- psql -h postgres -U postgres -d postgres < /root/cyan-create-databases.sql
```

MongoDB databases are created automatically when services first write records.

## 8. Create Platform Secrets

Start from the repository template after the source has been cloned, or create it directly on the server. For single-server staging with the in-cluster dependencies above:

```bash
cat >/root/cyan-platform-secret.yaml <<'YAML'
apiVersion: v1
kind: Secret
metadata:
  name: cyan-platform-secrets
  namespace: cyan-staging
type: Opaque
stringData:
  SPRING_DATASOURCE_USERNAME: postgres
  SPRING_DATASOURCE_PASSWORD: postgres
  KAFKA_BOOTSTRAP_SERVERS: kafka:9092
  JWKS_URI: http://sso-auth-service:9001/.well-known/jwks.json
  SSO_JWT_ISSUER: http://<server-public-ip-or-domain>
  SSO_JWT_AUDIENCE: cyan-business
  PUBLIC_PLATFORM_BASE_URL: http://<server-public-ip-or-domain>
  AXON_SERVER_SERVERS: axon-server:8124
  AXON_SERVER_TOKEN: ""
  AUTOMATION_CALLBACK_SECRET: <change-me-long-random-value>
  OPENAI_API_KEY: ""
  OPENROUTER_API_KEY: ""
  GAPGPT_API_KEY: ""
  CONTENT_SERVICE_INTERNAL_PASSWORD: <change-me-long-random-value>
  AI_ORCHESTRATOR_SERVICE_INTERNAL_PASSWORD: <change-me-long-random-value>
  BPM_SERVICE_INTERNAL_PASSWORD: <change-me-long-random-value>
  NOTIFICATION_SERVICE_INTERNAL_PASSWORD: <change-me-long-random-value>
YAML

kubectl apply -f /root/cyan-platform-secret.yaml
kubectl -n cyan-staging get secret cyan-platform-secrets
```

For production, do not use `postgres/postgres`, empty Axon tokens, or placeholder internal passwords.

## 9. Clone Source

```bash
mkdir -p /opt/cyan
cd /opt/cyan
git clone <git-repository-url> cyan_business
cd cyan_business
git checkout develop
```

Confirm deployment assets exist:

```bash
test -f SERVER_DEPLOYMENT_GUIDE.md
test -f deploy/kubernetes/kustomization.yaml
test -f deploy/kubernetes/apps.yaml
test -f deploy/kubernetes/envoy-gateway.yaml
```

## 10. Build And Push Images Manually

Install Java 25 if needed.

On RHEL/Rocky/Alma/CentOS:

```bash
dnf install -y java-25-openjdk java-25-openjdk-devel
java -version
```

On Ubuntu/Debian:

```bash
apt-get install -y openjdk-25-jdk
java -version
```

Set your registry and image tag:

```bash
export REGISTRY_HOST=<registry-host>/<registry-namespace>/cyan-business
export IMAGE_TAG=develop
docker login <registry-host>
```

For GitHub Container Registry, an example is:

```bash
export REGISTRY_HOST=ghcr.io/<github-owner>/cyan-business
docker login ghcr.io
```

Build every service jar:

```bash
export SERVICES="tax-pay-sys factor-service buyer-service product-service client-service sso-auth-service sso-user-service sso-captcha-service sso-otp-service sso-session-service sso-fido-service content-service catalog-service crm-service commerce-service finance-service inventory-service report-service processor-service event-service crm-automation-service finance-automation-service inventory-automation-service report-automation-service payment-service storefront-service media-service cart-service checkout-service bpm-service automation-orchestrator-service ai-orchestrator-service notification-service payment-orchestrator-service pricing-promotion-service search-index-service bot-adapter-service"

./gradlew $(for service in $SERVICES; do printf ":%s:bootJar " "$service"; done)
```

Build and push images:

```bash
for service in $SERVICES; do
  docker build -t "$REGISTRY_HOST/$service:$IMAGE_TAG" "$service"
  docker push "$REGISTRY_HOST/$service:$IMAGE_TAG"
done
```

If the registry is private, create an image pull secret:

```bash
kubectl -n cyan-staging create secret docker-registry cyan-registry \
  --docker-server=<registry-host> \
  --docker-username=<registry-username> \
  --docker-password=<registry-token-or-password>

kubectl -n cyan-staging patch serviceaccount default \
  -p '{"imagePullSecrets":[{"name":"cyan-registry"}]}'
```

## 11. Deploy The Project

Apply the app and Envoy route manifests:

```bash
kubectl -n cyan-staging apply -k deploy/kubernetes
```

Set images to your real registry. The committed manifests use placeholder images, so this step is required unless you edit the manifests first.

```bash
for service in $SERVICES; do
  kubectl -n cyan-staging set image "deployment/$service" "$service=$REGISTRY_HOST/$service:$IMAGE_TAG"
done
```

Watch startup:

```bash
kubectl -n cyan-staging get pods -w
```

Roll out in a smaller order if troubleshooting:

```bash
kubectl -n cyan-staging rollout status deployment/sso-auth-service --timeout=180s
kubectl -n cyan-staging rollout status deployment/event-service --timeout=180s
kubectl -n cyan-staging rollout status deployment/content-service --timeout=180s
```

To check all rollouts:

```bash
for service in $SERVICES; do
  kubectl -n cyan-staging rollout status "deployment/$service" --timeout=240s
done
```

## 12. Get The Public Gateway Address

Check Gateway API resources:

```bash
kubectl -n cyan-staging get gateway,httproute
kubectl -n cyan-staging describe gateway cyan-gateway
kubectl -n cyan-staging describe httproute cyan-platform-routes
```

Get the address:

```bash
kubectl -n cyan-staging get gateway cyan-gateway -o jsonpath='{.status.addresses[0].value}'
echo
```

On single-node k3s, the address may be the node IP. If the Gateway address is empty, inspect Envoy Gateway services:

```bash
kubectl -n envoy-gateway-system get pods,svc
kubectl get svc -A | grep -i envoy
```

Point your DNS name, for example `api.example.com`, at the Gateway address. If you do not have DNS yet, test with the server IP over HTTP.

## 13. Smoke Tests

Run these from your laptop or from the server:

```bash
curl -i http://<gateway-address>/.well-known/jwks.json
curl -i http://<gateway-address>/public/storefront/render?path=/
curl -i http://<gateway-address>/public/search-index/suggest?q=test
curl -i http://<gateway-address>/public/bot-adapter/health
```

If a service fails:

```bash
kubectl -n cyan-staging get pods
kubectl -n cyan-staging describe pod <pod-name>
kubectl -n cyan-staging logs deployment/<service-name> --tail=200
```

Common failures:

```text
ImagePullBackOff: wrong image name, missing registry login, or private registry secret missing.
CrashLoopBackOff with database errors: PostgreSQL database missing or secret has wrong credentials.
Kafka connection refused: kafka pod not ready or advertised listener is wrong.
401/403: token issuer, JWKS URI, or internal service credentials are wrong.
No Gateway address: Envoy Gateway or LoadBalancer integration is not ready.
```

## 14. CI/CD With GitHub Actions

The repository already has `.github/workflows/deploy.yml`.

Required setup:

1. Push this repository to GitHub.
2. Enable GitHub Actions.
3. Ensure Actions has package write permission for GHCR.
4. Create namespaces and `cyan-platform-secrets` manually first.
5. Install Envoy Gateway manually first.
6. Add the GitHub secret `KUBE_CONFIG`.

Create the kubeconfig secret on the server:

```bash
base64 < /root/.kube/config | tr -d '\n'
```

Save that value in GitHub:

```text
Repository Settings -> Secrets and variables -> Actions -> New repository secret
Name: KUBE_CONFIG
Value: <base64-output>
```

The workflow deploys:

```text
develop -> cyan-staging
main    -> cyan-production
```

For production, create the production namespace and secret before merging to `main`:

```bash
kubectl create namespace cyan-production --dry-run=client -o yaml | kubectl apply -f -
kubectl -n cyan-production apply -f /root/cyan-production-platform-secret.yaml
```

After a push, verify:

```bash
kubectl -n cyan-staging get pods
kubectl -n cyan-staging get events --sort-by=.lastTimestamp | tail -40
```

## 15. CI/CD With Jenkins

The repository also has `Jenkinsfile`.

Jenkins requirements:

```text
Java 25
Docker CLI with registry access
Git
kubectl
Network access to the Kubernetes API server
```

Create Jenkins credentials:

```text
cyan-container-registry: username/password for your image registry
cyan-kubeconfig: file credential containing /root/.kube/config
```

Edit `Jenkinsfile` before first production use:

```groovy
REGISTRY_HOST = 'ghcr.io/your-org/cyan-business'
```

Replace it with your real registry path, for example:

```groovy
REGISTRY_HOST = 'ghcr.io/<github-owner>/cyan-business'
```

Use a multibranch pipeline:

```text
develop -> cyan-staging
main    -> cyan-production
```

As with GitHub Actions, Jenkins does not create production secrets. Create `cyan-platform-secrets` manually in each namespace.

## 16. Production Hardening

Before public production traffic:

```text
Add HTTPS to cyan-gateway.
Use real DNS for PUBLIC_PLATFORM_BASE_URL and SSO_JWT_ISSUER.
Replace all change-me values.
Use per-service internal API credentials.
Move PostgreSQL, MongoDB, Kafka, and Axon Server to managed or HA deployments.
Add backup and restore jobs for PostgreSQL and MongoDB.
Add resource requests and limits to every Deployment.
Add NetworkPolicy rules around internal routes and infrastructure services.
Restrict access to /internal/** routes.
Enable centralized logs and metrics.
Scan images in CI.
Protect main branch deployments with approvals.
```

## 17. Recovery Commands

If `kubectl` has no context:

```bash
kubectl config get-contexts
ls -l /etc/rancher/k3s/k3s.yaml /root/.kube/config
cp -f /etc/rancher/k3s/k3s.yaml /root/.kube/config
chmod 600 /root/.kube/config
unset KUBECONFIG
kubectl get nodes -o wide
```

If k3s is broken:

```bash
systemctl status k3s --no-pager
journalctl -u k3s -xe --no-pager
```

If Envoy Gateway is broken:

```bash
kubectl -n envoy-gateway-system get pods
kubectl -n envoy-gateway-system logs deployment/envoy-gateway --tail=200
kubectl -n cyan-staging describe gateway cyan-gateway
```

If pods cannot pull images:

```bash
kubectl -n cyan-staging describe pod <pod-name>
kubectl -n cyan-staging get secret cyan-registry
kubectl -n cyan-staging get serviceaccount default -o yaml
```

If pods cannot reach dependencies:

```bash
kubectl -n cyan-staging get svc postgres mongo kafka axon-server
kubectl -n cyan-staging get pods -l app=postgres
kubectl -n cyan-staging get pods -l app=mongo
kubectl -n cyan-staging get pods -l app=kafka
kubectl -n cyan-staging get pods -l app=axon-server
kubectl -n cyan-staging logs statefulset/kafka --tail=200
```

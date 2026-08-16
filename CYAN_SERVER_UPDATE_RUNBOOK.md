# Cyan Server Update Runbook

Simple copy/paste commands for updating Cyan backend services and `panel-web` after `git pull`.

> Server repo: `/opt/cyan/cyan_business`  
> Kubernetes namespace: `cyan-staging`  
> Images are built locally, imported into K3s containerd, and deployed with a unique Git-based tag.

---

## 1. Pull latest code

```bash
cd /opt/cyan/cyan_business

git pull
git status
git log -1 --oneline
```

Create one release tag for this deployment:

```bash
TAG="$(git rev-parse --short HEAD)-$(date +%Y%m%d%H%M%S)"
echo "$TAG"
```

---

# 2. Backend services

## Services to deploy

```bash
SERVICES=(
  sso-auth-service
  sso-user-service
  sso-captcha-service
  sso-otp-service
  sso-session-service

  tenant-service
  billing-service
  processor-service
  media-service

  bpm-service
  automation-orchestrator-service
  ai-orchestrator-service

  storefront-service
  report-service

  content-service
  catalog-service
  crm-service
  notification-service

  event-service
  bot-adapter-service
  search-index-service

  batch-worker-service
  api-docs-service
)
```

Check that all source directories exist:

```bash
for SERVICE in "${SERVICES[@]}"; do
  [ -d "$SERVICE" ] && echo "OK      $SERVICE" || echo "MISSING $SERVICE"
done
```

---

## 3. Build all backend JARs

```bash
for SERVICE in "${SERVICES[@]}"; do
  echo
  echo "===== BUILDING $SERVICE ====="

  ./gradlew     ":${SERVICE}:clean"     ":${SERVICE}:bootJar" || exit 1
done
```

---

## 4. Build all Docker images

```bash
for SERVICE in "${SERVICES[@]}"; do
  IMAGE="docker.io/cyanlocal/${SERVICE}:${TAG}"

  echo
  echo "===== DOCKER BUILD $IMAGE ====="

  docker build     -t "$IMAGE"     "./${SERVICE}" || exit 1
done
```

---

## 5. Import all images into K3s

Docker and K3s use separate image stores.

```bash
for SERVICE in "${SERVICES[@]}"; do
  IMAGE="docker.io/cyanlocal/${SERVICE}:${TAG}"

  echo "Importing $IMAGE"

  docker save "$IMAGE" |     k3s ctr -n k8s.io images import - || exit 1
done
```

Verify:

```bash
for SERVICE in "${SERVICES[@]}"; do
  IMAGE="docker.io/cyanlocal/${SERVICE}:${TAG}"

  if k3s ctr -n k8s.io images list -q | grep -Fxq "$IMAGE"; then
    echo "OK      $SERVICE"
  else
    echo "MISSING $SERVICE"
  fi
done
```

---

## 6. Create operational services if missing

`batch-worker-service` and `api-docs-service` are defined in:

```text
deploy/kubernetes/operational-services.yaml
```

Apply the manifest:

```bash
kubectl apply -f deploy/kubernetes/operational-services.yaml
```

For this server, use local K3s images:

```bash
for SERVICE in batch-worker-service api-docs-service; do
  kubectl patch deployment "$SERVICE"     -n cyan-staging     --type=json     -p='[{"op":"replace","path":"/spec/template/spec/containers/0/imagePullPolicy","value":"Never"}]'
done
```

Use one batch worker on staging:

```bash
kubectl scale deployment batch-worker-service   -n cyan-staging   --replicas=1
```

---

## 7. Update Kubernetes backend images

```bash
for SERVICE in "${SERVICES[@]}"; do
  IMAGE="docker.io/cyanlocal/${SERVICE}:${TAG}"

  if kubectl get deployment "$SERVICE"       -n cyan-staging >/dev/null 2>&1; then

    echo "Deploying $SERVICE -> $IMAGE"

    kubectl set image       deployment/"$SERVICE"       -n cyan-staging       "$SERVICE=$IMAGE"
  else
    echo "NO DEPLOYMENT: $SERVICE"
  fi
done
```

---

## 8. Start backend services

```bash
for SERVICE in "${SERVICES[@]}"; do
  if kubectl get deployment "$SERVICE"       -n cyan-staging >/dev/null 2>&1; then

    kubectl scale deployment "$SERVICE"       -n cyan-staging       --replicas=1
  fi
done
```

Keep `batch-worker-service` at one replica:

```bash
kubectl scale deployment batch-worker-service   -n cyan-staging   --replicas=1
```

---

# 9. Update panel-web

Build the panel with the public API URL.

```bash
PANEL_IMAGE="docker.io/cyanlocal/panel-web:${TAG}"

docker build   --build-arg NEXT_PUBLIC_PLATFORM_API_BASE_URL=https://api.cyancoder.com   -t "$PANEL_IMAGE"   ./panel-web
```

Import into K3s:

```bash
docker save "$PANEL_IMAGE" |   k3s ctr -n k8s.io images import -
```

Verify:

```bash
k3s ctr -n k8s.io images list -q | grep -Fx "$PANEL_IMAGE"
```

Deploy:

```bash
kubectl set image deployment/panel-web   -n cyan-staging   "panel-web=$PANEL_IMAGE"
```

Wait for rollout:

```bash
kubectl rollout status deployment/panel-web   -n cyan-staging   --timeout=180s
```

---

# 10. Verify deployment

Show all pods:

```bash
kubectl get pods -n cyan-staging
```

Show only unhealthy pods:

```bash
kubectl get pods -n cyan-staging --no-headers | awk '$2 !~ /^1\/1$/ || $3 != "Running"'
```

Show deployed images:

```bash
kubectl get deployments -n cyan-staging   -o custom-columns='NAME:.metadata.name,IMAGE:.spec.template.spec.containers[0].image'
```

Check resources:

```bash
kubectl top pods -n cyan-staging --sort-by=memory
kubectl top node
free -h
```

---

# 11. Logs for failed services

Current logs:

```bash
kubectl logs deployment/SERVICE_NAME   -n cyan-staging   --tail=150
```

Previous crashed container:

```bash
kubectl logs deployment/SERVICE_NAME   -n cyan-staging   --previous   --tail=150
```

Example:

```bash
kubectl logs deployment/storefront-service   -n cyan-staging   --previous   --tail=150
```

---

# 12. Rollback one service

Check rollout history:

```bash
SERVICE=media-service

kubectl rollout history deployment/"$SERVICE"   -n cyan-staging
```

Rollback:

```bash
kubectl rollout undo deployment/"$SERVICE"   -n cyan-staging
```

Verify:

```bash
kubectl rollout status deployment/"$SERVICE"   -n cyan-staging   --timeout=180s
```

---

# Update only one backend service

Use this when only one microservice changed.

Change the first line:

```bash
SERVICE=media-service

cd /opt/cyan/cyan_business

git pull

TAG="$(git rev-parse --short HEAD)-$(date +%Y%m%d%H%M%S)"
IMAGE="docker.io/cyanlocal/${SERVICE}:${TAG}"

./gradlew   ":${SERVICE}:clean"   ":${SERVICE}:bootJar"

docker build   -t "$IMAGE"   "./${SERVICE}"

docker save "$IMAGE" |   k3s ctr -n k8s.io images import -

kubectl set image deployment/"$SERVICE"   -n cyan-staging   "$SERVICE=$IMAGE"

kubectl scale deployment "$SERVICE"   -n cyan-staging   --replicas=1

kubectl rollout status deployment/"$SERVICE"   -n cyan-staging   --timeout=180s

kubectl logs deployment/"$SERVICE"   -n cyan-staging   --tail=100
```

---

# Update only panel-web

```bash
cd /opt/cyan/cyan_business

git pull

TAG="$(git rev-parse --short HEAD)-$(date +%Y%m%d%H%M%S)"
PANEL_IMAGE="docker.io/cyanlocal/panel-web:${TAG}"

docker build   --build-arg NEXT_PUBLIC_PLATFORM_API_BASE_URL=https://api.cyancoder.com   -t "$PANEL_IMAGE"   ./panel-web

docker save "$PANEL_IMAGE" |   k3s ctr -n k8s.io images import -

kubectl set image deployment/panel-web   -n cyan-staging   "panel-web=$PANEL_IMAGE"

kubectl rollout status deployment/panel-web   -n cyan-staging   --timeout=180s

kubectl logs deployment/panel-web   -n cyan-staging   --tail=100
```

---

# Important rules

- Always use a **new image tag** for every deployment.
- Always run Gradle `clean` before `bootJar`.
- Docker images must be imported into **K3s containerd**.
- Do not reuse old image tags.
- Do not run `docker system prune -a` on this server without checking K3s/Docker image requirements.
- `NEXT_PUBLIC_PLATFORM_API_BASE_URL` is a panel **build-time** value, so rebuild `panel-web` when it changes.
- PostgreSQL and MongoDB remain internal Kubernetes services; do not expose them publicly.

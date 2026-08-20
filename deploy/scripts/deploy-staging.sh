#!/usr/bin/env bash
set -Eeuo pipefail

cd "$(git rev-parse --show-toplevel)"
source deploy/kubernetes/common-client-profile.sh

NAMESPACE="cyan-staging"
TAG="$(git rev-parse --short HEAD)-$(date +%Y%m%d%H%M%S)"

echo "Deploying Cyan version: ${TAG}"

# Mirrors the Deployment objects in deploy/kubernetes/*.yaml. Adding a gradle
# module also means adding it here and to the manifests, or this script will
# fail on a missing deployment/<service>.
BACKEND_SERVICES=(
  tax-pay-sys
  factor-service
  buyer-service
  product-service
  client-service

  "${COMMON_IDENTITY_SERVICES[@]}"
  sso-fido-service

  content-service
  catalog-service
  crm-service
  commerce-service
  finance-service
  inventory-service
  report-service
  processor-service
  event-service

  crm-automation-service
  finance-automation-service
  inventory-automation-service
  report-automation-service

  payment-service
  storefront-service
  media-service
  cart-service
  checkout-service
  payment-orchestrator-service
  automation-orchestrator-service
  pricing-promotion-service
  search-index-service
  notification-service
  bpm-service
  ai-orchestrator-service
  bot-adapter-service
  batch-worker-service
  api-docs-service

  tenant-service
  billing-service
)

for SERVICE in "${BACKEND_SERVICES[@]}"; do
  echo
  echo "======================================"
  echo "BUILD ${SERVICE}"
  echo "======================================"

  ./gradlew ":${SERVICE}:clean" ":${SERVICE}:bootJar"

  IMAGE="localhost/cyan/${SERVICE}:${TAG}"

  docker build -t "${IMAGE}" "./${SERVICE}"

  docker save "${IMAGE}" | sudo k3s ctr -n k8s.io images import -

  sudo k3s kubectl set image \
    "deployment/${SERVICE}" \
    -n "${NAMESPACE}" \
    "${SERVICE}=${IMAGE}"

  sudo k3s kubectl rollout status \
    "deployment/${SERVICE}" \
    -n "${NAMESPACE}" \
    --timeout=180s
done

echo
echo "======================================"
echo "BUILD panel-web"
echo "======================================"

PANEL_IMAGE="localhost/cyan/panel-web:${TAG}"

docker build \
  --build-arg NEXT_PUBLIC_PLATFORM_API_BASE_URL=https://api.cyancoder.com \
  -t "${PANEL_IMAGE}" \
  ./panel-web

docker save "${PANEL_IMAGE}" | sudo k3s ctr -n k8s.io images import -

sudo k3s kubectl set image \
  deployment/panel-web \
  -n "${NAMESPACE}" \
  "panel-web=${PANEL_IMAGE}"

sudo k3s kubectl rollout status \
  deployment/panel-web \
  -n "${NAMESPACE}" \
  --timeout=180s

echo
echo "Cyan deployment finished: ${TAG}"

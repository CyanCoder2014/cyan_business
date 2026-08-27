#!/usr/bin/env bash
set -Eeuo pipefail

cd "$(git rev-parse --show-toplevel)"
source deploy/kubernetes/common-client-profile.sh

NAMESPACE="cyan-staging"
TAG="$(git rev-parse --short HEAD)-$(date +%Y%m%d%H%M%S)"
APPLY_MANIFESTS="${APPLY_MANIFESTS:-false}"

REQUESTED_SERVICES=("$@")

if (( ${#REQUESTED_SERVICES[@]} == 0 )); then
  echo "No services requested, nothing to deploy."
  exit 0
fi

is_known_backend_service() {
  local candidate="$1"
  local service
  for service in "${ALL_BACKEND_SERVICES[@]}"; do
    if [[ "$service" == "$candidate" ]]; then
      return 0
    fi
  done
  return 1
}

deploy_backend() {
  local service="$1"

  echo
  echo "======================================"
  echo "BUILD ${service}"
  echo "======================================"

  ./gradlew ":${service}:clean" ":${service}:bootJar"

  local image="localhost/cyan/${service}:${TAG}"

  docker build -t "${image}" "./${service}"

  docker save "${image}" | sudo k3s ctr -n k8s.io images import -

  sudo k3s kubectl set image \
    "deployment/${service}" \
    -n "${NAMESPACE}" \
    "${service}=${image}"

  sudo k3s kubectl rollout status \
    "deployment/${service}" \
    -n "${NAMESPACE}" \
    --timeout=180s
}

deploy_panel() {
  echo
  echo "======================================"
  echo "BUILD panel-web"
  echo "======================================"

  local image="localhost/cyan/panel-web:${TAG}"

  docker build \
    --build-arg NEXT_PUBLIC_PLATFORM_API_BASE_URL=https://api.cyancoder.com \
    -t "${image}" \
    ./panel-web

  docker save "${image}" | sudo k3s ctr -n k8s.io images import -

  sudo k3s kubectl set image \
    deployment/panel-web \
    -n "${NAMESPACE}" \
    "panel-web=${image}"

  sudo k3s kubectl rollout status \
    deployment/panel-web \
    -n "${NAMESPACE}" \
    --timeout=180s
}

echo "Deploying Cyan version: ${TAG}"
echo "Requested services: ${REQUESTED_SERVICES[*]}"

# Changes under deploy/kubernetes/ (gateway routes, env wiring, new objects)
# reach the cluster only through an explicit apply — image rollouts alone never
# pick them up.
if [[ "$APPLY_MANIFESTS" == "true" ]]; then
  # kustomize carries placeholder ghcr.io/your-org/... image tags, so applying
  # resets every Deployment's image. That is only safe when this run goes on to
  # re-set the image for every service; otherwise the ones left out would be
  # pointing at an image the cluster cannot pull.
  missing_from_run=()
  for service in "${ALL_BACKEND_SERVICES[@]}" panel-web; do
    if ! printf '%s\n' "${REQUESTED_SERVICES[@]}" | grep -qx "$service"; then
      missing_from_run+=("$service")
    fi
  done
  if (( ${#missing_from_run[@]} > 0 )); then
    echo "Refusing to apply manifests: this run does not redeploy ${missing_from_run[*]}." >&2
    echo "Applying resets every image tag to a placeholder, so it requires a full deploy." >&2
    exit 1
  fi

  echo
  echo "======================================"
  echo "APPLY MANIFESTS"
  echo "======================================"
  sudo k3s kubectl create namespace "${NAMESPACE}" --dry-run=client -o yaml \
    | sudo k3s kubectl apply -f -
  sudo k3s kubectl apply -k deploy/kubernetes -n "${NAMESPACE}"
fi

for SERVICE in "${REQUESTED_SERVICES[@]}"; do
  if [[ "$SERVICE" == "panel-web" ]]; then
    deploy_panel
    continue
  fi

  if ! is_known_backend_service "$SERVICE"; then
    echo "Unknown service requested: ${SERVICE}" >&2
    echo "Known services: ${ALL_BACKEND_SERVICES[*]} panel-web" >&2
    exit 1
  fi

  deploy_backend "$SERVICE"
done

echo
echo "Cyan deployment finished: ${TAG}"

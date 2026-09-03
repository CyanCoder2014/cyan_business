#!/usr/bin/env bash
set -Eeuo pipefail

cd "$(git rev-parse --show-toplevel)"
source deploy/kubernetes/common-client-profile.sh

NAMESPACE="cyan-staging"
TAG="$(git rev-parse --short HEAD)-$(date +%Y%m%d%H%M%S)"
APPLY_MANIFESTS="${APPLY_MANIFESTS:-false}"

# Tooling per CYAN_SERVER_UPDATE_RUNBOOK.md: images are imported into K3s
# containerd with `k3s ctr`, while cluster changes go through the standalone
# kubectl (`k3s kubectl` is not what this host uses). sudo is only used when
# not already root, because sudo's secure_path drops /usr/local/bin and makes
# k3s unresolvable.
SUDO=()
if [[ "$(id -u)" -ne 0 ]]; then
  SUDO=(sudo)
fi
KUBECTL=("${SUDO[@]}" kubectl)
CTR=("${SUDO[@]}" k3s ctr)

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

  docker save "${image}" | "${CTR[@]}" -n k8s.io images import -

  "${KUBECTL[@]}" set image \
    "deployment/${service}" \
    -n "${NAMESPACE}" \
    "${service}=${image}"

  "${KUBECTL[@]}" rollout status \
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

  docker save "${image}" | "${CTR[@]}" -n k8s.io images import -

  "${KUBECTL[@]}" set image \
    deployment/panel-web \
    -n "${NAMESPACE}" \
    "panel-web=${image}"

  "${KUBECTL[@]}" rollout status \
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
  "${KUBECTL[@]}" create namespace "${NAMESPACE}" --dry-run=client -o yaml \
    | "${KUBECTL[@]}" apply -f -
  "${KUBECTL[@]}" apply -k deploy/kubernetes -n "${NAMESPACE}"
fi

# A deployment scaled to zero is declared but deliberately not running, so
# building and pushing an image for it costs minutes and gains nothing. This
# matters most when a shared-module change expands the list to every service.
is_scaled_up() {
  local replicas
  replicas="$("${KUBECTL[@]}" -n "${NAMESPACE}" get deployment "$1" -o jsonpath='{.spec.replicas}' 2>/dev/null || true)"
  [[ -n "$replicas" && "$replicas" != "0" ]]
}

for SERVICE in "${REQUESTED_SERVICES[@]}"; do
  # Validate the name before anything else, so a typo fails loudly instead of
  # being reported as "not deployed".
  if [[ "$SERVICE" != "panel-web" ]] && ! is_known_backend_service "$SERVICE"; then
    echo "Unknown service requested: ${SERVICE}" >&2
    echo "Known services: ${ALL_BACKEND_SERVICES[*]} panel-web" >&2
    exit 1
  fi

  if ! is_scaled_up "$SERVICE"; then
    echo "Skipping ${SERVICE}: not deployed (0 replicas, or no such deployment)."
    continue
  fi

  if [[ "$SERVICE" == "panel-web" ]]; then
    deploy_panel
    continue
  fi

  deploy_backend "$SERVICE"
done

echo
echo "Cyan deployment finished: ${TAG}"

#!/usr/bin/env bash
set -Eeuo pipefail

# Rebuilds and redeploys every service that currently has at least one replica,
# asking the cluster rather than a hand-maintained list. Use after something
# resets image tags — `kubectl apply` on the manifests puts the placeholder
# ghcr.io/your-org/... images back, which no cluster here can pull.
#
#   ./deploy/scripts/redeploy-running.sh            # rebuild everything scaled up
#   ./deploy/scripts/redeploy-running.sh --stale    # only those on a placeholder
#   ./deploy/scripts/redeploy-running.sh --dry-run  # print the list and stop

cd "$(git rev-parse --show-toplevel)"

NAMESPACE="cyan-staging"
STALE_ONLY=false
DRY_RUN=false
for arg in "$@"; do
  case "$arg" in
    --stale) STALE_ONLY=true ;;
    --dry-run) DRY_RUN=true ;;
    *) echo "Unknown option: $arg" >&2; exit 1 ;;
  esac
done

SUDO=()
if [[ "$(id -u)" -ne 0 ]]; then
  SUDO=(sudo)
fi
KUBECTL=("${SUDO[@]}" kubectl)

# replicas>0 is the definition of "in use" here; scaled-to-zero deployments are
# declared but deliberately not running, and rebuilding them wastes the node.
mapfile -t CANDIDATES < <(
  "${KUBECTL[@]}" -n "${NAMESPACE}" get deploy \
    -o jsonpath='{range .items[?(@.spec.replicas>0)]}{.metadata.name}{" "}{.spec.template.spec.containers[0].image}{"\n"}{end}'
)

SELECTED=()
for line in "${CANDIDATES[@]}"; do
  [[ -z "$line" ]] && continue
  name="${line%% *}"
  image="${line##* }"
  if [[ "$STALE_ONLY" == "true" && "$image" != *"your-org"* ]]; then
    continue
  fi
  SELECTED+=("$name")
done

if (( ${#SELECTED[@]} == 0 )); then
  echo "Nothing to redeploy."
  exit 0
fi

echo "Will redeploy ${#SELECTED[@]} service(s):"
printf '  %s\n' "${SELECTED[@]}"

if [[ "$DRY_RUN" == "true" ]]; then
  exit 0
fi

echo
exec ./deploy/scripts/deploy-staging.sh "${SELECTED[@]}"

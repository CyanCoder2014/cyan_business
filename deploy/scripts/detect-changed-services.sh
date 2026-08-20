#!/usr/bin/env bash
set -Eeuo pipefail

# Prints a space-separated list of services touched between BASE and HEAD,
# ready to hand to deploy-staging.sh. A change under a shared library or the
# root Gradle config is unsafe to isolate, so it deploys every backend
# service; a change under panel-web/ deploys panel-web; anything else is
# resolved by matching the top-level directory against ALL_BACKEND_SERVICES.

cd "$(git rev-parse --show-toplevel)"
source deploy/kubernetes/common-client-profile.sh

BASE_SHA="${1:-}"
HEAD_SHA="${2:-HEAD}"

ZERO_SHA="0000000000000000000000000000000000000000"

deploy_everything() {
  echo "${ALL_BACKEND_SERVICES[*]} panel-web"
}

# No base to diff against (workflow_dispatch, or the first push of a branch):
# safest is to deploy everything rather than guess.
if [[ -z "$BASE_SHA" || "$BASE_SHA" == "$ZERO_SHA" ]]; then
  deploy_everything
  exit 0
fi

if ! git cat-file -e "${BASE_SHA}^{commit}" 2>/dev/null; then
  echo "Base commit ${BASE_SHA} not found locally (shallow checkout?); deploying everything." >&2
  deploy_everything
  exit 0
fi

CHANGED_FILES="$(git diff --name-only "$BASE_SHA" "$HEAD_SHA")"

if [[ -z "$CHANGED_FILES" ]]; then
  exit 0
fi

SHARED_PATTERN='^(generic|dynamic-entity-core|platform-error-handling|platform-openapi-core|sso-common)/|^(build\.gradle|settings\.gradle|gradle\.properties)$|^gradle/'

if grep -Eq "$SHARED_PATTERN" <<<"$CHANGED_FILES"; then
  deploy_everything
  exit 0
fi

SELECTED=()

for SERVICE in "${ALL_BACKEND_SERVICES[@]}"; do
  if grep -q "^${SERVICE}/" <<<"$CHANGED_FILES"; then
    SELECTED+=("$SERVICE")
  fi
done

if grep -q "^panel-web/" <<<"$CHANGED_FILES"; then
  SELECTED+=(panel-web)
fi

echo "${SELECTED[*]}"

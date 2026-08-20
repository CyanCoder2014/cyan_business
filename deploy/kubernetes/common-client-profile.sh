#!/usr/bin/env bash

# Source this file from deployment scripts. It declares service groups only and
# deliberately does not build, push, deploy, or exit the caller's SSH shell.

COMMON_IDENTITY_SERVICES=(
  sso-auth-service
  sso-user-service
  sso-captcha-service
  sso-otp-service
  sso-session-service
)

COMMON_PANEL_CORE_SERVICES=(
  tenant-service
  billing-service
  storefront-service
)

COMMON_BUSINESS_SERVICES=(
  content-service
  catalog-service
  crm-service
  report-service
  processor-service
  bpm-service
  automation-orchestrator-service
  ai-orchestrator-service
  notification-service
  media-service
)

# Required to exercise every Phase 1-11 panel area. event-service needs Kafka.
COMMON_EXTENDED_SERVICES=(
  event-service
  bot-adapter-service
  search-index-service
  batch-worker-service
  api-docs-service
)

COMMON_GRADLE_SERVICES=(
  "${COMMON_IDENTITY_SERVICES[@]}"
  "${COMMON_PANEL_CORE_SERVICES[@]}"
  "${COMMON_BUSINESS_SERVICES[@]}"
  "${COMMON_EXTENDED_SERVICES[@]}"
)

# Commerce, finance, inventory, cart, checkout, pricing, and payment bundle.
# Deployed like everything else, just not advertised to a tenant unless the
# provider configuration for the bundle is also in place. See README.md.
COMMON_OPTIONAL_BUNDLE_SERVICES=(
  tax-pay-sys
  factor-service
  buyer-service
  product-service
  client-service
  commerce-service
  finance-service
  inventory-service
  crm-automation-service
  finance-automation-service
  inventory-automation-service
  report-automation-service
  payment-service
  cart-service
  checkout-service
  payment-orchestrator-service
  pricing-promotion-service
)

# Every backend service with a Deployment object under deploy/kubernetes/.
# Single source of truth for deploy-staging.sh and detect-changed-services.sh.
ALL_BACKEND_SERVICES=(
  "${COMMON_OPTIONAL_BUNDLE_SERVICES[@]}"
  "${COMMON_IDENTITY_SERVICES[@]}"
  sso-fido-service
  "${COMMON_PANEL_CORE_SERVICES[@]}"
  "${COMMON_BUSINESS_SERVICES[@]}"
  "${COMMON_EXTENDED_SERVICES[@]}"
)

COMMON_FRONTEND_SERVICES=(panel-web)
COMMON_DATA_SERVICES=(postgres mongo kafka)

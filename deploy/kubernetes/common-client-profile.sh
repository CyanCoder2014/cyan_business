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

COMMON_FRONTEND_SERVICES=(panel-web)
COMMON_DATA_SERVICES=(postgres mongo kafka)

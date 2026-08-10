package com.cyancoder.sso.common.dto;

import jakarta.validation.constraints.NotBlank;

public record SessionScopeRequest(@NotBlank String tenantKey, String siteKey) {}

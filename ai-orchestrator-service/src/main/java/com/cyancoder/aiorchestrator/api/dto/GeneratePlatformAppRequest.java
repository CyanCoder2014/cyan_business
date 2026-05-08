package com.cyancoder.aiorchestrator.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record GeneratePlatformAppRequest(
        @NotBlank String prompt,
        String appType,
        String tenantKey,
        String siteKey,
        String clientKey,
        String sessionId,
        boolean execute,
        Map<String, Object> answers
) {
}

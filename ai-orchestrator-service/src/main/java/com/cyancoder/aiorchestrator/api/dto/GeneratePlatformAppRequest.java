package com.cyancoder.aiorchestrator.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record GeneratePlatformAppRequest(
        @NotBlank String prompt,
        String tenantKey,
        String siteKey,
        boolean execute,
        Map<String, Object> answers
) {
}


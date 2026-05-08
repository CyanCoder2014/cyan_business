package com.cyancoder.aiorchestrator.api.dto;

import java.util.Map;

public record CreateDraftRequest(
        String appType,
        String blueprintKey,
        String tenantKey,
        String siteKey,
        String clientKey,
        String title,
        String prompt,
        Map<String, Object> answers
) {
}

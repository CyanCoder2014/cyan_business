package com.cyancoder.aiorchestrator.api.dto;

import java.util.Map;
import java.util.List;

public record CreateDraftRequest(
        String appType,
        String blueprintKey,
        String tenantKey,
        String siteKey,
        String clientKey,
        String title,
        String prompt,
        Map<String, Object> answers,
        List<String> availableServiceKeys
) {
    public CreateDraftRequest(String appType, String blueprintKey, String tenantKey, String siteKey,
                              String clientKey, String title, String prompt, Map<String, Object> answers) {
        this(appType, blueprintKey, tenantKey, siteKey, clientKey, title, prompt, answers, List.of());
    }
}

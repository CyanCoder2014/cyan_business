package com.cyancoder.aiorchestrator.api.dto;

import java.util.Map;
import java.util.List;

public record CreateConversationSessionRequest(
        String channelType,
        String tenantKey,
        String siteKey,
        String clientKey,
        String draftId,
        String appTypeHint,
        String title,
        Map<String, Object> extractedAnswers,
        List<String> availableServiceKeys
) {
    public CreateConversationSessionRequest(String channelType, String tenantKey, String siteKey,
                                            String clientKey, String draftId, String appTypeHint,
                                            String title, Map<String, Object> extractedAnswers) {
        this(channelType, tenantKey, siteKey, clientKey, draftId, appTypeHint, title,
                extractedAnswers, List.of());
    }
}

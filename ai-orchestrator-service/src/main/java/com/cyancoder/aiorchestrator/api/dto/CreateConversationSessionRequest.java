package com.cyancoder.aiorchestrator.api.dto;

import java.util.Map;

public record CreateConversationSessionRequest(
        String channelType,
        String tenantKey,
        String siteKey,
        String clientKey,
        String draftId,
        String appTypeHint,
        String title,
        Map<String, Object> extractedAnswers
) {
}

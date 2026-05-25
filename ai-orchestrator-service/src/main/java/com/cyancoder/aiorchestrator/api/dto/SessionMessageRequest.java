package com.cyancoder.aiorchestrator.api.dto;

import java.util.Map;

public record SessionMessageRequest(
        String role,
        String content,
        Map<String, Object> answersPatch
) {
}

package com.cyancoder.aiorchestrator.api.dto;

import java.util.Map;
import java.util.List;

public record SessionMessageRequest(
        String role,
        String content,
        Map<String, Object> answersPatch,
        List<String> availableServiceKeys
) {
}

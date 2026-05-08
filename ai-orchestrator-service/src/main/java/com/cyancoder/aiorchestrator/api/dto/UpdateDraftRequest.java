package com.cyancoder.aiorchestrator.api.dto;

import java.util.Map;

public record UpdateDraftRequest(
        String prompt,
        String title,
        Map<String, Object> answersPatch
) {
}

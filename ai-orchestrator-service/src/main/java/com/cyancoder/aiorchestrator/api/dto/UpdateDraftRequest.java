package com.cyancoder.aiorchestrator.api.dto;

import java.util.Map;
import java.util.List;

public record UpdateDraftRequest(
        String prompt,
        String title,
        Map<String, Object> answersPatch,
        List<String> availableServiceKeys
) {
    public UpdateDraftRequest(String prompt, String title, Map<String, Object> answersPatch) {
        this(prompt, title, answersPatch, List.of());
    }
}

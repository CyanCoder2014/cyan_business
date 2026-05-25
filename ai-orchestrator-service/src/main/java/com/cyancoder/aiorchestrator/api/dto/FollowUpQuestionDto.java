package com.cyancoder.aiorchestrator.api.dto;

import java.util.List;

public record FollowUpQuestionDto(
        String key,
        String prompt,
        boolean required,
        String reason,
        List<String> suggestedAnswers
) {
}

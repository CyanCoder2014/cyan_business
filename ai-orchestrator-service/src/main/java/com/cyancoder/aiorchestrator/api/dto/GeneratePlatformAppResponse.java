package com.cyancoder.aiorchestrator.api.dto;

import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;

import java.util.List;

public record GeneratePlatformAppResponse(
        String draftId,
        String sessionId,
        PlatformAppDslDefinition dsl,
        List<String> nextQuestions,
        List<FollowUpQuestionDto> followUpQuestions,
        ProvisioningResultDto provisioningResult
) {
}

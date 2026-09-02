package com.cyancoder.aiorchestrator.api.dto;

import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;

import java.util.List;

public record GeneratePlatformAppResponse(
        String draftId,
        String sessionId,
        PlatformAppDslDefinition dsl,
        List<String> nextQuestions,
        List<FollowUpQuestionDto> followUpQuestions,
        ProvisioningResultDto provisioningResult,
        /**
         * "MODEL" when a real LLM produced the draft, "HEURISTIC" when no
         * provider was configured and it came from the keyword fallback.
         */
        String generationMode
) {
    public GeneratePlatformAppResponse(String draftId, String sessionId, PlatformAppDslDefinition dsl,
                                       List<String> nextQuestions, List<FollowUpQuestionDto> followUpQuestions,
                                       ProvisioningResultDto provisioningResult) {
        this(draftId, sessionId, dsl, nextQuestions, followUpQuestions, provisioningResult, "MODEL");
    }
}

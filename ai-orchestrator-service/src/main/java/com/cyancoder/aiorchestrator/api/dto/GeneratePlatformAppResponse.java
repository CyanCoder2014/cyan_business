package com.cyancoder.aiorchestrator.api.dto;

import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;

import java.util.List;

public record GeneratePlatformAppResponse(
        String draftId,
        PlatformAppDslDefinition dsl,
        List<String> nextQuestions,
        ProvisioningResultDto provisioningResult
) {
}

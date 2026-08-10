package com.cyancoder.aiorchestrator.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.List;

public record AiOperationRequest(
        @NotNull AiOperationType operation,
        @NotBlank String instructions,
        Object input,
        Map<String, Object> outputSchema,
        String locale,
        String providerProfileKey,
        List<AiAssetInput> assets
) {
    public AiOperationRequest(AiOperationType operation,String instructions,Object input,Map<String,Object> outputSchema,String locale){this(operation,instructions,input,outputSchema,locale,null,List.of());}
    public enum AiOperationType { TRANSFORM_DATA, GENERATE_CONTENT, GENERATE_DSL, ANALYZE_ASSET, GENERATE_IMAGE, GENERATE_AUDIO, GENERATE_VIDEO }
    public record AiAssetInput(@NotBlank String assetKey, @NotBlank String modality) {}
}

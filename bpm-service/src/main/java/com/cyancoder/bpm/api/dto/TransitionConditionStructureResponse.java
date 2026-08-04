package com.cyancoder.bpm.api.dto;

import java.util.List;

public record TransitionConditionStructureResponse(
        List<MetadataFieldDescriptor> conditionFields,
        List<TransitionConditionOperatorDescriptor> operators,
        List<String> logicalOperators,
        boolean expressionSyntaxSupported,
        List<String> supportedFields
) {
}

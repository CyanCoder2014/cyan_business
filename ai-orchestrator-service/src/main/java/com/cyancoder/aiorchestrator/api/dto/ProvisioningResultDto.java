package com.cyancoder.aiorchestrator.api.dto;

import java.util.List;
import java.util.Map;

public record ProvisioningResultDto(
        String status,
        List<Map<String, Object>> createdDefinitions,
        List<Map<String, Object>> createdRecords,
        List<Map<String, Object>> createdFlows,
        List<Map<String, Object>> deliveryEndpoints,
        List<String> manualActions
) {
}


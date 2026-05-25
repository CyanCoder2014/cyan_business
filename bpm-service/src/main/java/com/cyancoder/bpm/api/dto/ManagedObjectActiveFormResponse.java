package com.cyancoder.bpm.api.dto;

import com.cyancoder.bpm.domain.FlowAccessRule;

import java.util.Map;

public record ManagedObjectActiveFormResponse(
        String objectId,
        String objectType,
        String flowKey,
        String state,
        String formKey,
        String processorKey,
        String submittedFormId,
        FlowAccessRule accessRule,
        Map<String, Object> rendererDefinition,
        String entityService,
        String entityKey,
        String submitMode
) {
}


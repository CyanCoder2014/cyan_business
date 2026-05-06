package com.cyancoder.bpm.api.dto;

import java.util.Map;

public record FormSubmissionSyncRequest(
        String objectId,
        String objectType,
        String flowKey,
        String stateId,
        String formKey,
        String processorKey,
        String existingSubmissionId,
        String actorUserId,
        Map<String, Object> formData,
        Map<String, Object> objectPayload,
        Map<String, Object> context
) {
}


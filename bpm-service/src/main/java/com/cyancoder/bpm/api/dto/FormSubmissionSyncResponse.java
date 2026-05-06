package com.cyancoder.bpm.api.dto;

import java.util.Map;

public record FormSubmissionSyncResponse(boolean success, String message, String submittedFormId, Map<String, Object> currentFormValues) {
}


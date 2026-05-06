package com.cyancoder.bpm.api.dto;

import com.cyancoder.bpm.domain.ManagedObject;

import java.util.Map;

public record ManagedObjectFormSubmissionResponse(ManagedObject object, String submittedFormId, Map<String, Object> currentFormValues) {
}


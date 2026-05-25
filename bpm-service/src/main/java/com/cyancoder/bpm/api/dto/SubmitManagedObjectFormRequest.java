package com.cyancoder.bpm.api.dto;

import java.util.Map;

public record SubmitManagedObjectFormRequest(Map<String, Object> formData, String nextState, Map<String, Object> context) {
}


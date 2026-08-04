package com.cyancoder.bpm.api.dto;

import java.util.Map;

public record ProcessorRunRequest(String targetType, Map<String, Object> payload) {
}

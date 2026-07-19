package com.cyancoder.bpm.api.dto;

import java.util.List;
import java.util.Map;

public record ProcessorRunResponse(boolean valid, List<String> errors, Map<String, Object> payload) {
}

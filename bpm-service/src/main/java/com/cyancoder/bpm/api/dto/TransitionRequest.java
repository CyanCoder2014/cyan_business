package com.cyancoder.bpm.api.dto;

import java.util.Map;

public record TransitionRequest(String nextState, Map<String, Object> context) {
}

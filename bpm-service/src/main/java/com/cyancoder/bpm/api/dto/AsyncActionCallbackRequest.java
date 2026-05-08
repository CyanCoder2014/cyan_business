package com.cyancoder.bpm.api.dto;

import java.util.Map;

public record AsyncActionCallbackRequest(
        String callbackId,
        String nextState,
        Map<String, Object> payload,
        Map<String, Object> context
) {
}


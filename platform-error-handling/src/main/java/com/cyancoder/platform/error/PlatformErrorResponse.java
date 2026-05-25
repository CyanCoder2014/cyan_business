package com.cyancoder.platform.error;

import java.time.Instant;
import java.util.Map;

public record PlatformErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String errorCode,
        String message,
        String path,
        Map<String, Object> details
) {
}

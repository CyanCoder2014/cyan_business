package com.cyancoder.platform.error;

import java.time.Instant;
import java.util.Map;
import java.util.List;

public record PlatformErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String errorCode,
        String message,
        String path,
        Map<String, Object> details,
        List<FieldError> fieldErrors,
        String correlationId,
        boolean retryable
) {
    public record FieldError(String field,String code,String message) {}
}

package com.cyancoder.aiorchestrator.api;

import com.cyancoder.aiorchestrator.exception.DownstreamServiceException;
import com.cyancoder.aiorchestrator.exception.LlmGenerationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class AiOrchestratorExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), Map.of());
    }

    @ExceptionHandler(LlmGenerationException.class)
    public ResponseEntity<Map<String, Object>> handleLlmGeneration(LlmGenerationException ex) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("providerFailures", ex.getProviderFailures());
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), details);
    }

    @ExceptionHandler(DownstreamServiceException.class)
    public ResponseEntity<Map<String, Object>> handleDownstream(DownstreamServiceException ex) {
        HttpStatus status = ex.getDownstreamStatus() != null && ex.getDownstreamStatus() == 404
                ? HttpStatus.BAD_GATEWAY
                : HttpStatus.SERVICE_UNAVAILABLE;
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("serviceKey", ex.getServiceKey());
        details.put("path", ex.getPath());
        details.put("downstreamStatus", ex.getDownstreamStatus());
        details.put("downstreamBody", ex.getResponseBody());
        return build(status, ex.getMessage(), details);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), Map.of());
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message, Map<String, Object> details) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        details.forEach((key, value) -> {
            if (value != null) {
                body.put(key, value);
            }
        });
        return ResponseEntity.status(status).body(body);
    }
}

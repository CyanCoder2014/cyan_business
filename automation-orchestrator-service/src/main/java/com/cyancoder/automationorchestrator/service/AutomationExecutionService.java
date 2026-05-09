package com.cyancoder.automationorchestrator.service;

import com.cyancoder.automationorchestrator.config.AutomationCallbackProperties;
import com.cyancoder.automationorchestrator.domain.AutomationExecution;
import com.cyancoder.automationorchestrator.domain.AutomationExecutionMode;
import com.cyancoder.automationorchestrator.domain.AutomationFailurePolicy;
import com.cyancoder.automationorchestrator.model.AutomationStartRequest;
import com.cyancoder.automationorchestrator.model.AutomationStartResponse;
import com.cyancoder.automationorchestrator.model.BpmAsyncCallbackRequest;
import com.cyancoder.automationorchestrator.repo.AutomationExecutionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class AutomationExecutionService {
    private final AutomationExecutionRepository repository;
    private final InternalServiceHttpSupport httpSupport;
    private final AutomationCallbackProperties callbackProperties;
    private final ObjectMapper objectMapper;

    public AutomationExecutionService(AutomationExecutionRepository repository,
                                      InternalServiceHttpSupport httpSupport,
                                      AutomationCallbackProperties callbackProperties,
                                      ObjectMapper objectMapper) {
        this.repository = repository;
        this.httpSupport = httpSupport;
        this.callbackProperties = callbackProperties;
        this.objectMapper = objectMapper;
    }

    public AutomationStartResponse start(AutomationStartRequest request) {
        AutomationExecution execution = new AutomationExecution();
        execution.setExecutionId("exec-" + UUID.randomUUID());
        execution.setBlockKey(firstNonBlank(request.blockKey(), "block-" + UUID.randomUUID().toString().substring(0, 8)));
        execution.setAutomationFlowKey(firstNonBlank(request.automationFlowKey(), "hybrid-screening-automation"));
        execution.setExecutionMode(request.executionMode() == null ? AutomationExecutionMode.ASYNC : request.executionMode());
        execution.setFailurePolicy(request.failurePolicy() == null ? AutomationFailurePolicy.MARK_FAILED : request.failurePolicy());
        execution.setCorrelationKey(request.correlationKey());
        execution.setTenantKey(request.tenantKey());
        execution.setSiteKey(request.siteKey());
        execution.setStatus("RUNNING");
        execution.setInput(new LinkedHashMap<>(request.input() == null ? Map.of() : request.input()));
        execution.setInlineFragment(new LinkedHashMap<>(request.inlineFragment() == null ? Map.of() : request.inlineFragment()));
        execution.setMaxRetries(request.maxRetries() == null ? 0 : Math.max(0, request.maxRetries()));
        execution.setTimeoutSeconds(request.timeoutSeconds());
        execution.setTimeoutAt(request.timeoutSeconds() == null ? null : Instant.now().plusSeconds(request.timeoutSeconds()));
        execution.setCreatedAt(Instant.now());
        execution.setUpdatedAt(Instant.now());
        repository.save(execution);

        if (execution.getExecutionMode() == AutomationExecutionMode.SYNC) {
            executeNow(execution, request);
            return toResponse(repository.findByExecutionId(execution.getExecutionId()).orElse(execution));
        }

        long delayMillis = request.delayMillis() == null ? 0L : Math.max(0L, request.delayMillis());
        CompletableFuture.runAsync(() -> {
            if (delayMillis > 0) {
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            executeNow(execution, request);
        });
        return toResponse(execution);
    }

    public AutomationStartResponse get(String executionId) {
        return toResponse(repository.findByExecutionId(executionId).orElseThrow());
    }

    public AutomationStartResponse cancel(String executionId) {
        AutomationExecution execution = repository.findByExecutionId(executionId).orElseThrow();
        execution.setCancelRequested(true);
        execution.setCancelledAt(Instant.now());
        if ("RUNNING".equalsIgnoreCase(execution.getStatus())) {
            execution.setStatus("CANCELLED");
        }
        execution.setUpdatedAt(Instant.now());
        execution.setSnapshot(buildSnapshot(execution, execution.getOutput(), execution.getStatus()));
        repository.save(execution);
        return toResponse(execution);
    }

    private void executeNow(AutomationExecution execution, AutomationStartRequest request) {
        AutomationExecution latest = repository.findByExecutionId(execution.getExecutionId()).orElse(execution);
        if (latest.isCancelRequested()) {
            latest.setStatus("CANCELLED");
            latest.setCancelledAt(firstInstant(latest.getCancelledAt(), Instant.now()));
            latest.setCompletedAt(Instant.now());
            latest.setUpdatedAt(Instant.now());
            latest.setSnapshot(buildSnapshot(latest, Map.of(), "CANCELLED"));
            repository.save(latest);
            if (request.callbackPath() != null && !request.callbackPath().isBlank()) {
                callbackBpm(latest, request.callbackPath(), request.context());
            }
            return;
        }
        if (latest.getTimeoutAt() != null && Instant.now().isAfter(latest.getTimeoutAt())) {
            latest.setStatus("TIMED_OUT");
            latest.setError(Map.of("message", "automation execution timed out"));
            latest.setCompletedAt(Instant.now());
            latest.setUpdatedAt(Instant.now());
            latest.setSnapshot(buildSnapshot(latest, Map.of(), "TIMED_OUT"));
            repository.save(latest);
            if (request.callbackPath() != null && !request.callbackPath().isBlank()) {
                callbackBpm(latest, request.callbackPath(), request.context());
            }
            return;
        }

        try {
            Map<String, Object> output = evaluateExecution(latest);
            latest.setOutput(output);
            latest.setStatus("COMPLETED");
            latest.setCompletedAt(Instant.now());
            latest.setUpdatedAt(Instant.now());
            latest.setSnapshot(buildSnapshot(latest, output, "COMPLETED"));
            repository.save(latest);
        } catch (RuntimeException ex) {
            latest.setStatus("FAILED");
            latest.setError(Map.of("message", firstNonBlank(ex.getMessage(), "automation failed")));
            latest.setCompletedAt(Instant.now());
            latest.setUpdatedAt(Instant.now());
            latest.setSnapshot(buildSnapshot(latest, Map.of(), "FAILED"));
            repository.save(latest);
        }

        if (request.callbackPath() != null && !request.callbackPath().isBlank()) {
            callbackBpm(latest, request.callbackPath(), request.context());
        }
    }

    private void callbackBpm(AutomationExecution execution, String callbackPath, Map<String, Object> context) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("executionId", execution.getExecutionId());
        payload.put("blockKey", execution.getBlockKey());
        payload.put("status", execution.getStatus());
        payload.put("snapshot", execution.getSnapshot());
        payload.putAll(execution.getOutput());
        if (execution.getError() != null && !execution.getError().isEmpty()) {
            payload.put("error", execution.getError());
        }

        BpmAsyncCallbackRequest callbackRequest = new BpmAsyncCallbackRequest(
                execution.getExecutionId(),
                execution.getStatus(),
                null,
                payload,
                context == null ? Map.of() : context
        );
        try {
            byte[] body = objectMapper.writeValueAsBytes(callbackRequest);
            String timestamp = String.valueOf(Instant.now().getEpochSecond());
            HttpHeaders headers = httpSupport.internalHeaders("bpm-service", execution.getTenantKey(), execution.getSiteKey());
            headers.set(callbackProperties.getTimestampHeader(), timestamp);
            headers.set(callbackProperties.getSignatureHeader(), sign(timestamp, body));
            httpSupport.exchange("bpm-service", callbackPath, HttpMethod.POST, callbackRequest, headers, Map.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to callback BPM automation result", ex);
        }
    }

    private Map<String, Object> evaluateExecution(AutomationExecution execution) {
        if (execution.getInlineFragment() != null && !execution.getInlineFragment().isEmpty()) {
            return evaluateInlineFragment(execution.getInlineFragment(), execution.getInput());
        }
        return evaluateHybridScreening(execution.getInput());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> evaluateInlineFragment(Map<String, Object> inlineFragment, Map<String, Object> input) {
        String type = string(inlineFragment.get("type"));
        if ("FAIL".equalsIgnoreCase(type)) {
            throw new IllegalStateException(firstNonBlank(string(inlineFragment.get("message")), "inline fragment failed"));
        }
        if ("MAP_OUTPUT".equalsIgnoreCase(type)) {
            Object output = inlineFragment.get("output");
            return output instanceof Map<?, ?> map ? new LinkedHashMap<>((Map<String, Object>) map) : Map.of();
        }
        if ("HYBRID_SCREENING".equalsIgnoreCase(type) || type == null || type.isBlank()) {
            return evaluateHybridScreening(input);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fragmentType", type);
        result.put("accepted", true);
        result.put("inputEcho", input);
        return result;
    }

    private Map<String, Object> evaluateHybridScreening(Map<String, Object> input) {
        double amount = number(input.get("requestedAmount"));
        String nationalId = string(input.get("nationalId"));
        String fullName = firstNonBlank(string(input.get("fullName")), "Applicant");
        int riskScore = (int) Math.max(5, Math.min(95, Math.round((amount / 1000.0) + (nationalId == null ? 30 : nationalId.length()))));
        String screeningRoute;
        if (riskScore >= 70) {
            screeningRoute = "REJECT";
        } else if (riskScore >= 40) {
            screeningRoute = "MANUAL_REVIEW";
        } else {
            screeningRoute = "FAST_TRACK";
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("riskScore", riskScore);
        result.put("screeningRoute", screeningRoute);
        result.put("externalRef", "screen-" + slug(fullName) + "-" + UUID.randomUUID().toString().substring(0, 8));
        result.put("providerDecision", screeningRoute);
        return result;
    }

    private Map<String, Object> buildSnapshot(AutomationExecution execution, Map<String, Object> output, String status) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("executionId", execution.getExecutionId());
        snapshot.put("blockKey", execution.getBlockKey());
        snapshot.put("automationFlowKey", execution.getAutomationFlowKey());
        snapshot.put("executionMode", execution.getExecutionMode().name());
        snapshot.put("failurePolicy", execution.getFailurePolicy().name());
        snapshot.put("status", status);
        snapshot.put("input", execution.getInput());
        snapshot.put("output", output);
        snapshot.put("error", execution.getError());
        snapshot.put("retryCount", execution.getRetryCount());
        snapshot.put("completedAt", Instant.now().toString());
        return snapshot;
    }

    private AutomationStartResponse toResponse(AutomationExecution execution) {
        return new AutomationStartResponse(
                execution.getExecutionId(),
                execution.getBlockKey(),
                execution.getAutomationFlowKey(),
                execution.getCorrelationKey(),
                execution.getStatus(),
                execution.getSnapshot(),
                execution.getOutput(),
                execution.getError()
        );
    }

    private String sign(String timestamp, byte[] canonicalBody) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(callbackProperties.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            mac.update(timestamp.getBytes(StandardCharsets.UTF_8));
            mac.update((byte) '.');
            mac.update(canonicalBody);
            return HexFormat.of().formatHex(mac.doFinal());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to sign automation callback", ex);
        }
    }

    private double number(Object value) {
        return value instanceof Number number ? number.doubleValue() : 0;
    }

    private String string(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private Instant firstInstant(Instant left, Instant right) {
        return left == null ? right : left;
    }

    private String slug(String value) {
        return value == null ? "applicant" : value.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}

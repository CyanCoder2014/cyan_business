package com.cyancoder.automationorchestrator.service;

import com.cyancoder.automationorchestrator.config.AutomationCallbackProperties;
import com.cyancoder.automationorchestrator.domain.AutomationExecution;
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
        execution.setAutomationFlowKey(firstNonBlank(request.automationFlowKey(), "hybrid-screening-automation"));
        execution.setCorrelationKey(request.correlationKey());
        execution.setTenantKey(request.tenantKey());
        execution.setSiteKey(request.siteKey());
        execution.setStatus("RUNNING");
        execution.setInput(new LinkedHashMap<>(request.input() == null ? Map.of() : request.input()));
        execution.setCreatedAt(Instant.now());
        execution.setUpdatedAt(Instant.now());

        Map<String, Object> output = evaluateHybridScreening(execution.getInput());
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("executionId", execution.getExecutionId());
        snapshot.put("automationFlowKey", execution.getAutomationFlowKey());
        snapshot.put("status", "COMPLETED");
        snapshot.put("input", execution.getInput());
        snapshot.put("output", output);
        snapshot.put("completedAt", Instant.now().toString());

        execution.setOutput(output);
        execution.setSnapshot(snapshot);
        execution.setStatus("COMPLETED");
        execution.setCompletedAt(Instant.now());
        execution.setUpdatedAt(Instant.now());
        repository.save(execution);

        if (request.callbackPath() != null && !request.callbackPath().isBlank()) {
            callbackBpm(execution, request.callbackPath(), request.context());
        }

        return new AutomationStartResponse(
                execution.getExecutionId(),
                execution.getAutomationFlowKey(),
                execution.getCorrelationKey(),
                execution.getStatus(),
                execution.getSnapshot()
        );
    }

    private void callbackBpm(AutomationExecution execution, String callbackPath, Map<String, Object> context) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("executionId", execution.getExecutionId());
        payload.put("status", execution.getStatus());
        payload.put("snapshot", execution.getSnapshot());
        payload.putAll(execution.getOutput());

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

    private String slug(String value) {
        return value == null ? "applicant" : value.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}

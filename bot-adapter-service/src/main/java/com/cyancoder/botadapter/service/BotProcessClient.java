package com.cyancoder.botadapter.service;

import com.cyancoder.botadapter.config.AiOrchestratorProperties;
import com.cyancoder.botadapter.domain.BotProcessBinding;
import com.cyancoder.botadapter.domain.BotProcessTargetType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class BotProcessClient {
    private final RestTemplate restTemplate;
    private final AiOrchestratorProperties properties;

    public BotProcessClient(RestTemplate restTemplate, AiOrchestratorProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public void validateTarget(BotProcessBinding binding) {
        if (binding.getTargetType() == BotProcessTargetType.AUTOMATION) {
            exchange(properties.getAutomationBaseUrl() + "/internal/automation-flows/" + binding.getTargetKey() + "/active",
                    HttpMethod.GET, binding, null, automationAuth());
        } else {
            exchange(properties.getBpmBaseUrl() + "/internal/bpm/flows/" + binding.getTargetKey(),
                    HttpMethod.GET, binding, null, bpmAuth());
        }
    }

    public String dispatch(BotProcessBinding binding, String inboundId, String externalMessageId,
                           String externalChatId, String text, String channel, String integrationKey) {
        Map<String, Object> input = new LinkedHashMap<>(binding.getInputTemplate());
        input.put("message", text);
        input.put("externalMessageId", externalMessageId);
        input.put("externalChatId", externalChatId);
        input.put("channel", channel);
        input.put("integrationKey", integrationKey);
        input.put("inboundMessageId", inboundId);
        String idempotencyKey = "bot:" + inboundId + ":" + binding.getId();

        if (binding.getTargetType() == BotProcessTargetType.AUTOMATION) {
            Map<String, Object> request = new LinkedHashMap<>();
            request.put("blockKey", "bot-inbound");
            request.put("automationFlowKey", binding.getTargetKey());
            request.put("executionMode", "ASYNC");
            request.put("failurePolicy", "MARK_FAILED");
            request.put("correlationKey", idempotencyKey);
            request.put("tenantKey", binding.getTenantKey());
            request.put("siteKey", binding.getSiteKey());
            request.put("input", input);
            request.put("context", Map.of("source", "BOT", "bindingKey", binding.getBindingKey()));
            request.put("flowKey", binding.getTargetKey());
            request.put("idempotencyKey", idempotencyKey);
            request.put("variables", input);
            Map<?, ?> result = exchange(properties.getAutomationBaseUrl() + "/internal/automation-orchestrator/executions/start",
                    HttpMethod.POST, binding, request, automationAuth());
            return stringValue(result, "executionId");
        }

        Map<String, Object> ref = Map.of(
                "service", "bot-adapter-service",
                "entityKey", "bot-inbound-message",
                "recordKey", inboundId
        );
        Map<String, Object> request = Map.of(
                "flowKey", binding.getTargetKey(),
                "objectType", "BOT_MESSAGE",
                "objectRef", ref,
                "payload", input
        );
        Map<?, ?> result = exchange(properties.getBpmBaseUrl() + "/internal/bpm/managed-objects",
                HttpMethod.POST, binding, request, bpmAuth(), Map.of(
                        "X-Actor-User", "bot:" + channel.toLowerCase() + ":" + externalChatId,
                        "X-Actor-Roles", "BOT_EXTERNAL"
                ));
        return stringValue(result, "id");
    }

    private Map<?, ?> exchange(String url, HttpMethod method, BotProcessBinding binding, Object body, String auth) {
        return exchange(url, method, binding, body, auth, Map.of());
    }

    @SuppressWarnings("unchecked")
    private Map<?, ?> exchange(String url, HttpMethod method, BotProcessBinding binding, Object body, String auth,
                               Map<String, String> extraHeaders) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, auth);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Tenant-Key", binding.getTenantKey());
        headers.set("X-Site-Key", binding.getSiteKey());
        extraHeaders.forEach(headers::set);
        Object response = restTemplate.exchange(url, method, new HttpEntity<>(body, headers), Map.class).getBody();
        return response instanceof Map<?, ?> map ? map : Map.of();
    }

    private String automationAuth() {
        return basic(properties.getAutomationInternalUsername(), properties.getAutomationInternalPassword());
    }

    private String bpmAuth() {
        return basic(properties.getBpmInternalUsername(), properties.getBpmInternalPassword());
    }

    private String basic(String username, String password) {
        String value = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String stringValue(Map<?, ?> response, String key) {
        Object value = response.get(key);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalStateException("Process service returned no " + key);
        }
        return value.toString();
    }
}

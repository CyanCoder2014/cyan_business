package com.cyancoder.botadapter.service;

import com.cyancoder.botadapter.config.AiOrchestratorProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class AiConversationClient {
    private final RestTemplate restTemplate;
    private final AiOrchestratorProperties properties;

    public AiConversationClient(RestTemplate restTemplate, AiOrchestratorProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public String createSession(String channelType,
                                String tenantKey,
                                String siteKey,
                                String clientKey,
                                String appTypeHint,
                                Map<String, Object> extractedAnswers) {
        Map<String, Object> request = Map.of(
                "channelType", channelType,
                "tenantKey", tenantKey,
                "siteKey", siteKey,
                "clientKey", clientKey == null ? "" : clientKey,
                "appTypeHint", appTypeHint == null ? "MIXED_BUSINESS_APP" : appTypeHint,
                "title", channelType + " bot session",
                "extractedAnswers", extractedAnswers
        );
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(
                properties.getBaseUrl() + "/endpoint/ai-orchestrator/sessions",
                request,
                Map.class
        );
        if (response == null || response.get("sessionId") == null) {
            throw new IllegalStateException("AI orchestrator did not return a sessionId");
        }
        return response.get("sessionId").toString();
    }

    public void appendUserMessage(String sessionId, String content, Map<String, Object> answersPatch) {
        Map<String, Object> request = Map.of(
                "role", "USER",
                "content", content,
                "answersPatch", answersPatch
        );
        restTemplate.postForObject(
                properties.getBaseUrl() + "/endpoint/ai-orchestrator/sessions/" + sessionId + "/message",
                request,
                Map.class
        );
    }
}

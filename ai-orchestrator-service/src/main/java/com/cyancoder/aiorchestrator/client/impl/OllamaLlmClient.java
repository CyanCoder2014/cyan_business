package com.cyancoder.aiorchestrator.client.impl;

import com.cyancoder.aiorchestrator.client.LlmClient;
import com.cyancoder.aiorchestrator.config.LlmProperties;
import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class OllamaLlmClient implements LlmClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private final LlmProperties properties;
    private final ObjectMapper objectMapper;

    public OllamaLlmClient(LlmProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public PlatformAppDslDefinition generateDsl(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> entity = restTemplate.exchange(
                    properties.getOllama().getBaseUrl() + properties.getOllama().getCompletionsPath(),
                    HttpMethod.POST,
                    new HttpEntity<>(Map.of(
                            "model", properties.getOllama().getModel(),
                            "stream", false,
                            "format", "json",
                            "messages", java.util.List.of(Map.of("role", "user", "content", prompt + "\nReturn strict JSON only."))
                    ), headers),
                    String.class
            );
            String response = entity.getBody();
            JsonNode root = objectMapper.readTree(response == null ? "{}" : response);
            String content = root.path("message").path("content").asText();
            return objectMapper.readValue(content, PlatformAppDslDefinition.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate DSL from Ollama", ex);
        }
    }
}

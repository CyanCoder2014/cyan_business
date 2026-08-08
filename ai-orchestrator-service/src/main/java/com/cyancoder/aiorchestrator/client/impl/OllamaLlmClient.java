package com.cyancoder.aiorchestrator.client.impl;

import com.cyancoder.aiorchestrator.client.LlmClient;
import com.cyancoder.aiorchestrator.config.LlmProperties;
import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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
    private final RestTemplate restTemplate;
    private final LlmProperties properties;
    private final ObjectMapper objectMapper;

    public OllamaLlmClient(LlmProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeoutMs());
        requestFactory.setReadTimeout(properties.getReadTimeoutMs());
        this.restTemplate = new RestTemplate(requestFactory);
    }

    @Override
    public PlatformAppDslDefinition generateDsl(String prompt) {
        try {
            return objectMapper.readValue(generateContent(prompt + "\nReturn strict JSON only."), PlatformAppDslDefinition.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate DSL from Ollama", ex);
        }
    }

    @Override
    public String generateContent(String prompt) {
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
                            "messages", java.util.List.of(Map.of("role", "user", "content", prompt))
                    ), headers),
                    String.class
            );
            String response = entity.getBody();
            JsonNode root = objectMapper.readTree(response == null ? "{}" : response);
            return root.path("message").path("content").asText();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate content from Ollama", ex);
        }
    }
}

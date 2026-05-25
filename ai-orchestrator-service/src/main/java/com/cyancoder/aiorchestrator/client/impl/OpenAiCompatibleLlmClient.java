package com.cyancoder.aiorchestrator.client.impl;

import com.cyancoder.aiorchestrator.client.LlmClient;
import com.cyancoder.aiorchestrator.config.AiProvider;
import com.cyancoder.aiorchestrator.config.LlmProperties;
import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenAiCompatibleLlmClient implements LlmClient {
    private static final Pattern JSON_BLOCK = Pattern.compile("\\{.*}", Pattern.DOTALL);
    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleLlmClient.class);

    private final AiProvider provider;
    private final LlmProperties llmProperties;
    private final LlmProperties.ProviderProperties providerProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OpenAiCompatibleLlmClient(AiProvider provider,
                                     LlmProperties llmProperties,
                                     LlmProperties.ProviderProperties providerProperties,
                                     ObjectMapper objectMapper) {
        this.provider = provider;
        this.llmProperties = llmProperties;
        this.providerProperties = providerProperties;
        this.objectMapper = objectMapper;
        this.restTemplate = buildRestTemplate(llmProperties);
    }

    @Override
    public PlatformAppDslDefinition generateDsl(String prompt) {
        String currentPrompt = buildPrompt(prompt);
        RuntimeException last = null;
        for (int attempt = 0; attempt < llmProperties.getMaxParseAttempts(); attempt++) {
            String response = callChatCompletion(currentPrompt);
            try {
                PlatformAppDslDefinition dsl = objectMapper.readValue(extractJson(response), PlatformAppDslDefinition.class);
                if (dsl.getApp() == null || dsl.getApp().getAppKey() == null || dsl.getApp().getAppKey().isBlank()) {
                    throw new IllegalStateException("Provider response did not include app.appKey");
                }
                return dsl;
            } catch (RuntimeException | java.io.IOException exception) {
                last = new IllegalStateException("Failed to parse provider response", exception);
                log.warn("Provider {} returned an invalid DSL payload on attempt {}: {}", provider, attempt + 1, summarizePayload(response));
                currentPrompt = buildPrompt(prompt + "\nReturn valid JSON only.");
            }
        }
        throw new IllegalStateException("Failed to parse LLM response into platform DSL from provider " + provider, last);
    }

    private String callChatCompletion(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        applyHeaders(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                providerProperties.getBaseUrl() + providerProperties.getCompletionsPath(),
                HttpMethod.POST,
                new HttpEntity<>(buildRequestBody(prompt), headers),
                String.class
        );
        String rawResponse = response.getBody();
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new IllegalStateException("Empty response from provider " + provider);
        }
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (contentNode.isTextual()) {
                return contentNode.asText();
            }
            return objectMapper.writeValueAsString(contentNode);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse provider response JSON", ex);
        }
    }

    private void applyHeaders(HttpHeaders headers) {
        headers.setBearerAuth(providerProperties.getApiKey());
        if (provider == AiProvider.OPENROUTER) {
            if (providerProperties.getReferer() != null && !providerProperties.getReferer().isBlank()) {
                headers.set("HTTP-Referer", providerProperties.getReferer());
            }
            if (providerProperties.getTitle() != null && !providerProperties.getTitle().isBlank()) {
                headers.set("X-OpenRouter-Title", providerProperties.getTitle());
            }
        }
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        return Map.of(
                "model", providerProperties.getModel(),
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );
    }

    private String extractJson(String response) {
        String trimmed = response == null ? "" : response.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "");
        }
        Matcher matcher = JSON_BLOCK.matcher(trimmed);
        return matcher.find() ? matcher.group() : trimmed;
    }

    private String buildPrompt(String prompt) {
        return prompt + "\nReturn strict JSON matching PlatformAppDslDefinition with no markdown.";
    }

    private String summarizePayload(String response) {
        if (response == null) {
            return "empty";
        }
        String normalized = response.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 240 ? normalized : normalized.substring(0, 240) + "...";
    }

    private RestTemplate buildRestTemplate(LlmProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeoutMs());
        requestFactory.setReadTimeout(properties.getReadTimeoutMs());
        return new RestTemplate(requestFactory);
    }
}

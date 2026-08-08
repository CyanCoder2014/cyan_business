package com.cyancoder.aiorchestrator.service;

import com.cyancoder.aiorchestrator.api.dto.AiOperationRequest;
import com.cyancoder.aiorchestrator.api.dto.AiOperationResponse;
import com.cyancoder.aiorchestrator.client.LlmClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class AiOperationService {
    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;

    public AiOperationService(LlmClient llmClient, ObjectMapper objectMapper) {
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
    }

    public AiOperationResponse execute(AiOperationRequest request) {
        Object output = switch (request.operation()) {
            case GENERATE_DSL -> llmClient.generateDsl(contextualPrompt(request));
            case GENERATE_CONTENT -> llmClient.generateContent(contextualPrompt(request));
            case TRANSFORM_DATA -> parseJson(llmClient.generateContent(contextualPrompt(request)
                    + "\nReturn strict JSON only with no markdown."));
        };
        return new AiOperationResponse("COMPLETED", request.operation(), output, Instant.now());
    }

    private String contextualPrompt(AiOperationRequest request) {
        StringBuilder prompt = new StringBuilder(request.instructions());
        if (request.locale() != null && !request.locale().isBlank()) prompt.append("\nOutput locale: ").append(request.locale());
        if (request.input() != null) prompt.append("\nInput JSON:\n").append(writeJson(request.input()));
        if (request.outputSchema() != null && !request.outputSchema().isEmpty()) {
            prompt.append("\nRequired output JSON Schema:\n").append(writeJson(request.outputSchema()));
        }
        return prompt.toString();
    }

    private Object parseJson(String value) {
        try {
            String normalized = value == null ? "" : value.trim()
                    .replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "");
            return objectMapper.readValue(normalized, Object.class);
        } catch (Exception ex) {
            throw new IllegalStateException("AI provider did not return valid JSON", ex);
        }
    }

    private String writeJson(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (Exception ex) { throw new IllegalArgumentException("AI operation input is not serializable", ex); }
    }
}

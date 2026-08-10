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
    private final AiProviderProfileService profiles;
    private final AiMediaAssetClient media;
    private final CustomAiProviderClient custom;

    public AiOperationService(LlmClient llmClient, ObjectMapper objectMapper, AiProviderProfileService profiles, AiMediaAssetClient media, CustomAiProviderClient custom) {
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
        this.profiles = profiles;
        this.media = media;
        this.custom = custom;
    }

    public AiOperationResponse execute(AiOperationRequest request) { return execute(request,null,null); }
    public AiOperationResponse execute(AiOperationRequest request,String tenant,String site) {
        if (request.providerProfileKey() != null && !request.providerProfileKey().isBlank()) {
            var profile=profiles.require(tenant,site,request.providerProfileKey());
            var assets=media.resolve(tenant,site,request.assets(),profile.getModalities());
            var result=custom.execute(profile,profiles.secret(profile),request,assets);
            Object output=request.operation()==AiOperationRequest.AiOperationType.TRANSFORM_DATA&&result.output() instanceof String text?parseJson(text):result.output();
            return new AiOperationResponse("COMPLETED",request.operation(),output,Instant.now(),profile.getProfileKey(),result.usage());
        }
        if(request.assets()!=null&&!request.assets().isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,"A provider profile is required for multimodal assets");
        Object output = switch (request.operation()) {
            case GENERATE_DSL -> llmClient.generateDsl(contextualPrompt(request));
            case GENERATE_CONTENT -> llmClient.generateContent(contextualPrompt(request));
            case TRANSFORM_DATA -> parseJson(llmClient.generateContent(contextualPrompt(request)
                    + "\nReturn strict JSON only with no markdown."));
            case ANALYZE_ASSET, GENERATE_IMAGE, GENERATE_AUDIO, GENERATE_VIDEO -> throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,"A custom provider profile is required for this operation");
        };
        return new AiOperationResponse("COMPLETED", request.operation(), output, Instant.now(), null, Map.of());
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

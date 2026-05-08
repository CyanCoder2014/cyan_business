package com.cyancoder.aiorchestrator.client.impl;

import com.cyancoder.aiorchestrator.client.LlmClient;
import com.cyancoder.aiorchestrator.config.AiProvider;
import com.cyancoder.aiorchestrator.config.LlmProperties;
import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Primary
@Component
public class RoutingLlmClient implements LlmClient {
    private final LlmProperties llmProperties;
    private final OllamaLlmClient ollamaLlmClient;
    private final HeuristicLlmClient heuristicLlmClient;
    private final ObjectMapper objectMapper;
    private final Map<AiProvider, LlmClient> openAiCompatibleClients = new EnumMap<>(AiProvider.class);

    public RoutingLlmClient(LlmProperties llmProperties,
                            OllamaLlmClient ollamaLlmClient,
                            HeuristicLlmClient heuristicLlmClient,
                            ObjectMapper objectMapper) {
        this.llmProperties = llmProperties;
        this.ollamaLlmClient = ollamaLlmClient;
        this.heuristicLlmClient = heuristicLlmClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public PlatformAppDslDefinition generateDsl(String prompt) {
        for (AiProvider provider : LlmProviderSelector.selectCandidates(llmProperties)) {
            if (LlmProviderSelector.isAvailable(llmProperties, provider)) {
                try {
                    return getClient(provider).generateDsl(prompt);
                } catch (RuntimeException ignored) {
                }
            }
        }
        return heuristicLlmClient.generateDsl(prompt);
    }

    private LlmClient getClient(AiProvider provider) {
        return switch (provider) {
            case OPENAI, OPENROUTER, GAPGPT -> openAiCompatibleClients.computeIfAbsent(provider, this::buildCompatibleClient);
            case OLLAMA -> ollamaLlmClient;
            case HEURISTIC -> heuristicLlmClient;
            case AUTO -> throw new IllegalStateException("AUTO is not a concrete provider");
        };
    }

    private LlmClient buildCompatibleClient(AiProvider provider) {
        return new OpenAiCompatibleLlmClient(
                provider,
                llmProperties,
                llmProperties.getProviderProperties(provider),
                objectMapper
        );
    }
}

package com.cyancoder.aiorchestrator.client.impl;

import com.cyancoder.aiorchestrator.client.LlmClient;
import com.cyancoder.aiorchestrator.config.AiProvider;
import com.cyancoder.aiorchestrator.config.LlmProperties;
import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;
import com.cyancoder.aiorchestrator.exception.LlmGenerationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Primary
@Component
public class RoutingLlmClient implements LlmClient {
    private static final Logger log = LoggerFactory.getLogger(RoutingLlmClient.class);

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
        Map<AiProvider, String> failures = new LinkedHashMap<>();
        for (AiProvider provider : LlmProviderSelector.selectCandidates(llmProperties)) {
            if (!LlmProviderSelector.isAvailable(llmProperties, provider)) {
                failures.put(provider, LlmProviderSelector.unavailabilityReason(llmProperties, provider));
                continue;
            }
            try {
                log.info("Attempting DSL generation with provider {}", provider);
                return getClient(provider).generateDsl(prompt);
            } catch (RuntimeException ex) {
                failures.put(provider, summarizeFailure(ex));
                log.warn("Provider {} failed DSL generation: {}", provider, failures.get(provider));
            }
        }
        if (LlmProviderSelector.selectCandidates(llmProperties).contains(AiProvider.HEURISTIC)) {
            log.warn("Falling back to HEURISTIC DSL generation after provider failures: {}", failures);
            return heuristicLlmClient.generateDsl(prompt);
        }
        throw new LlmGenerationException("No configured LLM provider produced a valid DSL", failures);
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

    private String summarizeFailure(RuntimeException ex) {
        Throwable current = ex;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return message == null || message.isBlank() ? current.getClass().getSimpleName() : message;
    }
}

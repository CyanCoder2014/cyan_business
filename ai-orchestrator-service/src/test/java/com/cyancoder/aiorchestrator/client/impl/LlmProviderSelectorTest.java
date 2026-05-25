package com.cyancoder.aiorchestrator.client.impl;

import com.cyancoder.aiorchestrator.config.AiProvider;
import com.cyancoder.aiorchestrator.config.LlmProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LlmProviderSelectorTest {

    @Test
    void autoProviderUsesConfiguredFallbackOrder() {
        LlmProperties properties = new LlmProperties();

        assertEquals(
                List.of(AiProvider.OPENAI, AiProvider.GAPGPT, AiProvider.OPENROUTER, AiProvider.OLLAMA, AiProvider.HEURISTIC),
                LlmProviderSelector.selectCandidates(properties)
        );
    }

    @Test
    void explicitProviderBypassesFallbackOrder() {
        LlmProperties properties = new LlmProperties();
        properties.setProvider(AiProvider.OPENROUTER);

        assertEquals(List.of(AiProvider.OPENROUTER), LlmProviderSelector.selectCandidates(properties));
    }

    @Test
    void availabilityRequiresApiKeyForOpenAiCompatibleProvidersButNotForOllama() {
        LlmProperties properties = new LlmProperties();

        properties.getGapgpt().setBaseUrl("https://api.gapgpt.app");
        assertFalse(LlmProviderSelector.isAvailable(properties, AiProvider.GAPGPT));

        properties.getGapgpt().setApiKey("gapgpt-key");
        assertTrue(LlmProviderSelector.isAvailable(properties, AiProvider.GAPGPT));

        properties.getOpenrouter().setBaseUrl("https://openrouter.ai");
        assertFalse(LlmProviderSelector.isAvailable(properties, AiProvider.OPENROUTER));
        assertEquals("apiKey is missing", LlmProviderSelector.unavailabilityReason(properties, AiProvider.OPENROUTER));

        properties.getOllama().setBaseUrl("http://localhost:11434");
        assertTrue(LlmProviderSelector.isAvailable(properties, AiProvider.OLLAMA));
    }
}

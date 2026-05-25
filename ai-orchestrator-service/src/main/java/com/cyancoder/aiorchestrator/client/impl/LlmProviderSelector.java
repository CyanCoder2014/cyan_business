package com.cyancoder.aiorchestrator.client.impl;

import com.cyancoder.aiorchestrator.config.AiProvider;
import com.cyancoder.aiorchestrator.config.LlmProperties;

import java.util.ArrayList;
import java.util.List;

final class LlmProviderSelector {
    private LlmProviderSelector() {
    }

    static List<AiProvider> selectCandidates(LlmProperties properties) {
        if (properties.getProvider() != null && properties.getProvider() != AiProvider.AUTO) {
            return List.of(properties.getProvider());
        }
        return new ArrayList<>(properties.getFallbackOrder());
    }

    static boolean isAvailable(LlmProperties properties, AiProvider provider) {
        return unavailabilityReason(properties, provider) == null;
    }

    static String unavailabilityReason(LlmProperties properties, AiProvider provider) {
        if (provider == AiProvider.HEURISTIC) {
            return null;
        }
        LlmProperties.ProviderProperties providerProperties = properties.getProviderProperties(provider);
        if (providerProperties == null) {
            return "provider properties are missing";
        }
        if (provider == AiProvider.OLLAMA) {
            return providerProperties.getBaseUrl() != null && !providerProperties.getBaseUrl().isBlank()
                    ? null
                    : "baseUrl is missing";
        }
        if (providerProperties.getApiKey() == null || providerProperties.getApiKey().isBlank()) {
            return "apiKey is missing";
        }
        if (providerProperties.getBaseUrl() == null || providerProperties.getBaseUrl().isBlank()) {
            return "baseUrl is missing";
        }
        return null;
    }
}

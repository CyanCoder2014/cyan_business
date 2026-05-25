package com.cyancoder.aiorchestrator.exception;

import com.cyancoder.aiorchestrator.config.AiProvider;

import java.util.Map;

public class LlmGenerationException extends RuntimeException {
    private final Map<AiProvider, String> providerFailures;

    public LlmGenerationException(String message, Map<AiProvider, String> providerFailures) {
        super(message);
        this.providerFailures = providerFailures;
    }

    public Map<AiProvider, String> getProviderFailures() {
        return providerFailures;
    }
}

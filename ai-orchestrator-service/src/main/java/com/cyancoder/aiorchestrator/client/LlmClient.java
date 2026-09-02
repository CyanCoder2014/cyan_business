package com.cyancoder.aiorchestrator.client;

import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;

public interface LlmClient {
    PlatformAppDslDefinition generateDsl(String prompt);

    /**
     * Whether a real model backs this client. False means drafts come from the
     * keyword heuristic, which produces a stock blueprint regardless of what
     * was asked for — callers should say so rather than presenting it as an
     * AI-generated design.
     */
    default boolean isModelBacked() {
        return true;
    }

    default String generateContent(String prompt) {
        throw new UnsupportedOperationException("This provider does not support generative content operations");
    }
}

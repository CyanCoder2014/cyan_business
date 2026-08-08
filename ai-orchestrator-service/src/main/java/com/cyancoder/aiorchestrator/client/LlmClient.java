package com.cyancoder.aiorchestrator.client;

import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;

public interface LlmClient {
    PlatformAppDslDefinition generateDsl(String prompt);

    default String generateContent(String prompt) {
        throw new UnsupportedOperationException("This provider does not support generative content operations");
    }
}

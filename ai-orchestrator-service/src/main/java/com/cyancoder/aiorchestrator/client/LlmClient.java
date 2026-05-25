package com.cyancoder.aiorchestrator.client;

import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;

public interface LlmClient {
    PlatformAppDslDefinition generateDsl(String prompt);
}


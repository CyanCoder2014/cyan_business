package com.cyancoder.aiorchestrator.service;

import java.util.List;
import java.util.Map;

public interface AiPromptBuilder {
    String buildPlatformPrompt(String userPrompt, Map<String, Object> platformMetadata, List<String> retrievedContext, String tenantKey, String siteKey);
}


package com.cyancoder.botadapter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai-orchestrator")
public class AiOrchestratorProperties {
    private String baseUrl = "http://localhost:9121";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}

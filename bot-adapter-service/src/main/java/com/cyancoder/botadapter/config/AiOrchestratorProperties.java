package com.cyancoder.botadapter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai-orchestrator")
public class AiOrchestratorProperties {
    private String baseUrl = "http://localhost:9121";
    private String publicBaseUrl = "http://localhost:8001";
    private String telegramBaseUrl = "https://api.telegram.org";
    private String baleBaseUrl = "https://tapi.bale.ai";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }

    public String getTelegramBaseUrl() {
        return telegramBaseUrl;
    }

    public void setTelegramBaseUrl(String telegramBaseUrl) {
        this.telegramBaseUrl = telegramBaseUrl;
    }

    public String getBaleBaseUrl() {
        return baleBaseUrl;
    }

    public void setBaleBaseUrl(String baleBaseUrl) {
        this.baleBaseUrl = baleBaseUrl;
    }
}

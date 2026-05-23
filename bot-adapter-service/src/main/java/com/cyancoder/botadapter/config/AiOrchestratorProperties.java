package com.cyancoder.botadapter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "ai-orchestrator")
public class AiOrchestratorProperties {
    private String baseUrl = "http://localhost:9121";
    private String publicBaseUrl = "http://localhost:8001";
    private String telegramBaseUrl = "https://api.telegram.org";
    private String baleBaseUrl = "https://tapi.bale.ai";
    private String botSecretsFile;
    private String botSecretEnvPrefix = "CYAN_BOT_SECRET_";
    private String botSecretHttpBaseUrl;
    private String botSecretHttpAuthToken;
    private Map<String, String> botSecretValues = new LinkedHashMap<>();

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

    public String getBotSecretsFile() {
        return botSecretsFile;
    }

    public void setBotSecretsFile(String botSecretsFile) {
        this.botSecretsFile = botSecretsFile;
    }

    public String getBotSecretEnvPrefix() {
        return botSecretEnvPrefix;
    }

    public void setBotSecretEnvPrefix(String botSecretEnvPrefix) {
        this.botSecretEnvPrefix = botSecretEnvPrefix;
    }

    public String getBotSecretHttpBaseUrl() {
        return botSecretHttpBaseUrl;
    }

    public void setBotSecretHttpBaseUrl(String botSecretHttpBaseUrl) {
        this.botSecretHttpBaseUrl = botSecretHttpBaseUrl;
    }

    public String getBotSecretHttpAuthToken() {
        return botSecretHttpAuthToken;
    }

    public void setBotSecretHttpAuthToken(String botSecretHttpAuthToken) {
        this.botSecretHttpAuthToken = botSecretHttpAuthToken;
    }

    public Map<String, String> getBotSecretValues() {
        return botSecretValues;
    }

    public void setBotSecretValues(Map<String, String> botSecretValues) {
        this.botSecretValues = botSecretValues;
    }
}

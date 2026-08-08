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
    private String automationBaseUrl = "http://localhost:9120";
    private String automationInternalUsername = "automation_orchestrator_internal";
    private String automationInternalPassword = "automation_orchestrator_secret";
    private String bpmBaseUrl = "http://localhost:9119";
    private String bpmInternalUsername = "bpm_internal";
    private String bpmInternalPassword = "bpm_secret";

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

    public String getAutomationBaseUrl() { return automationBaseUrl; }
    public void setAutomationBaseUrl(String automationBaseUrl) { this.automationBaseUrl = automationBaseUrl; }
    public String getAutomationInternalUsername() { return automationInternalUsername; }
    public void setAutomationInternalUsername(String value) { this.automationInternalUsername = value; }
    public String getAutomationInternalPassword() { return automationInternalPassword; }
    public void setAutomationInternalPassword(String value) { this.automationInternalPassword = value; }
    public String getBpmBaseUrl() { return bpmBaseUrl; }
    public void setBpmBaseUrl(String bpmBaseUrl) { this.bpmBaseUrl = bpmBaseUrl; }
    public String getBpmInternalUsername() { return bpmInternalUsername; }
    public void setBpmInternalUsername(String value) { this.bpmInternalUsername = value; }
    public String getBpmInternalPassword() { return bpmInternalPassword; }
    public void setBpmInternalPassword(String value) { this.bpmInternalPassword = value; }
}

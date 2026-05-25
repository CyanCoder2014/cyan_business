package com.cyancoder.automationorchestrator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "automation.callback")
public class AutomationCallbackProperties {
    private String secret;
    private String timestampHeader = "X-Dynamic-Flow-Timestamp";
    private String signatureHeader = "X-Dynamic-Flow-Signature";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getTimestampHeader() {
        return timestampHeader;
    }

    public void setTimestampHeader(String timestampHeader) {
        this.timestampHeader = timestampHeader;
    }

    public String getSignatureHeader() {
        return signatureHeader;
    }

    public void setSignatureHeader(String signatureHeader) {
        this.signatureHeader = signatureHeader;
    }
}

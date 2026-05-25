package com.cyancoder.bpm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dynamic-flow.callback")
public class DynamicFlowCallbackProperties {
    private boolean enabled = true;
    private String signatureHeader = "X-Dynamic-Flow-Signature";
    private String timestampHeader = "X-Dynamic-Flow-Timestamp";
    private String secret;
    private long maxSkewSeconds = 300;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getSignatureHeader() { return signatureHeader; }
    public void setSignatureHeader(String signatureHeader) { this.signatureHeader = signatureHeader; }
    public String getTimestampHeader() { return timestampHeader; }
    public void setTimestampHeader(String timestampHeader) { this.timestampHeader = timestampHeader; }
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public long getMaxSkewSeconds() { return maxSkewSeconds; }
    public void setMaxSkewSeconds(long maxSkewSeconds) { this.maxSkewSeconds = maxSkewSeconds; }
}


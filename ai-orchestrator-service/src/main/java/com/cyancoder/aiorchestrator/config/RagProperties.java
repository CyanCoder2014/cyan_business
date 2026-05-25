package com.cyancoder.aiorchestrator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag")
public class RagProperties {
    private boolean enabled = true;
    private boolean bootstrapEnabled = true;
    private String bootstrapResourcePattern = "classpath:/rag/*.md";
    private int topK = 5;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isBootstrapEnabled() { return bootstrapEnabled; }
    public void setBootstrapEnabled(boolean bootstrapEnabled) { this.bootstrapEnabled = bootstrapEnabled; }
    public String getBootstrapResourcePattern() { return bootstrapResourcePattern; }
    public void setBootstrapResourcePattern(String bootstrapResourcePattern) { this.bootstrapResourcePattern = bootstrapResourcePattern; }
    public int getTopK() { return topK; }
    public void setTopK(int topK) { this.topK = topK; }
}


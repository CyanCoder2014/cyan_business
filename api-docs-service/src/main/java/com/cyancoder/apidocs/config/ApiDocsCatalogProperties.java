package com.cyancoder.apidocs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("platform.api-docs")
public class ApiDocsCatalogProperties {
    private long cacheSeconds = 60;
    private int connectTimeoutMs = 3000;
    private int readTimeoutMs = 10000;
    private String targetsJson = "[]";

    public long getCacheSeconds() {
        return cacheSeconds;
    }

    public void setCacheSeconds(long cacheSeconds) {
        this.cacheSeconds = cacheSeconds;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public String getTargetsJson() {
        return targetsJson;
    }

    public void setTargetsJson(String targetsJson) {
        this.targetsJson = targetsJson;
    }
}

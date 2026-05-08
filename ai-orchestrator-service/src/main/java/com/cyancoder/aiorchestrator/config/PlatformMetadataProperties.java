package com.cyancoder.aiorchestrator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "platform.metadata")
public class PlatformMetadataProperties {
    private List<String> serviceKeys = new ArrayList<>();

    public List<String> getServiceKeys() { return serviceKeys; }
    public void setServiceKeys(List<String> serviceKeys) { this.serviceKeys = serviceKeys; }
}


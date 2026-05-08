package com.cyancoder.aiorchestrator.client;

import java.util.Map;

public interface PlatformMetadataClient {
    Map<String, Object> fetchMetadata(String tenantKey, String siteKey);
}


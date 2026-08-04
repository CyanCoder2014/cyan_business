package com.cyancoder.aiorchestrator.client;

import java.util.Map;
import com.cyancoder.aiorchestrator.service.ServiceAvailabilitySnapshot;

public interface PlatformMetadataClient {
    Map<String, Object> fetchMetadata(String tenantKey, String siteKey,
                                      ServiceAvailabilitySnapshot availability);
}

package com.cyancoder.aiorchestrator.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeliveryBlueprintTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void normalizesObjectShapedApiEntriesFromProviderResponses() throws Exception {
        PlatformAppDslDefinition dsl = objectMapper.readValue("""
                {
                  "app": {
                    "appKey": "request-intake",
                    "title": "Request Intake",
                    "type": "BPM",
                    "tenantKey": "tenant-demo",
                    "siteKey": "site-commerce"
                  },
                  "entities": [],
                  "routes": [],
                  "flows": [],
                  "delivery": {
                    "publicApis": [
                      {
                        "method": "get",
                        "path": "/endpoint/bpm/flows",
                        "description": "List flow definitions"
                      },
                      "/public/storefront/render?path=/"
                    ],
                    "botApis": [
                      {
                        "endpoint": "/endpoint/ai-orchestrator/drafts"
                      }
                    ]
                  },
                  "manualActions": []
                }
                """, PlatformAppDslDefinition.class);

        assertEquals("/endpoint/bpm/flows", dsl.getDelivery().getPublicApis().get(0));
        assertEquals("/public/storefront/render?path=/", dsl.getDelivery().getPublicApis().get(1));
        assertEquals("/endpoint/ai-orchestrator/drafts", dsl.getDelivery().getBotApis().get(0));
    }
}

package com.cyancoder.aiorchestrator.client.impl;

import com.cyancoder.aiorchestrator.config.AiProvider;
import com.cyancoder.aiorchestrator.config.LlmProperties;
import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GapGptLiveSmokeTest {

    @Test
    void parsesLiveGapGptResponseWhenApiKeyIsProvided() {
        String apiKey = System.getenv("GAPGPT_API_KEY");
        Assumptions.assumeTrue(apiKey != null && !apiKey.isBlank(), "GAPGPT_API_KEY is required for live smoke test");

        LlmProperties properties = new LlmProperties();
        properties.setMaxParseAttempts(2);
        LlmProperties.ProviderProperties gapgpt = properties.getGapgpt();
        gapgpt.setApiKey(apiKey);
        gapgpt.setBaseUrl("https://api.gapgpt.app");
        gapgpt.setCompletionsPath("/v1/chat/completions");
        gapgpt.setModel("gpt-4o");

        OpenAiCompatibleLlmClient client = new OpenAiCompatibleLlmClient(
                AiProvider.GAPGPT,
                properties,
                gapgpt,
                new ObjectMapper()
        );

        PlatformAppDslDefinition dsl = client.generateDsl("""
                Return a minimal valid PlatformAppDslDefinition for a combined shop and crm app.
                Requirements:
                - app.appKey = "gapgpt-shop-crm-live"
                - app.title = "GapGPT Shop CRM Live"
                - app.type = "SHOP"
                - app.tenantKey = "tenant-live"
                - app.siteKey = "site-live"
                - app.capabilities = ["website", "shop", "crm"]
                - entities = []
                - routes = []
                - flows = []
                - delivery.publicApis = ["/public/storefront/render?path=/"]
                - delivery.botApis = ["/endpoint/ai-orchestrator/drafts"]
                - manualActions = []
                Return JSON only.
                """);

        assertNotNull(dsl);
        assertEquals("gapgpt-shop-crm-live", dsl.getApp().getAppKey());
        assertEquals("tenant-live", dsl.getApp().getTenantKey());
        assertEquals("site-live", dsl.getApp().getSiteKey());
        assertEquals(1, dsl.getDelivery().getPublicApis().size());
        assertEquals(3, dsl.getApp().getCapabilities().size());
    }
}

package com.cyancoder.aiorchestrator.client.impl;

import com.cyancoder.aiorchestrator.config.AiProvider;
import com.cyancoder.aiorchestrator.config.LlmProperties;
import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenRouterLiveSmokeTest {

    @Test
    void parsesLiveOpenRouterResponseWhenApiKeyIsProvided() {
        String apiKey = System.getenv("OPENROUTER_API_KEY");
        Assumptions.assumeTrue(apiKey != null && !apiKey.isBlank(), "OPENROUTER_API_KEY is required for live smoke test");

        LlmProperties properties = new LlmProperties();
        properties.setMaxParseAttempts(2);
        LlmProperties.ProviderProperties openrouter = properties.getOpenrouter();
        openrouter.setApiKey(apiKey);
        openrouter.setBaseUrl(System.getenv().getOrDefault("OPENROUTER_BASE_URL", "https://openrouter.ai"));
        openrouter.setCompletionsPath(System.getenv().getOrDefault("OPENROUTER_COMPLETIONS_PATH", "/api/v1/chat/completions"));
        openrouter.setModel(System.getenv().getOrDefault("OPENROUTER_MODEL", "openrouter/free"));
        openrouter.setReferer(System.getenv("OPENROUTER_HTTP_REFERER"));
        openrouter.setTitle(System.getenv().getOrDefault("OPENROUTER_APP_TITLE", "ai-orchestrator-service"));

        OpenAiCompatibleLlmClient client = new OpenAiCompatibleLlmClient(
                AiProvider.OPENROUTER,
                properties,
                openrouter,
                new ObjectMapper()
        );

        PlatformAppDslDefinition dsl = client.generateDsl("""
                Return a minimal valid PlatformAppDslDefinition for a combined shop and crm app.
                Requirements:
                - app.appKey = "openrouter-shop-crm-live"
                - app.title = "OpenRouter Shop CRM Live"
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
        assertEquals("openrouter-shop-crm-live", dsl.getApp().getAppKey());
        assertEquals("tenant-live", dsl.getApp().getTenantKey());
        assertEquals("site-live", dsl.getApp().getSiteKey());
        assertTrue(dsl.getApp().getCapabilities().contains("shop"));
        assertTrue(dsl.getApp().getCapabilities().contains("crm"));
    }
}

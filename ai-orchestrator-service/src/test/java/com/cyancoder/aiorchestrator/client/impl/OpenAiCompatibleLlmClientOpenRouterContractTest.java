package com.cyancoder.aiorchestrator.client.impl;

import com.cyancoder.aiorchestrator.config.AiProvider;
import com.cyancoder.aiorchestrator.config.LlmProperties;
import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenAiCompatibleLlmClientOpenRouterContractTest {

    @Test
    void openRouterCompatibleChatCompletionResponseParsesIntoPlatformDsl() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setBody("""
                            {
                              "choices": [
                                {
                                  "message": {
                                    "content": "{\\"app\\":{\\"appKey\\":\\"shop-crm-studio\\",\\"title\\":\\"Shop CRM Studio\\",\\"type\\":\\"SHOP\\",\\"tenantKey\\":\\"tenant-shop\\",\\"siteKey\\":\\"site-shop\\",\\"capabilities\\":[\\"website\\",\\"shop\\",\\"crm\\"]},\\"entities\\":[],\\"routes\\":[],\\"flows\\":[],\\"delivery\\":{\\"publicApis\\":[\\"/public/storefront/render?path=/\\"],\\"botApis\\":[\\"/endpoint/ai-orchestrator/drafts\\"]},\\"manualActions\\":[]}"
                                  }
                                }
                              ]
                            }
                            """));
            server.start();

            LlmProperties properties = new LlmProperties();
            properties.setMaxParseAttempts(1);
            LlmProperties.ProviderProperties openrouter = properties.getOpenrouter();
            openrouter.setApiKey("openrouter-test-key");
            openrouter.setBaseUrl(server.url("/").toString().replaceAll("/$", ""));
            openrouter.setCompletionsPath("/api/v1/chat/completions");
            openrouter.setModel("openrouter/free");
            openrouter.setReferer("https://naviya.local");
            openrouter.setTitle("ai-orchestrator-service");

            OpenAiCompatibleLlmClient client = new OpenAiCompatibleLlmClient(
                    AiProvider.OPENROUTER,
                    properties,
                    openrouter,
                    new ObjectMapper()
            );

            PlatformAppDslDefinition dsl = client.generateDsl("Generate a shop and crm app");
            RecordedRequest request = server.takeRequest();

            assertEquals("/api/v1/chat/completions", request.getPath());
            assertEquals("Bearer openrouter-test-key", request.getHeader("Authorization"));
            assertEquals("https://naviya.local", request.getHeader("HTTP-Referer"));
            assertEquals("ai-orchestrator-service", request.getHeader("X-OpenRouter-Title"));
            assertTrue(request.getBody().readUtf8().contains("\"model\":\"openrouter/free\""));
            assertEquals("shop-crm-studio", dsl.getApp().getAppKey());
            assertEquals("tenant-shop", dsl.getApp().getTenantKey());
            assertEquals("site-shop", dsl.getApp().getSiteKey());
            assertEquals(3, dsl.getApp().getCapabilities().size());
        }
    }
}

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

class OpenAiCompatibleLlmClientGapGptContractTest {

    @Test
    void gapGptCompatibleChatCompletionResponseParsesIntoPlatformDsl() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setBody("""
                            {
                              "choices": [
                                {
                                  "message": {
                                    "content": "{\\"app\\":{\\"appKey\\":\\"spiffy-shop\\",\\"title\\":\\"Spiffy Shop\\",\\"type\\":\\"SHOP\\",\\"tenantKey\\":\\"tenant-spiffy\\",\\"siteKey\\":\\"site-spiffy\\",\\"capabilities\\":[\\"website\\",\\"shop\\",\\"crm\\"]},\\"entities\\":[],\\"routes\\":[],\\"flows\\":[],\\"delivery\\":{\\"publicApis\\":[\\"/public/storefront/render?path=/\\"],\\"botApis\\":[\\"/endpoint/ai-orchestrator/drafts\\"]},\\"manualActions\\":[]}"
                                  }
                                }
                              ]
                            }
                            """));
            server.start();

            LlmProperties properties = new LlmProperties();
            properties.setMaxParseAttempts(1);
            LlmProperties.ProviderProperties gapgpt = properties.getGapgpt();
            gapgpt.setApiKey("gapgpt-test-key");
            gapgpt.setBaseUrl(server.url("/").toString().replaceAll("/$", ""));
            gapgpt.setCompletionsPath("/v1/chat/completions");
            gapgpt.setModel("gpt-4o");

            OpenAiCompatibleLlmClient client = new OpenAiCompatibleLlmClient(
                    AiProvider.GAPGPT,
                    properties,
                    gapgpt,
                    new ObjectMapper()
            );

            PlatformAppDslDefinition dsl = client.generateDsl("Generate a shop app");
            RecordedRequest request = server.takeRequest();

            assertEquals("/v1/chat/completions", request.getPath());
            assertEquals("Bearer gapgpt-test-key", request.getHeader("Authorization"));
            assertTrue(request.getBody().readUtf8().contains("\"model\":\"gpt-4o\""));
            assertEquals("spiffy-shop", dsl.getApp().getAppKey());
            assertEquals("tenant-spiffy", dsl.getApp().getTenantKey());
            assertEquals("site-spiffy", dsl.getApp().getSiteKey());
            assertEquals(3, dsl.getApp().getCapabilities().size());
        }
    }
}

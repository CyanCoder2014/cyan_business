package com.cyancoder.botadapter.service;

import com.cyancoder.botadapter.config.AiOrchestratorProperties;
import com.cyancoder.botadapter.domain.BotChannel;
import com.cyancoder.botadapter.domain.BotProcessBinding;
import com.cyancoder.botadapter.domain.BotProcessTargetType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BotProcessClientTest {
    @Test
    void dispatchesTelegramMessageToAutomationWithStableIdempotency() {
        RestTemplate rest = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(rest).build();
        AiOrchestratorProperties properties = new AiOrchestratorProperties();
        BotProcessClient client = new BotProcessClient(rest, properties);
        BotProcessBinding binding = binding(BotProcessTargetType.AUTOMATION, "support-flow");
        server.expect(once(), requestTo("http://localhost:9120/internal/automation-orchestrator/executions/start"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Tenant-Key", "tenant-a"))
                .andExpect(header("X-Site-Key", "site-a"))
                .andExpect(jsonPath("$.idempotencyKey").value("bot:inbound-1:binding-1"))
                .andExpect(jsonPath("$.input.channel").value("TELEGRAM"))
                .andRespond(withSuccess("{\"executionId\":\"execution-1\"}", MediaType.APPLICATION_JSON));
        assertEquals("execution-1", client.dispatch(binding, "inbound-1", "message-1", "chat-1", "Run", "TELEGRAM", "support-bot"));
        server.verify();
    }

    @Test
    void dispatchesBaleMessageToBpmWithExternalBotActor() {
        RestTemplate rest = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(rest).build();
        BotProcessClient client = new BotProcessClient(rest, new AiOrchestratorProperties());
        BotProcessBinding binding = binding(BotProcessTargetType.BPM, "case-flow");
        server.expect(requestTo("http://localhost:9119/internal/bpm/managed-objects"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Actor-User", "bot:bale:chat-9"))
                .andExpect(header("X-Actor-Roles", "BOT_EXTERNAL"))
                .andExpect(jsonPath("$.flowKey").value("case-flow"))
                .andRespond(withSuccess("{\"id\":\"object-1\"}", MediaType.APPLICATION_JSON));
        assertEquals("object-1", client.dispatch(binding, "inbound-9", "message-9", "chat-9", "Start", "BALE", "case-bot"));
        server.verify();
    }

    private BotProcessBinding binding(BotProcessTargetType type, String target) {
        BotProcessBinding value = new BotProcessBinding(); value.setId("binding-1"); value.setBindingKey("binding"); value.setChannel(BotChannel.TELEGRAM); value.setIntegrationKey("support-bot"); value.setTenantKey("tenant-a"); value.setSiteKey("site-a"); value.setTargetType(type); value.setTargetKey(target); return value;
    }
}

package com.cyancoder.aiorchestrator.service;

import com.cyancoder.aiorchestrator.api.dto.AiOperationRequest;
import com.cyancoder.aiorchestrator.client.LlmClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AiOperationServiceTest {
    @Test void transformsDataThroughConfiguredLlmClient() {
        LlmClient llm = mock(LlmClient.class);
        when(llm.generateContent(anyString())).thenReturn("{\"title\":\"Normalized\"}");
        AiOperationService service = new AiOperationService(llm, new ObjectMapper());

        var response = service.execute(new AiOperationRequest(
                AiOperationRequest.AiOperationType.TRANSFORM_DATA,
                "Normalize the title", Map.of("title", " normalized "), Map.of(), "en"));

        assertEquals("COMPLETED", response.status());
        assertEquals("Normalized", ((Map<?, ?>) response.output()).get("title"));
        verify(llm, times(1)).generateContent(contains("Input JSON"));
    }
}

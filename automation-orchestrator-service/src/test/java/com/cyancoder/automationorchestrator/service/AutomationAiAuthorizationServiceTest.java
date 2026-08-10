package com.cyancoder.automationorchestrator.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AutomationAiAuthorizationServiceTest {
    @Test
    void allowsExecutionOnlyWhenBothPermissionsAndCapabilityAreEffective() {
        InternalServiceHttpSupport http = mock(InternalServiceHttpSupport.class);
        when(http.internalHeaders(eq("tenant-service"), eq("acme"), eq("shop"))).thenReturn(new HttpHeaders());
        when(http.exchange(eq("tenant-service"), contains("effective-access"), any(), isNull(), any(), eq(Map.class)))
                .thenReturn(Map.of("permissions", List.of("automation.execute", "ai.execute")));
        when(http.exchange(eq("tenant-service"), contains("capabilities"), any(), isNull(), any(), eq(List.class)))
                .thenReturn(List.of(Map.of("key", "ai-orchestrator", "enabled", true, "status", "AVAILABLE")));

        assertDoesNotThrow(() -> new AutomationAiAuthorizationService(http).requireExecution("acme", "shop", "head"));
    }

    @Test
    void rejectsExecutionWithoutAiPermission() {
        InternalServiceHttpSupport http = mock(InternalServiceHttpSupport.class);
        when(http.internalHeaders(anyString(), anyString(), nullable(String.class))).thenReturn(new HttpHeaders());
        when(http.exchange(eq("tenant-service"), contains("effective-access"), any(), isNull(), any(), eq(Map.class)))
                .thenReturn(Map.of("permissions", List.of("automation.execute")));

        AccessDeniedException error = assertThrows(AccessDeniedException.class,
                () -> new AutomationAiAuthorizationService(http).requireExecution("acme", null, "operator"));
        assertEquals("AI_PERMISSION_DENIED", error.getMessage());
    }

    @Test
    void rejectsExecutionWhenAiCapabilityIsUnavailable() {
        InternalServiceHttpSupport http = mock(InternalServiceHttpSupport.class);
        when(http.internalHeaders(anyString(), anyString(), nullable(String.class))).thenReturn(new HttpHeaders());
        when(http.exchange(eq("tenant-service"), contains("effective-access"), any(), isNull(), any(), eq(Map.class)))
                .thenReturn(Map.of("permissions", List.of("*")));
        when(http.exchange(eq("tenant-service"), contains("capabilities"), any(), isNull(), any(), eq(List.class)))
                .thenReturn(List.of(Map.of("key", "ai-orchestrator", "enabled", true, "status", "UNAVAILABLE")));

        AccessDeniedException error = assertThrows(AccessDeniedException.class,
                () -> new AutomationAiAuthorizationService(http).requireExecution("acme", null, "owner"));
        assertEquals("AI_CAPABILITY_DISABLED", error.getMessage());
    }
}

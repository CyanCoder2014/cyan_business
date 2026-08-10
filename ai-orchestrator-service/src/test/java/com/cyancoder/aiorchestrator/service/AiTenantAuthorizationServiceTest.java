package com.cyancoder.aiorchestrator.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.cyancoder.aiorchestrator.client.impl.InternalServiceHttpSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class AiTenantAuthorizationServiceTest {
    @Mock InternalServiceHttpSupport http;

    @Test
    void permitsPersistedAiExecutionWhenCapabilityIsAvailable() {
        when(http.get(eq("tenant-service"), anyString(), eq("tenant-a"), eq("site-a")))
                .thenReturn("{\"permissions\":[\"ai.execute\"]}")
                .thenReturn("[{\"key\":\"ai-orchestrator\",\"enabled\":true,\"status\":\"AVAILABLE\"}]");
        var service = new AiTenantAuthorizationService(http, new ObjectMapper());
        assertDoesNotThrow(() -> service.requireExecute("tenant-a", "site-a", "operator@example.com"));
    }

    @Test
    void deniesExecutionWithoutPersistedPermission() {
        when(http.get(eq("tenant-service"), anyString(), eq("tenant-a"), eq("site-a")))
                .thenReturn("{\"permissions\":[\"ai.read\"]}");
        var service = new AiTenantAuthorizationService(http, new ObjectMapper());
        assertThrows(AccessDeniedException.class,
                () -> service.requireExecute("tenant-a", "site-a", "viewer@example.com"));
    }

    @Test
    void deniesReadWhenAiCapabilityIsUnavailable() {
        when(http.get(eq("tenant-service"), anyString(), eq("tenant-a"), eq("site-a")))
                .thenReturn("{\"permissions\":[\"ai.read\"]}")
                .thenReturn("[{\"key\":\"ai-orchestrator\",\"enabled\":false,\"status\":\"NOT_CONFIGURED\"}]");
        var service = new AiTenantAuthorizationService(http, new ObjectMapper());
        assertThrows(AccessDeniedException.class,
                () -> service.requireRead("tenant-a", "site-a", "viewer@example.com"));
    }
}

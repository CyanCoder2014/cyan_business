package com.cyancoder.dynamiccore.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatformAuthorizationServiceTest {
    private final PlatformAuthorizationService authorization = new PlatformAuthorizationService();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void definitionManagerCannotMutateRecordsWithoutRecordPermission() {
        authenticate("definition.manage");

        assertTrue(authorization.canManageDefinitions("content-service"));
        assertTrue(authorization.canReadDefinitions("content-service"));
        assertFalse(authorization.canWriteRecords("content-service"));
    }

    @Test
    void recordManagerCannotMutateDefinitionsWithoutDefinitionPermission() {
        authenticate("record.manage");

        assertTrue(authorization.canWriteRecords("content-service"));
        assertTrue(authorization.canReadRecords("content-service"));
        assertFalse(authorization.canManageDefinitions("content-service"));
    }

    @Test
    void serviceSpecificManagePermissionRetainsLegacyAccess() {
        authenticate("service:content-service:manage");

        assertTrue(authorization.canManageDefinitions("content-service"));
        assertTrue(authorization.canWriteRecords("content-service"));
        assertFalse(authorization.canWriteRecords("crm-service"));
    }

    private void authenticate(String permission) {
        Jwt jwt = new Jwt("test-token", Instant.now(), Instant.now().plusSeconds(300),
                Map.of("alg", "none"),
                Map.of("sub", "test-user", "realm_access", Map.of("permissions", List.of(permission))));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }
}

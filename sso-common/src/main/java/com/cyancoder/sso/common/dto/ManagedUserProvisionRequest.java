package com.cyancoder.sso.common.dto;

import java.util.List;

public record ManagedUserProvisionRequest(
        String username,
        String password,
        String email,
        String phoneNumber,
        boolean mfaEnabled,
        String realmKey,
        String clientId,
        List<String> realmRoles,
        List<String> clientRoles
) {
}

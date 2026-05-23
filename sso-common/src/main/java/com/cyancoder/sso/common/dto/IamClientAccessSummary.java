package com.cyancoder.sso.common.dto;

import java.util.List;

public record IamClientAccessSummary(
        String clientId,
        String realmKey,
        List<String> clientRoles,
        List<String> clientPermissions
) {
}

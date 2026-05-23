package com.cyancoder.sso.common.dto;

import java.util.List;

public record IamUserAccessSummary(
        String username,
        String realmKey,
        List<String> realmRoles,
        List<String> realmPermissions,
        List<IamClientAccessSummary> clients
) {
}

package com.cyancoder.sso.common.dto;

import java.util.List;

public record ClientSummary(
        String clientId,
        String realmKey,
        String displayName,
        String description,
        boolean active,
        boolean publicClient,
        List<String> redirectUris
) {
}

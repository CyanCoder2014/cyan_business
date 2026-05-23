package com.cyancoder.sso.common.dto;

import java.util.List;

public record RoleCatalogSummary(
        String scopeType,
        String scopeKey,
        String roleKey,
        String displayName,
        String description,
        boolean active,
        List<String> permissions
) {
}

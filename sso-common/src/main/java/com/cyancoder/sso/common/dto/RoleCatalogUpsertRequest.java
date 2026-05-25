package com.cyancoder.sso.common.dto;

import java.util.List;

public record RoleCatalogUpsertRequest(
        String scopeType,
        String scopeKey,
        String roleKey,
        String displayName,
        String description,
        boolean active,
        List<String> permissions
) {
}

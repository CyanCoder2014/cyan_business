package com.cyancoder.sso.common.dto;

public record RealmSummary(
        String realmKey,
        String displayName,
        String description,
        boolean active
) {
}

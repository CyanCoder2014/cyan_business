package com.cyancoder.sso.common.dto;

public record RealmUpsertRequest(
        String realmKey,
        String displayName,
        String description,
        boolean active
) {
}

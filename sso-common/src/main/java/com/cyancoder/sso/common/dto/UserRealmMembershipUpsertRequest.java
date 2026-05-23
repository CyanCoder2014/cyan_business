package com.cyancoder.sso.common.dto;

public record UserRealmMembershipUpsertRequest(
        String username,
        String realmKey,
        boolean active,
        boolean defaultRealm
) {
}

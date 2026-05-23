package com.cyancoder.sso.common.dto;

public record UserRealmMembershipSummary(
        String username,
        String realmKey,
        boolean active,
        boolean defaultRealm
) {
}

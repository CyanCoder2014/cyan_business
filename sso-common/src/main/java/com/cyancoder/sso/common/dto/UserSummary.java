package com.cyancoder.sso.common.dto;

import java.time.Instant;
import java.util.List;

public record UserSummary(
        String username,
        String email,
        String phoneNumber,
        boolean mfaEnabled,
        List<String> roles,
        boolean active,
        /**
         * When the password last changed. Sessions issued before this are no
         * longer trustworthy, so the refresh path refuses to extend them.
         */
        Instant credentialsChangedAt
) {
    public UserSummary(String username, String email, String phoneNumber, boolean mfaEnabled, List<String> roles, boolean active) {
        this(username, email, phoneNumber, mfaEnabled, roles, active, null);
    }
}

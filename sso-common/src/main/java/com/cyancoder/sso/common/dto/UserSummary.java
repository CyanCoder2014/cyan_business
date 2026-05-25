package com.cyancoder.sso.common.dto;

import java.util.List;

public record UserSummary(
        String username,
        String email,
        String phoneNumber,
        boolean mfaEnabled,
        List<String> roles,
        boolean active
) {
}

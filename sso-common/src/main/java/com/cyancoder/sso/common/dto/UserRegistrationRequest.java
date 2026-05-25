package com.cyancoder.sso.common.dto;

import java.util.List;

public record UserRegistrationRequest(
        String username,
        String password,
        String email,
        String phoneNumber,
        boolean mfaEnabled,
        List<String> roles
) {
}

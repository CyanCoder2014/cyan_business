package com.cyancoder.sso.common.dto;

public record PasswordVerificationResponse(
        boolean valid,
        UserSummary user
) {
}

package com.cyancoder.sso.common.dto;

public record PasswordVerificationRequest(
        String username,
        String password
) {
}

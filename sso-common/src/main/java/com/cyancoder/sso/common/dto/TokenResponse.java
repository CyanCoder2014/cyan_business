package com.cyancoder.sso.common.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        String sessionId
) {
}

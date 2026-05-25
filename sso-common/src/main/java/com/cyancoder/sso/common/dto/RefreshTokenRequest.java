package com.cyancoder.sso.common.dto;

public record RefreshTokenRequest(
        String clientId,
        String refreshToken
) {
}

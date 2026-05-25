package com.cyancoder.sso.common.dto;

public record LoginRequest(
        String clientId,
        String username,
        String password,
        String captchaChallengeId,
        String captchaAnswer,
        String otpCode,
        String deviceId
) {
}

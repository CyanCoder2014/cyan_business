package com.cyancoder.sso.common.dto;

public record CaptchaVerifyRequest(
        String challengeId,
        String answer,
        String clientId
) {
}

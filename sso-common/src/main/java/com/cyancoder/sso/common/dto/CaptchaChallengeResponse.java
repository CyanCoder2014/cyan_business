package com.cyancoder.sso.common.dto;

public record CaptchaChallengeResponse(
        String challengeId,
        String prompt,
        long expiresAtEpochSecond
) {
}

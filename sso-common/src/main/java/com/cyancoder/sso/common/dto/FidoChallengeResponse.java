package com.cyancoder.sso.common.dto;

public record FidoChallengeResponse(
        String challengeId,
        String challenge,
        long expiresAtEpochSecond
) {
}

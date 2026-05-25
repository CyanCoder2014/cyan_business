package com.cyancoder.sso.common.dto;

public record FidoVerifyRequest(
        String challengeId,
        String username,
        String clientId,
        String signedChallenge
) {
}

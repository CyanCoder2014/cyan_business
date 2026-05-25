package com.cyancoder.sso.common.dto;

public record FidoChallengeRequest(
        String username,
        String clientId,
        String deviceId
) {
}

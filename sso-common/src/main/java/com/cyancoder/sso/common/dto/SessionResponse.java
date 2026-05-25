package com.cyancoder.sso.common.dto;

public record SessionResponse(
        String sessionId,
        String username,
        String clientId,
        String deviceId,
        boolean active,
        long issuedAtEpochSecond,
        long expiresAtEpochSecond
) {
}

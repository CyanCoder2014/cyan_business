package com.cyancoder.sso.common.dto;

public record SessionCreateRequest(
        String username,
        String clientId,
        String deviceId
) {
}

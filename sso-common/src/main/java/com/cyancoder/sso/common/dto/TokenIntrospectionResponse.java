package com.cyancoder.sso.common.dto;

public record TokenIntrospectionResponse(
        boolean active,
        String subject,
        String clientId,
        String sessionId,
        long exp
) {
}

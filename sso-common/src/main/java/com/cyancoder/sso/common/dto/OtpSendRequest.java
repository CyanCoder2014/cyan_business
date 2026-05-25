package com.cyancoder.sso.common.dto;

public record OtpSendRequest(
        String username,
        String clientId,
        String purpose
) {
}

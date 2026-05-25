package com.cyancoder.sso.common.dto;

public record OtpVerifyRequest(
        String username,
        String clientId,
        String code,
        String purpose
) {
}

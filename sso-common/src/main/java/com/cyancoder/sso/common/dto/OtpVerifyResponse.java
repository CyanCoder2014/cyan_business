package com.cyancoder.sso.common.dto;

public record OtpVerifyResponse(
        boolean success,
        String message
) {
}

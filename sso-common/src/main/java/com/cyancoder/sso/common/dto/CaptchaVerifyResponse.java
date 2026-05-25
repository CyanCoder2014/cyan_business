package com.cyancoder.sso.common.dto;

public record CaptchaVerifyResponse(
        boolean success,
        String message
) {
}

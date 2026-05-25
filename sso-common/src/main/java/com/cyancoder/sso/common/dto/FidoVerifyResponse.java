package com.cyancoder.sso.common.dto;

public record FidoVerifyResponse(
        boolean success,
        String message
) {
}

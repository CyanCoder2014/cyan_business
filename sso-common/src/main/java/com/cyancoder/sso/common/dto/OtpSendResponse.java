package com.cyancoder.sso.common.dto;

public record OtpSendResponse(
        String codeId,
        boolean sent,
        String deliveryTarget,
        String devCode
) {
}

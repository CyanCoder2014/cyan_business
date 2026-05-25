package com.cyancoder.payment.dto;

import com.cyancoder.payment.domain.PaymentProviderCode;
import com.cyancoder.payment.domain.PaymentTransactionStatus;

import java.util.Map;

public record PaymentCallbackResponse(
        String transactionKey,
        PaymentProviderCode providerCode,
        PaymentTransactionStatus status,
        boolean successful,
        String message,
        String redirectUrl,
        Map<String, String> payload
) {
}

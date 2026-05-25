package com.cyancoder.payment.dto;

import com.cyancoder.payment.domain.PaymentProviderCode;
import com.cyancoder.payment.domain.PaymentTransactionStatus;

import java.util.Map;

public record PaymentInitiationResponse(
        String transactionKey,
        PaymentTransactionStatus status,
        String paymentMethodKey,
        PaymentProviderCode providerCode,
        String paymentUrl,
        String clientToken,
        String providerReferenceId,
        String externalPaymentId,
        String httpMethod,
        Map<String, String> formFields,
        String publicCallbackUrl
) {
}

package com.cyancoder.payment.provider;

import java.util.Map;

public record PaymentProviderInitResult(
        String paymentUrl,
        String clientToken,
        String providerReferenceId,
        String externalPaymentId,
        String paymentToken,
        String httpMethod,
        Map<String, String> formFields,
        String providerResponseMessage
) {
}

package com.cyancoder.payment.provider;

public record PaymentProviderVerificationResult(
        boolean successful,
        String providerReferenceId,
        String externalPaymentId,
        String paymentToken,
        String maskedCardNumber,
        String message
) {
}

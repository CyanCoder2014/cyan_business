package com.cyancoder.checkout.model;

public record PaymentOrchestratorVerificationResponse(
        String transactionKey,
        String status,
        String paymentMethodKey,
        String providerCode,
        String verificationMessage
) {
}

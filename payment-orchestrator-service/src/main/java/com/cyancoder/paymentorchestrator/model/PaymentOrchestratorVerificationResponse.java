package com.cyancoder.paymentorchestrator.model;

public record PaymentOrchestratorVerificationResponse(
        String transactionKey,
        String status,
        String paymentMethodKey,
        String providerCode,
        String verificationMessage
) {
}

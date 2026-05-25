package com.cyancoder.checkout.model;

public record PaymentOrchestratorInitiationResponse(
        String transactionKey,
        String status,
        String paymentMethodKey,
        String providerCode,
        String paymentUrl,
        String clientToken,
        String providerReferenceId,
        String externalPaymentId,
        String publicCallbackUrl
) {
}

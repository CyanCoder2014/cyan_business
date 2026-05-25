package com.cyancoder.paymentorchestrator.model;

import java.math.BigDecimal;
import java.util.Map;

public record PaymentOrchestratorInitiationRequest(
        String paymentMethodKey,
        String orderKey,
        String invoiceKey,
        String customerKey,
        String relatedService,
        String relatedEntityType,
        String relatedEntityKey,
        BigDecimal amount,
        String currency,
        String description,
        String callbackUrl,
        String successUrl,
        String failureUrl,
        Map<String, String> metaData
) {
}

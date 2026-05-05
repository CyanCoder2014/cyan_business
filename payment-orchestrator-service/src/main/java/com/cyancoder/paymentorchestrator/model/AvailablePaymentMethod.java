package com.cyancoder.paymentorchestrator.model;

import java.util.Set;

public record AvailablePaymentMethod(
        String methodKey,
        String displayName,
        String providerCode,
        String region,
        String flowType,
        int priorityOrder,
        Set<String> supportedCurrencies,
        String description
) {
}

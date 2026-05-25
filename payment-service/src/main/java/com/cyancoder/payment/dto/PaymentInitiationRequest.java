package com.cyancoder.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Map;

public record PaymentInitiationRequest(
        @NotBlank String paymentMethodKey,
        String orderKey,
        String invoiceKey,
        String customerKey,
        String relatedService,
        String relatedEntityType,
        String relatedEntityKey,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank String currency,
        String description,
        String callbackUrl,
        String successUrl,
        String failureUrl,
        Map<String, String> metaData
) {
}

package com.cyancoder.payment.dto;

import com.cyancoder.payment.domain.PaymentFlowType;
import com.cyancoder.payment.domain.PaymentProviderCode;
import com.cyancoder.payment.domain.PaymentRegion;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

public record PaymentMethodAdminResponse(
        String methodKey,
        String displayName,
        PaymentProviderCode providerCode,
        PaymentRegion region,
        PaymentFlowType flowType,
        boolean enabled,
        boolean active,
        int priorityOrder,
        Set<String> supportedCurrencies,
        Map<String, Object> configuration,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}

package com.cyancoder.payment.dto;

import com.cyancoder.payment.domain.PaymentFlowType;
import com.cyancoder.payment.domain.PaymentProviderCode;
import com.cyancoder.payment.domain.PaymentRegion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.Set;

public record PaymentMethodRequest(
        @NotBlank String methodKey,
        @NotBlank String displayName,
        @NotNull PaymentProviderCode providerCode,
        @NotNull PaymentRegion region,
        @NotNull PaymentFlowType flowType,
        boolean enabled,
        boolean active,
        int priorityOrder,
        @NotEmpty Set<String> supportedCurrencies,
        @NotNull Map<String, Object> configuration,
        String description
) {
}

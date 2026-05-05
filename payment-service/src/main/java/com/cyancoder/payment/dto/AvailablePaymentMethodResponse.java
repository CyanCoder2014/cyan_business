package com.cyancoder.payment.dto;

import com.cyancoder.payment.domain.PaymentFlowType;
import com.cyancoder.payment.domain.PaymentProviderCode;
import com.cyancoder.payment.domain.PaymentRegion;

import java.util.Set;

public record AvailablePaymentMethodResponse(
        String methodKey,
        String displayName,
        PaymentProviderCode providerCode,
        PaymentRegion region,
        PaymentFlowType flowType,
        int priorityOrder,
        Set<String> supportedCurrencies,
        String description
) {
}

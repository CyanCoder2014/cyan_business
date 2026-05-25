package com.cyancoder.payment.provider;

import com.cyancoder.payment.entity.PaymentMethodEntity;
import com.cyancoder.payment.entity.PaymentTransactionEntity;

import java.util.Map;

public record PaymentProviderContext(
        PaymentMethodEntity paymentMethod,
        PaymentTransactionEntity transaction,
        Map<String, Object> configuration,
        String publicBaseUrl
) {
}

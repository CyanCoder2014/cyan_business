package com.cyancoder.payment.provider;

import com.cyancoder.payment.entity.PaymentMethodEntity;
import com.cyancoder.payment.entity.PaymentTransactionEntity;

import java.util.Map;

public record PaymentVerificationContext(
        PaymentMethodEntity paymentMethod,
        PaymentTransactionEntity transaction,
        Map<String, Object> configuration,
        Map<String, String> payload
) {
}

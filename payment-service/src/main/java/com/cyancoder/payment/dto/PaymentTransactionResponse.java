package com.cyancoder.payment.dto;

import com.cyancoder.payment.domain.PaymentProviderCode;
import com.cyancoder.payment.domain.PaymentTransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record PaymentTransactionResponse(
        String transactionKey,
        String paymentMethodKey,
        PaymentProviderCode providerCode,
        PaymentTransactionStatus status,
        BigDecimal amount,
        String currency,
        String orderKey,
        String invoiceKey,
        String customerKey,
        String relatedService,
        String relatedEntityType,
        String relatedEntityKey,
        String description,
        String paymentUrl,
        String callbackUrl,
        String successUrl,
        String failureUrl,
        String providerReferenceId,
        String externalPaymentId,
        String paymentToken,
        String maskedCardNumber,
        String verificationMessage,
        Map<String, String> metaData,
        Instant createdAt,
        Instant updatedAt
) {
}

package com.cyancoder.payment.domain;

public enum PaymentTransactionStatus {
    CREATED,
    INITIATED,
    PENDING_CALLBACK,
    VERIFIED,
    FAILED,
    CANCELLED,
    EXPIRED
}

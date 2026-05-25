package com.cyancoder.checkout.model;

import java.util.Map;

public record CheckoutPaymentVerificationRequest(
        String transactionKey,
        Map<String, String> payload
) {
}

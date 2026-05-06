package com.cyancoder.checkout.model;

import java.util.Map;

public record PaymentOrchestratorVerificationRequest(
        Map<String, String> payload
) {
}

package com.cyancoder.paymentorchestrator.model;

import java.util.Map;

public record PaymentOrchestratorVerificationRequest(
        Map<String, String> payload
) {
}

package com.cyancoder.checkout.model;

import java.util.List;
import java.util.Map;

public record CheckoutPreparationSnapshot(
        Map<String, Object> checkout,
        Map<String, Object> cart,
        PricingEvaluationResponse pricing,
        List<AvailablePaymentMethod> paymentMethods
) {
}

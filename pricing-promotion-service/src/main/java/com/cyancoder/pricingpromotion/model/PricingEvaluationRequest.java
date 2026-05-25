package com.cyancoder.pricingpromotion.model;

import java.util.List;
import java.util.Map;

public record PricingEvaluationRequest(
        String currency,
        List<Map<String, Object>> items,
        List<String> promotionCodes,
        Map<String, Object> customer,
        Map<String, Object> shippingAddress
) {
}

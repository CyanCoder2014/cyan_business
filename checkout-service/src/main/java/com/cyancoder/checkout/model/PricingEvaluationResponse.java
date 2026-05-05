package com.cyancoder.checkout.model;

import java.util.List;
import java.util.Map;

public record PricingEvaluationResponse(
        String subtotal,
        String discountTotal,
        String taxTotal,
        String grandTotal,
        List<String> appliedPromotionCodes,
        List<Map<String, Object>> appliedTaxRules
) {
}

package com.cyancoder.pricingpromotion.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record PricingEvaluationResponse(
        BigDecimal subtotal,
        BigDecimal discountTotal,
        BigDecimal taxTotal,
        BigDecimal grandTotal,
        List<String> appliedPromotionCodes,
        List<Map<String, Object>> appliedTaxRules
) {
}

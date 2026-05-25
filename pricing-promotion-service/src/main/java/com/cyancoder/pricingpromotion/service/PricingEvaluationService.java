package com.cyancoder.pricingpromotion.service;

import com.cyancoder.dynamiccore.runtime.DynamicRuntimeService;
import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordDocument;
import com.cyancoder.pricingpromotion.model.PricingEvaluationRequest;
import com.cyancoder.pricingpromotion.model.PricingEvaluationResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class PricingEvaluationService {
    private final DynamicRuntimeService dynamicRuntimeService;

    public PricingEvaluationService(DynamicRuntimeService dynamicRuntimeService) {
        this.dynamicRuntimeService = dynamicRuntimeService;
    }

    public PricingEvaluationResponse evaluate(PricingEvaluationRequest request) {
        BigDecimal subtotal = subtotal(request.items());
        BigDecimal discountTotal = evaluateDiscounts(subtotal, request.promotionCodes());
        List<Map<String, Object>> appliedTaxRules = loadActiveTaxRules();
        BigDecimal taxRate = appliedTaxRules.stream()
                .map(rule -> toDecimal(rule.get("rate")))
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxableBase = subtotal.subtract(discountTotal).max(BigDecimal.ZERO);
        BigDecimal taxTotal = taxableBase.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = taxableBase.add(taxTotal);
        return new PricingEvaluationResponse(
                subtotal,
                discountTotal,
                taxTotal,
                grandTotal,
                appliedPromotions(request.promotionCodes()),
                appliedTaxRules
        );
    }

    private BigDecimal subtotal(List<Map<String, Object>> items) {
        if (items == null) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map(this::lineSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal lineSubtotal(Map<String, Object> item) {
        if (item == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal quantity = toDecimal(item.get("quantity"));
        BigDecimal unitPrice = toDecimal(item.get("unitPrice"));
        if (quantity == null || unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return quantity.multiply(unitPrice);
    }

    private BigDecimal evaluateDiscounts(BigDecimal subtotal, List<String> promotionCodes) {
        if (promotionCodes == null || promotionCodes.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        List<DynamicEntityRecordDocument> rules = safeList("promotion-rule");
        for (DynamicEntityRecordDocument rule : rules) {
            Map<String, Object> data = rule.getData();
            if (data == null) {
                continue;
            }
            String code = Objects.toString(data.get("code"), "");
            String status = Objects.toString(data.get("status"), "ACTIVE");
            if (!promotionCodes.contains(code) || !"ACTIVE".equalsIgnoreCase(status)) {
                continue;
            }
            String discountType = Objects.toString(data.get("discountType"), "PERCENTAGE");
            BigDecimal discountValue = toDecimal(data.get("discountValue"));
            if (discountValue == null) {
                continue;
            }
            if ("FIXED_AMOUNT".equalsIgnoreCase(discountType)) {
                total = total.add(discountValue);
            } else if ("PERCENTAGE".equalsIgnoreCase(discountType)) {
                total = total.add(subtotal.multiply(discountValue).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            }
        }
        return total.min(subtotal);
    }

    private List<String> appliedPromotions(List<String> promotionCodes) {
        return promotionCodes == null ? List.of() : promotionCodes;
    }

    private List<Map<String, Object>> loadActiveTaxRules() {
        List<Map<String, Object>> active = new ArrayList<>();
        for (DynamicEntityRecordDocument item : safeList("tax-rule")) {
            Map<String, Object> data = item.getData();
            if (data != null && "ACTIVE".equalsIgnoreCase(Objects.toString(data.get("status"), "ACTIVE"))) {
                active.add(data);
            }
        }
        return active;
    }

    private List<DynamicEntityRecordDocument> safeList(String entityKey) {
        try {
            return dynamicRuntimeService.listRecords(entityKey);
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    private BigDecimal toDecimal(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (Exception ex) {
            return null;
        }
    }
}

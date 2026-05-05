package com.cyancoder.checkout.service;

import com.cyancoder.checkout.model.AvailablePaymentMethod;
import com.cyancoder.checkout.model.CheckoutPreparationSnapshot;
import com.cyancoder.checkout.model.PaymentOrchestratorInitiationRequest;
import com.cyancoder.checkout.model.PaymentOrchestratorInitiationResponse;
import com.cyancoder.checkout.model.PricingEvaluationRequest;
import com.cyancoder.checkout.model.PricingEvaluationResponse;
import com.cyancoder.dynamiccore.runtime.DynamicRuntimeService;
import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordDocument;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CheckoutPreparationService {
    private final DynamicRuntimeService dynamicRuntimeService;
    private final InternalServiceHttpSupport httpSupport;

    public CheckoutPreparationService(DynamicRuntimeService dynamicRuntimeService, InternalServiceHttpSupport httpSupport) {
        this.dynamicRuntimeService = dynamicRuntimeService;
        this.httpSupport = httpSupport;
    }

    public CheckoutPreparationSnapshot snapshot(String entityKey, String recordKey) {
        DynamicEntityRecordDocument checkout = dynamicRuntimeService.getRecord(entityKey, recordKey);
        Map<String, Object> checkoutData = checkout.getData();
        Map<String, Object> cartRef = objectMap(checkoutData.get("cartRef"));
        String cartEntityKey = string(cartRef, "entityKey");
        String cartRecordKey = string(cartRef, "recordKey");
        Map<String, Object> cart = httpSupport.get("cart-service", "/internal/entities/records/" + cartEntityKey + "/" + cartRecordKey, Map.class);
        Map<String, Object> cartData = objectMap(cart.get("data"));
        PricingEvaluationResponse pricing = httpSupport.post("pricing-promotion-service", "/internal/pricing-promotions/evaluate",
                new PricingEvaluationRequest(
                        string(checkoutData, "currency", "IRR"),
                        listOfMaps(cartData.get("items")),
                        promotionCodes(checkoutData, cartData),
                        objectMap(checkoutData.get("customer")),
                        objectMap(checkoutData.get("shippingAddress"))
                ),
                PricingEvaluationResponse.class);
        AvailablePaymentMethod[] methods = httpSupport.get("payment-orchestrator-service", "/internal/payment-orchestrator/methods", AvailablePaymentMethod[].class);
        return new CheckoutPreparationSnapshot(checkoutData, cartData, pricing, methods == null ? List.of() : Arrays.asList(methods));
    }

    public PaymentOrchestratorInitiationResponse initiatePayment(String entityKey, String recordKey, String paymentMethodKey) {
        CheckoutPreparationSnapshot snapshot = snapshot(entityKey, recordKey);
        Map<String, Object> checkout = snapshot.checkout();
        PricingEvaluationResponse pricing = snapshot.pricing();
        BigDecimal grandTotal = new BigDecimal(Objects.toString(pricing.grandTotal(), "0"));
        return httpSupport.post("payment-orchestrator-service", "/internal/payment-orchestrator/sessions/initiate",
                new PaymentOrchestratorInitiationRequest(
                        paymentMethodKey,
                        string(checkout, "orderKey"),
                        string(checkout, "invoiceKey"),
                        objectMap(checkout.get("customer")).get("customerKey") == null ? null : String.valueOf(objectMap(checkout.get("customer")).get("customerKey")),
                        "checkout-service",
                        entityKey,
                        recordKey,
                        grandTotal,
                        string(checkout, "currency", "IRR"),
                        "Checkout payment for " + recordKey,
                        string(checkout, "callbackUrl"),
                        string(checkout, "successUrl"),
                        string(checkout, "failureUrl"),
                        Map.of("checkoutKey", string(checkout, "checkoutKey", recordKey))
                ),
                PaymentOrchestratorInitiationResponse.class);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> objectMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listOfMaps(Object value) {
        return value instanceof List<?> list ? (List<Map<String, Object>>) list : List.of();
    }

    @SuppressWarnings("unchecked")
    private List<String> stringList(Object value) {
        return value instanceof List<?> list ? (List<String>) list : List.of();
    }

    private List<String> promotionCodes(Map<String, Object> checkoutData, Map<String, Object> cartData) {
        List<String> checkoutCodes = stringList(checkoutData.get("promotionCodes"));
        if (!checkoutCodes.isEmpty()) {
            return checkoutCodes;
        }
        return stringList(cartData.get("appliedPromotions"));
    }

    private String string(Map<String, Object> map, String key) {
        return string(map, key, null);
    }

    private String string(Map<String, Object> map, String key, String fallback) {
        Object value = map == null ? null : map.get(key);
        return value == null ? fallback : String.valueOf(value);
    }
}

package com.cyancoder.checkout.service;

import com.cyancoder.checkout.model.AvailablePaymentMethod;
import com.cyancoder.checkout.model.CheckoutPreparationSnapshot;
import com.cyancoder.checkout.model.CheckoutLifecycleAdvanceRequest;
import com.cyancoder.checkout.model.CheckoutPaymentVerificationRequest;
import com.cyancoder.checkout.model.NotificationDispatchRequest;
import com.cyancoder.checkout.model.NotificationDispatchResponse;
import com.cyancoder.checkout.model.PaymentOrchestratorInitiationRequest;
import com.cyancoder.checkout.model.PaymentOrchestratorInitiationResponse;
import com.cyancoder.checkout.model.PaymentOrchestratorVerificationRequest;
import com.cyancoder.checkout.model.PaymentOrchestratorVerificationResponse;
import com.cyancoder.checkout.model.PricingEvaluationRequest;
import com.cyancoder.checkout.model.PricingEvaluationResponse;
import com.cyancoder.dynamiccore.runtime.DynamicRecordRequest;
import com.cyancoder.dynamiccore.runtime.DynamicRuntimeService;
import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordDocument;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
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
        PaymentOrchestratorInitiationResponse response = httpSupport.post("payment-orchestrator-service", "/internal/payment-orchestrator/sessions/initiate",
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
        Map<String, Object> updated = new LinkedHashMap<>(checkout);
        updated.put("status", "PAYMENT_PENDING");
        Map<String, Object> paymentPreference = new LinkedHashMap<>(objectMap(updated.get("paymentPreference")));
        paymentPreference.put("methodKey", paymentMethodKey);
        paymentPreference.put("paymentTransactionKey", response.transactionKey());
        paymentPreference.put("paymentUrl", response.paymentUrl());
        updated.put("paymentPreference", paymentPreference);
        updated.put("orderLifecycle", mergeLifecycle(updated, "PENDING_PAYMENT", "UNPAID", null));
        saveCheckout(entityKey, recordKey, updated);
        return response;
    }

    public DynamicEntityRecordDocument verifyPayment(String entityKey, String recordKey, CheckoutPaymentVerificationRequest request) {
        DynamicEntityRecordDocument existing = dynamicRuntimeService.getRecord(entityKey, recordKey);
        Map<String, Object> checkout = new LinkedHashMap<>(existing.getData() == null ? Map.of() : existing.getData());
        PaymentOrchestratorVerificationResponse response = httpSupport.post(
                "payment-orchestrator-service",
                "/internal/payment-orchestrator/transactions/" + request.transactionKey() + "/verify",
                new PaymentOrchestratorVerificationRequest(request.payload() == null ? Map.of() : request.payload()),
                PaymentOrchestratorVerificationResponse.class
        );
        boolean verified = response != null && "VERIFIED".equalsIgnoreCase(response.status());
        checkout.put("status", verified ? "PAYMENT_VERIFIED" : "FAILED");
        checkout.put("orderLifecycle", mergeLifecycle(
                checkout,
                verified ? "CONFIRMED" : "PENDING_PAYMENT",
                verified ? "PAID" : "FAILED",
                verified ? "UNFULFILLED" : null
        ));
        DynamicEntityRecordDocument saved = saveCheckout(entityKey, recordKey, checkout);
        dispatchNotifications(entityKey, recordKey, checkout, verified ? "payment-verified" : "payment-failed");
        return saved;
    }

    public DynamicEntityRecordDocument advanceLifecycle(String entityKey, String recordKey, CheckoutLifecycleAdvanceRequest request) {
        DynamicEntityRecordDocument existing = dynamicRuntimeService.getRecord(entityKey, recordKey);
        Map<String, Object> checkout = new LinkedHashMap<>(existing.getData() == null ? Map.of() : existing.getData());
        if (request.status() != null && !request.status().isBlank()) {
            checkout.put("status", request.status());
        }
        checkout.put("orderLifecycle", mergeLifecycle(checkout, request.orderStatus(), request.paymentStatus(), request.fulfillmentStatus()));
        DynamicEntityRecordDocument saved = saveCheckout(entityKey, recordKey, checkout);
        if (request.sendNotifications()) {
            dispatchNotifications(entityKey, recordKey, checkout, request.eventCode());
        }
        return saved;
    }

    private DynamicEntityRecordDocument saveCheckout(String entityKey, String recordKey, Map<String, Object> data) {
        DynamicRecordRequest request = new DynamicRecordRequest();
        request.setRecordKey(recordKey);
        request.setData(data);
        return dynamicRuntimeService.replace(entityKey, recordKey, request, true);
    }

    private Map<String, Object> mergeLifecycle(Map<String, Object> checkout,
                                               String orderStatus,
                                               String paymentStatus,
                                               String fulfillmentStatus) {
        Map<String, Object> lifecycle = new LinkedHashMap<>(objectMap(checkout.get("orderLifecycle")));
        if (orderStatus != null && !orderStatus.isBlank()) {
            lifecycle.put("orderStatus", orderStatus);
        }
        if (paymentStatus != null && !paymentStatus.isBlank()) {
            lifecycle.put("paymentStatus", paymentStatus);
        }
        if (fulfillmentStatus != null && !fulfillmentStatus.isBlank()) {
            lifecycle.put("fulfillmentStatus", fulfillmentStatus);
        }
        return lifecycle;
    }

    private void dispatchNotifications(String entityKey, String recordKey, Map<String, Object> checkout, String eventCode) {
        List<Map<String, Object>> notifications = new ArrayList<>(listOfMaps(checkout.get("notifications")));
        if (notifications.isEmpty()) {
            return;
        }
        for (Map<String, Object> notification : notifications) {
            if (!Objects.toString(notification.get("status"), "PENDING").matches("PENDING|QUEUED")) {
                continue;
            }
            NotificationDispatchResponse response = httpSupport.post("notification-service", "/internal/notifications/send",
                    new NotificationDispatchRequest(
                            "notif-" + recordKey + "-" + Objects.toString(notification.get("channel"), "EMAIL").toLowerCase(),
                            Objects.toString(notification.get("channel"), "EMAIL"),
                            string(notification, "templateKey"),
                            Objects.toString(notification.get("recipient"), firstNonBlank(string(objectMap(checkout.get("customer")), "email"), string(objectMap(checkout.get("customer")), "mobile"))),
                            "Checkout " + eventCode,
                            "Checkout " + string(checkout, "checkoutKey", recordKey) + " changed event: " + eventCode,
                            Map.of(
                                    "checkoutKey", string(checkout, "checkoutKey", recordKey),
                                    "status", string(checkout, "status", ""),
                                    "eventCode", eventCode
                            ),
                            Map.of("service", "checkout-service", "entityKey", entityKey, "recordKey", recordKey)
                    ),
                    NotificationDispatchResponse.class);
            notification.put("status", response == null ? "FAILED" : response.status());
        }
        checkout.put("notifications", notifications);
        checkout.put("notificationStatus", notifications.stream().allMatch(row -> "SENT".equalsIgnoreCase(Objects.toString(row.get("status"), ""))) ? "SENT" : "FAILED");
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

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}

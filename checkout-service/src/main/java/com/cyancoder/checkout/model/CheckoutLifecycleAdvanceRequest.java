package com.cyancoder.checkout.model;

public record CheckoutLifecycleAdvanceRequest(
        String status,
        String orderStatus,
        String paymentStatus,
        String fulfillmentStatus,
        boolean sendNotifications,
        String eventCode
) {
}

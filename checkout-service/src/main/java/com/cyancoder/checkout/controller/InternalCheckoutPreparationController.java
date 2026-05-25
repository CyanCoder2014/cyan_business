package com.cyancoder.checkout.controller;

import com.cyancoder.checkout.model.CheckoutPreparationSnapshot;
import com.cyancoder.checkout.model.CheckoutLifecycleAdvanceRequest;
import com.cyancoder.checkout.model.CheckoutPaymentVerificationRequest;
import com.cyancoder.checkout.model.PaymentOrchestratorInitiationResponse;
import com.cyancoder.checkout.service.CheckoutPreparationService;
import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordDocument;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/internal/checkout")
public class InternalCheckoutPreparationController {
    private final CheckoutPreparationService checkoutPreparationService;

    public InternalCheckoutPreparationController(CheckoutPreparationService checkoutPreparationService) {
        this.checkoutPreparationService = checkoutPreparationService;
    }

    @GetMapping("/sessions/{entityKey}/{recordKey}/snapshot")
    public CheckoutPreparationSnapshot snapshot(@PathVariable String entityKey, @PathVariable String recordKey) {
        return checkoutPreparationService.snapshot(entityKey, recordKey);
    }

    @PostMapping("/sessions/{entityKey}/{recordKey}/initiate-payment")
    public PaymentOrchestratorInitiationResponse initiatePayment(@PathVariable String entityKey,
                                                                 @PathVariable String recordKey,
                                                                 @RequestBody Map<String, String> request) {
        return checkoutPreparationService.initiatePayment(entityKey, recordKey, request.get("paymentMethodKey"));
    }

    @PostMapping("/sessions/{entityKey}/{recordKey}/verify-payment")
    public DynamicEntityRecordDocument verifyPayment(@PathVariable String entityKey,
                                                     @PathVariable String recordKey,
                                                     @RequestBody CheckoutPaymentVerificationRequest request) {
        return checkoutPreparationService.verifyPayment(entityKey, recordKey, request);
    }

    @PostMapping("/sessions/{entityKey}/{recordKey}/advance")
    public DynamicEntityRecordDocument advance(@PathVariable String entityKey,
                                               @PathVariable String recordKey,
                                               @RequestBody CheckoutLifecycleAdvanceRequest request) {
        return checkoutPreparationService.advanceLifecycle(entityKey, recordKey, request);
    }
}

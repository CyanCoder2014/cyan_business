package com.cyancoder.checkout.controller;

import com.cyancoder.checkout.model.CheckoutPreparationSnapshot;
import com.cyancoder.checkout.model.PaymentOrchestratorInitiationResponse;
import com.cyancoder.checkout.service.CheckoutPreparationService;
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
}

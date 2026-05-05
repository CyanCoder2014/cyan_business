package com.cyancoder.paymentorchestrator.controller;

import com.cyancoder.paymentorchestrator.model.AvailablePaymentMethod;
import com.cyancoder.paymentorchestrator.model.PaymentOrchestratorInitiationRequest;
import com.cyancoder.paymentorchestrator.model.PaymentOrchestratorInitiationResponse;
import com.cyancoder.paymentorchestrator.service.PaymentGatewayBridgeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/payment-orchestrator")
public class InternalPaymentOrchestratorController {
    private final PaymentGatewayBridgeService paymentGatewayBridgeService;

    public InternalPaymentOrchestratorController(PaymentGatewayBridgeService paymentGatewayBridgeService) {
        this.paymentGatewayBridgeService = paymentGatewayBridgeService;
    }

    @GetMapping("/methods")
    public List<AvailablePaymentMethod> methods() {
        return paymentGatewayBridgeService.availableMethods();
    }

    @PostMapping("/sessions/initiate")
    public PaymentOrchestratorInitiationResponse initiate(@RequestBody PaymentOrchestratorInitiationRequest request) {
        return paymentGatewayBridgeService.initiate(request);
    }
}

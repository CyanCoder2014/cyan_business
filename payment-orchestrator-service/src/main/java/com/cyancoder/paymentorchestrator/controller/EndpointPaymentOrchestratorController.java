package com.cyancoder.paymentorchestrator.controller;

import com.cyancoder.paymentorchestrator.model.AvailablePaymentMethod;
import com.cyancoder.paymentorchestrator.model.PaymentOrchestratorInitiationRequest;
import com.cyancoder.paymentorchestrator.model.PaymentOrchestratorInitiationResponse;
import com.cyancoder.paymentorchestrator.model.PaymentOrchestratorVerificationRequest;
import com.cyancoder.paymentorchestrator.model.PaymentOrchestratorVerificationResponse;
import com.cyancoder.paymentorchestrator.service.PaymentGatewayBridgeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/endpoint/payment-orchestrator")
public class EndpointPaymentOrchestratorController {
    private final PaymentGatewayBridgeService paymentGatewayBridgeService;

    public EndpointPaymentOrchestratorController(PaymentGatewayBridgeService paymentGatewayBridgeService) {
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

    @PostMapping("/transactions/{transactionKey}/verify")
    public PaymentOrchestratorVerificationResponse verify(@PathVariable String transactionKey,
                                                          @RequestBody(required = false) PaymentOrchestratorVerificationRequest request) {
        return paymentGatewayBridgeService.verify(transactionKey, request);
    }
}

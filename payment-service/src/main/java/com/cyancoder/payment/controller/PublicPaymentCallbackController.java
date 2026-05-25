package com.cyancoder.payment.controller;

import com.cyancoder.payment.dto.PaymentCallbackResponse;
import com.cyancoder.payment.service.PaymentTransactionService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/public/payment")
public class PublicPaymentCallbackController {
    private final PaymentTransactionService paymentTransactionService;

    public PublicPaymentCallbackController(PaymentTransactionService paymentTransactionService) {
        this.paymentTransactionService = paymentTransactionService;
    }

    @GetMapping("/callback/{providerCode}/{transactionKey}")
    public PaymentCallbackResponse callbackGet(@PathVariable String providerCode,
                                               @PathVariable String transactionKey,
                                               @RequestParam Map<String, String> payload) {
        return paymentTransactionService.handleCallback(providerCode, transactionKey, payload);
    }

    @PostMapping("/callback/{providerCode}/{transactionKey}")
    public PaymentCallbackResponse callbackPost(@PathVariable String providerCode,
                                                @PathVariable String transactionKey,
                                                @RequestParam Map<String, String> payload) {
        return paymentTransactionService.handleCallback(providerCode, transactionKey, payload);
    }

    @GetMapping("/simulate/{providerCode}/{transactionKey}")
    public PaymentCallbackResponse simulate(@PathVariable String providerCode,
                                            @PathVariable String transactionKey,
                                            @RequestParam Map<String, String> payload) {
        return paymentTransactionService.handleCallback(providerCode, transactionKey, payload);
    }
}

package com.cyancoder.payment.controller;

import com.cyancoder.payment.domain.PaymentTransactionStatus;
import com.cyancoder.payment.dto.PaymentInitiationRequest;
import com.cyancoder.payment.dto.PaymentInitiationResponse;
import com.cyancoder.payment.dto.PaymentTransactionResponse;
import com.cyancoder.payment.service.PaymentTransactionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/endpoint/payment/transactions")
public class EndpointPaymentTransactionController {
    private final PaymentTransactionService paymentTransactionService;

    public EndpointPaymentTransactionController(PaymentTransactionService paymentTransactionService) {
        this.paymentTransactionService = paymentTransactionService;
    }

    @PostMapping("/initiate")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('commerce:*')")
    public PaymentInitiationResponse initiate(@Valid @RequestBody PaymentInitiationRequest request) {
        return paymentTransactionService.initiate(request);
    }

    @PostMapping("/{transactionKey}/verify")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('commerce:*')")
    public PaymentTransactionResponse verify(@PathVariable String transactionKey, @RequestBody(required = false) Map<String, String> payload) {
        return paymentTransactionService.verify(transactionKey, payload);
    }

    @GetMapping("/{transactionKey}")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('commerce:*')")
    public PaymentTransactionResponse get(@PathVariable String transactionKey) {
        return paymentTransactionService.get(transactionKey);
    }

    @GetMapping
    @PreAuthorize("@platformAuthorizationService.canUseCapability('commerce:*')")
    public List<PaymentTransactionResponse> list(@RequestParam(required = false) String orderKey,
                                                 @RequestParam(required = false) PaymentTransactionStatus status) {
        return paymentTransactionService.list(orderKey, status);
    }
}

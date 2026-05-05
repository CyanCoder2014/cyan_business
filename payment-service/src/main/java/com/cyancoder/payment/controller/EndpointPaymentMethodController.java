package com.cyancoder.payment.controller;

import com.cyancoder.payment.dto.AvailablePaymentMethodResponse;
import com.cyancoder.payment.dto.PaymentMethodAdminResponse;
import com.cyancoder.payment.dto.PaymentMethodRequest;
import com.cyancoder.payment.service.PaymentMethodAdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/endpoint/payment")
public class EndpointPaymentMethodController {
    private final PaymentMethodAdminService paymentMethodAdminService;

    public EndpointPaymentMethodController(PaymentMethodAdminService paymentMethodAdminService) {
        this.paymentMethodAdminService = paymentMethodAdminService;
    }

    @GetMapping("/methods")
    public List<AvailablePaymentMethodResponse> listAvailableMethods() {
        return paymentMethodAdminService.listAvailable();
    }

    @GetMapping("/admin/methods")
    public List<PaymentMethodAdminResponse> listMethods() {
        return paymentMethodAdminService.list();
    }

    @GetMapping("/admin/methods/{methodKey}")
    public PaymentMethodAdminResponse getMethod(@PathVariable String methodKey) {
        return paymentMethodAdminService.get(methodKey);
    }

    @PostMapping("/admin/methods")
    public PaymentMethodAdminResponse createMethod(@Valid @RequestBody PaymentMethodRequest request) {
        return paymentMethodAdminService.create(request);
    }

    @PutMapping("/admin/methods/{methodKey}")
    public PaymentMethodAdminResponse updateMethod(@PathVariable String methodKey, @Valid @RequestBody PaymentMethodRequest request) {
        return paymentMethodAdminService.update(methodKey, request);
    }

    @DeleteMapping("/admin/methods/{methodKey}")
    public void deleteMethod(@PathVariable String methodKey) {
        paymentMethodAdminService.delete(methodKey);
    }
}

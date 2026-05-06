package com.cyancoder.paymentorchestrator.service;

import com.cyancoder.paymentorchestrator.model.AvailablePaymentMethod;
import com.cyancoder.paymentorchestrator.model.PaymentOrchestratorInitiationRequest;
import com.cyancoder.paymentorchestrator.model.PaymentOrchestratorInitiationResponse;
import com.cyancoder.paymentorchestrator.model.PaymentOrchestratorVerificationRequest;
import com.cyancoder.paymentorchestrator.model.PaymentOrchestratorVerificationResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class PaymentGatewayBridgeService {
    private final InternalServiceHttpSupport httpSupport;

    public PaymentGatewayBridgeService(InternalServiceHttpSupport httpSupport) {
        this.httpSupport = httpSupport;
    }

    public List<AvailablePaymentMethod> availableMethods() {
        AvailablePaymentMethod[] body = httpSupport.get("payment-service", "/internal/payment/methods", AvailablePaymentMethod[].class);
        return body == null ? List.of() : Arrays.asList(body);
    }

    @SuppressWarnings("unchecked")
    public PaymentOrchestratorInitiationResponse initiate(PaymentOrchestratorInitiationRequest request) {
        Map<String, Object> body = httpSupport.post("payment-service", "/internal/payment/transactions/initiate", request, Map.class);
        return new PaymentOrchestratorInitiationResponse(
                string(body, "transactionKey"),
                string(body, "status"),
                string(body, "paymentMethodKey"),
                string(body, "providerCode"),
                string(body, "paymentUrl"),
                string(body, "clientToken"),
                string(body, "providerReferenceId"),
                string(body, "externalPaymentId"),
                string(body, "publicCallbackUrl")
        );
    }

    @SuppressWarnings("unchecked")
    public PaymentOrchestratorVerificationResponse verify(String transactionKey, PaymentOrchestratorVerificationRequest request) {
        Map<String, Object> body = httpSupport.post("payment-service", "/internal/payment/transactions/" + transactionKey + "/verify",
                request == null ? Map.of() : request.payload(),
                Map.class);
        return new PaymentOrchestratorVerificationResponse(
                string(body, "transactionKey"),
                string(body, "status"),
                string(body, "paymentMethodKey"),
                string(body, "providerCode"),
                string(body, "verificationMessage")
        );
    }

    private String string(Map<String, Object> body, String key) {
        return body == null || body.get(key) == null ? null : String.valueOf(body.get(key));
    }
}

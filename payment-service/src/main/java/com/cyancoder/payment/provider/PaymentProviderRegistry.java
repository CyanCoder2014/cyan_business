package com.cyancoder.payment.provider;

import com.cyancoder.payment.domain.PaymentProviderCode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentProviderRegistry {
    private final List<PaymentProviderStrategy> strategies;

    public PaymentProviderRegistry(List<PaymentProviderStrategy> strategies) {
        this.strategies = strategies;
    }

    public PaymentProviderStrategy get(PaymentProviderCode providerCode) {
        return strategies.stream()
                .filter(strategy -> strategy.providerCode() == providerCode)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No payment strategy registered for " + providerCode));
    }
}

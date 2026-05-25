package com.cyancoder.payment.provider;

import com.cyancoder.payment.domain.PaymentProviderCode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaypalPaymentStrategy extends AbstractHostedPaymentProviderStrategy {
    @Override
    public PaymentProviderCode providerCode() {
        return PaymentProviderCode.PAYPAL;
    }

    @Override
    protected List<String> requiredConfigurationKeys() {
        return List.of("clientId", "environment");
    }
}

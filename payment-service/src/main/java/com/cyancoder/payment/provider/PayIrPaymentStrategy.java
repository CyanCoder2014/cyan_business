package com.cyancoder.payment.provider;

import com.cyancoder.payment.domain.PaymentProviderCode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PayIrPaymentStrategy extends AbstractHostedPaymentProviderStrategy {
    @Override
    public PaymentProviderCode providerCode() {
        return PaymentProviderCode.PAY_IR;
    }

    @Override
    protected List<String> requiredConfigurationKeys() {
        return List.of("apiKey");
    }
}

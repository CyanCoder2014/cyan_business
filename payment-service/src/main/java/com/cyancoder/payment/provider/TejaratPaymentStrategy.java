package com.cyancoder.payment.provider;

import com.cyancoder.payment.domain.PaymentProviderCode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TejaratPaymentStrategy extends AbstractHostedPaymentProviderStrategy {
    @Override
    public PaymentProviderCode providerCode() {
        return PaymentProviderCode.TEJARAT;
    }

    @Override
    protected List<String> requiredConfigurationKeys() {
        return List.of("merchantId", "terminalId");
    }
}

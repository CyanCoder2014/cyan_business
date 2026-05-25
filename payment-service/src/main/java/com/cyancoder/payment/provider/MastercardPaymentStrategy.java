package com.cyancoder.payment.provider;

import com.cyancoder.payment.domain.PaymentProviderCode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MastercardPaymentStrategy extends AbstractHostedPaymentProviderStrategy {
    @Override
    public PaymentProviderCode providerCode() {
        return PaymentProviderCode.MASTERCARD;
    }

    @Override
    protected List<String> requiredConfigurationKeys() {
        return List.of("merchantId", "acquirer");
    }
}

package com.cyancoder.payment.provider;

import com.cyancoder.payment.domain.PaymentProviderCode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ZarinpalPaymentStrategy extends AbstractHostedPaymentProviderStrategy {
    @Override
    public PaymentProviderCode providerCode() {
        return PaymentProviderCode.ZARINPAL;
    }

    @Override
    protected List<String> requiredConfigurationKeys() {
        return List.of("merchantId");
    }
}

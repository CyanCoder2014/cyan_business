package com.cyancoder.payment.provider;

import com.cyancoder.payment.domain.PaymentProviderCode;

import java.util.Map;

public interface PaymentProviderStrategy {
    PaymentProviderCode providerCode();
    void validateConfiguration(Map<String, Object> configuration);
    PaymentProviderInitResult initiate(PaymentProviderContext context);
    PaymentProviderVerificationResult verify(PaymentVerificationContext context);
    default PaymentProviderVerificationResult handleCallback(PaymentVerificationContext context) {
        return verify(context);
    }
}

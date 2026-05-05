package com.cyancoder.payment.provider;

import com.cyancoder.payment.domain.PaymentProviderCode;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractHostedPaymentProviderStrategy implements PaymentProviderStrategy {

    @Override
    public void validateConfiguration(Map<String, Object> configuration) {
        for (String key : requiredConfigurationKeys()) {
            String value = stringValue(configuration, key);
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Missing required config key `" + key + "` for " + providerCode());
            }
        }
    }

    @Override
    public PaymentProviderInitResult initiate(PaymentProviderContext context) {
        validateConfiguration(context.configuration());
        String providerRef = providerCode().name() + "-" + UUID.randomUUID();
        String paymentToken = "token-" + UUID.randomUUID();
        String externalPaymentId = "ext-" + UUID.randomUUID();
        String paymentUrl = resolvePaymentUrl(context, paymentToken);
        return new PaymentProviderInitResult(
                paymentUrl,
                paymentToken,
                providerRef,
                externalPaymentId,
                paymentToken,
                "GET",
                Map.of(),
                providerCode().name() + " initiation created"
        );
    }

    @Override
    public PaymentProviderVerificationResult verify(PaymentVerificationContext context) {
        String status = firstNonBlank(
                context.payload().get("status"),
                context.payload().get("Status"),
                context.payload().get("result")
        );
        boolean success = matchesSuccess(status)
                || asBoolean(context.payload().get("success"))
                || asBoolean(context.payload().get("approved"))
                || "100".equals(context.payload().get("resCode"));
        String providerRef = firstNonBlank(
                context.payload().get("referenceId"),
                context.payload().get("RefNum"),
                context.payload().get("authority"),
                context.transaction().getProviderReferenceId()
        );
        String externalPaymentId = firstNonBlank(
                context.payload().get("paymentId"),
                context.payload().get("token"),
                context.transaction().getExternalPaymentId()
        );
        return new PaymentProviderVerificationResult(
                success,
                providerRef,
                externalPaymentId,
                context.transaction().getPaymentToken(),
                context.payload().getOrDefault("cardNumber", context.transaction().getMaskedCardNumber()),
                success ? providerCode().name() + " verification successful" : providerCode().name() + " verification failed"
        );
    }

    protected String resolvePaymentUrl(PaymentProviderContext context, String paymentToken) {
        if (mockMode(context.configuration())) {
            return context.publicBaseUrl() + "/public/payment/simulate/" + providerCode().name().toLowerCase(Locale.ROOT)
                    + "/" + context.transaction().getTransactionKey() + "?success=true&referenceId=" + paymentToken;
        }
        String gatewayUrl = stringValue(context.configuration(), "gatewayUrl");
        if (gatewayUrl == null || gatewayUrl.isBlank()) {
            return context.publicBaseUrl() + "/public/payment/simulate/" + providerCode().name().toLowerCase(Locale.ROOT)
                    + "/" + context.transaction().getTransactionKey() + "?success=true&referenceId=" + paymentToken;
        }
        return gatewayUrl.endsWith("/") ? gatewayUrl + paymentToken : gatewayUrl + "/" + paymentToken;
    }

    protected boolean mockMode(Map<String, Object> configuration) {
        Object value = configuration.get("mockMode");
        return value == null || Boolean.parseBoolean(String.valueOf(value));
    }

    protected String stringValue(Map<String, Object> configuration, String key) {
        Object value = configuration.get(key);
        return value == null ? null : String.valueOf(value);
    }

    protected abstract List<String> requiredConfigurationKeys();

    private boolean matchesSuccess(String status) {
        if (status == null) {
            return false;
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        return normalized.equals("OK") || normalized.equals("SUCCESS") || normalized.equals("SUCCEEDED") || normalized.equals("VERIFIED");
    }

    private boolean asBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}

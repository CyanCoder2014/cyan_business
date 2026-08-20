package com.cyancoder.automationorchestrator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.Map;

/** Best-effort usage metering: never lets a billing-service hiccup fail a flow run. */
@Component
public class BillingUsageReporter {
    private static final Logger log = LoggerFactory.getLogger(BillingUsageReporter.class);
    private final InternalServiceHttpSupport httpSupport;

    public BillingUsageReporter(InternalServiceHttpSupport httpSupport) {
        this.httpSupport = httpSupport;
    }

    public void increment(String tenantKey, String metricKey) {
        if (tenantKey == null || tenantKey.isBlank()) return;
        try {
            httpSupport.exchange("billing-service", "/internal/billing/tenants/" + tenantKey + "/usage/increment",
                    HttpMethod.POST, Map.of("metricKey", metricKey, "delta", 1),
                    httpSupport.internalHeaders("billing-service", tenantKey, null), Void.class);
        } catch (Exception error) {
            log.warn("Usage metering failed for tenant {} metric {}: {}", tenantKey, metricKey, error.getMessage());
        }
    }
}

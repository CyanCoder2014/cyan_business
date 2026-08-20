package com.cyancoder.aiorchestrator.service;

import com.cyancoder.aiorchestrator.client.impl.InternalServiceHttpSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/** Best-effort usage metering: never lets a billing-service hiccup fail the caller's real work. */
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
            httpSupport.post("billing-service", "/internal/billing/tenants/" + tenantKey + "/usage/increment",
                    Map.of("metricKey", metricKey, "delta", 1), tenantKey, null);
        } catch (Exception error) {
            log.warn("Usage metering failed for tenant {} metric {}: {}", tenantKey, metricKey, error.getMessage());
        }
    }
}

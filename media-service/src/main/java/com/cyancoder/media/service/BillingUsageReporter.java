package com.cyancoder.media.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Best-effort usage metering: never lets a billing-service hiccup fail an upload. */
@Component
public class BillingUsageReporter {
    private static final Logger log = LoggerFactory.getLogger(BillingUsageReporter.class);
    private final RestClient client;
    private final String username;
    private final String password;

    public BillingUsageReporter(RestClient.Builder builder,
                                @Value("${billing-service.base-url:http://localhost:9130}") String baseUrl,
                                @Value("${billing-service.internal.username:billing_internal}") String username,
                                @Value("${billing-service.internal.password:billing_secret}") String password) {
        this.client = builder.baseUrl(baseUrl).build();
        this.username = username;
        this.password = password;
    }

    public void increment(String tenantKey, String metricKey, long delta) {
        if (tenantKey == null || tenantKey.isBlank() || delta == 0) return;
        try {
            client.post().uri("/internal/billing/tenants/{tenantKey}/usage/increment", tenantKey)
                    .headers(h -> h.setBasicAuth(username, password, StandardCharsets.UTF_8))
                    .body(Map.of("metricKey", metricKey, "delta", delta))
                    .retrieve().toBodilessEntity();
        } catch (Exception error) {
            log.warn("Usage metering failed for tenant {} metric {}: {}", tenantKey, metricKey, error.getMessage());
        }
    }
}

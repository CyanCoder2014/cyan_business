package com.cyancoder.aiorchestrator.client.impl;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Component
public class InternalServiceHttpSupport {
    private final DiscoveryClient discoveryClient;
    private final RestTemplate restTemplate = new RestTemplate();

    public InternalServiceHttpSupport(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    public String get(String serviceKey, String path, String tenantKey, String siteKey) {
        return exchange(serviceKey, path, HttpMethod.GET, null, tenantKey, siteKey);
    }

    public String post(String serviceKey, String path, Object body, String tenantKey, String siteKey) {
        return exchange(serviceKey, path, HttpMethod.POST, body, tenantKey, siteKey);
    }

    private String exchange(String serviceKey, String path, HttpMethod method, Object body, String tenantKey, String siteKey) {
        ServiceInstance instance = discoveryClient.getInstances(serviceKey).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("service not found: " + serviceKey));
        HttpHeaders headers = new HttpHeaders();
        String normalized = normalizeServiceCredentialsKey(serviceKey);
        headers.setBasicAuth(normalized + "_internal", normalized + "_secret", StandardCharsets.UTF_8);
        if (tenantKey != null && !tenantKey.isBlank()) {
            headers.set("X-Tenant-Key", tenantKey);
        }
        if (siteKey != null && !siteKey.isBlank()) {
            headers.set("X-Site-Key", siteKey);
        }
        ResponseEntity<String> response = restTemplate.exchange(resolveBaseUri(instance) + path, method, new HttpEntity<>(body, headers), String.class);
        return response.getBody();
    }

    private String normalizeServiceCredentialsKey(String serviceKey) {
        String base = serviceKey.endsWith("-service") ? serviceKey.substring(0, serviceKey.length() - "-service".length()) : serviceKey;
        return base.replace('-', '_');
    }

    private URI resolveBaseUri(ServiceInstance instance) {
        String host = instance.getHost();
        if (host == null || host.isBlank() || "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host)) {
            return instance.getUri();
        }
        return URI.create(instance.isSecure() ? "https://localhost:" + instance.getPort() : "http://localhost:" + instance.getPort());
    }
}

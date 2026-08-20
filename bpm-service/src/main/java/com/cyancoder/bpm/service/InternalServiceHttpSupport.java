package com.cyancoder.bpm.service;

import com.cyancoder.platform.internalhttp.InternalServiceCredentialsResolver;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Component
public class InternalServiceHttpSupport {
    private final DiscoveryClient discoveryClient;
    private final InternalServiceCredentialsResolver credentialsResolver;
    private final RestTemplate restTemplate = new RestTemplate();

    public InternalServiceHttpSupport(DiscoveryClient discoveryClient,
                                      InternalServiceCredentialsResolver credentialsResolver) {
        this.discoveryClient = discoveryClient;
        this.credentialsResolver = credentialsResolver;
    }

    public <T> T get(String serviceKey, String path, String tenantKey, String siteKey, Class<T> responseType) {
        return exchange(serviceKey, path, HttpMethod.GET, null, tenantKey, siteKey, responseType);
    }

    public <T> T post(String serviceKey, String path, Object request, String tenantKey, String siteKey, Class<T> responseType) {
        return exchange(serviceKey, path, HttpMethod.POST, request, tenantKey, siteKey, responseType);
    }

    public <T> T exchange(String serviceKey, String path, HttpMethod method, Object request, String tenantKey, String siteKey, Class<T> responseType) {
        ServiceInstance instance = discoveryClient.getInstances(serviceKey).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("service not found: " + serviceKey));
        HttpHeaders headers = new HttpHeaders();
        credentialsResolver.applyBasicAuth(headers, serviceKey);
        if (tenantKey != null && !tenantKey.isBlank()) {
            headers.set("X-Tenant-Key", tenantKey);
        }
        if (siteKey != null && !siteKey.isBlank()) {
            headers.set("X-Site-Key", siteKey);
        }
        ResponseEntity<T> response = restTemplate.exchange(resolveBaseUri(instance) + path, method, new HttpEntity<>(request, headers), responseType);
        return response.getBody();
    }

    private URI resolveBaseUri(ServiceInstance instance) {
        return instance.getUri();
    }
}

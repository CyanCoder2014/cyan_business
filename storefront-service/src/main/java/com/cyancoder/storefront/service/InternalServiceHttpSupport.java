package com.cyancoder.storefront.service;

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
import org.springframework.http.MediaType;

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
        return getAsActor(serviceKey, path, tenantKey, siteKey, null, null, null, responseType);
    }

    public <T> T getAsActor(String serviceKey, String path, String tenantKey, String siteKey,
                            String actor, String roles, String groups, Class<T> responseType) {
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
        if (actor != null && !actor.isBlank()) headers.set("X-Actor-User", actor);
        if (roles != null && !roles.isBlank()) headers.set("X-Actor-Roles", roles);
        if (groups != null && !groups.isBlank()) headers.set("X-Actor-Groups", groups);
        ResponseEntity<T> response = restTemplate.exchange(resolveBaseUri(instance) + path, HttpMethod.GET, new HttpEntity<>(headers), responseType);
        return response.getBody();
    }

    public <T> T post(String serviceKey, String path, String tenantKey, String siteKey, Object body, Class<T> responseType) {
        ServiceInstance instance = discoveryClient.getInstances(serviceKey).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("service not found: " + serviceKey));
        HttpHeaders headers = new HttpHeaders();
        credentialsResolver.applyBasicAuth(headers, serviceKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (tenantKey != null && !tenantKey.isBlank()) headers.set("X-Tenant-Key", tenantKey);
        if (siteKey != null && !siteKey.isBlank()) headers.set("X-Site-Key", siteKey);
        ResponseEntity<T> response = restTemplate.exchange(resolveBaseUri(instance) + path, HttpMethod.POST, new HttpEntity<>(body, headers), responseType);
        return response.getBody();
    }

    private URI resolveBaseUri(ServiceInstance instance) {
        return instance.getUri();
    }
}

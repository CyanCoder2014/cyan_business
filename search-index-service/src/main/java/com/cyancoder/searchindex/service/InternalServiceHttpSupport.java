package com.cyancoder.searchindex.service;

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
import java.util.List;
import java.util.Map;

@Component
public class InternalServiceHttpSupport {
    private final DiscoveryClient discoveryClient;
    private final RestTemplate restTemplate = new RestTemplate();

    public InternalServiceHttpSupport(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getList(String serviceKey, String path) { return getList(serviceKey,path,null,null); }
    public List<Map<String, Object>> getList(String serviceKey, String path, String tenantKey, String siteKey) {
        ServiceInstance instance = discoveryClient.getInstances(serviceKey).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("service not found: " + serviceKey));
        HttpHeaders headers = new HttpHeaders();
        String normalized = normalizeServiceCredentialsKey(serviceKey);
        headers.setBasicAuth(normalized + "_internal", normalized + "_secret", StandardCharsets.UTF_8);
        if (tenantKey != null) headers.set("X-Tenant-Key",tenantKey);
        if (siteKey != null) headers.set("X-Site-Key",siteKey);
        ResponseEntity<List> response = restTemplate.exchange(resolveBaseUri(instance) + path, HttpMethod.GET, new HttpEntity<>(headers), List.class);
        return response.getBody() == null ? List.of() : (List<Map<String, Object>>) response.getBody();
    }

    private String normalizeServiceCredentialsKey(String serviceKey) {
        String base = serviceKey.endsWith("-service") ? serviceKey.substring(0, serviceKey.length() - "-service".length()) : serviceKey;
        return base.replace('-', '_');
    }

    private URI resolveBaseUri(ServiceInstance instance) {
        return instance.getUri();
    }
}

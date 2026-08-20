package com.cyancoder.searchindex.service;

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
import java.util.List;
import java.util.Map;

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

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getList(String serviceKey, String path) { return getList(serviceKey,path,null,null); }
    public List<Map<String, Object>> getList(String serviceKey, String path, String tenantKey, String siteKey) {
        ServiceInstance instance = discoveryClient.getInstances(serviceKey).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("service not found: " + serviceKey));
        HttpHeaders headers = new HttpHeaders();
        credentialsResolver.applyBasicAuth(headers, serviceKey);
        if (tenantKey != null) headers.set("X-Tenant-Key",tenantKey);
        if (siteKey != null) headers.set("X-Site-Key",siteKey);
        ResponseEntity<List> response = restTemplate.exchange(resolveBaseUri(instance) + path, HttpMethod.GET, new HttpEntity<>(headers), List.class);
        return response.getBody() == null ? List.of() : (List<Map<String, Object>>) response.getBody();
    }

    private URI resolveBaseUri(ServiceInstance instance) {
        return instance.getUri();
    }
}

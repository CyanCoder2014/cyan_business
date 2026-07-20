package com.cyancoder.automationorchestrator.service;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Component
public class InternalServiceHttpSupport {
    private final DiscoveryClient discoveryClient;
    private final RestTemplate restTemplate = new RestTemplate();

    public InternalServiceHttpSupport(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    public <T> T exchange(String serviceKey,
                          String path,
                          HttpMethod method,
                          Object request,
                          HttpHeaders headers,
                          Class<T> responseType) {
        ServiceInstance instance = discoveryClient.getInstances(serviceKey).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("service not found: " + serviceKey));
        HttpHeaders actualHeaders = headers == null ? new HttpHeaders() : headers;
        ResponseEntity<T> response = restTemplate.exchange(resolveBaseUri(instance) + path, method, new HttpEntity<>(request, actualHeaders), responseType);
        return response.getBody();
    }

    public HttpHeaders internalHeaders(String serviceKey, String tenantKey, String siteKey) {
        HttpHeaders headers = new HttpHeaders();
        String normalized = normalizeServiceCredentialsKey(serviceKey);
        headers.setBasicAuth(normalized + "_internal", normalized + "_secret", StandardCharsets.UTF_8);
        if (tenantKey != null && !tenantKey.isBlank()) {
            headers.set("X-Tenant-Key", tenantKey);
        }
        if (siteKey != null && !siteKey.isBlank()) {
            headers.set("X-Site-Key", siteKey);
        }
        return headers;
    }

    public <T> T exchangeUrl(String url, HttpMethod method, Object request, HttpHeaders headers,
                             Integer connectTimeoutMs, Integer readTimeoutMs, Class<T> responseType) {
        if (url == null || !(url.startsWith("http://") || url.startsWith("https://"))) {
            throw new IllegalArgumentException("automation URL must use http or https");
        }
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs == null ? 5000 : Math.max(1, connectTimeoutMs));
        factory.setReadTimeout(readTimeoutMs == null ? 30000 : Math.max(1, readTimeoutMs));
        return new RestTemplate(factory).exchange(URI.create(url), method,
                new HttpEntity<>(request, headers == null ? new HttpHeaders() : headers), responseType).getBody();
    }

    private String normalizeServiceCredentialsKey(String serviceKey) {
        String base = serviceKey.endsWith("-service") ? serviceKey.substring(0, serviceKey.length() - "-service".length()) : serviceKey;
        return base.replace('-', '_');
    }

    private URI resolveBaseUri(ServiceInstance instance) {
        return instance.getUri();
    }
}

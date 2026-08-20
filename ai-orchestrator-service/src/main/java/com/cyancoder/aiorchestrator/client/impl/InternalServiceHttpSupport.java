package com.cyancoder.aiorchestrator.client.impl;

import com.cyancoder.aiorchestrator.exception.DownstreamServiceException;
import com.cyancoder.platform.internalhttp.InternalServiceCredentialsResolver;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Component
public class InternalServiceHttpSupport {
    private final DiscoveryClient discoveryClient;
    private final InternalServiceCredentialsResolver credentialsResolver;
    private final RestTemplate restTemplate;

    public InternalServiceHttpSupport(DiscoveryClient discoveryClient,
                                      InternalServiceCredentialsResolver credentialsResolver) {
        this.discoveryClient = discoveryClient;
        this.credentialsResolver = credentialsResolver;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(30000);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    public String get(String serviceKey, String path, String tenantKey, String siteKey) {
        return exchange(serviceKey, path, HttpMethod.GET, null, tenantKey, siteKey);
    }

    public String post(String serviceKey, String path, Object body, String tenantKey, String siteKey) {
        return exchange(serviceKey, path, HttpMethod.POST, body, tenantKey, siteKey);
    }

    public String put(String serviceKey, String path, Object body, String tenantKey, String siteKey) {
        return exchange(serviceKey, path, HttpMethod.PUT, body, tenantKey, siteKey);
    }

    public BinaryResponse getBytes(String serviceKey, String path, String tenantKey, String siteKey) {
        ServiceInstance instance = discoveryClient.getInstances(serviceKey).stream().findFirst()
                .orElseThrow(() -> new DownstreamServiceException("No internal route is configured for service: " + serviceKey, serviceKey, path, 503, null, null));
        HttpHeaders headers = new HttpHeaders();
        credentialsResolver.applyBasicAuth(headers, serviceKey);
        if (tenantKey != null && !tenantKey.isBlank()) headers.set("X-Tenant-Key", tenantKey);
        if (siteKey != null && !siteKey.isBlank()) headers.set("X-Site-Key", siteKey);
        ResponseEntity<byte[]> response = restTemplate.exchange(resolveBaseUri(instance) + path, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
        return new BinaryResponse(response.getBody() == null ? new byte[0] : response.getBody(), response.getHeaders().getContentType() == null ? "application/octet-stream" : response.getHeaders().getContentType().toString(), response.getHeaders().getFirst("X-Media-File-Name"));
    }

    public record BinaryResponse(byte[] bytes, String mimeType, String fileName) {}

    private String exchange(String serviceKey, String path, HttpMethod method, Object body, String tenantKey, String siteKey) {
        try {
            ServiceInstance instance = discoveryClient.getInstances(serviceKey).stream().findFirst()
                    .orElseThrow(() -> new DownstreamServiceException(
                            "No internal route is configured for service: " + serviceKey,
                            serviceKey,
                            path,
                            503,
                            null,
                            null
                    ));
            HttpHeaders headers = new HttpHeaders();
            credentialsResolver.applyBasicAuth(headers, serviceKey);
            if (tenantKey != null && !tenantKey.isBlank()) {
                headers.set("X-Tenant-Key", tenantKey);
            }
            if (siteKey != null && !siteKey.isBlank()) {
                headers.set("X-Site-Key", siteKey);
            }
            ResponseEntity<String> response = restTemplate.exchange(resolveBaseUri(instance) + path, method, new HttpEntity<>(body, headers), String.class);
            return response.getBody();
        } catch (DownstreamServiceException ex) {
            throw ex;
        } catch (HttpStatusCodeException ex) {
            throw new DownstreamServiceException(
                    "Downstream service returned an error: " + serviceKey + " " + path,
                    serviceKey,
                    path,
                    ex.getStatusCode().value(),
                    ex.getResponseBodyAsString(),
                    ex
            );
        } catch (ResourceAccessException ex) {
            throw new DownstreamServiceException(
                    "Downstream service is unreachable: " + serviceKey + " " + path,
                    serviceKey,
                    path,
                    503,
                    ex.getMessage(),
                    ex
            );
        }
    }

    private URI resolveBaseUri(ServiceInstance instance) {
        return instance.getUri();
    }
}

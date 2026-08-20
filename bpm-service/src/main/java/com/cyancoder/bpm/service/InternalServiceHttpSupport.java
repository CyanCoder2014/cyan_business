package com.cyancoder.bpm.service;

import com.cyancoder.platform.error.PlatformErrorCode;
import com.cyancoder.platform.error.PlatformServiceException;
import com.cyancoder.platform.internalhttp.InternalServiceCredentialsResolver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class InternalServiceHttpSupport {
    private final DiscoveryClient discoveryClient;
    private final InternalServiceCredentialsResolver credentialsResolver;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        try {
            ResponseEntity<T> response = restTemplate.exchange(resolveBaseUri(instance) + path, method, new HttpEntity<>(request, headers), responseType);
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            throw translateDownstreamError(serviceKey, ex);
        }
    }

    /**
     * A downstream internal call failing with a 4xx/5xx is not itself an unexpected error worth masking as a
     * generic 500 — it usually carries a real PlatformErrorResponse body (e.g. field validation errors) that the
     * caller needs to see. Re-parse that body and re-throw as a PlatformServiceException so
     * PlatformExceptionHandler propagates the original status, message, and fieldErrors instead of collapsing
     * everything into "An unexpected internal error occurred."
     */
    private PlatformServiceException translateDownstreamError(String serviceKey, HttpStatusCodeException ex) {
        String body = ex.getResponseBodyAsString();
        JsonNode node = null;
        if (body != null && !body.isBlank()) {
            try {
                node = objectMapper.readTree(body);
            } catch (Exception ignored) {
                node = null;
            }
        }
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) status = HttpStatus.BAD_GATEWAY;
        String message = node != null && node.hasNonNull("message") ? node.get("message").asText() : serviceKey + " returned " + ex.getStatusCode().value();
        Map<String, Object> details = new LinkedHashMap<>();
        if (node != null && node.has("fieldErrors") && node.get("fieldErrors").isArray()) {
            List<Map<String, Object>> validationErrors = new ArrayList<>();
            for (JsonNode fieldError : node.get("fieldErrors")) {
                validationErrors.add(Map.of(
                        "field", fieldError.path("field").asText(""),
                        "message", fieldError.path("message").asText("Invalid value")
                ));
            }
            if (!validationErrors.isEmpty()) details.put("validationErrors", validationErrors);
        }
        details.put("sourceService", serviceKey);
        PlatformErrorCode code = status == HttpStatus.NOT_FOUND ? PlatformErrorCode.RESOURCE_NOT_FOUND
                : status.is4xxClientError() ? PlatformErrorCode.VALIDATION_ERROR
                : PlatformErrorCode.DOWNSTREAM_SERVICE_ERROR;
        return new PlatformServiceException(code, status, message, message, details, ex);
    }

    private URI resolveBaseUri(ServiceInstance instance) {
        return instance.getUri();
    }
}

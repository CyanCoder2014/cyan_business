package com.cyancoder.commerce.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class ProcessorSupport {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;
    private final String processorBaseUrl;

    public ProcessorSupport(ObjectMapper objectMapper, @Value("${processor.service.base-url:http://localhost:9108}") String processorBaseUrl) {
        this.objectMapper = objectMapper;
        this.processorBaseUrl = processorBaseUrl;
    }

    public <T> T apply(String processorKey, String targetType, T payload, Class<T> type) {
        if (processorKey == null || processorKey.isBlank()) {
            return payload;
        }
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("targetType", targetType);
        request.put("payload", objectMapper.convertValue(payload, Map.class));
        ResponseEntity<ProcessorResponse> response = restTemplate.exchange(
                processorBaseUrl + "/api/processor-service/processors/" + processorKey + "/run",
                HttpMethod.POST,
                new HttpEntity<>(request),
                ProcessorResponse.class
        );
        ProcessorResponse body = response.getBody();
        if (body == null) {
            throw new ResponseStatusException(BAD_REQUEST, "processor returned empty response");
        }
        if (!body.valid()) {
            throw new ResponseStatusException(BAD_REQUEST, String.join("; ", body.errors() == null ? List.of("submission rejected") : body.errors()));
        }
        return objectMapper.convertValue(body.payload(), type);
    }

    public record ProcessorResponse(boolean valid, List<String> errors, Map<String, Object> payload) {
    }
}

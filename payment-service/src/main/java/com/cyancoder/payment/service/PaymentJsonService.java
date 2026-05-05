package com.cyancoder.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Service
public class PaymentJsonService {
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private static final TypeReference<Map<String, String>> STRING_MAP_TYPE = new TypeReference<>() {};
    private static final TypeReference<Set<String>> STRING_SET_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public PaymentJsonService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Collections.emptyMap() : value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not serialize json payload", e);
        }
    }

    public Map<String, Object> readObjectMap(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(value, MAP_TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not parse object json", e);
        }
    }

    public Map<String, String> readStringMap(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(value, STRING_MAP_TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not parse string map json", e);
        }
    }

    public Set<String> readStringSet(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptySet();
        }
        try {
            return objectMapper.readValue(value, STRING_SET_TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not parse string set json", e);
        }
    }
}

package com.cyancoder.batchworker.service;

import com.cyancoder.batchworker.api.BatchDefinitionRequest;
import com.cyancoder.batchworker.api.BatchDefinitionSpec;
import com.cyancoder.batchworker.domain.BatchDefinition;
import com.cyancoder.batchworker.repository.BatchDefinitionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BatchDefinitionService {
    private final BatchDefinitionRepository repository;
    private final ObjectMapper objectMapper;

    public BatchDefinitionService(BatchDefinitionRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public BatchDefinition save(String tenant, String site, BatchDefinitionRequest request) {
        validate(request.spec());
        BatchDefinition definition = repository
                .findByTenantKeyAndSiteKeyAndDefinitionKey(tenant, site, request.definitionKey())
                .orElseGet(BatchDefinition::new);
        Instant now = Instant.now();
        if (definition.getCreatedAt() == null) {
            definition.setCreatedAt(now);
        }
        definition.setTenantKey(tenant);
        definition.setSiteKey(site);
        definition.setDefinitionKey(request.definitionKey());
        definition.setTitle(request.title());
        definition.setActive(request.active() == null || request.active());
        definition.setUpdatedAt(now);
        try {
            definition.setSpecJson(objectMapper.writeValueAsString(request.spec()));
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid batch definition", exception);
        }
        return repository.save(definition);
    }

    public BatchDefinition get(String tenant, String site, String key) {
        return repository.findByTenantKeyAndSiteKeyAndDefinitionKey(tenant, site, key)
                .orElseThrow(() -> new IllegalArgumentException("Batch definition not found: " + key));
    }

    public BatchDefinitionSpec spec(BatchDefinition definition) {
        try {
            return objectMapper.readValue(definition.getSpecJson(), BatchDefinitionSpec.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored batch definition is invalid", exception);
        }
    }

    private void validate(BatchDefinitionSpec spec) {
        if (spec.source() == null || blank(spec.source().url())) {
            throw new IllegalArgumentException("spec.source.url is required");
        }
        if (spec.destination() == null || blank(spec.destination().url())) {
            throw new IllegalArgumentException("spec.destination.url is required");
        }
        String method = spec.destination().method();
        if (method != null && !method.equalsIgnoreCase("POST") && !method.equalsIgnoreCase("PUT")) {
            throw new IllegalArgumentException("Destination method must be POST or PUT");
        }
        validateAuthentication(
                spec.source().bearerTokenEnvironmentVariable(),
                spec.source().authentication(),
                "spec.source");
        validateAuthentication(
                spec.destination().bearerTokenEnvironmentVariable(),
                spec.destination().authentication(),
                "spec.destination");
    }

    private void validateAuthentication(
            String legacyBearerEnvironmentVariable,
            BatchDefinitionSpec.Authentication authentication,
            String path
    ) {
        if (!blank(legacyBearerEnvironmentVariable) && authentication != null) {
            throw new IllegalArgumentException(
                    path + " cannot configure bearerTokenEnvironmentVariable and authentication together");
        }
        if (authentication == null) {
            return;
        }
        String type = authentication.type() == null
                ? ""
                : authentication.type().trim().toUpperCase();
        if (!type.equals("BASIC") && !type.equals("BEARER")) {
            throw new IllegalArgumentException(path + ".authentication.type must be BASIC or BEARER");
        }
        if (blank(authentication.secretEnvironmentVariable())) {
            throw new IllegalArgumentException(
                    path + ".authentication.secretEnvironmentVariable is required");
        }
        if (type.equals("BASIC")
                && blank(authentication.username())
                && blank(authentication.usernameEnvironmentVariable())) {
            throw new IllegalArgumentException(
                    path + ".authentication requires username or usernameEnvironmentVariable");
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}

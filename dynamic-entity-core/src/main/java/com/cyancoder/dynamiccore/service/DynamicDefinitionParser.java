package com.cyancoder.dynamiccore.service;

import com.cyancoder.dynamiccore.model.EntityDefinitionModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DynamicDefinitionParser {

    private final ObjectMapper objectMapper;

    public DynamicDefinitionParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public EntityDefinitionModel parse(String json) {
        try {
            return objectMapper.readValue(json, EntityDefinitionModel.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid definition json", ex);
        }
    }

    public <T> T parse(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid json", ex);
        }
    }

    public String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid object to json", ex);
        }
    }
}

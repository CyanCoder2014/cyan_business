package com.cyancoder.bpm.api.dto;

public record MetadataFieldDescriptor(
        String key,
        String type,
        boolean required,
        String description,
        Object example
) {
}

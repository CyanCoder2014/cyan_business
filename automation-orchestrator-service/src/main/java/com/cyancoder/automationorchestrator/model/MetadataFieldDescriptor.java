package com.cyancoder.automationorchestrator.model;

public record MetadataFieldDescriptor(String key, String type, boolean required, String description, Object example) {
}

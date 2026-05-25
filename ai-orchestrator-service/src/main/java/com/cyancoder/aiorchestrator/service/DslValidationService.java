package com.cyancoder.aiorchestrator.service;

import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;

import java.util.Map;

public interface DslValidationService {
    void validate(PlatformAppDslDefinition dsl, Map<String, Object> platformMetadata);
}


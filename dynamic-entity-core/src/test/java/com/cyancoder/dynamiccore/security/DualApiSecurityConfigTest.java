package com.cyancoder.dynamiccore.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DualApiSecurityConfigTest {

    @Test
    void buildsQualifiedEndpointPatternFromFixedServiceKey() {
        assertEquals(
                "/api/bpm-service/endpoint/**",
                DualApiSecurityConfig.qualifiedEndpointPattern("bpm-service")
        );
    }
}

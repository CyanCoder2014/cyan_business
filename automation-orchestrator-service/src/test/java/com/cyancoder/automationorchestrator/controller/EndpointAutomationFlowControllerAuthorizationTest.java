package com.cyancoder.automationorchestrator.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EndpointAutomationFlowControllerAuthorizationTest {
    @Test
    void automationDefinitionEndpointsUseBuilderAccess() {
        PreAuthorize authorization = EndpointAutomationFlowController.class.getAnnotation(PreAuthorize.class);

        assertEquals("@platformAuthorizationService.canUseCapability('builder:use')", authorization.value());
    }
}

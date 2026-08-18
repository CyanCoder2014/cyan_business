package com.cyancoder.automationorchestrator.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EndpointAutomationFlowControllerAuthorizationTest {
    @Test
    void automationDefinitionReadsUseCanonicalAutomationAccess() throws NoSuchMethodException {
        PreAuthorize authorization = EndpointAutomationFlowController.class
                .getDeclaredMethod("list", String.class, String.class)
                .getAnnotation(PreAuthorize.class);

        assertEquals("@platformAuthorizationService.hasAnyPermission('automation.read','automation.manage','builder:*')", authorization.value());
    }
}

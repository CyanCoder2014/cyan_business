package com.cyancoder.bpm.api;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EndpointManagedObjectFlowControllerAuthorizationTest {
    @Test
    void queueReadsAcceptCanonicalBpmPermissions() {
        assertEquals(
                "@platformAuthorizationService.hasAnyPermission('bpm.read','bpm.transition','bpm.manage','operations:*')",
                authorization("getVisibleToCurrentUser")
        );
    }

    @Test
    void transitionsRequireCanonicalBpmOperationPermission() {
        assertEquals(
                "@platformAuthorizationService.hasAnyPermission('bpm.transition','bpm.manage','operations:*')",
                authorization("transition")
        );
    }

    private String authorization(String methodName) {
        for (Method method : EndpointManagedObjectFlowController.class.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method.getAnnotation(PreAuthorize.class).value();
            }
        }
        throw new AssertionError("Method not found: " + methodName);
    }
}

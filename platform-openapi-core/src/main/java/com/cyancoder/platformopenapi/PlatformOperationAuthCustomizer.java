package com.cyancoder.platformopenapi;

import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;

public class PlatformOperationAuthCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        PlatformOpenApiAuth methodAuth = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(), PlatformOpenApiAuth.class);
        PlatformOpenApiAuth controllerAuth = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getBeanType(), PlatformOpenApiAuth.class);
        PlatformOpenApiAuth resolved = methodAuth == null ? controllerAuth : methodAuth;
        if (resolved != null) {
            PlatformOpenApiCustomizer.applySecurity(operation, resolved.value());
        }
        return operation;
    }
}

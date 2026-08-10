package com.cyancoder.aiorchestrator.service;

import org.springframework.stereotype.Component;

@Component
public class AiSecretReferenceResolver {
    public String resolve(String reference) {
        if (reference == null || reference.isBlank()) return null;
        if (reference.startsWith("env:")) {
            String value = System.getenv(reference.substring(4));
            return value == null || value.isBlank() ? null : value;
        }
        // Kubernetes/Vault secret injection must materialize as configuration; never fetch or expose it from the browser.
        return null;
    }
}

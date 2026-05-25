package com.cyancoder.dynamiccore.runtime;

public final class DynamicScopeResolver {
    private DynamicScopeResolver() {
    }

    public static DynamicScope fromHeaders(String tenantKey, String siteKey) {
        return new DynamicScope(normalize(tenantKey), normalize(siteKey));
    }

    public static DynamicScope fromRequest(String tenantKey, String siteKey) {
        return new DynamicScope(normalize(tenantKey), normalize(siteKey));
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}

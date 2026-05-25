package com.cyancoder.bpm.api.dto;

public final class FlowScopeResolver {
    private FlowScopeResolver() {
    }

    public static BpmScope fromHeaders(String tenantKey, String siteKey) {
        return new BpmScope(blankToNull(tenantKey), blankToNull(siteKey));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}


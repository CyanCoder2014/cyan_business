package com.cyancoder.aiorchestrator.api.dto;

public record ProvisionDraftRequest(
        String mode,
        String idempotencyKey,
        String triggerType,
        String triggeredBy
) {
}

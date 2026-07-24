package com.cyancoder.aiorchestrator.service;

import java.util.List;

public record ServiceAvailabilitySnapshot(
        List<String> availableServiceKeys,
        String source
) {
}

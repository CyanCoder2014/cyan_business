package com.cyancoder.bpm.api.dto;

public record TransitionConditionOperatorDescriptor(
        String key,
        String valueShape,
        String description,
        Object example
) {
}

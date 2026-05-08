package com.cyancoder.bpm.api.dto;

public record TransitionOptionResponse(String transitionId, String label, String fromState, String toState) {
}


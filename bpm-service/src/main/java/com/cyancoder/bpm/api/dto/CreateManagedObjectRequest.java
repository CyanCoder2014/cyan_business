package com.cyancoder.bpm.api.dto;

import com.cyancoder.bpm.domain.ManagedObjectRef;

import java.util.Map;

public record CreateManagedObjectRequest(String flowKey, String objectType, String title, ManagedObjectRef objectRef, Map<String, Object> payload) {
}


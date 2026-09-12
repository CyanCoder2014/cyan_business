package com.cyancoder.bpm.api.dto;
import java.util.Map;
public record ApplicantActiveFormResponse(String objectId, String state, String formKey, Map<String, Object> rendererDefinition) { }

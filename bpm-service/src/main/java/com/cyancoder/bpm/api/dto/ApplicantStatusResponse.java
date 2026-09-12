package com.cyancoder.bpm.api.dto;
import java.util.Map;
public record ApplicantStatusResponse(String objectId, String flowKey, String state, boolean terminal, Map<String, Object> data) { }

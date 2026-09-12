package com.cyancoder.bpm.api.dto;
import java.util.Map;
public record ApplicantStartRequest(String flowKey, Map<String, Object> payload) { }

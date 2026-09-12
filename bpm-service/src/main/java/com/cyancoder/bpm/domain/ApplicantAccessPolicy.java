package com.cyancoder.bpm.domain;

import java.util.Map;
import java.util.Set;

/** Definition-owned public/applicant surface. It never grants operator permissions. */
public record ApplicantAccessPolicy(Boolean enabled, String objectType, Set<String> allowedRoles,
                                    Set<String> startPayloadFields, Set<String> formStateIds,
                                    Map<String, String> formNextStates, Set<String> statusPayloadFields) {
    public boolean isEnabled() { return Boolean.TRUE.equals(enabled); }
}

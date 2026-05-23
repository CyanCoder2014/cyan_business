package com.cyancoder.sso.common.dto;

import java.util.List;

public record UserClientRoleAssignmentSummary(
        String username,
        String clientId,
        List<String> roles
) {
}

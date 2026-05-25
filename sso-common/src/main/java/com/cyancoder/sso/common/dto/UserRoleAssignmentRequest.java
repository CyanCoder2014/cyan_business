package com.cyancoder.sso.common.dto;

public record UserRoleAssignmentRequest(
        String username,
        String roleKey
) {
}

package com.cyancoder.sso.common.dto;

import jakarta.validation.constraints.NotBlank;

public record SessionRenewRequest(
        @NotBlank String username,
        @NotBlank String clientId
) {
}

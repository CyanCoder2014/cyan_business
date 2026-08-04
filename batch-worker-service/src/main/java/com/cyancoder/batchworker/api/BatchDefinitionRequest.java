package com.cyancoder.batchworker.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BatchDefinitionRequest(
        @NotBlank String definitionKey,
        @NotBlank String title,
        @NotNull @Valid BatchDefinitionSpec spec,
        Boolean active
) {}

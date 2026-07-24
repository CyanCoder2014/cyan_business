package com.cyancoder.batchworker.api;

import jakarta.validation.constraints.NotBlank;

public record StartBatchRequest(@NotBlank String runKey) {}

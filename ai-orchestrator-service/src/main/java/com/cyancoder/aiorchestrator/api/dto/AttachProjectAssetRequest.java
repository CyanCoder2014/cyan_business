package com.cyancoder.aiorchestrator.api.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
public record AttachProjectAssetRequest(@NotBlank String assetKey, @NotBlank String fileName, @NotBlank String mimeType, @Positive long sizeBytes) {}

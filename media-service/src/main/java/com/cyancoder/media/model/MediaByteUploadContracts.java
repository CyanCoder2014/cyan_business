package com.cyancoder.media.model;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
public final class MediaByteUploadContracts {
    private MediaByteUploadContracts() {}
    public record PrepareRequest(@NotBlank String originalFileName, @NotBlank String mimeType, @Positive long sizeBytes, String visibility) {}
    public record UploadResponse(String uploadId, String assetKey, String uploadUrl, String method, String status, long expectedSizeBytes, long uploadedSizeBytes, Instant expiresAt, String deliveryUrl) {}
    public record GeneratedAssetRequest(@NotBlank String fileName,@NotBlank String mimeType,@NotBlank String base64,
                                        @NotBlank String generatedBy,Integer retentionDays) {}
}

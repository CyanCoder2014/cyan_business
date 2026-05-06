package com.cyancoder.media.model;

public record MediaUploadPrepareRequest(
        String assetKey,
        String assetType,
        String originalFileName,
        String mimeType,
        String visibility,
        String altText,
        String caption,
        String title,
        String license,
        String bucket,
        String path,
        Integer width,
        Integer height,
        Long sizeBytes
) {
}

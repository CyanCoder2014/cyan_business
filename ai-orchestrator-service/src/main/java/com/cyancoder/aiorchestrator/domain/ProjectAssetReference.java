package com.cyancoder.aiorchestrator.domain;
import java.time.Instant;
public record ProjectAssetReference(String assetKey, String fileName, String mimeType, long sizeBytes, String attachedBy, Instant attachedAt) {}

package com.cyancoder.media.model;

import java.util.Map;

public record MediaAssetResponse(
        String assetKey,
        String deliveryUrl,
        String status,
        Map<String, Object> data
) {
}

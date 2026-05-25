package com.cyancoder.media.controller;

import com.cyancoder.media.model.MediaAssetResponse;
import com.cyancoder.media.service.MediaAssetService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/media/assets")
public class PublicMediaAssetController {
    private final MediaAssetService mediaAssetService;

    public PublicMediaAssetController(MediaAssetService mediaAssetService) {
        this.mediaAssetService = mediaAssetService;
    }

    @GetMapping("/{assetKey}")
    public MediaAssetResponse get(@PathVariable String assetKey) {
        return mediaAssetService.get(assetKey);
    }

    @GetMapping("/{assetKey}/variants/{variantKey}")
    public MediaAssetResponse getVariant(@PathVariable String assetKey, @PathVariable String variantKey) {
        return mediaAssetService.getVariant(assetKey, variantKey);
    }
}

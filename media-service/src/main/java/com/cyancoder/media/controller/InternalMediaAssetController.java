package com.cyancoder.media.controller;

import com.cyancoder.media.model.MediaAssetResponse;
import com.cyancoder.media.model.MediaUploadPrepareRequest;
import com.cyancoder.media.service.MediaAssetService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/media/assets")
public class InternalMediaAssetController {
    private final MediaAssetService mediaAssetService;

    public InternalMediaAssetController(MediaAssetService mediaAssetService) {
        this.mediaAssetService = mediaAssetService;
    }

    @PostMapping("/prepare-upload")
    public MediaAssetResponse prepareUpload(@RequestBody MediaUploadPrepareRequest request) {
        return mediaAssetService.prepareUpload(request);
    }

    @GetMapping("/{assetKey}")
    public MediaAssetResponse get(@PathVariable String assetKey) {
        return mediaAssetService.get(assetKey);
    }
}

package com.cyancoder.media.controller;

import com.cyancoder.media.model.MediaAssetResponse;
import com.cyancoder.media.model.MediaUploadPrepareRequest;
import com.cyancoder.media.service.MediaAssetService;
import com.cyancoder.dynamiccore.runtime.DynamicScope;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/media/assets")
public class InternalMediaAssetController {
    private final MediaAssetService mediaAssetService;

    public InternalMediaAssetController(MediaAssetService mediaAssetService) {
        this.mediaAssetService = mediaAssetService;
    }

    @PostMapping("/prepare-upload")
    public MediaAssetResponse prepareUpload(@RequestBody MediaUploadPrepareRequest request,
            @RequestHeader(value="X-Tenant-Key",required=false) String tenantKey,
            @RequestHeader(value="X-Site-Key",required=false) String siteKey) {
        return mediaAssetService.prepareUpload(request, new DynamicScope(tenantKey,siteKey));
    }

    @GetMapping("/{assetKey}")
    public MediaAssetResponse get(@PathVariable String assetKey,
            @RequestHeader(value="X-Tenant-Key",required=false) String tenantKey,
            @RequestHeader(value="X-Site-Key",required=false) String siteKey) {
        return mediaAssetService.get(assetKey, new DynamicScope(tenantKey,siteKey));
    }
}

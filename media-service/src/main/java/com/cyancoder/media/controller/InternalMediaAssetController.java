package com.cyancoder.media.controller;

import com.cyancoder.media.model.MediaAssetResponse;
import com.cyancoder.media.model.MediaUploadPrepareRequest;
import com.cyancoder.media.service.MediaAssetService;
import com.cyancoder.media.service.MediaByteUploadService;
import com.cyancoder.dynamiccore.runtime.DynamicScope;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/media/assets")
public class InternalMediaAssetController {
    private final MediaAssetService mediaAssetService;
    private final MediaByteUploadService byteUploadService;
    private final com.cyancoder.media.service.MediaReferenceService references;

    public InternalMediaAssetController(MediaAssetService mediaAssetService, MediaByteUploadService byteUploadService, com.cyancoder.media.service.MediaReferenceService references) {
        this.mediaAssetService = mediaAssetService;
        this.byteUploadService = byteUploadService;
        this.references = references;
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

    @GetMapping("/{assetKey}/content")
    public org.springframework.http.ResponseEntity<byte[]> content(
            @PathVariable String assetKey,
            @RequestHeader(value="X-Tenant-Key",required=false) String tenantKey,
            @RequestHeader(value="X-Site-Key",required=false) String siteKey,
            @RequestParam(defaultValue="26214400") long maxBytes,
            @RequestParam(required=false) java.util.Set<String> allowedMimeType) {
        var content = byteUploadService.readInternal(assetKey, tenantKey, siteKey, maxBytes, allowedMimeType);
        return org.springframework.http.ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(content.mimeType()))
                .contentLength(content.sizeBytes())
                .header("X-Media-Asset-Key", content.assetKey())
                .header("X-Media-File-Name", content.fileName())
                .body(content.bytes());
    }

    @PostMapping("/generated")
    public com.cyancoder.media.model.MediaByteUploadContracts.UploadResponse generated(
            @jakarta.validation.Valid @RequestBody com.cyancoder.media.model.MediaByteUploadContracts.GeneratedAssetRequest request,
            @RequestHeader(value="X-Tenant-Key",required=false) String tenantKey,
            @RequestHeader(value="X-Site-Key",required=false) String siteKey) {
        return byteUploadService.ingestGenerated(request,tenantKey,siteKey);
    }
    @PutMapping("/{assetKey}/references") public java.util.Map<String,Object> register(@PathVariable String assetKey,@RequestHeader("X-Tenant-Key")String tenant,@RequestHeader(value="X-Site-Key",required=false)String site,@RequestBody com.cyancoder.media.service.MediaReferenceService.ReferenceRequest request){return references.register(tenant,site,assetKey,request);}
    @DeleteMapping("/{assetKey}/references") public void unregister(@PathVariable String assetKey,@RequestHeader("X-Tenant-Key")String tenant,@RequestHeader(value="X-Site-Key",required=false)String site,@RequestBody com.cyancoder.media.service.MediaReferenceService.ReferenceRequest request){references.unregister(tenant,site,assetKey,request);}
    @GetMapping("/{assetKey}/references") public java.util.List<java.util.Map<String,Object>> references(@PathVariable String assetKey,@RequestHeader("X-Tenant-Key")String tenant,@RequestHeader(value="X-Site-Key",required=false)String site){return references.list(tenant,site,assetKey);}
}

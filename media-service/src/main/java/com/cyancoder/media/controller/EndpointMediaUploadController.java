package com.cyancoder.media.controller;

import com.cyancoder.media.model.MediaByteUploadContracts.PrepareRequest;
import com.cyancoder.media.model.MediaByteUploadContracts.UploadResponse;
import com.cyancoder.media.service.MediaByteUploadService;
import com.cyancoder.media.service.TenantMembershipClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController
@RequestMapping("/endpoint/media/uploads")
public class EndpointMediaUploadController {
    private final MediaByteUploadService service;
    private final TenantMembershipClient memberships;
    public EndpointMediaUploadController(MediaByteUploadService service,TenantMembershipClient memberships) { this.service = service; this.memberships=memberships; }
    @PostMapping("/prepare")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('media:write')")
    public UploadResponse prepare(@Valid @RequestBody PrepareRequest request, @RequestHeader("X-Tenant-Key") String tenantKey, @RequestHeader(value = "X-Site-Key", required = false) String siteKey, Authentication authentication) {
        memberships.requireMembership(tenantKey,authentication.getName());
        return service.prepare(request, tenantKey, siteKey, authentication.getName());
    }
    @PutMapping("/{uploadId}")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('media:write')")
    public UploadResponse upload(@PathVariable String uploadId, @RequestHeader("X-Tenant-Key") String tenantKey, @RequestHeader(value = "X-Site-Key", required = false) String siteKey, @RequestHeader("Content-Length") long contentLength, Authentication authentication, HttpServletRequest request) throws IOException {
        memberships.requireMembership(tenantKey,authentication.getName());
        return service.upload(uploadId, tenantKey, siteKey, authentication.getName(), request.getInputStream(), contentLength);
    }
    @DeleteMapping("/{uploadId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@platformAuthorizationService.canUseCapability('media:write')")
    public void cancel(@PathVariable String uploadId, @RequestHeader("X-Tenant-Key") String tenantKey, @RequestHeader(value = "X-Site-Key", required = false) String siteKey, Authentication authentication) {
        memberships.requireMembership(tenantKey,authentication.getName());
        service.cancel(uploadId, tenantKey, siteKey, authentication.getName());
    }
}

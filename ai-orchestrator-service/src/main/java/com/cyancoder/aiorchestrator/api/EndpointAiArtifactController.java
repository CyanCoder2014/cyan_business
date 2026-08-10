package com.cyancoder.aiorchestrator.api;

import com.cyancoder.aiorchestrator.api.dto.AiArtifactContracts.ArtifactJobView;
import com.cyancoder.aiorchestrator.api.dto.AiArtifactContracts.CreateArtifactJobRequest;
import com.cyancoder.aiorchestrator.service.AiArtifactJobService;
import com.cyancoder.aiorchestrator.service.AiTenantAuthorizationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/endpoint/ai-orchestrator/artifact-jobs")
@PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
public class EndpointAiArtifactController {
    private final AiArtifactJobService service;
    private final AiTenantAuthorizationService authorization;
    public EndpointAiArtifactController(AiArtifactJobService service,AiTenantAuthorizationService authorization) { this.service=service;this.authorization=authorization; }
    @PostMapping public ArtifactJobView start(@RequestHeader("X-Tenant-Key") String tenant,@RequestHeader(value="X-Site-Key",required=false) String site,Authentication authentication,@Valid @RequestBody CreateArtifactJobRequest request){authorization.requireExecute(tenant,site,authentication.getName());return service.start(tenant,site,authentication.getName(),request);}
    @GetMapping public List<ArtifactJobView> list(@RequestHeader("X-Tenant-Key") String tenant,@RequestHeader(value="X-Site-Key",required=false) String site,Authentication authentication){authorization.requireRead(tenant,site,authentication.getName());return service.list(tenant,site);}
    @GetMapping("/{id}") public ArtifactJobView get(@PathVariable String id,@RequestHeader("X-Tenant-Key") String tenant,@RequestHeader(value="X-Site-Key",required=false) String site,Authentication authentication){authorization.requireRead(tenant,site,authentication.getName());return service.get(tenant,site,id);}
    @PostMapping("/{id}/cancel") public ArtifactJobView cancel(@PathVariable String id,@RequestHeader("X-Tenant-Key") String tenant,@RequestHeader(value="X-Site-Key",required=false) String site,Authentication authentication){authorization.requireExecute(tenant,site,authentication.getName());return service.cancel(tenant,site,id);}
}

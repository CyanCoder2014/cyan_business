package com.cyancoder.aiorchestrator.api;

import com.cyancoder.aiorchestrator.api.dto.AiProviderProfileContracts.ProfileView;
import com.cyancoder.aiorchestrator.api.dto.AiProviderProfileContracts.SaveProfileRequest;
import com.cyancoder.aiorchestrator.service.AiProviderProfileService;
import com.cyancoder.aiorchestrator.service.AiTenantAuthorizationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/endpoint/ai-orchestrator/provider-profiles")
@PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
public class EndpointAiProviderProfileController {
    private final AiProviderProfileService service; private final AiTenantAuthorizationService authorization; public EndpointAiProviderProfileController(AiProviderProfileService service,AiTenantAuthorizationService authorization){this.service=service;this.authorization=authorization;}
    @GetMapping public List<ProfileView> list(@RequestHeader("X-Tenant-Key")String tenant,@RequestHeader(value="X-Site-Key",required=false)String site,Authentication auth){authorization.requireRead(tenant,site,auth.getName());return service.list(tenant,site);}
    @PutMapping("/{profileKey}") public ProfileView save(@PathVariable String profileKey,@RequestHeader("X-Tenant-Key")String tenant,@RequestHeader(value="X-Site-Key",required=false)String site,Authentication auth,@Valid @RequestBody SaveProfileRequest request){authorization.requireExecute(tenant,site,auth.getName());if(!profileKey.equals(request.profileKey()))throw new IllegalArgumentException("profileKey path/body mismatch");return service.save(tenant,site,auth.getName(),request);}
}

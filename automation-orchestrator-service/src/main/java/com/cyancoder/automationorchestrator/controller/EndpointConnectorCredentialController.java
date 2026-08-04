package com.cyancoder.automationorchestrator.controller;

import com.cyancoder.automationorchestrator.domain.ConnectorCredential;
import com.cyancoder.automationorchestrator.service.ConnectorCredentialService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/endpoint/automation-orchestrator/credentials")
@PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')")
public class EndpointConnectorCredentialController {
    private final ConnectorCredentialService service;
    public EndpointConnectorCredentialController(ConnectorCredentialService service) { this.service = service; }
    @PostMapping public ConnectorCredential save(@RequestHeader(value="X-Tenant-Key", required=false) String tenant, @RequestHeader(value="X-Site-Key", required=false) String site, @RequestBody ConnectorCredential value) { return service.save(tenant, site, value); }
    @GetMapping public List<ConnectorCredential> list(@RequestHeader(value="X-Tenant-Key", required=false) String tenant, @RequestHeader(value="X-Site-Key", required=false) String site) { return service.list(tenant, site); }
    @PatchMapping("/{id}/rotate") public ConnectorCredential rotate(@RequestHeader(value="X-Tenant-Key", required=false) String tenant,@RequestHeader(value="X-Site-Key", required=false) String site,@PathVariable String id,@RequestBody Map<String,Object> body){return service.rotate(tenant,site,id,body.get("secret")==null?null:body.get("secret").toString());}
}

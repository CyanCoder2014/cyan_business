package com.cyancoder.tenant.controller;

import com.cyancoder.platformopenapi.PlatformApiSecurity;
import com.cyancoder.platformopenapi.PlatformOpenApiAuth;
import com.cyancoder.tenant.api.TenantContracts.ClientProvisioningResult;
import com.cyancoder.tenant.api.TenantContracts.CreateClientRequest;
import com.cyancoder.tenant.api.TenantContracts.TenantSummary;
import com.cyancoder.tenant.service.ClientProvisioningService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/endpoint/clients")
@PlatformOpenApiAuth(PlatformApiSecurity.BEARER)
public class EndpointClientController {
    private final ClientProvisioningService service;
    public EndpointClientController(ClientProvisioningService service){this.service=service;}
    @GetMapping public List<TenantSummary> list(){return service.list();}
    @GetMapping("/capabilities/catalog") public List<String> capabilities(){return service.capabilityCatalog();}
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ClientProvisioningResult create(@Valid @RequestBody CreateClientRequest request,@RequestHeader("Idempotency-Key") String idempotencyKey){return service.create(request,idempotencyKey);}
}

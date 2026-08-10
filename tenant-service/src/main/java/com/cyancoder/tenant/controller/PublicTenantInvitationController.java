package com.cyancoder.tenant.controller;
import com.cyancoder.tenant.api.TenantContracts.AcceptInvitationRequest;import com.cyancoder.tenant.api.TenantContracts.TenantUserSummary;import com.cyancoder.tenant.service.TenantInvitationService;import jakarta.validation.Valid;import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/public/tenant-invitations")
public class PublicTenantInvitationController{private final TenantInvitationService service;public PublicTenantInvitationController(TenantInvitationService service){this.service=service;}@PostMapping("/accept")public TenantUserSummary accept(@Valid @RequestBody AcceptInvitationRequest request){return service.accept(request);}}

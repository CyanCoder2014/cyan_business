package com.cyancoder.tenant.service;

import com.cyancoder.tenant.api.TenantContracts.ClientProvisioningResult;
import com.cyancoder.tenant.api.TenantContracts.CreateClientRequest;
import com.cyancoder.tenant.api.TenantContracts.TenantSummary;
import com.cyancoder.tenant.api.TenantContracts.TenantUserSummary;
import com.cyancoder.tenant.model.IdempotencyRecordEntity;
import com.cyancoder.tenant.model.TenantCapabilityOverrideEntity;
import com.cyancoder.tenant.model.TenantEntity;
import com.cyancoder.tenant.model.TenantMembershipEntity;
import com.cyancoder.tenant.repository.IdempotencyRecordRepository;
import com.cyancoder.tenant.repository.TenantCapabilityOverrideRepository;
import com.cyancoder.tenant.repository.TenantMembershipRepository;
import com.cyancoder.tenant.repository.TenantRepository;
import com.cyancoder.tenant.security.TenantSecurity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ClientProvisioningService {
    private final TenantRepository tenants; private final TenantMembershipRepository memberships;
    private final TenantCapabilityOverrideRepository overrides; private final IdempotencyRecordRepository idempotency;
    private final TenantSecurity security; private final IdentityDirectoryClient identities; private final BillingEntitlementClient billing;

    public ClientProvisioningService(TenantRepository tenants, TenantMembershipRepository memberships,
                                     TenantCapabilityOverrideRepository overrides, IdempotencyRecordRepository idempotency,
                                     TenantSecurity security, IdentityDirectoryClient identities, BillingEntitlementClient billing) {
        this.tenants=tenants;this.memberships=memberships;this.overrides=overrides;this.idempotency=idempotency;
        this.security=security;this.identities=identities;this.billing=billing;
    }

    public List<TenantSummary> list() {
        requireAdmin();
        return tenants.findAll().stream().map(tenant -> new TenantSummary(tenant.getTenantKey(), tenant.getDisplayName(), tenant.getStatus(), null, tenant.getCreatedAt(), tenant.getUpdatedAt())).toList();
    }

    public List<String> capabilityCatalog() { requireAdmin(); return TenantCapabilityService.catalog(); }

    @Transactional
    public ClientProvisioningResult create(CreateClientRequest request, String idempotencyKey) {
        requireAdmin();
        if (idempotencyKey == null || idempotencyKey.isBlank()) throw new IllegalArgumentException("Idempotency-Key is required");
        String recordId = security.username()+"|client-create|"+idempotencyKey.trim();
        IdempotencyRecordEntity previous=idempotency.findById(recordId).orElse(null);
        if(previous!=null)return result(previous.getResourceKey(),request.planKey(),"ALREADY_PROVISIONED");
        if(tenants.existsById(request.tenantKey()))throw new IllegalArgumentException("Client key already exists");
        List<String> capabilities=request.capabilityKeys()==null?List.of():request.capabilityKeys().stream().distinct().toList();
        if(capabilities.stream().anyMatch(key->!TenantCapabilityService.isKnownCapability(key)))throw new IllegalArgumentException("Unknown capability key");

        var head=request.headUser();
        IdentityDirectoryClient.IdentityUser identity=identities.provision(head.username(),head.initialPassword(),head.email(),head.phoneNumber(),head.mfaRequired());
        Instant now=Instant.now();
        TenantEntity tenant=new TenantEntity();tenant.setTenantKey(request.tenantKey());tenant.setDisplayName(request.displayName().trim());tenant.setStatus("ACTIVE");tenant.setCreatedBy(security.username());tenant.setCreatedAt(now);tenant.setUpdatedAt(now);tenants.save(tenant);
        TenantMembershipEntity membership=new TenantMembershipEntity();membership.setMembershipId(request.tenantKey()+"|"+identity.username());membership.setTenantKey(request.tenantKey());membership.setUsername(identity.username());membership.setRoleKey("TENANT_OWNER");membership.setActive(true);membership.setCreatedAt(now);membership.setUpdatedAt(now);memberships.save(membership);
        capabilities.forEach(key->{TenantCapabilityOverrideEntity item=new TenantCapabilityOverrideEntity();item.setOverrideId(request.tenantKey()+"||"+key);item.setTenantKey(request.tenantKey());item.setCapabilityKey(key);item.setEnabled(true);item.setReason("Enabled during client provisioning");overrides.save(item);});
        billing.activateFreePlan(request.tenantKey(),request.planKey(),idempotencyKey);
        IdempotencyRecordEntity record=new IdempotencyRecordEntity();record.setRecordId(recordId);record.setResourceKey(request.tenantKey());record.setCreatedAt(now);idempotency.save(record);
        return result(request.tenantKey(),request.planKey(),"ACTIVE");
    }

    private ClientProvisioningResult result(String tenantKey,String planKey,String status){TenantEntity tenant=tenants.findById(tenantKey).orElseThrow();TenantMembershipEntity head=memberships.findByTenantKeyOrderByUsernameAsc(tenantKey).stream().filter(item->"TENANT_OWNER".equals(item.getRoleKey())).findFirst().orElseThrow();var identity=identities.get(head.getUsername());return new ClientProvisioningResult(new TenantSummary(tenantKey,tenant.getDisplayName(),tenant.getStatus(),null,tenant.getCreatedAt(),tenant.getUpdatedAt()),new TenantUserSummary(head.getUsername(),identity==null?null:identity.email(),identity==null?null:identity.phoneNumber(),head.getRoleKey(),head.isActive(),head.getCreatedAt(),head.getUpdatedAt()),overrides.findByTenantKey(tenantKey).stream().filter(TenantCapabilityOverrideEntity::isEnabled).map(TenantCapabilityOverrideEntity::getCapabilityKey).toList(),planKey,status);}
    private void requireAdmin(){if(!security.isPlatformAdmin())throw new org.springframework.security.access.AccessDeniedException("Platform administrator access is required");}
}

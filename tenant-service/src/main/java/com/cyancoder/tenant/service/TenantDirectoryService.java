package com.cyancoder.tenant.service;

import com.cyancoder.tenant.api.TenantContracts.CreateTenantRequest;
import com.cyancoder.tenant.api.TenantContracts.MembershipAccess;
import com.cyancoder.tenant.api.TenantContracts.TenantSummary;
import com.cyancoder.tenant.model.IdempotencyRecordEntity;
import com.cyancoder.tenant.model.TenantEntity;
import com.cyancoder.tenant.model.TenantMembershipEntity;
import com.cyancoder.tenant.repository.IdempotencyRecordRepository;
import com.cyancoder.tenant.repository.TenantMembershipRepository;
import com.cyancoder.tenant.repository.TenantRepository;
import com.cyancoder.tenant.security.TenantSecurity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TenantDirectoryService {
    private final TenantRepository tenantRepository;
    private final TenantMembershipRepository membershipRepository;
    private final IdempotencyRecordRepository idempotencyRepository;
    private final TenantSecurity security;

    public TenantDirectoryService(TenantRepository tenantRepository, TenantMembershipRepository membershipRepository, IdempotencyRecordRepository idempotencyRepository, TenantSecurity security) {
        this.tenantRepository = tenantRepository;
        this.membershipRepository = membershipRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.security = security;
    }

    public List<TenantSummary> listForCurrentUser() {
        return membershipRepository.findByUsernameAndActiveTrueOrderByTenantKeyAsc(security.username()).stream()
                .map(membership -> tenantRepository.findById(membership.getTenantKey())
                        .map(tenant -> summary(tenant, membership.getRoleKey())).orElse(null))
                .filter(java.util.Objects::nonNull).toList();
    }

    public TenantSummary getForCurrentUser(String tenantKey) {
        TenantMembershipEntity membership = requireMembership(tenantKey, security.username());
        return summary(tenantRepository.findById(tenantKey).orElseThrow(NoSuchElementException::new), membership.getRoleKey());
    }

    @Transactional
    public TenantSummary create(CreateTenantRequest request, String idempotencyKey) {
        String actor = security.username();
        if (idempotencyKey == null || idempotencyKey.isBlank()) throw new IllegalArgumentException("Idempotency-Key is required");
        String recordId = actor + "|tenant-create|" + idempotencyKey.trim();
        IdempotencyRecordEntity existing = idempotencyRepository.findById(recordId).orElse(null);
        if (existing != null) return getForCurrentUser(existing.getResourceKey());
        if (tenantRepository.existsById(request.tenantKey())) throw new IllegalArgumentException("Tenant key already exists");

        Instant now = Instant.now();
        TenantEntity tenant = new TenantEntity();
        tenant.setTenantKey(request.tenantKey());
        tenant.setDisplayName(request.displayName().trim());
        tenant.setStatus("ACTIVE");
        tenant.setCreatedBy(actor);
        tenant.setCreatedAt(now);
        tenant.setUpdatedAt(now);
        tenantRepository.save(tenant);

        TenantMembershipEntity membership = new TenantMembershipEntity();
        membership.setMembershipId(request.tenantKey() + "|" + actor);
        membership.setTenantKey(request.tenantKey());
        membership.setUsername(actor);
        membership.setRoleKey("TENANT_OWNER");
        membership.setActive(true);
        membership.setCreatedAt(now);
        membership.setUpdatedAt(now);
        membershipRepository.save(membership);

        IdempotencyRecordEntity record = new IdempotencyRecordEntity();
        record.setRecordId(recordId);
        record.setResourceKey(request.tenantKey());
        record.setCreatedAt(now);
        idempotencyRepository.save(record);
        return summary(tenant, membership.getRoleKey());
    }

    public MembershipAccess internalMembership(String tenantKey, String username) {
        TenantMembershipEntity membership = membershipRepository.findByTenantKeyAndUsernameAndActiveTrue(tenantKey, username)
                .orElseThrow(() -> new AccessDeniedException("Tenant membership is required"));
        return new MembershipAccess(tenantKey, username, membership.getRoleKey(), true);
    }

    public void requireCurrentMembership(String tenantKey) { requireMembership(tenantKey, security.username()); }

    private TenantMembershipEntity requireMembership(String tenantKey, String username) {
        return membershipRepository.findByTenantKeyAndUsernameAndActiveTrue(tenantKey, username)
                .orElseThrow(() -> new AccessDeniedException("Tenant membership is required"));
    }

    private TenantSummary summary(TenantEntity tenant, String role) {
        return new TenantSummary(tenant.getTenantKey(), tenant.getDisplayName(), tenant.getStatus(), role, tenant.getCreatedAt(), tenant.getUpdatedAt());
    }
}

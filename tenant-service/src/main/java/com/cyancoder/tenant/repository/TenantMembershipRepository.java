package com.cyancoder.tenant.repository;

import com.cyancoder.tenant.model.TenantMembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TenantMembershipRepository extends JpaRepository<TenantMembershipEntity, String> {
    List<TenantMembershipEntity> findByUsernameAndActiveTrueOrderByTenantKeyAsc(String username);
    Optional<TenantMembershipEntity> findByTenantKeyAndUsernameAndActiveTrue(String tenantKey, String username);
}

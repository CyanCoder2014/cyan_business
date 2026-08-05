package com.cyancoder.tenant.repository;

import com.cyancoder.tenant.model.TenantFeatureFlagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TenantFeatureFlagRepository extends JpaRepository<TenantFeatureFlagEntity, String> {
    List<TenantFeatureFlagEntity> findByTenantKeyOrderByFlagKeyAsc(String tenantKey);
}

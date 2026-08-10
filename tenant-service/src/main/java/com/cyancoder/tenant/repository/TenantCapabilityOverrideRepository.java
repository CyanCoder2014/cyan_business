package com.cyancoder.tenant.repository;

import com.cyancoder.tenant.model.TenantCapabilityOverrideEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TenantCapabilityOverrideRepository extends JpaRepository<TenantCapabilityOverrideEntity, String> {
    List<TenantCapabilityOverrideEntity> findByTenantKey(String tenantKey);
}

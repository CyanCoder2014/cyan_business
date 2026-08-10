package com.cyancoder.tenant.repository;

import com.cyancoder.tenant.model.TenantRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TenantRoleRepository extends JpaRepository<TenantRoleEntity, String> {
    List<TenantRoleEntity> findByTenantKeyOrderBySystemRoleDescDisplayNameAsc(String tenantKey);
    Optional<TenantRoleEntity> findByTenantKeyAndRoleKey(String tenantKey, String roleKey);
    long countByTenantKeyAndRoleKey(String tenantKey, String roleKey);
}

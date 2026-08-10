package com.cyancoder.tenant.repository;

import com.cyancoder.tenant.model.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<TenantEntity, String> {}

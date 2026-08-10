package com.cyancoder.storefront.repository;

import com.cyancoder.storefront.model.SiteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface SiteRepository extends JpaRepository<SiteEntity, Long> {
    List<SiteEntity> findAllByTenantKeyOrderByNameAsc(String tenantKey);
    Optional<SiteEntity> findByTenantKeyAndSiteKey(String tenantKey, String siteKey);
    boolean existsByTenantKeyAndSiteKey(String tenantKey, String siteKey);
}

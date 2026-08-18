package com.cyancoder.storefront.repository;

import com.cyancoder.storefront.model.PublishedFormEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublishedFormRepository extends JpaRepository<PublishedFormEntity, Long> {
    Optional<PublishedFormEntity> findBySlug(String slug);
    List<PublishedFormEntity> findAllByTenantKeyAndSiteKeyOrderByUpdatedAtDesc(String tenantKey, String siteKey);
}

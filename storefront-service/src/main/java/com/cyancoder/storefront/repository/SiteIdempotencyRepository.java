package com.cyancoder.storefront.repository;

import com.cyancoder.storefront.model.SiteIdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SiteIdempotencyRepository extends JpaRepository<SiteIdempotencyEntity, Long> {
    Optional<SiteIdempotencyEntity> findByActorAndIdempotencyKey(String actor, String idempotencyKey);
}

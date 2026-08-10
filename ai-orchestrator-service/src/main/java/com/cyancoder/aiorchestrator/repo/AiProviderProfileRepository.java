package com.cyancoder.aiorchestrator.repo;

import com.cyancoder.aiorchestrator.domain.AiProviderProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AiProviderProfileRepository extends MongoRepository<AiProviderProfile,String> {
    List<AiProviderProfile> findByTenantKeyAndSiteKeyOrderByDisplayNameAsc(String tenantKey,String siteKey);
    Optional<AiProviderProfile> findByTenantKeyAndSiteKeyAndProfileKey(String tenantKey,String siteKey,String profileKey);
}

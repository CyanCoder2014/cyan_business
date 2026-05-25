package com.cyancoder.aiorchestrator.repo;

import com.cyancoder.aiorchestrator.domain.ClientAppDraft;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ClientAppDraftRepository extends MongoRepository<ClientAppDraft, String> {
    List<ClientAppDraft> findByTenantKeyOrderByUpdatedAtDesc(String tenantKey);
    List<ClientAppDraft> findByTenantKeyAndSiteKeyOrderByUpdatedAtDesc(String tenantKey, String siteKey);
    List<ClientAppDraft> findByClientKeyOrderByUpdatedAtDesc(String clientKey);
    Optional<ClientAppDraft> findByDraftId(String draftId);
    Optional<ClientAppDraft> findFirstByTenantKeyAndSiteKeyAndClientKeyAndAppTypeOrderByUpdatedAtDesc(String tenantKey, String siteKey, String clientKey, String appType);
    Optional<ClientAppDraft> findFirstByTenantKeyAndSiteKeyAndAppTypeOrderByUpdatedAtDesc(String tenantKey, String siteKey, String appType);
}

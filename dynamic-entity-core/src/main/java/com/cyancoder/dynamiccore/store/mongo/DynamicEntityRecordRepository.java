package com.cyancoder.dynamiccore.store.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DynamicEntityRecordRepository extends MongoRepository<DynamicEntityRecordDocument, String> {
    Optional<DynamicEntityRecordDocument> findByServiceKeyAndTenantKeyAndSiteKeyAndEntityKeyAndRecordKey(String serviceKey, String tenantKey, String siteKey, String entityKey, String recordKey);
    Optional<DynamicEntityRecordDocument> findFirstByServiceKeyAndTenantKeyAndSiteKeyAndEntityKeyAndRecordKeyOrderByUpdatedAtDesc(String serviceKey, String tenantKey, String siteKey, String entityKey, String recordKey);
    List<DynamicEntityRecordDocument> findByServiceKeyAndTenantKeyAndSiteKeyAndEntityKeyOrderByCreatedAtDesc(String serviceKey, String tenantKey, String siteKey, String entityKey);
    void deleteByServiceKeyAndTenantKeyAndSiteKeyAndEntityKeyAndRecordKey(String serviceKey, String tenantKey, String siteKey, String entityKey, String recordKey);
    void deleteAllByServiceKeyAndTenantKeyAndSiteKeyAndEntityKeyAndRecordKey(String serviceKey, String tenantKey, String siteKey, String entityKey, String recordKey);
}

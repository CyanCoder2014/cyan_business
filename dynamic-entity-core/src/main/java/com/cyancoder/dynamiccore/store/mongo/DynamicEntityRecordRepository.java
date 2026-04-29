package com.cyancoder.dynamiccore.store.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DynamicEntityRecordRepository extends MongoRepository<DynamicEntityRecordDocument, String> {
    Optional<DynamicEntityRecordDocument> findByServiceKeyAndEntityKeyAndRecordKey(String serviceKey, String entityKey, String recordKey);
    List<DynamicEntityRecordDocument> findByServiceKeyAndEntityKeyOrderByCreatedAtDesc(String serviceKey, String entityKey);
}

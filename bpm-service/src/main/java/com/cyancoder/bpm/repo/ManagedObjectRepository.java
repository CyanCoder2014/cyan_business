package com.cyancoder.bpm.repo;

import com.cyancoder.bpm.domain.ManagedObject;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ManagedObjectRepository extends MongoRepository<ManagedObject, String> {
    List<ManagedObject> findByTenantKeyAndSiteKeyOrderByUpdatedAtDesc(String tenantKey, String siteKey);
    List<ManagedObject> findByTenantKeyAndSiteKeyAndAssigneeOrderByUpdatedAtDesc(String tenantKey, String siteKey, String assignee);
}


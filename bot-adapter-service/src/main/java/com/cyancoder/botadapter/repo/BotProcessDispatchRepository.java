package com.cyancoder.botadapter.repo;

import com.cyancoder.botadapter.domain.BotProcessDispatch;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BotProcessDispatchRepository extends MongoRepository<BotProcessDispatch, String> {
    Optional<BotProcessDispatch> findByBindingIdAndInboundMessageId(String bindingId, String inboundMessageId);
    List<BotProcessDispatch> findByTenantKeyAndSiteKeyAndBindingKeyOrderByCreatedAtDesc(String tenantKey, String siteKey, String bindingKey);
}

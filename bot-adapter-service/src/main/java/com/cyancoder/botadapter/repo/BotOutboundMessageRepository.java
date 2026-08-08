package com.cyancoder.botadapter.repo;

import com.cyancoder.botadapter.domain.BotOutboundMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BotOutboundMessageRepository extends MongoRepository<BotOutboundMessage, String> {
    List<BotOutboundMessage> findByTenantKeyAndSiteKeyOrderByUpdatedAtDesc(String tenantKey, String siteKey);
    List<BotOutboundMessage> findByTenantKeyOrderByUpdatedAtDesc(String tenantKey);
    List<BotOutboundMessage> findByIntegrationKeyOrderByUpdatedAtDesc(String integrationKey);
    Optional<BotOutboundMessage> findByIntegrationKeyAndIdempotencyKey(String integrationKey, String idempotencyKey);
}

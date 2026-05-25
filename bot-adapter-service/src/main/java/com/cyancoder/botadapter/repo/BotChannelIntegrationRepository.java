package com.cyancoder.botadapter.repo;

import com.cyancoder.botadapter.domain.BotChannel;
import com.cyancoder.botadapter.domain.BotChannelIntegration;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BotChannelIntegrationRepository extends MongoRepository<BotChannelIntegration, String> {
    Optional<BotChannelIntegration> findByChannelAndIntegrationKeyAndActiveTrue(BotChannel channel, String integrationKey);
    List<BotChannelIntegration> findByTenantKeyOrderByUpdatedAtDesc(String tenantKey);
    List<BotChannelIntegration> findByTenantKeyAndSiteKeyOrderByUpdatedAtDesc(String tenantKey, String siteKey);
}

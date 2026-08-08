package com.cyancoder.botadapter.repo;

import com.cyancoder.botadapter.domain.BotChannel;
import com.cyancoder.botadapter.domain.BotProcessBinding;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BotProcessBindingRepository extends MongoRepository<BotProcessBinding, String> {
    Optional<BotProcessBinding> findByChannelAndIntegrationKeyAndBindingKey(BotChannel channel, String integrationKey, String bindingKey);
    List<BotProcessBinding> findByChannelAndIntegrationKeyAndEnabledTrueOrderByUpdatedAtDesc(BotChannel channel, String integrationKey);
    List<BotProcessBinding> findByTenantKeyAndSiteKeyAndIntegrationKeyOrderByUpdatedAtDesc(String tenantKey, String siteKey, String integrationKey);
}

package com.cyancoder.botadapter.repo;

import com.cyancoder.botadapter.domain.BotChannel;
import com.cyancoder.botadapter.domain.BotMiniAppBuild;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BotMiniAppBuildRepository extends MongoRepository<BotMiniAppBuild, String> {
    List<BotMiniAppBuild> findByTenantKeyAndSiteKeyOrderByUpdatedAtDesc(String tenantKey, String siteKey);
    Optional<BotMiniAppBuild> findByChannelAndIntegrationKeyAndBuildKey(BotChannel channel, String integrationKey, String buildKey);
}

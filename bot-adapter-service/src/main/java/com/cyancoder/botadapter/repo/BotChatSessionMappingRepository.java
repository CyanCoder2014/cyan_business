package com.cyancoder.botadapter.repo;

import com.cyancoder.botadapter.domain.BotChannel;
import com.cyancoder.botadapter.domain.BotChatSessionMapping;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BotChatSessionMappingRepository extends MongoRepository<BotChatSessionMapping, String> {
    Optional<BotChatSessionMapping> findByChannelAndIntegrationKeyAndExternalChatId(BotChannel channel, String integrationKey, String externalChatId);
}

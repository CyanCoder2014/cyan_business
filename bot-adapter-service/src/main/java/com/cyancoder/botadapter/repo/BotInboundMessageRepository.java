package com.cyancoder.botadapter.repo;

import com.cyancoder.botadapter.domain.BotChannel;
import com.cyancoder.botadapter.domain.BotInboundMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BotInboundMessageRepository extends MongoRepository<BotInboundMessage, String> {
    Optional<BotInboundMessage> findByChannelAndIntegrationKeyAndExternalMessageId(BotChannel channel, String integrationKey, String externalMessageId);
}

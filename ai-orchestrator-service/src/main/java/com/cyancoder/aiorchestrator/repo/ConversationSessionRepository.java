package com.cyancoder.aiorchestrator.repo;

import com.cyancoder.aiorchestrator.domain.ConversationSession;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationSessionRepository extends MongoRepository<ConversationSession, String> {
    Optional<ConversationSession> findBySessionId(String sessionId);
    List<ConversationSession> findByDraftIdOrderByUpdatedAtDesc(String draftId);
    List<ConversationSession> findByClientKeyOrderByUpdatedAtDesc(String clientKey);
    List<ConversationSession> findByTenantKeyOrderByUpdatedAtDesc(String tenantKey);
    List<ConversationSession> findByTenantKeyAndSiteKeyOrderByUpdatedAtDesc(String tenantKey, String siteKey);
}

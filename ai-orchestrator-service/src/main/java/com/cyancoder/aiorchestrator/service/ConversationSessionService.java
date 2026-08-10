package com.cyancoder.aiorchestrator.service;

import com.cyancoder.aiorchestrator.api.dto.CreateConversationSessionRequest;
import com.cyancoder.aiorchestrator.api.dto.SessionMessageRequest;
import com.cyancoder.aiorchestrator.domain.ConversationSession;

import java.util.List;

public interface ConversationSessionService {
    ConversationSession createSession(CreateConversationSessionRequest request);
    List<ConversationSession> listSessions(String tenantKey, String siteKey, String clientKey, String draftId);
    ConversationSession getSession(String sessionId);
    ConversationSession linkDraft(String sessionId, String draftId);
    ConversationSession appendMessage(String sessionId, SessionMessageRequest request);
    ConversationSession closeSession(String sessionId);
}

package com.cyancoder.aiorchestrator.service;

import com.cyancoder.aiorchestrator.api.dto.CreateConversationSessionRequest;
import com.cyancoder.aiorchestrator.api.dto.SessionMessageRequest;
import com.cyancoder.aiorchestrator.domain.ConversationSession;

public interface ConversationSessionService {
    ConversationSession createSession(CreateConversationSessionRequest request);
    ConversationSession getSession(String sessionId);
    ConversationSession appendMessage(String sessionId, SessionMessageRequest request);
}

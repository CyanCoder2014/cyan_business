package com.cyancoder.aiorchestrator.api;

import com.cyancoder.aiorchestrator.api.dto.CreateConversationSessionRequest;
import com.cyancoder.aiorchestrator.api.dto.SessionMessageRequest;
import com.cyancoder.aiorchestrator.domain.ConversationSession;
import com.cyancoder.aiorchestrator.service.ConversationSessionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/ai-orchestrator/sessions")
public class InternalConversationSessionController {
    private final ConversationSessionService conversationSessionService;

    public InternalConversationSessionController(ConversationSessionService conversationSessionService) {
        this.conversationSessionService = conversationSessionService;
    }

    @PostMapping
    public ConversationSession create(@RequestBody CreateConversationSessionRequest request) {
        return conversationSessionService.createSession(request);
    }

    @GetMapping("/{sessionId}")
    public ConversationSession get(@PathVariable String sessionId) {
        return conversationSessionService.getSession(sessionId);
    }

    @PostMapping("/{sessionId}/message")
    public ConversationSession appendMessage(@PathVariable String sessionId, @RequestBody SessionMessageRequest request) {
        return conversationSessionService.appendMessage(sessionId, request);
    }
}

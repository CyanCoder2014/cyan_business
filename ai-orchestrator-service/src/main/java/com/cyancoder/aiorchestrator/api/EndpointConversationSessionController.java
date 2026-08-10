package com.cyancoder.aiorchestrator.api;

import com.cyancoder.aiorchestrator.api.dto.CreateConversationSessionRequest;
import com.cyancoder.aiorchestrator.api.dto.SessionMessageRequest;
import com.cyancoder.aiorchestrator.domain.ConversationSession;
import com.cyancoder.aiorchestrator.service.ConversationSessionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/endpoint/ai-orchestrator/sessions")
public class EndpointConversationSessionController {
    private final ConversationSessionService conversationSessionService;

    public EndpointConversationSessionController(ConversationSessionService conversationSessionService) {
        this.conversationSessionService = conversationSessionService;
    }

    @PostMapping
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public ConversationSession create(@RequestBody CreateConversationSessionRequest request) {
        return conversationSessionService.createSession(request);
    }

    @GetMapping
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public List<ConversationSession> list(
            @RequestParam(value = "tenantKey", required = false) String tenantKey,
            @RequestParam(value = "siteKey", required = false) String siteKey,
            @RequestParam(value = "clientKey", required = false) String clientKey,
            @RequestParam(value = "draftId", required = false) String draftId
    ) {
        return conversationSessionService.listSessions(tenantKey, siteKey, clientKey, draftId);
    }

    @GetMapping("/{sessionId}")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public ConversationSession get(@PathVariable("sessionId") String sessionId) {
        return conversationSessionService.getSession(sessionId);
    }

    @PostMapping("/{sessionId}/message")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public ConversationSession appendMessage(@PathVariable("sessionId") String sessionId, @RequestBody SessionMessageRequest request) {
        return conversationSessionService.appendMessage(sessionId, request);
    }

    @PostMapping("/{sessionId}/close")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public ConversationSession close(@PathVariable("sessionId") String sessionId) {
        return conversationSessionService.closeSession(sessionId);
    }
}

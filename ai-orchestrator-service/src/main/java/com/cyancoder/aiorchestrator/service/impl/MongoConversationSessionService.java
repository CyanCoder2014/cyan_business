package com.cyancoder.aiorchestrator.service.impl;

import com.cyancoder.aiorchestrator.api.dto.CreateConversationSessionRequest;
import com.cyancoder.aiorchestrator.api.dto.CreateDraftRequest;
import com.cyancoder.aiorchestrator.api.dto.SessionMessageRequest;
import com.cyancoder.aiorchestrator.api.dto.UpdateDraftRequest;
import com.cyancoder.aiorchestrator.domain.ClientAppDraft;
import com.cyancoder.aiorchestrator.domain.ConversationSession;
import com.cyancoder.aiorchestrator.domain.SessionMessage;
import com.cyancoder.aiorchestrator.domain.SessionStatus;
import com.cyancoder.aiorchestrator.repo.ConversationSessionRepository;
import com.cyancoder.aiorchestrator.service.AppDraftService;
import com.cyancoder.aiorchestrator.service.ConversationSessionService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MongoConversationSessionService implements ConversationSessionService {
    private final ConversationSessionRepository repository;
    private final AppDraftService appDraftService;

    public MongoConversationSessionService(ConversationSessionRepository repository,
                                           AppDraftService appDraftService) {
        this.repository = repository;
        this.appDraftService = appDraftService;
    }

    @Override
    public ConversationSession createSession(CreateConversationSessionRequest request) {
        ConversationSession session = new ConversationSession();
        session.setSessionId("session-" + UUID.randomUUID());
        session.setChannelType(firstNonBlank(request.channelType(), "PANEL"));
        session.setTenantKey(request.tenantKey());
        session.setSiteKey(request.siteKey());
        session.setClientKey(request.clientKey());
        session.setAppTypeHint(request.appTypeHint());
        session.setStatus(SessionStatus.OPEN);
        session.setMessages(new ArrayList<>());
        session.setExtractedAnswers(new LinkedHashMap<>(request.extractedAnswers() == null ? Map.of() : request.extractedAnswers()));
        session.setCreatedAt(Instant.now());
        session.setUpdatedAt(Instant.now());

        if (request.draftId() != null && !request.draftId().isBlank()) {
            ClientAppDraft draft = appDraftService.getDraft(request.draftId());
            session.setDraftId(draft.getDraftId());
            syncPendingState(session, draft);
        } else if (request.appTypeHint() != null && !request.appTypeHint().isBlank()) {
            ClientAppDraft draft = appDraftService.resolveKnownAppDraft(
                            request.appTypeHint(),
                            request.tenantKey(),
                            request.siteKey(),
                            request.clientKey(),
                            null
                    )
                    .orElseGet(() -> appDraftService.createDraft(new CreateDraftRequest(
                            request.appTypeHint(),
                            null,
                            request.tenantKey(),
                            request.siteKey(),
                            request.clientKey(),
                            request.title(),
                            null,
                            session.getExtractedAnswers()
                    ), "session-create"));
            session.setDraftId(draft.getDraftId());
            syncPendingState(session, draft);
        }
        return repository.save(session);
    }

    @Override
    public List<ConversationSession> listSessions(String tenantKey, String siteKey, String clientKey, String draftId) {
        if (draftId != null && !draftId.isBlank()) {
            return repository.findByDraftIdOrderByUpdatedAtDesc(draftId);
        }
        if (clientKey != null && !clientKey.isBlank()) {
            return repository.findByClientKeyOrderByUpdatedAtDesc(clientKey);
        }
        if (tenantKey != null && !tenantKey.isBlank() && siteKey != null && !siteKey.isBlank()) {
            return repository.findByTenantKeyAndSiteKeyOrderByUpdatedAtDesc(tenantKey, siteKey);
        }
        if (tenantKey != null && !tenantKey.isBlank()) {
            return repository.findByTenantKeyOrderByUpdatedAtDesc(tenantKey);
        }
        return repository.findAll().stream()
                .sorted(Comparator.comparing(ConversationSession::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public ConversationSession getSession(String sessionId) {
        return repository.findBySessionId(sessionId).orElseThrow();
    }

    @Override
    public ConversationSession appendMessage(String sessionId, SessionMessageRequest request) {
        ConversationSession session = getSession(sessionId);
        SessionMessage message = new SessionMessage();
        message.setMessageId("msg-" + UUID.randomUUID());
        message.setRole(firstNonBlank(request.role(), "USER"));
        message.setContent(request.content());
        message.setCreatedAt(Instant.now());
        session.getMessages().add(message);
        if (request.answersPatch() != null) {
            session.getExtractedAnswers().putAll(request.answersPatch());
        }
        if ("USER".equalsIgnoreCase(message.getRole())) {
            session.setLatestPrompt(message.getContent());
        } else {
            session.setLatestQuestion(message.getContent());
        }
        if (session.getDraftId() != null && request.answersPatch() != null && !request.answersPatch().isEmpty()) {
            ClientAppDraft updatedDraft = appDraftService.updateDraft(session.getDraftId(), new UpdateDraftRequest(
                    request.content(),
                    null,
                    request.answersPatch()
            ), "session-message");
            syncPendingState(session, updatedDraft);
        }
        session.setUpdatedAt(Instant.now());
        return repository.save(session);
    }

    private void syncPendingState(ConversationSession session, ClientAppDraft draft) {
        session.setPendingQuestionKeys(new ArrayList<>(draft.getPendingQuestionKeys()));
        session.setPendingQuestions(new ArrayList<>(draft.getPendingQuestions()));
        session.setLatestQuestion(draft.getPendingQuestions().isEmpty() ? null : draft.getPendingQuestions().get(0));
        session.setStatus(draft.getPendingQuestions().isEmpty() ? SessionStatus.RESOLVED : SessionStatus.WAITING_FOR_ANSWERS);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}

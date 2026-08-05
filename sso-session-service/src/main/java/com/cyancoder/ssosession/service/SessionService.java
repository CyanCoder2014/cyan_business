package com.cyancoder.ssosession.service;

import com.cyancoder.sso.common.dto.SessionCreateRequest;
import com.cyancoder.sso.common.dto.SessionResponse;
import com.cyancoder.sso.common.dto.SessionScopeRequest;
import com.cyancoder.sso.common.dto.SessionScopeResponse;
import com.cyancoder.ssosession.entity.SessionStateEntity;
import com.cyancoder.ssosession.repository.SessionStateRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class SessionService {

    private final SessionStateRepository sessionStateRepository;
    private final ScopeBoundaryClient scopeBoundaryClient;

    public SessionService(SessionStateRepository sessionStateRepository, ScopeBoundaryClient scopeBoundaryClient) {
        this.sessionStateRepository = sessionStateRepository;
        this.scopeBoundaryClient = scopeBoundaryClient;
    }

    public SessionResponse create(SessionCreateRequest request) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = Instant.now().plusSeconds(3600).getEpochSecond();
        String sessionId = UUID.randomUUID().toString();
        SessionStateEntity sessionState = new SessionStateEntity();
        sessionState.setSessionId(sessionId);
        sessionState.setUsername(request.username());
        sessionState.setClientId(request.clientId());
        sessionState.setDeviceId(request.deviceId());
        sessionState.setActive(true);
        sessionState.setIssuedAtEpochSecond(issuedAt);
        sessionState.setExpiresAtEpochSecond(expiresAt);
        return toResponse(sessionStateRepository.save(sessionState));
    }

    public SessionResponse get(String sessionId) {
        SessionStateEntity sessionState = sessionStateRepository.findById(sessionId).orElse(null);
        return sessionState == null ? null : toResponse(sessionState);
    }

    public SessionResponse revoke(String sessionId) {
        SessionStateEntity sessionState = sessionStateRepository.findById(sessionId).orElse(null);
        if (sessionState == null) {
            return null;
        }
        sessionState.setActive(false);
        return toResponse(sessionStateRepository.save(sessionState));
    }

    public SessionScopeResponse getScope(String sessionId, String subject) {
        SessionStateEntity session = ownedActiveSession(sessionId, subject);
        return new SessionScopeResponse(sessionId, session.getActiveTenantKey(), session.getActiveSiteKey());
    }

    public SessionScopeResponse updateScope(String sessionId, String subject, SessionScopeRequest request) {
        SessionStateEntity session = ownedActiveSession(sessionId, subject);
        scopeBoundaryClient.validate(subject, request.tenantKey(), request.siteKey());
        session.setActiveTenantKey(request.tenantKey());
        session.setActiveSiteKey(request.siteKey() == null || request.siteKey().isBlank() ? null : request.siteKey());
        sessionStateRepository.save(session);
        return new SessionScopeResponse(sessionId, session.getActiveTenantKey(), session.getActiveSiteKey());
    }

    private SessionStateEntity ownedActiveSession(String sessionId, String subject) {
        SessionStateEntity session = sessionStateRepository.findById(sessionId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Session not found"));
        if (!session.isActive() || session.getExpiresAtEpochSecond() <= Instant.now().getEpochSecond())
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Session is inactive");
        if (!session.getUsername().equals(subject))
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Session ownership required");
        return session;
    }

    private SessionResponse toResponse(SessionStateEntity sessionState) {
        return new SessionResponse(
                sessionState.getSessionId(),
                sessionState.getUsername(),
                sessionState.getClientId(),
                sessionState.getDeviceId(),
                sessionState.isActive(),
                sessionState.getIssuedAtEpochSecond(),
                sessionState.getExpiresAtEpochSecond()
        );
    }
}

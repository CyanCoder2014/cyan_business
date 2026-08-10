package com.cyancoder.ssosession.service;

import com.cyancoder.sso.common.dto.SessionCreateRequest;
import com.cyancoder.sso.common.dto.SessionResponse;
import com.cyancoder.sso.common.dto.SessionRenewRequest;
import com.cyancoder.sso.common.dto.SessionScopeRequest;
import com.cyancoder.sso.common.dto.SessionScopeResponse;
import com.cyancoder.ssosession.entity.SessionStateEntity;
import com.cyancoder.ssosession.repository.SessionStateRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

@Service
public class SessionService {

    private final SessionStateRepository sessionStateRepository;
    private final ScopeBoundaryClient scopeBoundaryClient;
    private final long sessionTtlSeconds;

    public SessionService(SessionStateRepository sessionStateRepository, ScopeBoundaryClient scopeBoundaryClient,
                          @Value("${sso.session.ttl-seconds:86400}") long sessionTtlSeconds) {
        this.sessionStateRepository = sessionStateRepository;
        this.scopeBoundaryClient = scopeBoundaryClient;
        this.sessionTtlSeconds = sessionTtlSeconds;
    }

    public SessionResponse create(SessionCreateRequest request) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = Instant.now().plusSeconds(sessionTtlSeconds).getEpochSecond();
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
        if (sessionState != null && sessionState.isActive()
                && sessionState.getExpiresAtEpochSecond() <= Instant.now().getEpochSecond()) {
            sessionState.setActive(false);
            sessionStateRepository.save(sessionState);
        }
        return sessionState == null ? null : toResponse(sessionState);
    }

    public SessionResponse renew(String sessionId, SessionRenewRequest request) {
        SessionStateEntity session = sessionStateRepository.findById(sessionId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Session not found"));
        if (!session.isActive())
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Session is inactive");
        if (!session.getUsername().equals(request.username()) || !session.getClientId().equals(request.clientId()))
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Session binding mismatch");
        session.setExpiresAtEpochSecond(Instant.now().plusSeconds(sessionTtlSeconds).getEpochSecond());
        return toResponse(sessionStateRepository.save(session));
    }

    public SessionResponse revoke(String sessionId) {
        SessionStateEntity sessionState = sessionStateRepository.findById(sessionId).orElse(null);
        if (sessionState == null) {
            return null;
        }
        sessionState.setActive(false);
        return toResponse(sessionStateRepository.save(sessionState));
    }

    public List<SessionResponse> listOwned(String subject) {
        long now=Instant.now().getEpochSecond();
        return sessionStateRepository.findByUsernameOrderByIssuedAtEpochSecondDesc(subject).stream().map(session->{
            if(session.isActive()&&session.getExpiresAtEpochSecond()<=now){session.setActive(false);sessionStateRepository.save(session);}
            return toResponse(session);
        }).toList();
    }

    public SessionResponse revokeOwned(String sessionId,String subject) {
        SessionStateEntity session=sessionStateRepository.findById(sessionId).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,"Session not found"));
        if(!session.getUsername().equals(subject))throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,"Session ownership required");
        session.setActive(false);return toResponse(sessionStateRepository.save(session));
    }

    public void revokeAllOwned(String subject) {
        sessionStateRepository.findByUsernameOrderByIssuedAtEpochSecondDesc(subject).forEach(session->{if(session.isActive()){session.setActive(false);sessionStateRepository.save(session);}});
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

package com.cyancoder.ssosession.service;

import com.cyancoder.sso.common.dto.SessionCreateRequest;
import com.cyancoder.sso.common.dto.SessionResponse;
import com.cyancoder.ssosession.entity.SessionStateEntity;
import com.cyancoder.ssosession.repository.SessionStateRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class SessionService {

    private final SessionStateRepository sessionStateRepository;

    public SessionService(SessionStateRepository sessionStateRepository) {
        this.sessionStateRepository = sessionStateRepository;
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

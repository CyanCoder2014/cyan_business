package com.cyancoder.ssoauth.service;

import com.cyancoder.sso.common.dto.SessionResponse;
import com.cyancoder.sso.common.dto.TokenIntrospectionResponse;
import com.cyancoder.sso.common.dto.UserSummary;
import com.cyancoder.ssoauth.entity.RefreshTokenEntity;
import com.cyancoder.ssoauth.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String issue(String clientId, UserSummary user, SessionResponse sessionResponse, long ttlSeconds) {
        String refreshToken = UUID.randomUUID().toString() + "." + UUID.randomUUID();
        long expiresAt = Instant.now().plusSeconds(ttlSeconds * 24).getEpochSecond();
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setToken(refreshToken);
        entity.setSubject(user.username());
        entity.setClientId(clientId);
        entity.setSessionId(sessionResponse.sessionId());
        entity.setExpiresAtEpochSecond(expiresAt);
        entity.setActive(true);
        refreshTokenRepository.save(entity);
        return refreshToken;
    }

    public TokenIntrospectionResponse introspect(String token) {
        RefreshTokenEntity state = refreshTokenRepository.findById(token).orElse(null);
        if (state == null) {
            return new TokenIntrospectionResponse(false, null, null, null, 0);
        }
        boolean active = state.isActive() && state.getExpiresAtEpochSecond() >= Instant.now().getEpochSecond();
        return new TokenIntrospectionResponse(active, state.getSubject(), state.getClientId(), state.getSessionId(), state.getExpiresAtEpochSecond());
    }

    public RefreshTokenState consume(String clientId, String token) {
        RefreshTokenEntity state = refreshTokenRepository.findById(token).orElse(null);
        if (state == null || !state.isActive() || state.getExpiresAtEpochSecond() < Instant.now().getEpochSecond()) {
            return null;
        }
        if (!state.getClientId().equals(clientId)) {
            return null;
        }
        return new RefreshTokenState(
                state.getToken(),
                state.getSubject(),
                state.getClientId(),
                state.getSessionId(),
                state.getExpiresAtEpochSecond(),
                state.isActive()
        );
    }

    public void revokeBySessionId(String sessionId) {
        refreshTokenRepository.findBySessionId(sessionId).forEach(entity -> {
            entity.setActive(false);
            refreshTokenRepository.save(entity);
        });
    }

    public record RefreshTokenState(
            String token,
            String subject,
            String clientId,
            String sessionId,
            long expiresAtEpochSecond,
            boolean active
    ) {
    }
}

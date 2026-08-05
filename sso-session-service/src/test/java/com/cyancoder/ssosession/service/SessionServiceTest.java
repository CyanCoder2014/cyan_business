package com.cyancoder.ssosession.service;

import com.cyancoder.sso.common.dto.SessionScopeRequest;
import com.cyancoder.ssosession.entity.SessionStateEntity;
import com.cyancoder.ssosession.repository.SessionStateRepository;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionServiceTest {
    @Test void persistsOnlyValidatedScopeForSessionOwner() {
        SessionStateRepository repository = mock(SessionStateRepository.class);
        ScopeBoundaryClient boundaries = mock(ScopeBoundaryClient.class);
        SessionStateEntity session = new SessionStateEntity();
        session.setSessionId("session-1"); session.setUsername("owner@cyan.local"); session.setClientId("cyan-panel"); session.setActive(true); session.setExpiresAtEpochSecond(Instant.now().plusSeconds(600).getEpochSecond());
        when(repository.findById("session-1")).thenReturn(Optional.of(session));
        SessionService service = new SessionService(repository, boundaries);

        var result = service.updateScope("session-1", "owner@cyan.local", new SessionScopeRequest("north-star", "main-store"));

        assertEquals("north-star", result.tenantKey());
        assertEquals("main-store", result.siteKey());
        verify(boundaries).validate("owner@cyan.local", "north-star", "main-store");
        verify(repository).save(session);
    }
}

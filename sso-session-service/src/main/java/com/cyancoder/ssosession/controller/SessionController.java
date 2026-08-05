package com.cyancoder.ssosession.controller;

import com.cyancoder.sso.common.dto.LogoutRequest;
import com.cyancoder.sso.common.dto.SessionCreateRequest;
import com.cyancoder.sso.common.dto.SessionResponse;
import com.cyancoder.sso.common.dto.SessionScopeRequest;
import com.cyancoder.sso.common.dto.SessionScopeResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import com.cyancoder.ssosession.service.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sso/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponse create(@RequestBody SessionCreateRequest request) {
        return sessionService.create(request);
    }

    @GetMapping("/{sessionId}")
    public SessionResponse get(@PathVariable String sessionId) {
        return sessionService.get(sessionId);
    }

    @PostMapping("/revoke")
    public SessionResponse revoke(@RequestBody LogoutRequest request) {
        return sessionService.revoke(request.sessionId());
    }

    @GetMapping("/{sessionId}/scope")
    public SessionScopeResponse getScope(@PathVariable String sessionId, Authentication authentication) {
        return sessionService.getScope(sessionId, authentication.getName());
    }

    @PutMapping("/{sessionId}/scope")
    public SessionScopeResponse updateScope(@PathVariable String sessionId, @Valid @RequestBody SessionScopeRequest request, Authentication authentication) {
        return sessionService.updateScope(sessionId, authentication.getName(), request);
    }
}

package com.cyancoder.ssosession.controller;

import com.cyancoder.sso.common.dto.SessionRenewRequest;
import com.cyancoder.sso.common.dto.SessionResponse;
import com.cyancoder.ssosession.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/sso/sessions")
public class InternalSessionController {
    private final SessionService sessionService;

    public InternalSessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/{sessionId}/renew")
    public SessionResponse renew(@PathVariable String sessionId, @Valid @RequestBody SessionRenewRequest request) {
        return sessionService.renew(sessionId, request);
    }
}

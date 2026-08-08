package com.cyancoder.ssoauth.client;

import com.cyancoder.sso.common.dto.LogoutRequest;
import com.cyancoder.sso.common.dto.SessionCreateRequest;
import com.cyancoder.sso.common.dto.SessionResponse;
import com.cyancoder.sso.common.dto.SessionRenewRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "sso-session-service", configuration = SessionClientConfiguration.class)
public interface SessionClient {

    @PostMapping("/api/sso/sessions")
    SessionResponse create(@RequestBody SessionCreateRequest request);

    @GetMapping("/api/sso/sessions/{sessionId}")
    SessionResponse get(@PathVariable("sessionId") String sessionId);

    @PostMapping("/internal/sso/sessions/{sessionId}/renew")
    SessionResponse renew(@PathVariable("sessionId") String sessionId, @RequestBody SessionRenewRequest request);

    @PostMapping("/api/sso/sessions/revoke")
    SessionResponse revoke(@RequestBody LogoutRequest request);
}

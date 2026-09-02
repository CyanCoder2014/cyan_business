package com.cyancoder.ssoauth.client;

import com.cyancoder.sso.common.dto.PasswordVerificationRequest;
import com.cyancoder.sso.common.dto.PasswordVerificationResponse;
import com.cyancoder.sso.common.dto.IamUserAccessSummary;
import com.cyancoder.sso.common.dto.UserSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "sso-user-service")
public interface UserClient {

    @GetMapping("/api/sso/users/{username}")
    UserSummary getUser(@PathVariable("username") String username);

    @PostMapping("/api/sso/users/verify-password")
    PasswordVerificationResponse verifyPassword(@RequestBody PasswordVerificationRequest request);

    record SetPasswordRequest(String newPassword) {}

    @PostMapping("/internal/users/{username}/password")
    void setPassword(@PathVariable("username") String username, @RequestBody SetPasswordRequest request);

    @GetMapping("/api/sso/iam/internal/users/{username}/access")
    IamUserAccessSummary resolveAccess(@PathVariable("username") String username, @RequestParam("clientId") String clientId);
}

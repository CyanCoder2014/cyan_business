package com.cyancoder.ssouser.controller;

import com.cyancoder.sso.common.dto.PasswordVerificationRequest;
import com.cyancoder.sso.common.dto.PasswordVerificationResponse;
import com.cyancoder.sso.common.dto.UserRegistrationRequest;
import com.cyancoder.sso.common.dto.UserSummary;
import com.cyancoder.ssouser.service.UserDirectoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sso/users")
public class UserController {

    private final UserDirectoryService userDirectoryService;

    public UserController(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserSummary register(@RequestBody UserRegistrationRequest request) {
        return userDirectoryService.register(request);
    }

    @GetMapping("/{username}")
    public UserSummary getUser(@PathVariable String username) {
        return userDirectoryService.getUser(username);
    }

    @PostMapping("/verify-password")
    public PasswordVerificationResponse verifyPassword(@RequestBody PasswordVerificationRequest request) {
        return userDirectoryService.verifyPassword(request.username(), request.password());
    }
}

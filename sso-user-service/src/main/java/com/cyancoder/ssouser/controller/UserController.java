package com.cyancoder.ssouser.controller;

import com.cyancoder.platformopenapi.PlatformApiSecurity;
import com.cyancoder.platformopenapi.PlatformOpenApiAuth;
import com.cyancoder.sso.common.dto.PasswordVerificationRequest;
import com.cyancoder.sso.common.dto.PasswordVerificationResponse;
import com.cyancoder.sso.common.dto.UserRegistrationRequest;
import com.cyancoder.sso.common.dto.UserSummary;
import com.cyancoder.ssouser.service.IamDirectoryService;
import com.cyancoder.ssouser.service.IamSecurityService;
import com.cyancoder.ssouser.service.UserDirectoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sso/users")
public class UserController {

    private final UserDirectoryService userDirectoryService;
    private final IamSecurityService iamSecurityService;
    private final IamDirectoryService iamDirectoryService;

    public UserController(UserDirectoryService userDirectoryService, IamSecurityService iamSecurityService, IamDirectoryService iamDirectoryService) {
        this.userDirectoryService = userDirectoryService;
        this.iamSecurityService = iamSecurityService;
        this.iamDirectoryService = iamDirectoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PlatformOpenApiAuth(PlatformApiSecurity.BEARER)
    public UserSummary register(@RequestBody UserRegistrationRequest request) {
        iamSecurityService.requirePlatformAdmin();
        return userDirectoryService.register(request);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserSummary registerPublic(@RequestBody UserRegistrationRequest request) {
        return iamDirectoryService.registerPublicUser(request);
    }

    @GetMapping
    @PlatformOpenApiAuth(PlatformApiSecurity.BEARER)
    public java.util.List<UserSummary> listUsers() {
        iamSecurityService.requirePlatformAdmin();
        return userDirectoryService.listUsers();
    }

    @GetMapping("/{username}")
    public UserSummary getUser(@PathVariable("username") String username) {
        return userDirectoryService.getUser(username);
    }

    @PostMapping("/verify-password")
    public PasswordVerificationResponse verifyPassword(@RequestBody PasswordVerificationRequest request) {
        return userDirectoryService.verifyPassword(request.username(), request.password());
    }
}

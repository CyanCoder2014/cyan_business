package com.cyancoder.ssouser.controller;

import com.cyancoder.platformopenapi.PlatformApiSecurity;
import com.cyancoder.platformopenapi.PlatformOpenApiAuth;
import com.cyancoder.sso.common.dto.UserRegistrationRequest;
import com.cyancoder.sso.common.dto.UserSummary;
import com.cyancoder.sso.common.dto.ManagedUserProvisionRequest;
import com.cyancoder.ssouser.service.IamDirectoryService;
import com.cyancoder.ssouser.service.UserDirectoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
@PlatformOpenApiAuth(PlatformApiSecurity.BASIC)
public class InternalUserController {
    private final UserDirectoryService directory;
    private final IamDirectoryService iam;

    public InternalUserController(UserDirectoryService directory, IamDirectoryService iam) { this.directory = directory; this.iam = iam; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public UserSummary provision(@RequestBody UserRegistrationRequest request) {
        return directory.registerIdempotent(request);
    }

    @PostMapping("/managed")
    @ResponseStatus(HttpStatus.CREATED)
    public UserSummary provisionManaged(@RequestBody ManagedUserProvisionRequest request) {
        return iam.provisionManagedUserInternal(request);
    }

    @GetMapping("/{username}")
    public UserSummary get(@PathVariable String username) { return directory.getUser(username); }

    public record SetPasswordRequest(String newPassword) {}

    /** Used by the OTP-verified reset flow in sso-auth-service. */
    @PostMapping("/{username}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setPassword(@PathVariable String username, @RequestBody SetPasswordRequest request) {
        directory.setPassword(username, request.newPassword());
    }

    /** Null fields are left unchanged, so a caller can flip one setting in isolation. */
    public record AdministerUserRequest(String email, String phoneNumber, Boolean mfaEnabled, Boolean active) {}

    @PatchMapping("/{username}")
    public UserSummary administer(@PathVariable String username, @RequestBody AdministerUserRequest request) {
        return directory.administer(username, request.email(), request.phoneNumber(), request.mfaEnabled(), request.active());
    }
}

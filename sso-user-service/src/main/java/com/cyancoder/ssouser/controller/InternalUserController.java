package com.cyancoder.ssouser.controller;

import com.cyancoder.platformopenapi.PlatformApiSecurity;
import com.cyancoder.platformopenapi.PlatformOpenApiAuth;
import com.cyancoder.sso.common.dto.UserRegistrationRequest;
import com.cyancoder.sso.common.dto.UserSummary;
import com.cyancoder.ssouser.service.UserDirectoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
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

    public InternalUserController(UserDirectoryService directory) { this.directory = directory; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public UserSummary provision(@RequestBody UserRegistrationRequest request) {
        return directory.registerIdempotent(request);
    }

    @GetMapping("/{username}")
    public UserSummary get(@PathVariable String username) { return directory.getUser(username); }
}

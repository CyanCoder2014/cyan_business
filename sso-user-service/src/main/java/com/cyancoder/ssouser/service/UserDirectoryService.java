package com.cyancoder.ssouser.service;

import com.cyancoder.sso.common.dto.PasswordVerificationResponse;
import com.cyancoder.sso.common.dto.UserRegistrationRequest;
import com.cyancoder.sso.common.dto.UserSummary;
import com.cyancoder.ssouser.entity.StoredUserEntity;
import com.cyancoder.ssouser.repository.StoredUserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDirectoryService {

    private final StoredUserRepository storedUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDirectoryService(StoredUserRepository storedUserRepository, PasswordEncoder passwordEncoder) {
        this.storedUserRepository = storedUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    void seedDefaults() {
        if (storedUserRepository.count() == 0) {
            register(new UserRegistrationRequest(
                    "cyan-admin",
                    "admin123",
                    "admin@cyan.local",
                    "09120000000",
                    true,
                    List.of("admin", "user")
            ));
            register(new UserRegistrationRequest(
                    "cyan-user",
                    "user123",
                    "user@cyan.local",
                    "09121111111",
                    false,
                    List.of("user")
            ));
        }
    }

    public UserSummary register(UserRegistrationRequest request) {
        StoredUserEntity storedUser = new StoredUserEntity();
        storedUser.setUsername(request.username());
        storedUser.setPasswordHash(passwordEncoder.encode(request.password()));
        storedUser.setEmail(request.email());
        storedUser.setPhoneNumber(request.phoneNumber());
        storedUser.setMfaEnabled(request.mfaEnabled());
        storedUser.setRoles(request.roles() == null ? List.of("user") : request.roles());
        storedUser.setActive(true);
        return toSummary(storedUserRepository.save(storedUser));
    }

    public UserSummary getUser(String username) {
        StoredUserEntity storedUser = storedUserRepository.findById(username).orElse(null);
        return storedUser == null ? null : toSummary(storedUser);
    }

    public List<UserSummary> listUsers() {
        return storedUserRepository.findAll().stream().map(this::toSummary).toList();
    }

    public PasswordVerificationResponse verifyPassword(String username, String password) {
        StoredUserEntity storedUser = storedUserRepository.findById(username).orElse(null);
        if (storedUser == null || !storedUser.isActive()) {
            return new PasswordVerificationResponse(false, null);
        }

        boolean valid = passwordEncoder.matches(password, storedUser.getPasswordHash());
        return new PasswordVerificationResponse(valid, valid ? toSummary(storedUser) : null);
    }

    private UserSummary toSummary(StoredUserEntity storedUser) {
        return new UserSummary(
                storedUser.getUsername(),
                storedUser.getEmail(),
                storedUser.getPhoneNumber(),
                storedUser.isMfaEnabled(),
                storedUser.getRoles(),
                storedUser.isActive()
        );
    }
}

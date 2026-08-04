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
import java.util.Locale;

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
        String username = normalizeUsername(required(request.username(), "username"));
        String email = normalizeEmail(request.email());
        if (storedUserRepository.existsById(username)) {
            throw new IllegalArgumentException("User already exists");
        }
        if (email != null && storedUserRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        StoredUserEntity storedUser = new StoredUserEntity();
        storedUser.setUsername(username);
        storedUser.setPasswordHash(passwordEncoder.encode(required(request.password(), "password")));
        storedUser.setEmail(email);
        storedUser.setPhoneNumber(request.phoneNumber());
        storedUser.setMfaEnabled(request.mfaEnabled());
        storedUser.setRoles(request.roles() == null ? List.of("user") : request.roles());
        storedUser.setActive(true);
        return toSummary(storedUserRepository.save(storedUser));
    }

    public UserSummary getUser(String username) {
        StoredUserEntity storedUser = resolveUser(username);
        return storedUser == null ? null : toSummary(storedUser);
    }

    public List<UserSummary> listUsers() {
        return storedUserRepository.findAll().stream().map(this::toSummary).toList();
    }

    public PasswordVerificationResponse verifyPassword(String username, String password) {
        StoredUserEntity storedUser = resolveUser(username);
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

    private StoredUserEntity resolveUser(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            return null;
        }
        String normalized = normalizeUsername(usernameOrEmail);
        StoredUserEntity byUsername = storedUserRepository.findById(normalized).orElse(null);
        if (byUsername != null) {
            return byUsername;
        }
        return storedUserRepository.findByEmail(normalizeEmail(normalized)).orElse(null);
    }

    private String normalizeUsername(String value) {
        String trimmed = value.trim();
        return trimmed.contains("@") ? trimmed.toLowerCase(Locale.ROOT) : trimmed;
    }

    private String normalizeEmail(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }
}

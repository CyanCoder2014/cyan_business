package com.cyancoder.tenant.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.Valid;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class TenantContracts {
    private TenantContracts() {}

    public record CreateTenantRequest(
            @NotBlank @Pattern(regexp = "[a-z0-9][a-z0-9-]{2,79}") String tenantKey,
            @NotBlank @Size(max = 180) String displayName
    ) {}

    public record TenantSummary(
            String tenantKey,
            String displayName,
            String status,
            String membershipRole,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record EffectiveCapability(
            String key,
            boolean enabled,
            String source,
            String status,
            Map<String, Object> limits,
            String reason
    ) {}

    public record MembershipAccess(String tenantKey, String username, String roleKey, boolean active) {}

    public record PermissionDescriptor(String key, String groupKey, String displayName, String description) {}

    public record TenantRoleSummary(String roleKey, String displayName, String description, boolean systemRole,
                                    long revision, List<String> permissions, long memberCount) {}

    public record SaveTenantRoleRequest(
            @NotBlank @Pattern(regexp = "[A-Z][A-Z0-9_]{2,63}") String roleKey,
            @NotBlank @Size(max = 120) String displayName,
            @Size(max = 400) String description,
            @NotEmpty List<@NotBlank String> permissions,
            Long expectedRevision
    ) {}

    public record TenantUserSummary(String username, String email, String phoneNumber, String roleKey,
                                    boolean active, Instant joinedAt, Instant updatedAt) {}

    public record AddTenantUserRequest(
            @NotBlank @Size(max = 180) String username,
            @Email @Size(max = 240) String email,
            @Size(max = 40) String phoneNumber,
            @Size(min = 8, max = 200) String initialPassword,
            @NotBlank String roleKey,
            boolean mfaRequired
    ) {}

    public record UpdateTenantUserRequest(@NotBlank String roleKey, boolean active) {}

    public record EffectiveAccess(String tenantKey, String username, String roleKey, List<String> permissions,
                                  boolean active) {}

    public record AssignableTarget(String type, String key, String displayName, boolean active) {}

    public record ClientHeadUser(
            @NotBlank @Size(max = 180) String username,
            @Email @NotBlank @Size(max = 240) String email,
            @Size(max = 40) String phoneNumber,
            @NotBlank @Size(min = 8, max = 200) String initialPassword,
            boolean mfaRequired
    ) {}

    public record CreateClientRequest(
            @NotBlank @Pattern(regexp = "[a-z0-9][a-z0-9-]{2,79}") String tenantKey,
            @NotBlank @Size(max = 180) String displayName,
            @NotNull @Valid ClientHeadUser headUser,
            @NotBlank String planKey,
            List<@NotBlank String> capabilityKeys
    ) {}

    public record ClientProvisioningResult(TenantSummary client, TenantUserSummary headUser,
                                           List<String> capabilityKeys, String planKey, String status) {}
    public record UpdateClientCapabilitiesRequest(List<@NotBlank String> capabilityKeys) {}
    public record TransferOwnershipRequest(@NotBlank String newOwnerUsername,@NotBlank String previousOwnerRoleKey) {}
    public record CreateInvitationRequest(@Email @NotBlank String email,@NotBlank String roleKey,@Min(1) @Max(168) Integer expiresInHours) {}
    public record AcceptInvitationRequest(@NotBlank String token,@NotBlank @Size(max=180) String username,@NotBlank @Size(min=8,max=200) String password,@Size(max=40) String phoneNumber,boolean mfaRequired) {}
    public record TenantInvitationView(String invitationId,String tenantKey,String email,String roleKey,String status,String deliveryStatus,Instant expiresAt,Instant createdAt) {}

    public record BillingEntitlements(String planKey, String status, List<String> features, Map<String, Object> limits) {
        public static BillingEntitlements none() {
            return new BillingEntitlements(null, "NONE", List.of(), Map.of());
        }
    }
}

package com.cyancoder.aiorchestrator.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;

public final class AiProviderProfileContracts {
    private AiProviderProfileContracts() {}
    public record SaveProfileRequest(
            @NotBlank @Pattern(regexp="[a-z0-9][a-z0-9-]{2,63}") String profileKey,
            @NotBlank @Size(max=120) String displayName,
            @NotBlank @Size(max=512) String baseUrl,
            @NotBlank @Size(max=160) String operationPath,
            @NotBlank @Size(max=160) String model,
            @NotBlank @Pattern(regexp="(env|k8s):[A-Za-z0-9_./-]{3,240}") String secretRef,
            @NotEmpty Set<@Pattern(regexp="TEXT|IMAGE|AUDIO|VIDEO|FILE") String> modalities,
            boolean enabled) {}
    public record ProfileView(String profileKey,String displayName,String baseUrl,String operationPath,String model,
                              String secretRef,Set<String> modalities,boolean enabled,String configurationStatus,
                              Long revision,Instant updatedAt) {}
}

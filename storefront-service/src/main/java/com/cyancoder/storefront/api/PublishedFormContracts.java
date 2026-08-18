package com.cyancoder.storefront.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Map;

public final class PublishedFormContracts {
    private PublishedFormContracts() {}

    public record PublishFormRequest(
            @NotBlank @Pattern(regexp = "[a-z0-9][a-z0-9-]{2,119}") String slug,
            @NotBlank @Pattern(regexp = "[a-z0-9-]+-service") String serviceKey,
            @NotBlank @Size(max = 180) @Pattern(regexp = "[a-z0-9][a-z0-9._-]{0,179}") String entityKey,
            @NotBlank @Size(max = 240) String title,
            @Size(max = 1000) String description,
            @NotBlank @Pattern(regexp = "PUBLIC|AUTHENTICATED") String visibility
    ) {}

    public record PublishedFormSummary(
            String slug, String tenantKey, String siteKey, String serviceKey, String entityKey,
            String title, String description, String visibility, String status, Instant createdAt, Instant updatedAt
    ) {}

    public record PublishedFormView(
            String slug, String title, String description, String visibility,
            String serviceKey, String entityKey, Map<String, Object> definition
    ) {}

    public record FormSubmissionResponse(String submissionKey, String status) {}
}

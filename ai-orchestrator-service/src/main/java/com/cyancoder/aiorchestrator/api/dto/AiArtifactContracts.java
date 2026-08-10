package com.cyancoder.aiorchestrator.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class AiArtifactContracts {
    private AiArtifactContracts() {}
    public record CreateArtifactJobRequest(
            @NotNull AiOperationRequest.AiOperationType operation,
            @NotBlank @Size(max=12000) String instructions,
            Object input,
            @NotBlank String providerProfileKey,
            @Valid List<AiOperationRequest.AiAssetInput> assets,
            @NotBlank String mimeType,
            @NotBlank @Size(max=180) String fileName,
            @Min(1) @Max(365) Integer retentionDays) {}
    public record ArtifactJobView(String jobId,String status,AiOperationRequest.AiOperationType operation,
                                  String providerProfileKey,String assetKey,String deliveryUrl,
                                  Map<String,Object> usage,String errorCode,String errorMessage,
                                  Instant createdAt,Instant startedAt,Instant completedAt) {}
}

package com.cyancoder.aiorchestrator.api;

import com.cyancoder.aiorchestrator.api.dto.GeneratePlatformAppRequest;
import com.cyancoder.aiorchestrator.api.dto.GeneratePlatformAppResponse;
import com.cyancoder.aiorchestrator.service.AiPlatformGenerationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/endpoint/ai-orchestrator")
public class AiPlatformGenerationController {
    private final AiPlatformGenerationService generationService;

    public AiPlatformGenerationController(AiPlatformGenerationService generationService) {
        this.generationService = generationService;
    }

    @PostMapping("/generate/app")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public ResponseEntity<GeneratePlatformAppResponse> generate(@Valid @RequestBody GeneratePlatformAppRequest request) {
        return ResponseEntity.ok(generationService.generate(request));
    }
}

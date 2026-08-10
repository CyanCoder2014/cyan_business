package com.cyancoder.aiorchestrator.api;

import com.cyancoder.aiorchestrator.api.dto.AiOperationRequest;
import com.cyancoder.aiorchestrator.api.dto.AiOperationResponse;
import com.cyancoder.aiorchestrator.service.AiOperationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/ai-orchestrator/operations")
public class InternalAiOperationController {
    private final AiOperationService operations;

    public InternalAiOperationController(AiOperationService operations) { this.operations = operations; }

    @PostMapping
    public AiOperationResponse execute(@Valid @RequestBody AiOperationRequest request,
                                       @RequestHeader(value="X-Tenant-Key",required=false) String tenant,
                                       @RequestHeader(value="X-Site-Key",required=false) String site) {
        return operations.execute(request,tenant,site);
    }
}

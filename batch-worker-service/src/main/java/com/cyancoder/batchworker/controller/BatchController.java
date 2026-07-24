package com.cyancoder.batchworker.controller;

import com.cyancoder.batchworker.api.*;
import com.cyancoder.batchworker.domain.BatchDefinition;
import com.cyancoder.batchworker.domain.BatchRejectedItem;
import com.cyancoder.batchworker.repository.BatchRejectedItemRepository;
import com.cyancoder.batchworker.service.BatchDefinitionService;
import com.cyancoder.batchworker.service.BatchRunService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/endpoint/batch", "/internal/batch"})
@PreAuthorize("hasRole('INTERNAL') or @platformAuthorizationService.canUseCapability('operations:*')")
public class BatchController {
    private final BatchDefinitionService definitions;
    private final BatchRunService runs;
    private final BatchRejectedItemRepository rejectedItems;

    public BatchController(BatchDefinitionService definitions, BatchRunService runs,
                           BatchRejectedItemRepository rejectedItems) {
        this.definitions = definitions;
        this.runs = runs;
        this.rejectedItems = rejectedItems;
    }

    @PostMapping("/definitions")
    public BatchDefinitionResponse saveDefinition(
            @RequestHeader("X-Tenant-Key") String tenant,
            @RequestHeader("X-Site-Key") String site,
            @Valid @RequestBody BatchDefinitionRequest request) {
        BatchDefinition saved = definitions.save(tenant, site, request);
        return BatchDefinitionResponse.from(saved, definitions.spec(saved));
    }

    @GetMapping("/definitions/{definitionKey}")
    public BatchDefinitionResponse definition(
            @RequestHeader("X-Tenant-Key") String tenant,
            @RequestHeader("X-Site-Key") String site,
            @PathVariable String definitionKey) {
        BatchDefinition value = definitions.get(tenant, site, definitionKey);
        return BatchDefinitionResponse.from(value, definitions.spec(value));
    }

    @PostMapping("/definitions/{definitionKey}/runs")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BatchRunResponse start(
            @RequestHeader("X-Tenant-Key") String tenant,
            @RequestHeader("X-Site-Key") String site,
            @PathVariable String definitionKey,
            @Valid @RequestBody StartBatchRequest request) {
        return BatchRunResponse.from(runs.start(tenant, site, definitionKey, request.runKey()));
    }

    @GetMapping("/runs/{id}")
    public BatchRunResponse run(
            @RequestHeader("X-Tenant-Key") String tenant,
            @RequestHeader("X-Site-Key") String site,
            @PathVariable UUID id) {
        return BatchRunResponse.from(runs.get(tenant, site, id));
    }

    @GetMapping("/runs")
    public List<BatchRunResponse> history(
            @RequestHeader("X-Tenant-Key") String tenant,
            @RequestHeader("X-Site-Key") String site,
            @RequestParam(defaultValue = "50") int limit) {
        return runs.history(tenant, site, limit).stream().map(BatchRunResponse::from).toList();
    }

    @GetMapping("/runs/{id}/rejected-items")
    public List<BatchRejectedItem> rejectedItems(
            @RequestHeader("X-Tenant-Key") String tenant,
            @RequestHeader("X-Site-Key") String site,
            @PathVariable UUID id) {
        runs.get(tenant, site, id);
        return rejectedItems.findAllByRunIdOrderByCreatedAtAsc(id);
    }

    @PostMapping("/runs/{id}/retry")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BatchRunResponse retry(
            @RequestHeader("X-Tenant-Key") String tenant,
            @RequestHeader("X-Site-Key") String site,
            @PathVariable UUID id) {
        return BatchRunResponse.from(runs.retry(tenant, site, id));
    }
}

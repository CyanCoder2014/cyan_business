package com.cyancoder.dynamiccore.runtime;

import com.cyancoder.dynamiccore.model.DynamicValidationResult;
import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordDocument;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/entities")
public class InternalDynamicEntityController {

    private final DynamicRuntimeService runtimeService;
    private final DynamicEntityResponseMapper responseMapper;

    public InternalDynamicEntityController(
            DynamicRuntimeService runtimeService,
            DynamicEntityResponseMapper responseMapper
    ) {
        this.runtimeService = runtimeService;
        this.responseMapper = responseMapper;
    }

    @GetMapping("/definitions")
    public List<DynamicEntityDefinitionResponse> listDefinitions(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey
    ) {
        return runtimeService.listDefinitions(DynamicScopeResolver.fromHeaders(tenantKey, siteKey)).stream()
                .map(responseMapper::toDefinitionResponse)
                .toList();
    }

    @GetMapping("/definitions/{entityKey}")
    public DynamicEntityDefinitionResponse getDefinition(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable("entityKey") String entityKey
    ) {
        return responseMapper.toDefinitionResponse(runtimeService.getDefinition(entityKey, DynamicScopeResolver.fromHeaders(tenantKey, siteKey)));
    }

    @DeleteMapping("/definitions/{entityKey}")
    public void deleteDefinition(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable("entityKey") String entityKey
    ) {
        runtimeService.deleteDefinition(entityKey, DynamicScopeResolver.fromHeaders(tenantKey, siteKey));
    }

    @GetMapping("/templates")
    public List<DynamicEntityTemplateResponse> listTemplates() {
        return runtimeService.listTemplates().stream()
                .map(responseMapper::toTemplateResponse)
                .toList();
    }

    @GetMapping("/templates/{templateKey}")
    public DynamicEntityTemplateResponse getTemplate(@PathVariable("templateKey") String templateKey) {
        return responseMapper.toTemplateResponse(runtimeService.getTemplate(templateKey));
    }

    @PostMapping("/templates/{templateKey}/definitions")
    public DynamicEntityDefinitionResponse createFromTemplate(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable("templateKey") String templateKey,
            @RequestBody(required = false) TemplateCreateRequest request
    ) {
        return responseMapper.toDefinitionResponse(runtimeService.createFromTemplate(templateKey, request == null ? null : request.getEntityKey(), DynamicScopeResolver.fromHeaders(
                request != null && request.getTenantKey() != null ? request.getTenantKey() : tenantKey,
                request != null && request.getSiteKey() != null ? request.getSiteKey() : siteKey
        )));
    }

    @PostMapping("/records/{entityKey}/validate")
    public ResponseEntity<DynamicValidationResult> validate(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable("entityKey") String entityKey,
            @RequestBody Map<String, Object> input
    ) {
        DynamicValidationResult result = runtimeService.validate(entityKey, input, true, DynamicScopeResolver.fromHeaders(tenantKey, siteKey));
        return ResponseEntity.status(result.valid() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(result);
    }

    @PostMapping("/submit/{entityKey}")
    public DynamicEntityRecordDocument submitMap(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable("entityKey") String entityKey,
            @RequestBody Map<String, Object> input,
            @RequestParam(value = "recordKey", required = false) String recordKey
    ) {
        return runtimeService.submitMap(entityKey, recordKey, input, true, DynamicScopeResolver.fromHeaders(tenantKey, siteKey));
    }

    @PostMapping("/records/{entityKey}")
    public DynamicEntityRecordDocument submit(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable("entityKey") String entityKey,
            @RequestBody DynamicRecordRequest request
    ) {
        return runtimeService.submit(entityKey, request, true, DynamicScopeResolver.fromHeaders(
                request.getTenantKey() != null ? request.getTenantKey() : tenantKey,
                request.getSiteKey() != null ? request.getSiteKey() : siteKey
        ));
    }

    @PutMapping("/records/{entityKey}/{recordKey}")
    public DynamicEntityRecordDocument replace(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable("entityKey") String entityKey,
            @PathVariable("recordKey") String recordKey,
            @RequestBody DynamicRecordRequest request
    ) {
        return runtimeService.replace(entityKey, recordKey, request, true, DynamicScopeResolver.fromHeaders(
                request.getTenantKey() != null ? request.getTenantKey() : tenantKey,
                request.getSiteKey() != null ? request.getSiteKey() : siteKey
        ));
    }

    @PatchMapping("/records/{entityKey}/{recordKey}")
    public DynamicEntityRecordDocument update(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable("entityKey") String entityKey,
            @PathVariable("recordKey") String recordKey,
            @RequestBody DynamicRecordRequest request
    ) {
        return runtimeService.update(entityKey, recordKey, request, true, DynamicScopeResolver.fromHeaders(
                request.getTenantKey() != null ? request.getTenantKey() : tenantKey,
                request.getSiteKey() != null ? request.getSiteKey() : siteKey
        ));
    }

    @GetMapping("/records/{entityKey}")
    public List<DynamicEntityRecordDocument> listRecords(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable("entityKey") String entityKey
    ) {
        return runtimeService.listRecords(entityKey, DynamicScopeResolver.fromHeaders(tenantKey, siteKey));
    }

    @GetMapping("/records/{entityKey}/{recordKey}")
    public DynamicEntityRecordDocument getRecord(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable("entityKey") String entityKey,
            @PathVariable("recordKey") String recordKey
    ) {
        return runtimeService.getRecord(entityKey, recordKey, DynamicScopeResolver.fromHeaders(tenantKey, siteKey));
    }

    @DeleteMapping("/records/{entityKey}/{recordKey}")
    public void deleteRecord(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable("entityKey") String entityKey,
            @PathVariable("recordKey") String recordKey
    ) {
        runtimeService.deleteRecord(entityKey, recordKey, DynamicScopeResolver.fromHeaders(tenantKey, siteKey));
    }
}

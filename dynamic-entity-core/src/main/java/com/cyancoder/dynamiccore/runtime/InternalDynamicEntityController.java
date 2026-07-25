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
    public DynamicPageResponse<DynamicEntityDefinitionResponse> listDefinitions(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "entityKey,asc") String sort
    ) {
        return DynamicPageResponse.from(
                runtimeService.listDefinitions(
                        DynamicScopeResolver.fromHeaders(tenantKey, siteKey), page, size, sort),
                responseMapper::toDefinitionResponse
        );
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
    public Object listRecords(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable("entityKey") String entityKey,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sort", required = false) String sort
    ) {
        DynamicScope scope = DynamicScopeResolver.fromHeaders(tenantKey, siteKey);
        if (page == null && size == null && sort == null) {
            return runtimeService.listRecords(entityKey, scope);
        }
        return DynamicPageResponse.from(
                runtimeService.listRecords(
                        entityKey, scope, page == null ? 0 : page, size == null ? 200 : size, sort),
                record -> record);
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

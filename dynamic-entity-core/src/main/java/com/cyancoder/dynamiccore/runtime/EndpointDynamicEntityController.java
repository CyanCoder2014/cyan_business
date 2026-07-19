package com.cyancoder.dynamiccore.runtime;

import com.cyancoder.dynamiccore.model.DynamicValidationResult;
import com.cyancoder.dynamiccore.config.DynamicRuntimeProperties;
import com.cyancoder.dynamiccore.store.jpa.StoredEntityDefinition;
import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordDocument;
import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({
        "/endpoint/entities",
        "/api/${dynamic.runtime.service-key}/endpoint/entities"
})
public class EndpointDynamicEntityController {

    private final DynamicRuntimeService runtimeService;
    private final DynamicRuntimeProperties properties;

    public EndpointDynamicEntityController(DynamicRuntimeService runtimeService, DynamicRuntimeProperties properties) {
        this.runtimeService = runtimeService;
        this.properties = properties;
    }

    @PostMapping("/definitions")
    @PreAuthorize("@platformAuthorizationService.canManageService(@endpointDynamicEntityController.serviceKey())")
    public StoredEntityDefinition createDefinition(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @RequestBody DynamicEntityDefinitionRequest request
    ) {
        request.setTenantKey(tenantKey);
        request.setSiteKey(siteKey);
        return runtimeService.saveDefinition(request);
    }

    @PutMapping("/definitions/{entityKey}")
    @PreAuthorize("@platformAuthorizationService.canManageService(@endpointDynamicEntityController.serviceKey())")
    public StoredEntityDefinition updateDefinition(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable("entityKey") String entityKey,
            @RequestBody DynamicEntityDefinitionRequest request
    ) {
        request.setEntityKey(entityKey);
        request.setTenantKey(tenantKey);
        request.setSiteKey(siteKey);
        return runtimeService.saveDefinition(request);
    }

    @GetMapping("/definitions")
    @PreAuthorize("@platformAuthorizationService.canReadService(@endpointDynamicEntityController.serviceKey())")
    public List<StoredEntityDefinition> listDefinitions(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey
    ) {
        return runtimeService.listDefinitions(DynamicScopeResolver.fromHeaders(tenantKey, siteKey));
    }

    @GetMapping("/definitions/{entityKey}")
    @PreAuthorize("@platformAuthorizationService.canReadService(@endpointDynamicEntityController.serviceKey())")
    public StoredEntityDefinition getDefinition(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable("entityKey") String entityKey
    ) {
        return runtimeService.getDefinition(entityKey, DynamicScopeResolver.fromHeaders(tenantKey, siteKey));
    }

    @DeleteMapping("/definitions/{entityKey}")
    @PreAuthorize("@platformAuthorizationService.canManageService(@endpointDynamicEntityController.serviceKey())")
    public void deleteDefinition(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable("entityKey") String entityKey
    ) {
        runtimeService.deleteDefinition(entityKey, DynamicScopeResolver.fromHeaders(tenantKey, siteKey));
    }

    @GetMapping("/templates")
    @PreAuthorize("@platformAuthorizationService.canReadService(@endpointDynamicEntityController.serviceKey())")
    public List<DynamicEntityTemplate> listTemplates() {
        return runtimeService.listTemplates();
    }

    @GetMapping("/templates/{templateKey}")
    @PreAuthorize("@platformAuthorizationService.canReadService(@endpointDynamicEntityController.serviceKey())")
    public DynamicEntityTemplate getTemplate(@PathVariable("templateKey") String templateKey) {
        return runtimeService.getTemplate(templateKey);
    }

    @PostMapping("/templates/{templateKey}/definitions")
    @PreAuthorize("@platformAuthorizationService.canManageService(@endpointDynamicEntityController.serviceKey())")
    public StoredEntityDefinition createFromTemplate(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable("templateKey") String templateKey,
            @RequestBody(required = false) TemplateCreateRequest request
    ) {
        return runtimeService.createFromTemplate(templateKey, request == null ? null : request.getEntityKey(), DynamicScopeResolver.fromHeaders(
                request != null && request.getTenantKey() != null ? request.getTenantKey() : tenantKey,
                request != null && request.getSiteKey() != null ? request.getSiteKey() : siteKey
        ));
    }

    @PostMapping("/records/{entityKey}/validate")
    @PreAuthorize("@platformAuthorizationService.canWriteService(@endpointDynamicEntityController.serviceKey())")
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
    @PreAuthorize("@platformAuthorizationService.canWriteService(@endpointDynamicEntityController.serviceKey())")
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
    @PreAuthorize("@platformAuthorizationService.canWriteService(@endpointDynamicEntityController.serviceKey())")
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
    @PreAuthorize("@platformAuthorizationService.canWriteService(@endpointDynamicEntityController.serviceKey())")
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
    @PreAuthorize("@platformAuthorizationService.canWriteService(@endpointDynamicEntityController.serviceKey())")
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
    @PreAuthorize("@platformAuthorizationService.canReadService(@endpointDynamicEntityController.serviceKey())")
    public List<DynamicEntityRecordDocument> listRecords(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable("entityKey") String entityKey
    ) {
        return runtimeService.listRecords(entityKey, DynamicScopeResolver.fromHeaders(tenantKey, siteKey));
    }

    @GetMapping("/records/{entityKey}/{recordKey}")
    @PreAuthorize("@platformAuthorizationService.canReadService(@endpointDynamicEntityController.serviceKey())")
    public DynamicEntityRecordDocument getRecord(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable("entityKey") String entityKey,
            @PathVariable("recordKey") String recordKey
    ) {
        return runtimeService.getRecord(entityKey, recordKey, DynamicScopeResolver.fromHeaders(tenantKey, siteKey));
    }

    @DeleteMapping("/records/{entityKey}/{recordKey}")
    @PreAuthorize("@platformAuthorizationService.canWriteService(@endpointDynamicEntityController.serviceKey())")
    public void deleteRecord(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable("entityKey") String entityKey,
            @PathVariable("recordKey") String recordKey
    ) {
        runtimeService.deleteRecord(entityKey, recordKey, DynamicScopeResolver.fromHeaders(tenantKey, siteKey));
    }

    public String serviceKey() {
        return properties.getServiceKey();
    }
}

package com.cyancoder.dynamiccore.runtime;

import com.cyancoder.dynamiccore.model.DynamicValidationResult;
import com.cyancoder.dynamiccore.store.jpa.StoredEntityDefinition;
import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordDocument;
import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/endpoint/entities")
public class EndpointDynamicEntityController {

    private final DynamicRuntimeService runtimeService;

    public EndpointDynamicEntityController(DynamicRuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @PostMapping("/definitions")
    public StoredEntityDefinition createDefinition(@RequestBody DynamicEntityDefinitionRequest request) {
        return runtimeService.saveDefinition(request);
    }

    @PutMapping("/definitions/{entityKey}")
    public StoredEntityDefinition updateDefinition(@PathVariable String entityKey, @RequestBody DynamicEntityDefinitionRequest request) {
        request.setEntityKey(entityKey);
        return runtimeService.saveDefinition(request);
    }

    @GetMapping("/definitions")
    public List<StoredEntityDefinition> listDefinitions() {
        return runtimeService.listDefinitions();
    }

    @GetMapping("/definitions/{entityKey}")
    public StoredEntityDefinition getDefinition(@PathVariable String entityKey) {
        return runtimeService.getDefinition(entityKey);
    }

    @GetMapping("/templates")
    public List<DynamicEntityTemplate> listTemplates() {
        return runtimeService.listTemplates();
    }

    @GetMapping("/templates/{templateKey}")
    public DynamicEntityTemplate getTemplate(@PathVariable String templateKey) {
        return runtimeService.getTemplate(templateKey);
    }

    @PostMapping("/templates/{templateKey}/definitions")
    public StoredEntityDefinition createFromTemplate(@PathVariable String templateKey, @RequestBody(required = false) TemplateCreateRequest request) {
        return runtimeService.createFromTemplate(templateKey, request == null ? null : request.getEntityKey());
    }

    @PostMapping("/records/{entityKey}/validate")
    public ResponseEntity<DynamicValidationResult> validate(@PathVariable String entityKey, @RequestBody Map<String, Object> input) {
        DynamicValidationResult result = runtimeService.validate(entityKey, input, true);
        return ResponseEntity.status(result.valid() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(result);
    }

    @PostMapping("/records/{entityKey}")
    public DynamicEntityRecordDocument submit(@PathVariable String entityKey, @RequestBody DynamicRecordRequest request) {
        return runtimeService.submit(entityKey, request, true);
    }

    @PatchMapping("/records/{entityKey}/{recordKey}")
    public DynamicEntityRecordDocument update(@PathVariable String entityKey, @PathVariable String recordKey, @RequestBody DynamicRecordRequest request) {
        return runtimeService.update(entityKey, recordKey, request, true);
    }

    @GetMapping("/records/{entityKey}")
    public List<DynamicEntityRecordDocument> listRecords(@PathVariable String entityKey) {
        return runtimeService.listRecords(entityKey);
    }

    @GetMapping("/records/{entityKey}/{recordKey}")
    public DynamicEntityRecordDocument getRecord(@PathVariable String entityKey, @PathVariable String recordKey) {
        return runtimeService.getRecord(entityKey, recordKey);
    }
}

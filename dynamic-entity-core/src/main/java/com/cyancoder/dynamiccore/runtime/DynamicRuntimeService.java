package com.cyancoder.dynamiccore.runtime;

import com.cyancoder.dynamiccore.config.DynamicRuntimeProperties;
import com.cyancoder.dynamiccore.model.DynamicValidationResult;
import com.cyancoder.dynamiccore.model.EntityDefinitionModel;
import com.cyancoder.dynamiccore.service.DynamicDefinitionParser;
import com.cyancoder.dynamiccore.service.DynamicOperatorEngine;
import com.cyancoder.dynamiccore.service.DynamicValidationEngine;
import com.cyancoder.dynamiccore.store.jpa.StoredEntityDefinition;
import com.cyancoder.dynamiccore.store.jpa.StoredEntityDefinitionRepository;
import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordDocument;
import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordRepository;
import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import com.cyancoder.dynamiccore.template.DynamicTemplateRegistry;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DynamicRuntimeService {

    private final StoredEntityDefinitionRepository definitionRepository;
    private final DynamicEntityRecordRepository recordRepository;
    private final DynamicDefinitionParser definitionParser;
    private final DynamicValidationEngine validationEngine;
    private final DynamicOperatorEngine operatorEngine;
    private final DynamicRuntimeProperties properties;
    private final DynamicTemplateRegistry templateRegistry;

    public DynamicRuntimeService(
            StoredEntityDefinitionRepository definitionRepository,
            DynamicEntityRecordRepository recordRepository,
            DynamicDefinitionParser definitionParser,
            DynamicValidationEngine validationEngine,
            DynamicOperatorEngine operatorEngine,
            DynamicRuntimeProperties properties,
            DynamicTemplateRegistry templateRegistry
    ) {
        this.definitionRepository = definitionRepository;
        this.recordRepository = recordRepository;
        this.definitionParser = definitionParser;
        this.validationEngine = validationEngine;
        this.operatorEngine = operatorEngine;
        this.properties = properties;
        this.templateRegistry = templateRegistry;
    }

    public StoredEntityDefinition saveDefinition(DynamicEntityDefinitionRequest request) {
        EntityDefinitionModel model = definitionParser.parse(request.getDefinitionJson());
        StoredEntityDefinition definition = definitionRepository
                .findByServiceKeyAndEntityKey(properties.getServiceKey(), request.getEntityKey())
                .orElseGet(StoredEntityDefinition::new);
        definition.setServiceKey(properties.getServiceKey());
        definition.setEntityKey(request.getEntityKey());
        definition.setEntityType(model.getEntityType());
        definition.setTitle(model.getTitle());
        definition.setDefinitionJson(request.getDefinitionJson());
        definition.setActive(true);
        return definitionRepository.save(definition);
    }

    public List<StoredEntityDefinition> listDefinitions() {
        return definitionRepository.findByServiceKeyOrderByEntityKeyAsc(properties.getServiceKey());
    }

    public StoredEntityDefinition getDefinition(String entityKey) {
        return definitionRepository.findByServiceKeyAndEntityKey(properties.getServiceKey(), entityKey).orElseThrow();
    }

    public List<DynamicEntityTemplate> listTemplates() {
        return templateRegistry.list();
    }

    public DynamicEntityTemplate getTemplate(String templateKey) {
        return templateRegistry.get(templateKey);
    }

    public StoredEntityDefinition createFromTemplate(String templateKey, String entityKeyOverride) {
        DynamicEntityTemplate template = getTemplate(templateKey);
        EntityDefinitionModel model = definitionParser.parse(template.getDefinitionJson());
        String resolvedEntityKey = entityKeyOverride == null || entityKeyOverride.isBlank()
                ? (model.getEntityKey() == null || model.getEntityKey().isBlank() ? templateKey : model.getEntityKey())
                : entityKeyOverride;

        model.setServiceKey(properties.getServiceKey());
        model.setEntityKey(resolvedEntityKey);

        DynamicEntityDefinitionRequest request = new DynamicEntityDefinitionRequest();
        request.setEntityKey(resolvedEntityKey);
        request.setDefinitionJson(definitionParser.write(model));
        return saveDefinition(request);
    }

    public DynamicValidationResult validate(String entityKey, Map<String, Object> input, boolean strict) {
        StoredEntityDefinition stored = getDefinition(entityKey);
        EntityDefinitionModel definition = definitionParser.parse(stored.getDefinitionJson());
        Map<String, Object> merged = mergeForValidation(definition, input);
        DynamicValidationResult result = validationEngine.validate(properties.getServiceKey(), entityKey, definition.getFields(), definition.getValidations(), merged, !strict);
        return new DynamicValidationResult(applyNonFieldDefaults(definition, result.data()), result.errors());
    }

    public DynamicEntityRecordDocument submit(String entityKey, DynamicRecordRequest request, boolean strict) {
        StoredEntityDefinition stored = getDefinition(entityKey);
        EntityDefinitionModel definition = definitionParser.parse(stored.getDefinitionJson());
        Map<String, Object> merged = mergeForValidation(definition, request.getData());
        DynamicValidationResult result = validationEngine.validate(properties.getServiceKey(), entityKey, definition.getFields(), definition.getValidations(), merged, !strict);
        if (!result.valid()) {
            throw new IllegalArgumentException("validation failed: " + result.errors());
        }
        Map<String, Object> resolved = applyNonFieldDefaults(definition, result.data());
        Map<String, Object> operated = operatorEngine.apply(definition.getFields(), definition.getOperations(), new LinkedHashMap<>(resolved));
        DynamicEntityRecordDocument document = new DynamicEntityRecordDocument();
        document.setServiceKey(properties.getServiceKey());
        document.setEntityKey(entityKey);
        document.setRecordKey(request.getRecordKey() == null || request.getRecordKey().isBlank() ? UUID.randomUUID().toString() : request.getRecordKey());
        document.setData(operated);
        document.setRelations(definition.getRelationDefinitions());
        document.setStatus("ACTIVE");
        document.setCreatedAt(Instant.now());
        document.setUpdatedAt(Instant.now());
        return recordRepository.save(document);
    }

    public DynamicEntityRecordDocument update(String entityKey, String recordKey, DynamicRecordRequest request, boolean strict) {
        DynamicEntityRecordDocument existing = getRecord(entityKey, recordKey);
        DynamicRecordRequest mergedRequest = new DynamicRecordRequest();
        mergedRequest.setRecordKey(recordKey);
        Map<String, Object> data = new LinkedHashMap<>(existing.getData() == null ? Map.of() : existing.getData());
        if (request.getData() != null) {
            data.putAll(request.getData());
        }
        mergedRequest.setData(data);
        DynamicEntityRecordDocument updated = submit(entityKey, mergedRequest, strict);
        updated.setId(existing.getId());
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setUpdatedAt(Instant.now());
        return recordRepository.save(updated);
    }

    public DynamicEntityRecordDocument getRecord(String entityKey, String recordKey) {
        return recordRepository.findByServiceKeyAndEntityKeyAndRecordKey(properties.getServiceKey(), entityKey, recordKey).orElseThrow();
    }

    public List<DynamicEntityRecordDocument> listRecords(String entityKey) {
        return recordRepository.findByServiceKeyAndEntityKeyOrderByCreatedAtDesc(properties.getServiceKey(), entityKey);
    }

    private Map<String, Object> mergeForValidation(EntityDefinitionModel definition, Map<String, Object> input) {
        Map<String, Object> merged = new LinkedHashMap<>();
        if (definition.getDefaultValues() != null && definition.getFields() != null) {
            for (Map.Entry<String, Object> entry : definition.getDefaultValues().entrySet()) {
                if (definition.getFields().containsKey(entry.getKey())) {
                    merged.put(entry.getKey(), entry.getValue());
                }
            }
        }
        if (input != null) {
            merged.putAll(input);
        }
        return merged;
    }

    private Map<String, Object> applyNonFieldDefaults(EntityDefinitionModel definition, Map<String, Object> data) {
        Map<String, Object> resolved = new LinkedHashMap<>(data == null ? Map.of() : data);
        if (definition.getDefaultValues() != null) {
            for (Map.Entry<String, Object> entry : definition.getDefaultValues().entrySet()) {
                resolved.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
        return resolved;
    }
}

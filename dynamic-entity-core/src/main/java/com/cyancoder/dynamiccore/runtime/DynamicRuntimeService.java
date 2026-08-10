package com.cyancoder.dynamiccore.runtime;

import com.cyancoder.dynamiccore.config.DynamicRuntimeProperties;
import com.cyancoder.dynamiccore.model.DynamicValidationResult;
import com.cyancoder.dynamiccore.model.EntityDefinitionModel;
import com.cyancoder.dynamiccore.service.DynamicDefinitionParser;
import com.cyancoder.dynamiccore.service.DynamicOperatorEngine;
import com.cyancoder.dynamiccore.service.DynamicValidationEngine;
import com.cyancoder.dynamiccore.store.jpa.StoredEntityDefinition;
import com.cyancoder.dynamiccore.store.jpa.StoredEntityDefinitionRepository;
import com.cyancoder.dynamiccore.store.jpa.StoredEntityDefinitionVersion;
import com.cyancoder.dynamiccore.store.jpa.StoredEntityDefinitionVersionRepository;
import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordDocument;
import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordRepository;
import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import com.cyancoder.dynamiccore.template.DynamicTemplateRegistry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DynamicRuntimeService {
    private static final int DEFAULT_DEFINITION_PAGE_SIZE = 20;
    private static final int MAX_DEFINITION_PAGE_SIZE = 200;
    private static final int DEFAULT_RECORD_PAGE_SIZE = 200;
    private static final int MAX_RECORD_PAGE_SIZE = 1000;

    private final StoredEntityDefinitionRepository definitionRepository;
    private final StoredEntityDefinitionVersionRepository definitionVersionRepository;
    private final DynamicEntityRecordRepository recordRepository;
    private final DynamicDefinitionParser definitionParser;
    private final DynamicValidationEngine validationEngine;
    private final DynamicOperatorEngine operatorEngine;
    private final DynamicRuntimeProperties properties;
    private final DynamicTemplateRegistry templateRegistry;

    public DynamicRuntimeService(
            StoredEntityDefinitionRepository definitionRepository,
            StoredEntityDefinitionVersionRepository definitionVersionRepository,
            DynamicEntityRecordRepository recordRepository,
            DynamicDefinitionParser definitionParser,
            DynamicValidationEngine validationEngine,
            DynamicOperatorEngine operatorEngine,
            DynamicRuntimeProperties properties,
            DynamicTemplateRegistry templateRegistry
    ) {
        this.definitionRepository = definitionRepository;
        this.definitionVersionRepository = definitionVersionRepository;
        this.recordRepository = recordRepository;
        this.definitionParser = definitionParser;
        this.validationEngine = validationEngine;
        this.operatorEngine = operatorEngine;
        this.properties = properties;
        this.templateRegistry = templateRegistry;
    }

    public StoredEntityDefinition saveDefinition(DynamicEntityDefinitionRequest request) {
        ResolvedDefinition resolvedDefinition = resolveDefinition(request);
        EntityDefinitionModel model = resolvedDefinition.model();
        DynamicScope scope = DynamicScopeResolver.fromRequest(request.getTenantKey(), request.getSiteKey());
        StoredEntityDefinition definition = definitionRepository
                .findByServiceKeyAndTenantKeyAndSiteKeyAndEntityKey(properties.getServiceKey(), scope.tenantKey(), scope.siteKey(), request.getEntityKey())
                .orElseGet(StoredEntityDefinition::new);
        if (request.getExpectedRevision() != null && definition.getId() != null
                && request.getExpectedRevision() != definition.getRevision()) {
            throw new OptimisticLockingFailureException("Definition revision is stale; reload before saving");
        }
        definition.setServiceKey(properties.getServiceKey());
        definition.setTenantKey(scope.tenantKey());
        definition.setSiteKey(scope.siteKey());
        definition.setEntityKey(request.getEntityKey());
        definition.setEntityType(model.getEntityType());
        definition.setTitle(model.getTitle());
        definition.setDefinitionJson(resolvedDefinition.json());
        definition.setActive(true);
        definition.setRevision(definition.getRevision() + 1);
        StoredEntityDefinition saved = definitionRepository.save(definition);
        StoredEntityDefinitionVersion version = new StoredEntityDefinitionVersion();
        version.setServiceKey(saved.getServiceKey()); version.setTenantKey(saved.getTenantKey()); version.setSiteKey(saved.getSiteKey()); version.setEntityKey(saved.getEntityKey());
        version.setRevision(saved.getRevision()); version.setStatus("DRAFT"); version.setDefinitionJson(saved.getDefinitionJson()); version.setCreatedAt(Instant.now()); definitionVersionRepository.save(version);
        return saved;
    }

    private ResolvedDefinition resolveDefinition(DynamicEntityDefinitionRequest request) {
        if (request.getDefinition() != null) {
            EntityDefinitionModel model = request.getDefinition();
            model.setServiceKey(properties.getServiceKey());
            model.setEntityKey(request.getEntityKey());
            return new ResolvedDefinition(model, definitionParser.write(model));
        }
        if (request.getDefinitionJson() == null || request.getDefinitionJson().isBlank()) {
            throw new IllegalArgumentException("definition or definitionJson is required");
        }
        return new ResolvedDefinition(definitionParser.parse(request.getDefinitionJson()), request.getDefinitionJson());
    }

    public Page<StoredEntityDefinition> listDefinitions(int page, int size, String sort) {
        return listDefinitions(new DynamicScope(null, null), page, size, sort);
    }

    public Page<StoredEntityDefinition> listDefinitions(
            DynamicScope scope, int page, int size, String sort) {
        int safePage = Math.max(page, 0);
        int safeSize = size < 1
                ? DEFAULT_DEFINITION_PAGE_SIZE
                : Math.min(size, MAX_DEFINITION_PAGE_SIZE);
        PageRequest pageable = PageRequest.of(
                safePage,
                safeSize,
                definitionSort(sort)
        );
        return definitionRepository.findByServiceKeyAndTenantKeyAndSiteKey(
                properties.getServiceKey(), scope.tenantKey(), scope.siteKey(), pageable);
    }

    private Sort definitionSort(String value) {
        String[] parts = value == null ? new String[0] : value.split(",", 2);
        String property = switch (parts.length == 0 ? "" : parts[0].trim()) {
            case "title" -> "title";
            case "entityType" -> "entityType";
            case "createdAt" -> "createdAt";
            case "updatedAt" -> "updatedAt";
            default -> "entityKey";
        };
        Sort.Direction direction = parts.length > 1
                && "desc".equalsIgnoreCase(parts[1].trim())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Sort requested = Sort.by(direction, property);
        return "entityKey".equals(property)
                ? requested
                : requested.and(Sort.by(Sort.Direction.ASC, "entityKey"));
    }

    public StoredEntityDefinition getDefinition(String entityKey) {
        return getDefinition(entityKey, new DynamicScope(null, null));
    }

    public StoredEntityDefinition getDefinition(String entityKey, DynamicScope scope) {
        return definitionRepository.findByServiceKeyAndTenantKeyAndSiteKeyAndEntityKey(properties.getServiceKey(), scope.tenantKey(), scope.siteKey(), entityKey).orElseThrow();
    }

    public List<StoredEntityDefinitionVersion> listDefinitionVersions(String entityKey, DynamicScope scope) {
        getDefinition(entityKey, scope);
        return definitionVersionRepository.findByServiceKeyAndTenantKeyAndSiteKeyAndEntityKeyOrderByRevisionDesc(properties.getServiceKey(), scope.tenantKey(), scope.siteKey(), entityKey);
    }

    public StoredEntityDefinition publishDefinition(String entityKey, DynamicScope scope) {
        StoredEntityDefinition value=getDefinition(entityKey,scope); value.setActive(true); StoredEntityDefinition saved=definitionRepository.save(value);
        definitionVersionRepository.findByServiceKeyAndTenantKeyAndSiteKeyAndEntityKeyAndRevision(properties.getServiceKey(),scope.tenantKey(),scope.siteKey(),entityKey,saved.getRevision()).ifPresent(v->{v.setStatus("PUBLISHED");definitionVersionRepository.save(v);}); return saved;
    }

    public void deleteDefinition(String entityKey) {
        deleteDefinition(entityKey, new DynamicScope(null, null));
    }

    public void deleteDefinition(String entityKey, DynamicScope scope) {
        getDefinition(entityKey, scope);
        definitionRepository.deleteByServiceKeyAndTenantKeyAndSiteKeyAndEntityKey(properties.getServiceKey(), scope.tenantKey(), scope.siteKey(), entityKey);
    }

    public List<DynamicEntityTemplate> listTemplates() {
        return templateRegistry.list();
    }

    public DynamicEntityTemplate getTemplate(String templateKey) {
        return templateRegistry.get(templateKey);
    }

    public StoredEntityDefinition createFromTemplate(String templateKey, String entityKeyOverride, DynamicScope scope) {
        DynamicEntityTemplate template = getTemplate(templateKey);
        EntityDefinitionModel model = definitionParser.parse(template.getDefinitionJson());
        String resolvedEntityKey = entityKeyOverride == null || entityKeyOverride.isBlank()
                ? (model.getEntityKey() == null || model.getEntityKey().isBlank() ? templateKey : model.getEntityKey())
                : entityKeyOverride;

        model.setServiceKey(properties.getServiceKey());
        model.setEntityKey(resolvedEntityKey);

        DynamicEntityDefinitionRequest request = new DynamicEntityDefinitionRequest();
        request.setEntityKey(resolvedEntityKey);
        request.setTenantKey(scope.tenantKey());
        request.setSiteKey(scope.siteKey());
        request.setDefinitionJson(definitionParser.write(model));
        return saveDefinition(request);
    }

    public StoredEntityDefinition createFromTemplate(String templateKey, String entityKeyOverride) {
        return createFromTemplate(templateKey, entityKeyOverride, new DynamicScope(null, null));
    }

    public DynamicValidationResult validate(String entityKey, Map<String, Object> input, boolean strict, DynamicScope scope) {
        StoredEntityDefinition stored = getDefinition(entityKey, scope);
        EntityDefinitionModel definition = definitionParser.parse(stored.getDefinitionJson());
        Map<String, Object> merged = mergeForValidation(definition, input);
        DynamicValidationResult result = validationEngine.validate(properties.getServiceKey(), entityKey, definition.getFields(), definition.getValidations(), merged, !strict);
        return new DynamicValidationResult(applyNonFieldDefaults(definition, result.data()), result.errors());
    }

    public DynamicValidationResult validate(String entityKey, Map<String, Object> input, boolean strict) {
        return validate(entityKey, input, strict, new DynamicScope(null, null));
    }

    public DynamicEntityRecordDocument submitMap(String entityKey, String recordKey, Map<String, Object> input, boolean strict, DynamicScope scope) {
        DynamicRecordRequest request = new DynamicRecordRequest();
        request.setRecordKey(recordKey);
        request.setTenantKey(scope.tenantKey());
        request.setSiteKey(scope.siteKey());
        request.setData(input);
        return submit(entityKey, request, strict, scope);
    }

    public DynamicEntityRecordDocument submitMap(String entityKey, String recordKey, Map<String, Object> input, boolean strict) {
        return submitMap(entityKey, recordKey, input, strict, new DynamicScope(null, null));
    }

    public DynamicEntityRecordDocument submit(String entityKey, DynamicRecordRequest request, boolean strict, DynamicScope scope) {
        StoredEntityDefinition stored = getDefinition(entityKey, scope);
        EntityDefinitionModel definition = definitionParser.parse(stored.getDefinitionJson());
        Map<String, Object> merged = mergeForValidation(definition, request.getData());
        DynamicValidationResult result = validationEngine.validate(properties.getServiceKey(), entityKey, definition.getFields(), definition.getValidations(), merged, !strict);
        if (!result.valid()) {
            throw new IllegalArgumentException("validation failed: " + result.errors());
        }
        Map<String, Object> resolved = applyNonFieldDefaults(definition, result.data());
        Map<String, Object> operated = operatorEngine.apply(definition.getFields(), definition.getOperations(), new LinkedHashMap<>(resolved));
        String resolvedRecordKey = request.getRecordKey() == null || request.getRecordKey().isBlank() ? UUID.randomUUID().toString() : request.getRecordKey();
        DynamicEntityRecordDocument document = recordRepository
                .findFirstByServiceKeyAndTenantKeyAndSiteKeyAndEntityKeyAndRecordKeyOrderByUpdatedAtDesc(properties.getServiceKey(), scope.tenantKey(), scope.siteKey(), entityKey, resolvedRecordKey)
                .orElseGet(DynamicEntityRecordDocument::new);
        document.setServiceKey(properties.getServiceKey());
        document.setTenantKey(scope.tenantKey());
        document.setSiteKey(scope.siteKey());
        document.setEntityKey(entityKey);
        document.setRecordKey(resolvedRecordKey);
        document.setData(operated);
        document.setRelations(definition.getRelationDefinitions());
        document.setStatus("ACTIVE");
        if (document.getCreatedAt() == null) {
            document.setCreatedAt(Instant.now());
        }
        document.setUpdatedAt(Instant.now());
        return recordRepository.save(document);
    }

    public DynamicEntityRecordDocument submit(String entityKey, DynamicRecordRequest request, boolean strict) {
        return submit(entityKey, request, strict, new DynamicScope(null, null));
    }

    public DynamicEntityRecordDocument update(String entityKey, String recordKey, DynamicRecordRequest request, boolean strict, DynamicScope scope) {
        DynamicEntityRecordDocument existing = getRecord(entityKey, recordKey, scope);
        DynamicRecordRequest mergedRequest = new DynamicRecordRequest();
        mergedRequest.setRecordKey(recordKey);
        mergedRequest.setTenantKey(scope.tenantKey());
        mergedRequest.setSiteKey(scope.siteKey());
        Map<String, Object> data = new LinkedHashMap<>(existing.getData() == null ? Map.of() : existing.getData());
        if (request.getData() != null) {
            data.putAll(request.getData());
        }
        mergedRequest.setData(data);
        DynamicEntityRecordDocument updated = submit(entityKey, mergedRequest, strict, scope);
        updated.setId(existing.getId());
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setUpdatedAt(Instant.now());
        return recordRepository.save(updated);
    }

    public DynamicEntityRecordDocument update(String entityKey, String recordKey, DynamicRecordRequest request, boolean strict) {
        return update(entityKey, recordKey, request, strict, new DynamicScope(null, null));
    }

    public DynamicEntityRecordDocument replace(String entityKey, String recordKey, DynamicRecordRequest request, boolean strict, DynamicScope scope) {
        DynamicEntityRecordDocument existing = getRecord(entityKey, recordKey, scope);
        DynamicRecordRequest replaceRequest = new DynamicRecordRequest();
        replaceRequest.setRecordKey(recordKey);
        replaceRequest.setTenantKey(scope.tenantKey());
        replaceRequest.setSiteKey(scope.siteKey());
        replaceRequest.setData(request.getData());
        DynamicEntityRecordDocument replaced = submit(entityKey, replaceRequest, strict, scope);
        replaced.setId(existing.getId());
        replaced.setCreatedAt(existing.getCreatedAt());
        replaced.setUpdatedAt(Instant.now());
        return recordRepository.save(replaced);
    }

    public DynamicEntityRecordDocument replace(String entityKey, String recordKey, DynamicRecordRequest request, boolean strict) {
        return replace(entityKey, recordKey, request, strict, new DynamicScope(null, null));
    }

    public DynamicEntityRecordDocument getRecord(String entityKey, String recordKey, DynamicScope scope) {
        return recordRepository.findFirstByServiceKeyAndTenantKeyAndSiteKeyAndEntityKeyAndRecordKeyOrderByUpdatedAtDesc(properties.getServiceKey(), scope.tenantKey(), scope.siteKey(), entityKey, recordKey).orElseThrow();
    }

    public DynamicEntityRecordDocument getRecord(String entityKey, String recordKey) {
        return getRecord(entityKey, recordKey, new DynamicScope(null, null));
    }

    public List<DynamicEntityRecordDocument> listRecords(String entityKey, DynamicScope scope) {
        return recordRepository.findByServiceKeyAndTenantKeyAndSiteKeyAndEntityKeyOrderByCreatedAtDesc(properties.getServiceKey(), scope.tenantKey(), scope.siteKey(), entityKey);
    }

    public List<DynamicEntityRecordDocument> listRecords(String entityKey) {
        return listRecords(entityKey, new DynamicScope(null, null));
    }

    public Page<DynamicEntityRecordDocument> listRecords(
            String entityKey, DynamicScope scope, int page, int size, String sort) {
        int safePage = Math.max(page, 0);
        int safeSize = size < 1
                ? DEFAULT_RECORD_PAGE_SIZE
                : Math.min(size, MAX_RECORD_PAGE_SIZE);
        return recordRepository.findByServiceKeyAndTenantKeyAndSiteKeyAndEntityKey(
                properties.getServiceKey(),
                scope.tenantKey(),
                scope.siteKey(),
                entityKey,
                PageRequest.of(safePage, safeSize, recordSort(sort)));
    }

    private Sort recordSort(String value) {
        String[] parts = value == null ? new String[0] : value.split(",", 2);
        String property = switch (parts.length == 0 ? "" : parts[0].trim()) {
            case "recordKey" -> "recordKey";
            case "updatedAt" -> "updatedAt";
            case "status" -> "status";
            default -> "createdAt";
        };
        Sort.Direction direction = parts.length > 1
                && "asc".equalsIgnoreCase(parts[1].trim())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Sort requested = Sort.by(direction, property);
        return "recordKey".equals(property)
                ? requested
                : requested.and(Sort.by(Sort.Direction.ASC, "recordKey"));
    }

    public void deleteRecord(String entityKey, String recordKey, DynamicScope scope) {
        getRecord(entityKey, recordKey, scope);
        recordRepository.deleteAllByServiceKeyAndTenantKeyAndSiteKeyAndEntityKeyAndRecordKey(properties.getServiceKey(), scope.tenantKey(), scope.siteKey(), entityKey, recordKey);
    }

    public void deleteRecord(String entityKey, String recordKey) {
        deleteRecord(entityKey, recordKey, new DynamicScope(null, null));
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

    private record ResolvedDefinition(EntityDefinitionModel model, String json) {
    }
}

package com.cyancoder.dynamiccore.runtime;

import com.cyancoder.dynamiccore.config.DynamicRuntimeProperties;
import com.cyancoder.dynamiccore.model.EntityDefinitionModel;
import com.cyancoder.dynamiccore.model.FieldDefinition;
import com.cyancoder.dynamiccore.service.DynamicDefinitionParser;
import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordDocument;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Backs relation-field pickers: a scoped, searchable, paginated lookup over another entity's records
 * that returns only {recordKey, label} rather than whole records, so a picker never over-fetches and
 * never exposes fields the form did not ask for.
 */
public class DynamicRelationLookupService {
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private final MongoTemplate mongoTemplate;
    private final DynamicRuntimeService runtimeService;
    private final DynamicDefinitionParser definitionParser;
    private final DynamicRuntimeProperties properties;

    public DynamicRelationLookupService(MongoTemplate mongoTemplate,
                                        DynamicRuntimeService runtimeService,
                                        DynamicDefinitionParser definitionParser,
                                        DynamicRuntimeProperties properties) {
        this.mongoTemplate = mongoTemplate;
        this.runtimeService = runtimeService;
        this.definitionParser = definitionParser;
        this.properties = properties;
    }

    public record LookupItem(String recordKey, String label) {}

    public record LookupPage(List<LookupItem> items, long total, int page, int size) {}

    /**
     * @param displayField optional override; when absent the target definition's own first string
     *                     field is used, falling back to the record key itself.
     */
    public LookupPage lookup(String entityKey, DynamicScope scope, String query, String displayField, int page, int size) {
        String resolvedDisplayField = displayField == null || displayField.isBlank()
                ? defaultDisplayField(entityKey, scope)
                : displayField;
        int safePage = Math.max(page, 0);
        int safeSize = size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);

        Criteria criteria = Criteria.where("serviceKey").is(properties.getServiceKey())
                .and("tenantKey").is(scope.tenantKey())
                .and("siteKey").is(scope.siteKey())
                .and("entityKey").is(entityKey);
        if (query != null && !query.isBlank()) {
            String safeQuery = Pattern.quote(query.trim());
            List<Criteria> matches = new ArrayList<>();
            matches.add(Criteria.where("recordKey").regex(safeQuery, "i"));
            if (resolvedDisplayField != null && !resolvedDisplayField.isBlank()) {
                matches.add(Criteria.where("data." + resolvedDisplayField).regex(safeQuery, "i"));
            }
            criteria = criteria.andOperator(new Criteria().orOperator(matches.toArray(new Criteria[0])));
        }

        Query mongoQuery = new Query(criteria);
        long total = mongoTemplate.count(mongoQuery, DynamicEntityRecordDocument.class);
        mongoQuery.with(PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "updatedAt")));
        List<DynamicEntityRecordDocument> records = mongoTemplate.find(mongoQuery, DynamicEntityRecordDocument.class);

        List<LookupItem> items = records.stream()
                .map(record -> new LookupItem(record.getRecordKey(), label(record, resolvedDisplayField)))
                .toList();
        return new LookupPage(items, total, safePage, safeSize);
    }

    /** Resolves the labels for already-selected values, so an edit form can show names instead of raw keys. */
    public List<LookupItem> resolve(String entityKey, DynamicScope scope, List<String> recordKeys, String displayField) {
        if (recordKeys == null || recordKeys.isEmpty()) {
            return List.of();
        }
        String resolvedDisplayField = displayField == null || displayField.isBlank()
                ? defaultDisplayField(entityKey, scope)
                : displayField;
        Query mongoQuery = new Query(Criteria.where("serviceKey").is(properties.getServiceKey())
                .and("tenantKey").is(scope.tenantKey())
                .and("siteKey").is(scope.siteKey())
                .and("entityKey").is(entityKey)
                .and("recordKey").in(recordKeys));
        return mongoTemplate.find(mongoQuery, DynamicEntityRecordDocument.class).stream()
                .map(record -> new LookupItem(record.getRecordKey(), label(record, resolvedDisplayField)))
                .toList();
    }

    /** The relation config the form should use for this field, or null when it is not a relation field. */
    public Map<String, Object> relationConfig(String entityKey, DynamicScope scope, String fieldName) {
        EntityDefinitionModel definition = definitionParser.parse(runtimeService.getDefinition(entityKey, scope).getDefinitionJson());
        if (definition.getFields() == null) {
            return null;
        }
        FieldDefinition field = definition.getFields().get(fieldName);
        if (field == null || field.getRelation() == null) {
            return null;
        }
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("serviceKey", field.getRelation().getServiceKey());
        config.put("entityKey", field.getRelation().getEntityKey());
        config.put("displayField", field.getRelation().getDisplayField());
        config.put("publicLookup", field.getRelation().isPublicLookup());
        return config;
    }

    private String defaultDisplayField(String entityKey, DynamicScope scope) {
        try {
            EntityDefinitionModel definition = definitionParser.parse(runtimeService.getDefinition(entityKey, scope).getDefinitionJson());
            if (definition.getFields() == null) {
                return null;
            }
            for (Map.Entry<String, FieldDefinition> entry : definition.getFields().entrySet()) {
                String type = entry.getValue().getType();
                if (type == null || "string".equals(type)) {
                    return entry.getKey();
                }
            }
        } catch (Exception ignored) {
            // A missing or unreadable target definition should degrade to record keys, not fail the lookup.
        }
        return null;
    }

    private String label(DynamicEntityRecordDocument record, String displayField) {
        if (displayField != null && record.getData() != null) {
            Object value = record.getData().get(displayField);
            if (value != null && !String.valueOf(value).isBlank()) {
                return String.valueOf(value);
            }
        }
        return record.getRecordKey();
    }
}

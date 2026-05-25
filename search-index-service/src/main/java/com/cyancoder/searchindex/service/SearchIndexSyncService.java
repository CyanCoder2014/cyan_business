package com.cyancoder.searchindex.service;

import com.cyancoder.dynamiccore.runtime.DynamicEntityDefinitionRequest;
import com.cyancoder.dynamiccore.runtime.DynamicRuntimeService;
import com.cyancoder.searchindex.model.SearchIndexSyncResponse;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class SearchIndexSyncService {
    private final DynamicRuntimeService dynamicRuntimeService;
    private final InternalServiceHttpSupport httpSupport;

    public SearchIndexSyncService(DynamicRuntimeService dynamicRuntimeService, InternalServiceHttpSupport httpSupport) {
        this.dynamicRuntimeService = dynamicRuntimeService;
        this.httpSupport = httpSupport;
    }

    public SearchIndexSyncResponse sync(String sourceServiceKey, String sourceEntityKey) {
        ensureSearchDocumentDefinition();
        List<Map<String, Object>> rows = httpSupport.getList(sourceServiceKey, "/internal/entities/records/" + sourceEntityKey);
        int synced = 0;
        for (Map<String, Object> row : rows) {
            Map<String, Object> data = objectMap(row.get("data"));
            String recordKey = Objects.toString(row.get("recordKey"), null);
            if (recordKey == null) {
                continue;
            }
            Map<String, Object> projection = new LinkedHashMap<>();
            projection.put("documentKey", sourceServiceKey + ":" + sourceEntityKey + ":" + recordKey);
            projection.put("source", Map.of(
                    "serviceKey", sourceServiceKey,
                    "entityKey", sourceEntityKey,
                    "recordKey", recordKey
            ));
            projection.put("title", firstNonBlank(data.get("title"), data.get("name"), data.get("slug"), recordKey));
            projection.put("summary", firstNonBlank(data.get("summary"), data.get("description"), data.get("heroSubtitle"), ""));
            projection.put("content", flatten(data));
            projection.put("entityType", Objects.toString(data.get("itemType"), Objects.toString(row.get("entityKey"), sourceEntityKey)));
            projection.put("status", Objects.toString(row.get("status"), "ACTIVE"));
            projection.put("keywords", keywords(data));
            projection.put("routing", routing(data));
            projection.put("filters", filters(sourceEntityKey, data));
            projection.put("sortValues", sortValues(data));
            projection.put("seoSignals", Map.of(
                    "canonicalUrl", firstNonBlank(
                            nestedValue(data, "seo", "canonicalUrl"),
                            nestedValue(data, "routing", "primaryPath"),
                            data.get("canonicalUrl"),
                            data.get("slug")
                    ),
                    "lastModified", Objects.toString(row.get("updatedAt"), ""),
                    "indexable", "true",
                    "metaTitle", firstNonBlank(nestedValue(data, "seo", "title"), data.get("title"), data.get("name"), ""),
                    "metaDescription", firstNonBlank(nestedValue(data, "seo", "description"), data.get("summary"), nestedValue(data, "details", "shortDescription"), ""),
                    "schemaType", firstNonBlank(nestedValue(data, "seo", "schemaType"), defaultSchemaType(sourceEntityKey), "Thing")
            ));
            dynamicRuntimeService.submitMap("search-document", (String) projection.get("documentKey"), projection, true);
            synced++;
        }
        return new SearchIndexSyncResponse(sourceServiceKey, sourceEntityKey, synced);
    }

    private void ensureSearchDocumentDefinition() {
        try {
            dynamicRuntimeService.getDefinition("search-document");
        } catch (Exception ex) {
            dynamicRuntimeService.createFromTemplate("search-document", "search-document");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> objectMap(Object raw) {
        return raw instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    private String flatten(Map<String, Object> data) {
        return data.entrySet().stream()
                .map(entry -> entry.getKey() + ":" + Objects.toString(entry.getValue(), ""))
                .reduce("", (left, right) -> left.isBlank() ? right : left + " " + right);
    }

    private List<String> keywords(Map<String, Object> data) {
        Object tags = data.get("tags");
        if (tags instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        Object searchIndex = data.get("searchIndex");
        if (searchIndex instanceof Map<?, ?> map) {
            Object keywords = map.get("keywords");
            if (keywords instanceof List<?> list) {
                return list.stream().map(String::valueOf).toList();
            }
        }
        return List.of();
    }

    private Map<String, Object> routing(Map<String, Object> data) {
        return Map.of(
                "path", firstNonBlank(
                        nestedValue(data, "routing", "primaryPath"),
                        data.get("slug"),
                        ""
                ),
                "canonicalUrl", firstNonBlank(
                        nestedValue(data, "seo", "canonicalUrl"),
                        data.get("canonicalUrl"),
                        ""
                ),
                "sitemapPriority", numericValue(nestedValue(data, "routing", "sitemapPriority")),
                "changeFrequency", firstNonBlank(nestedValue(data, "routing", "changeFrequency"), "weekly")
        );
    }

    private List<Map<String, Object>> filters(String sourceEntityKey, Map<String, Object> data) {
        Stream<Map<String, Object>> base = Stream.of(
                filterEntry("sourceEntity", "Source Entity", "TEXT", sourceEntityKey, null, null, List.of()),
                filterEntry("status", "Status", "TEXT", Objects.toString(data.get("status"), "ACTIVE"), null, null, List.of())
        );

        Stream<Map<String, Object>> category = Stream.ofNullable(stringOrNull(data.get("categoryKey")))
                .map(value -> filterEntry("categoryKey", "Category", "TEXT", value, null, null, List.of()));

        Stream<Map<String, Object>> brand = Stream.ofNullable(stringOrNull(nestedValue(data, "details", "brand")))
                .map(value -> filterEntry("brand", "Brand", "TEXT", value, null, null, List.of()));

        Stream<Map<String, Object>> tags = listOfStrings(data.get("tags")).isEmpty()
                ? Stream.empty()
                : Stream.of(filterEntry("tags", "Tags", "LIST", null, null, null, listOfStrings(data.get("tags"))));

        Stream<Map<String, Object>> searchIndexEntries = searchIndexEntries(data, "filterEntries");

        return Stream.of(base, category, brand, tags, searchIndexEntries)
                .flatMap(stream -> stream)
                .toList();
    }

    private List<Map<String, Object>> sortValues(Map<String, Object> data) {
        Stream<Map<String, Object>> base = Stream.of(
                sortEntry("updatedAt", null, Objects.toString(data.get("updatedAt"), "")),
                sortEntry("title", null, firstNonBlank(data.get("title"), data.get("name"), data.get("slug"), ""))
        );

        Stream<Map<String, Object>> price = Stream.ofNullable(numberOrNull(data.get("defaultPrice")))
                .map(value -> sortEntry("defaultPrice", value, null));

        Stream<Map<String, Object>> searchIndexEntries = searchIndexSortEntries(data);

        return Stream.of(base, price, searchIndexEntries)
                .flatMap(stream -> stream)
                .toList();
    }

    private Stream<Map<String, Object>> searchIndexEntries(Map<String, Object> data, String fieldKey) {
        Object searchIndex = data.get("searchIndex");
        if (!(searchIndex instanceof Map<?, ?> map)) {
            return Stream.empty();
        }
        Object entries = map.get(fieldKey);
        if (!(entries instanceof List<?> list)) {
            return Stream.empty();
        }
        return list.stream()
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .map(entry -> filterEntry(
                        Objects.toString(entry.get("key"), ""),
                        Objects.toString(entry.get("label"), ""),
                        Objects.toString(entry.get("valueType"), "TEXT"),
                        stringOrNull(entry.get("stringValue")),
                        numberOrNull(entry.get("numberValue")),
                        entry.get("booleanValue") instanceof Boolean bool ? bool : null,
                        listOfStrings(entry.get("listValues"))
                ))
                .filter(entry -> !Objects.toString(entry.get("key"), "").isBlank());
    }

    private Stream<Map<String, Object>> searchIndexSortEntries(Map<String, Object> data) {
        Object searchIndex = data.get("searchIndex");
        if (!(searchIndex instanceof Map<?, ?> map)) {
            return Stream.empty();
        }
        Object entries = map.get("sortEntries");
        if (!(entries instanceof List<?> list)) {
            return Stream.empty();
        }
        return list.stream()
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .map(entry -> sortEntry(
                        Objects.toString(entry.get("key"), ""),
                        numberOrNull(entry.get("numberValue")),
                        stringOrNull(entry.get("stringValue"))
                ))
                .filter(entry -> !Objects.toString(entry.get("key"), "").isBlank());
    }

    private Map<String, Object> filterEntry(String key, String label, String valueType, String stringValue, Number numberValue, Boolean booleanValue, List<String> listValues) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("key", key);
        entry.put("label", label);
        entry.put("valueType", valueType);
        entry.put("stringValue", stringValue);
        entry.put("numberValue", numberValue);
        entry.put("booleanValue", booleanValue);
        entry.put("listValues", listValues == null ? List.of() : listValues);
        return entry;
    }

    private Map<String, Object> sortEntry(String key, Number numberValue, String stringValue) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("key", key);
        entry.put("numberValue", numberValue);
        entry.put("stringValue", stringValue);
        return entry;
    }

    @SuppressWarnings("unchecked")
    private Object nestedValue(Map<String, Object> data, String objectKey, String fieldKey) {
        Object nested = data.get(objectKey);
        if (nested instanceof Map<?, ?> map) {
            return ((Map<String, Object>) map).get(fieldKey);
        }
        return null;
    }

    private String defaultSchemaType(String sourceEntityKey) {
        return switch (sourceEntityKey) {
            case "catalog-product" -> "Product";
            case "blog-page" -> "BlogPosting";
            case "landing-page" -> "WebPage";
            default -> "Thing";
        };
    }

    private String stringOrNull(Object value) {
        if (value == null) {
            return null;
        }
        String string = String.valueOf(value);
        return string.isBlank() ? null : string;
    }

    private Number numberOrNull(Object value) {
        if (value instanceof Number number) {
            return number;
        }
        if (value instanceof String string && !string.isBlank()) {
            try {
                return Double.parseDouble(string);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private Number numericValue(Object value) {
        Number number = numberOrNull(value);
        return number == null ? 0 : number;
    }

    private List<String> listOfStrings(Object value) {
        if (value instanceof List<?> list) {
            return list.stream()
                    .map(String::valueOf)
                    .filter(item -> !item.isBlank())
                    .toList();
        }
        return List.of();
    }

    private String firstNonBlank(Object... values) {
        for (Object value : values) {
            if (value != null) {
                String string = String.valueOf(value);
                if (!string.isBlank()) {
                    return string;
                }
            }
        }
        return "";
    }
}

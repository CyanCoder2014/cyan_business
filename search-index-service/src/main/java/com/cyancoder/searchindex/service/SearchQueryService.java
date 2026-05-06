package com.cyancoder.searchindex.service;

import com.cyancoder.dynamiccore.runtime.DynamicRuntimeService;
import com.cyancoder.dynamiccore.runtime.DynamicScope;
import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordDocument;
import com.cyancoder.searchindex.model.SearchQueryResponse;
import com.cyancoder.searchindex.model.SearchSuggestionResponse;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SearchQueryService {
    private final DynamicRuntimeService dynamicRuntimeService;

    public SearchQueryService(DynamicRuntimeService dynamicRuntimeService) {
        this.dynamicRuntimeService = dynamicRuntimeService;
    }

    public SearchQueryResponse search(String query,
                                      List<String> entityTypes,
                                      String filterKey,
                                      String filterValue,
                                      String sort,
                                      int page,
                                      int size,
                                      DynamicScope scope) {
        List<Map<String, Object>> matched = dynamicRuntimeService.listRecords("search-document", scope).stream()
                .map(this::toSearchHit)
                .filter(hit -> matchesQuery(hit, query))
                .filter(hit -> matchesEntityTypes(hit, entityTypes))
                .filter(hit -> matchesFilter(hit, filterKey, filterValue))
                .sorted(resolveComparator(sort))
                .toList();

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 100));
        int from = Math.min(safePage * safeSize, matched.size());
        int to = Math.min(from + safeSize, matched.size());

        Map<String, Long> facetCounts = matched.stream()
                .flatMap(hit -> filterEntries(hit).stream())
                .map(entry -> facetKey(entry) + "=" + facetValue(entry))
                .filter(value -> !value.endsWith("="))
                .collect(Collectors.groupingBy(value -> value, LinkedHashMap::new, Collectors.counting()));

        return new SearchQueryResponse(query == null ? "" : query, safePage, safeSize, matched.size(), matched.subList(from, to), facetCounts);
    }

    public SearchSuggestionResponse suggest(String query, int limit, DynamicScope scope) {
        String normalized = normalize(query);
        List<String> suggestions = dynamicRuntimeService.listRecords("search-document", scope).stream()
                .map(this::toSearchHit)
                .map(hit -> Objects.toString(hit.get("title"), ""))
                .filter(title -> !title.isBlank())
                .filter(title -> normalize(title).contains(normalized))
                .distinct()
                .limit(Math.max(1, Math.min(limit, 20)))
                .toList();
        return new SearchSuggestionResponse(query == null ? "" : query, suggestions);
    }

    private Map<String, Object> toSearchHit(DynamicEntityRecordDocument record) {
        Map<String, Object> data = record.getData() == null ? Map.of() : record.getData();
        Map<String, Object> hit = new LinkedHashMap<>(data);
        hit.put("recordKey", record.getRecordKey());
        hit.put("entityKey", record.getEntityKey());
        hit.put("status", record.getStatus());
        hit.put("tenantKey", record.getTenantKey());
        hit.put("siteKey", record.getSiteKey());
        hit.put("updatedAt", Objects.toString(record.getUpdatedAt(), ""));
        hit.put("createdAt", Objects.toString(record.getCreatedAt(), ""));
        return hit;
    }

    private boolean matchesQuery(Map<String, Object> hit, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String haystack = Stream.of(
                        hit.get("title"),
                        hit.get("summary"),
                        hit.get("content"),
                        hit.get("entityType"),
                        keywords(hit).toString()
                )
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.joining(" "))
                .toLowerCase(Locale.ROOT);
        return haystack.contains(normalize(query));
    }

    private boolean matchesEntityTypes(Map<String, Object> hit, List<String> entityTypes) {
        if (entityTypes == null || entityTypes.isEmpty()) {
            return true;
        }
        String entityType = Objects.toString(hit.get("entityType"), "");
        return entityTypes.stream().anyMatch(type -> type.equalsIgnoreCase(entityType));
    }

    private boolean matchesFilter(Map<String, Object> hit, String filterKey, String filterValue) {
        if (filterKey == null || filterKey.isBlank() || filterValue == null || filterValue.isBlank()) {
            return true;
        }
        return filterEntries(hit).stream().anyMatch(entry ->
                filterKey.equalsIgnoreCase(Objects.toString(entry.get("key"), ""))
                        && facetValue(entry).equalsIgnoreCase(filterValue)
        );
    }

    private Comparator<Map<String, Object>> resolveComparator(String sort) {
        if ("price_desc".equalsIgnoreCase(sort)) {
            return Comparator.<Map<String, Object>, Double>comparing(hit -> numericSortValue(hit, "price"), Comparator.nullsLast(Double::compareTo)).reversed();
        }
        if ("price_asc".equalsIgnoreCase(sort)) {
            return Comparator.comparing(hit -> numericSortValue(hit, "price"), Comparator.nullsLast(Double::compareTo));
        }
        if ("title_desc".equalsIgnoreCase(sort)) {
            return Comparator.<Map<String, Object>, String>comparing(hit -> stringSortValue(hit, "name"), Comparator.nullsLast(String::compareToIgnoreCase)).reversed();
        }
        return Comparator.<Map<String, Object>, String>comparing(hit -> stringSortValue(hit, "name"), Comparator.nullsLast(String::compareToIgnoreCase));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> filterEntries(Map<String, Object> hit) {
        Object value = hit.get("filters");
        return value instanceof List<?> list ? (List<Map<String, Object>>) list : List.of();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> sortEntries(Map<String, Object> hit) {
        Object value = hit.get("sortValues");
        return value instanceof List<?> list ? (List<Map<String, Object>>) list : List.of();
    }

    @SuppressWarnings("unchecked")
    private List<String> keywords(Map<String, Object> hit) {
        Object value = hit.get("keywords");
        return value instanceof List<?> list ? (List<String>) list : List.of();
    }

    private Double numericSortValue(Map<String, Object> hit, String key) {
        return sortEntries(hit).stream()
                .filter(entry -> key.equalsIgnoreCase(Objects.toString(entry.get("key"), "")))
                .map(entry -> entry.get("numberValue"))
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .map(Number::doubleValue)
                .findFirst()
                .orElse(null);
    }

    private String stringSortValue(Map<String, Object> hit, String key) {
        return sortEntries(hit).stream()
                .filter(entry -> key.equalsIgnoreCase(Objects.toString(entry.get("key"), "")))
                .map(entry -> Objects.toString(entry.get("stringValue"), ""))
                .filter(value -> !value.isBlank())
                .findFirst()
                .orElse(Objects.toString(hit.get("title"), ""));
    }

    private String facetKey(Map<String, Object> entry) {
        return Objects.toString(entry.get("key"), "");
    }

    private String facetValue(Map<String, Object> entry) {
        Object valueType = entry.get("valueType");
        String type = Objects.toString(valueType, "TEXT");
        if ("LIST".equalsIgnoreCase(type)) {
            Object list = entry.get("listValues");
            return list == null ? "" : String.valueOf(list);
        }
        if ("NUMBER".equalsIgnoreCase(type)) {
            return Objects.toString(entry.get("numberValue"), "");
        }
        if ("BOOLEAN".equalsIgnoreCase(type)) {
            return Objects.toString(entry.get("booleanValue"), "");
        }
        return Objects.toString(entry.get("stringValue"), "");
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }
}

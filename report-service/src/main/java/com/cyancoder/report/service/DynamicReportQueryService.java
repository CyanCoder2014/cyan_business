package com.cyancoder.report.service;

import com.cyancoder.dynamiccore.runtime.DynamicRuntimeService;
import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordDocument;
import com.cyancoder.report.model.ReportFilter;
import com.cyancoder.report.model.ReportRunRequest;
import com.cyancoder.report.model.ReportRunResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DynamicReportQueryService {

    private final DynamicRuntimeService dynamicRuntimeService;
    private final DiscoveryClient discoveryClient;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public DynamicReportQueryService(
            DynamicRuntimeService dynamicRuntimeService,
            DiscoveryClient discoveryClient,
            ObjectMapper objectMapper
    ) {
        this.dynamicRuntimeService = dynamicRuntimeService;
        this.discoveryClient = discoveryClient;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    public ReportRunResponse run(String reportEntityKey, String recordKey, ReportRunRequest request) {
        DynamicEntityRecordDocument reportRecord = dynamicRuntimeService.getRecord(reportEntityKey, recordKey);
        Map<String, Object> config = reportRecord.getData() == null ? Map.of() : reportRecord.getData();
        String sourceType = Objects.toString(config.get("sourceType"), "DYNAMIC");
        if (!"DYNAMIC".equalsIgnoreCase(sourceType)) {
            throw new IllegalArgumentException("report sourceType must be DYNAMIC");
        }

        String targetServiceKey = require(config, "serviceKey");
        String targetEntityKey = require(config, "entityKey");
        String resolvedReportKey = Objects.toString(config.getOrDefault("reportKey", recordKey), recordKey);

        List<Map<String, Object>> rows = fetchRows(targetServiceKey, targetEntityKey);
        List<ReportFilter> filters = request != null && request.filters() != null
                ? request.filters()
                : convertFilters(config.get("filters"));
        List<Map<String, Object>> filtered = rows.stream()
                .filter(row -> filters.stream().allMatch(filter -> matches(row, filter)))
                .toList();

        String sumField = request != null && request.sumField() != null
                ? request.sumField()
                : Objects.toString(config.getOrDefault("defaultSumField", ""), "");
        BigDecimal sum = filtered.stream()
                .map(row -> toDecimal(row.get(sumField)))
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String groupField = request != null && request.groupByField() != null
                ? request.groupByField()
                : Objects.toString(config.getOrDefault("groupByField", ""), "");
        Map<String, Long> groups = groupField.isBlank()
                ? Map.of()
                : filtered.stream().collect(Collectors.groupingBy(
                row -> Objects.toString(row.get(groupField), "null"),
                LinkedHashMap::new,
                Collectors.counting()
        ));

        return new ReportRunResponse(resolvedReportKey, filtered.size(), sum, groups, filtered);
    }

    private List<Map<String, Object>> fetchRows(String serviceKey, String entityKey) {
        ServiceInstance instance = discoveryClient.getInstances(serviceKey).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("service not found: " + serviceKey));

        HttpHeaders headers = new HttpHeaders();
        String prefix = serviceKey.split("-")[0];
        headers.setBasicAuth(prefix + "_internal", prefix + "_secret", StandardCharsets.UTF_8);

        String url = resolveBaseUri(instance) + "/internal/entities/records/" + entityKey;
        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), List.class);
        List<?> body = response.getBody() == null ? List.of() : response.getBody();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Object item : body) {
            Map<String, Object> raw = objectMapper.convertValue(item, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("serviceKey", raw.get("serviceKey"));
            row.put("entityKey", raw.get("entityKey"));
            row.put("recordKey", raw.get("recordKey"));
            row.put("status", raw.get("status"));
            row.put("createdAt", raw.get("createdAt"));
            row.put("updatedAt", raw.get("updatedAt"));
            Map<String, Object> data = objectMapper.convertValue(raw.get("data"), new TypeReference<Map<String, Object>>() {});
            if (data != null) {
                row.putAll(data);
            }
            rows.add(row);
        }
        return rows;
    }

    private URI resolveBaseUri(ServiceInstance instance) {
        return instance.getUri();
    }

    private List<ReportFilter> convertFilters(Object raw) {
        if (raw == null) {
            return List.of();
        }
        return objectMapper.convertValue(raw, new TypeReference<List<ReportFilter>>() {});
    }

    private String require(Map<String, Object> config, String key) {
        String value = Objects.toString(config.get(key), "").trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("missing report field: " + key);
        }
        return value;
    }

    private boolean matches(Map<String, Object> row, ReportFilter filter) {
        Object raw = row.get(filter.field());
        String operator = filter.operator() == null ? "EQ" : filter.operator();
        String value = filter.value() == null ? "" : filter.value();
        if ("EQ".equalsIgnoreCase(operator)) {
            return Objects.toString(raw, "").equals(value);
        }
        if ("CONTAINS".equalsIgnoreCase(operator)) {
            return Objects.toString(raw, "").toLowerCase().contains(value.toLowerCase());
        }
        if ("GT".equalsIgnoreCase(operator)) {
            BigDecimal left = toDecimal(raw);
            BigDecimal right = toDecimal(value);
            return left != null && right != null && left.compareTo(right) > 0;
        }
        if ("GTE".equalsIgnoreCase(operator)) {
            BigDecimal left = toDecimal(raw);
            BigDecimal right = toDecimal(value);
            return left != null && right != null && left.compareTo(right) >= 0;
        }
        if ("LT".equalsIgnoreCase(operator)) {
            BigDecimal left = toDecimal(raw);
            BigDecimal right = toDecimal(value);
            return left != null && right != null && left.compareTo(right) < 0;
        }
        if ("LTE".equalsIgnoreCase(operator)) {
            BigDecimal left = toDecimal(raw);
            BigDecimal right = toDecimal(value);
            return left != null && right != null && left.compareTo(right) <= 0;
        }
        return true;
    }

    private BigDecimal toDecimal(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (Exception ex) {
            return null;
        }
    }
}

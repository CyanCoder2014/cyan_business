package com.cyancoder.report.service;

import com.cyancoder.report.client.*;
import com.cyancoder.report.entity.ReportDefinition;
import com.cyancoder.report.model.ReportFilter;
import com.cyancoder.report.model.ReportRunRequest;
import com.cyancoder.report.model.ReportRunResponse;
import com.cyancoder.report.repository.ReportDefinitionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportQueryService {

    private final ReportDefinitionRepository reportDefinitionRepository;
    private final ContentClient contentClient;
    private final CatalogClient catalogClient;
    private final CrmClient crmClient;
    private final CommerceClient commerceClient;
    private final FinanceClient financeClient;
    private final InventoryClient inventoryClient;

    public ReportQueryService(
            ReportDefinitionRepository reportDefinitionRepository,
            ContentClient contentClient,
            CatalogClient catalogClient,
            CrmClient crmClient,
            CommerceClient commerceClient,
            FinanceClient financeClient,
            InventoryClient inventoryClient
    ) {
        this.reportDefinitionRepository = reportDefinitionRepository;
        this.contentClient = contentClient;
        this.catalogClient = catalogClient;
        this.crmClient = crmClient;
        this.commerceClient = commerceClient;
        this.financeClient = financeClient;
        this.inventoryClient = inventoryClient;
    }

    public ReportRunResponse run(String reportKey, ReportRunRequest request) {
        ReportDefinition definition = reportDefinitionRepository.findByReportKey(reportKey).orElseThrow();
        List<Map<String, Object>> rows = switch (definition.getSourceType()) {
            case "CONTENT" -> contentClient.export();
            case "CATALOG" -> catalogClient.export();
            case "CRM" -> crmClient.export();
            case "COMMERCE" -> commerceClient.export();
            case "FINANCE" -> financeClient.export();
            case "INVENTORY" -> inventoryClient.export();
            default -> List.of();
        };

        List<ReportFilter> filters = request.filters() == null ? List.of() : request.filters();
        List<Map<String, Object>> filtered = rows.stream()
                .filter(row -> filters.stream().allMatch(filter -> matches(row, filter)))
                .collect(Collectors.toList());

        String sumField = request.sumField() != null ? request.sumField() : definition.getDefaultSumField();
        BigDecimal sum = filtered.stream()
                .map(row -> toDecimal(row.get(sumField)))
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String groupField = request.groupByField() != null ? request.groupByField() : definition.getGroupByField();
        Map<String, Long> groups = groupField == null || groupField.isBlank()
                ? Map.of()
                : filtered.stream().collect(Collectors.groupingBy(
                        row -> Objects.toString(row.get(groupField), "null"),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        return new ReportRunResponse(reportKey, filtered.size(), sum, groups, filtered);
    }

    private boolean matches(Map<String, Object> row, ReportFilter filter) {
        Object raw = row.get(filter.field());
        String operator = filter.operator() == null ? "EQ" : filter.operator();
        String value = filter.value();
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

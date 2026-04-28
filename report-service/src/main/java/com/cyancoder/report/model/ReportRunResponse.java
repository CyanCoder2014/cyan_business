package com.cyancoder.report.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ReportRunResponse(
        String reportKey,
        long count,
        BigDecimal sum,
        Map<String, Long> groups,
        List<Map<String, Object>> rows
) {
}

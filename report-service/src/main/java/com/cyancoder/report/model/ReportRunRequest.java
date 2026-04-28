package com.cyancoder.report.model;

import java.util.List;

public record ReportRunRequest(
        List<ReportFilter> filters,
        String sumField,
        String groupByField
) {
}

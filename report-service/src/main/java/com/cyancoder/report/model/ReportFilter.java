package com.cyancoder.report.model;

public record ReportFilter(
        String field,
        String operator,
        String value
) {
}

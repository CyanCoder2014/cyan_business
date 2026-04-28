package com.cyancoder.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@FeignClient(name = "finance-service")
public interface FinanceClient {
    @GetMapping("/api/finance-service/transactions/internal/export")
    List<Map<String, Object>> export();
}

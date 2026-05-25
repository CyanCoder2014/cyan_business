package com.cyancoder.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@FeignClient(name = "crm-service")
public interface CrmClient {
    @GetMapping("/api/crm-service/records/internal/export")
    List<Map<String, Object>> export();
}

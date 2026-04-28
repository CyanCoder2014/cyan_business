package com.cyancoder.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@FeignClient(name = "commerce-service")
public interface CommerceClient {
    @GetMapping("/api/commerce-service/documents/internal/export")
    List<Map<String, Object>> export();
}

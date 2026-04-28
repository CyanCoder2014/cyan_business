package com.cyancoder.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@FeignClient(name = "content-service")
public interface ContentClient {
    @GetMapping("/api/content-service/content/internal/export")
    List<Map<String, Object>> export();
}

package com.cyancoder.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@FeignClient(name = "catalog-service")
public interface CatalogClient {
    @GetMapping("/api/catalog-service/items/internal/export")
    List<Map<String, Object>> export();
}

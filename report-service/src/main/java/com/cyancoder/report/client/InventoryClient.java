package com.cyancoder.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@FeignClient(name = "inventory-service")
public interface InventoryClient {
    @GetMapping("/api/inventory-service/items/internal/export")
    List<Map<String, Object>> export();
}

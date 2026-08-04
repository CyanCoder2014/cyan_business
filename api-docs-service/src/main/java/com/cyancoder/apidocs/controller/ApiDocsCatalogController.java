package com.cyancoder.apidocs.controller;

import com.cyancoder.apidocs.model.ApiDocsServiceSummary;
import com.cyancoder.apidocs.service.ApiDocsCatalogService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({
        "/endpoint/api-docs",
        "/internal/api-docs"
})
public class ApiDocsCatalogController {
    private final ApiDocsCatalogService service;

    public ApiDocsCatalogController(ApiDocsCatalogService service) {
        this.service = service;
    }

    @GetMapping("/services")
    public List<ApiDocsServiceSummary> services() {
        return service.list();
    }

    @GetMapping("/services/{serviceKey}")
    public JsonNode service(
            @PathVariable("serviceKey") String serviceKey,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh
    ) {
        return service.get(serviceKey, refresh);
    }

    @GetMapping("/aggregate")
    public JsonNode aggregate(
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh
    ) {
        return service.aggregate(refresh);
    }
}

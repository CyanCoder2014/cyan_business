package com.cyancoder.dynamiccore.runtime;

import com.cyancoder.platformopenapi.PlatformApiSecurity;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/entities")
public class InternalDynamicEntityOpenApiController {
    private final DynamicEntityOpenApiService openApiService;

    public InternalDynamicEntityOpenApiController(DynamicEntityOpenApiService openApiService) {
        this.openApiService = openApiService;
    }

    @GetMapping("/definitions/{entityKey}/openapi")
    public OpenAPI entityOpenApi(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable("entityKey") String entityKey
    ) {
        return openApiService.generate(
                entityKey,
                DynamicScopeResolver.fromHeaders(tenantKey, siteKey),
                PlatformApiSecurity.BASIC);
    }
}

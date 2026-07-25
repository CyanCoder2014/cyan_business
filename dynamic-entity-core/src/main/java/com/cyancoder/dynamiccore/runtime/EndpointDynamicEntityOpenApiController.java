package com.cyancoder.dynamiccore.runtime;

import com.cyancoder.dynamiccore.config.DynamicRuntimeProperties;
import com.cyancoder.platformopenapi.PlatformApiSecurity;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({
        "/endpoint/entities",
        "/api/${dynamic.runtime.service-key}/endpoint/entities"
})
public class EndpointDynamicEntityOpenApiController {
    private final DynamicEntityOpenApiService openApiService;
    private final DynamicRuntimeProperties properties;

    public EndpointDynamicEntityOpenApiController(
            DynamicEntityOpenApiService openApiService,
            DynamicRuntimeProperties properties
    ) {
        this.openApiService = openApiService;
        this.properties = properties;
    }

    @GetMapping("/definitions/{entityKey}/openapi")
    @PreAuthorize("@platformAuthorizationService.canReadService(@endpointDynamicEntityOpenApiController.serviceKey())")
    public OpenAPI entityOpenApi(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable("entityKey") String entityKey
    ) {
        return openApiService.generate(
                entityKey,
                DynamicScopeResolver.fromHeaders(tenantKey, siteKey),
                PlatformApiSecurity.BEARER);
    }

    public String serviceKey() {
        return properties.getServiceKey();
    }
}

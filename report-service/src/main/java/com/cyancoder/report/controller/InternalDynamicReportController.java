package com.cyancoder.report.controller;

import com.cyancoder.report.model.ReportRunRequest;
import com.cyancoder.report.model.ReportRunResponse;
import com.cyancoder.report.service.DynamicReportQueryService;
import com.cyancoder.dynamiccore.runtime.DynamicScope;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/dynamic-reports")
public class InternalDynamicReportController {

    private final DynamicReportQueryService dynamicReportQueryService;

    public InternalDynamicReportController(DynamicReportQueryService dynamicReportQueryService) {
        this.dynamicReportQueryService = dynamicReportQueryService;
    }

    @PostMapping("/{entityKey}/{recordKey}/run")
    public ReportRunResponse run(
            @PathVariable String entityKey,
            @PathVariable String recordKey,
            @RequestBody(required = false) ReportRunRequest request,
            @RequestHeader(value="X-Tenant-Key",required=false) String tenantKey,
            @RequestHeader(value="X-Site-Key",required=false) String siteKey
    ) {
        return dynamicReportQueryService.run(entityKey, recordKey, request,new DynamicScope(tenantKey,siteKey));
    }
}

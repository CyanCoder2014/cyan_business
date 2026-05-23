package com.cyancoder.bpm.api;

import com.cyancoder.bpm.service.DynamicFlowMetadataService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/endpoint/bpm/metadata")
public class EndpointBpmMetadataController {
    private final DynamicFlowMetadataService metadataService;

    public EndpointBpmMetadataController(DynamicFlowMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @GetMapping("/actions")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public List<Map<String, Object>> actions() {
        return metadataService.stateActionStructures();
    }

    @GetMapping("/transition-conditions")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public Map<String, Object> transitionConditions() {
        return metadataService.transitionConditionStructure();
    }
}

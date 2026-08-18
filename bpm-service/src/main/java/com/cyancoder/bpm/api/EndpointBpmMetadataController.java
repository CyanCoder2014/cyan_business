package com.cyancoder.bpm.api;

import com.cyancoder.bpm.api.dto.StateActionStructureResponse;
import com.cyancoder.bpm.api.dto.TransitionConditionStructureResponse;
import com.cyancoder.bpm.service.DynamicFlowMetadataService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/endpoint/bpm/metadata")
public class EndpointBpmMetadataController {
    private final DynamicFlowMetadataService metadataService;

    public EndpointBpmMetadataController(DynamicFlowMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @GetMapping({"/actions", "/state-actions"})
    @PreAuthorize("@platformAuthorizationService.hasAnyPermission('bpm.read','bpm.manage','builder:*')")
    public List<StateActionStructureResponse> actions() {
        return metadataService.stateActionStructures();
    }

    @GetMapping("/transition-conditions")
    @PreAuthorize("@platformAuthorizationService.hasAnyPermission('bpm.read','bpm.manage','builder:*')")
    public TransitionConditionStructureResponse transitionConditions() {
        return metadataService.transitionConditionStructure();
    }
}

package com.cyancoder.bpm.api;

import com.cyancoder.bpm.api.dto.StateActionStructureResponse;
import com.cyancoder.bpm.api.dto.TransitionConditionStructureResponse;
import com.cyancoder.bpm.service.DynamicFlowMetadataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/dynamic-flows")
public class PublicBpmMetadataController {
    private final DynamicFlowMetadataService metadataService;

    public PublicBpmMetadataController(DynamicFlowMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @GetMapping("/state-action-structures")
    public List<StateActionStructureResponse> stateActionStructures() {
        return metadataService.stateActionStructures();
    }

    @GetMapping("/transition-condition-structures")
    public TransitionConditionStructureResponse transitionConditionStructures() {
        return metadataService.transitionConditionStructure();
    }
}

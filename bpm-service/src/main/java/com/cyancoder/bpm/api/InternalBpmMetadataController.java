package com.cyancoder.bpm.api;

import com.cyancoder.bpm.api.dto.StateActionStructureResponse;
import com.cyancoder.bpm.api.dto.TransitionConditionStructureResponse;
import com.cyancoder.bpm.service.DynamicFlowMetadataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/bpm/metadata")
public class InternalBpmMetadataController {
    private final DynamicFlowMetadataService metadataService;

    public InternalBpmMetadataController(DynamicFlowMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @GetMapping({"/actions", "/state-actions"})
    public List<StateActionStructureResponse> actions() {
        return metadataService.stateActionStructures();
    }

    @GetMapping("/transition-conditions")
    public TransitionConditionStructureResponse transitionConditions() {
        return metadataService.transitionConditionStructure();
    }
}

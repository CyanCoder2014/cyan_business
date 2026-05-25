package com.cyancoder.bpm.api;

import com.cyancoder.bpm.service.DynamicFlowMetadataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/bpm/metadata")
public class InternalBpmMetadataController {
    private final DynamicFlowMetadataService metadataService;

    public InternalBpmMetadataController(DynamicFlowMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @GetMapping("/actions")
    public List<Map<String, Object>> actions() {
        return metadataService.stateActionStructures();
    }

    @GetMapping("/transition-conditions")
    public Map<String, Object> transitionConditions() {
        return metadataService.transitionConditionStructure();
    }
}

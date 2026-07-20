package com.cyancoder.automationorchestrator.controller;

import com.cyancoder.automationorchestrator.service.AutomationMetadataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/public/automation-flows")
public class PublicAutomationMetadataController {
    private final AutomationMetadataService metadata;
    public PublicAutomationMetadataController(AutomationMetadataService metadata) { this.metadata = metadata; }
    @GetMapping("/node-structures") public List<Map<String, Object>> nodes() { return metadata.nodes(); }
    @GetMapping("/edge-structures") public Map<String, Object> edges() { return metadata.edges(); }
}

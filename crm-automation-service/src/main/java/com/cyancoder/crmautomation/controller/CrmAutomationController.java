package com.cyancoder.crmautomation.controller;

import com.cyancoder.crmautomation.entity.CrmAutomationAction;
import com.cyancoder.crmautomation.repository.CrmAutomationActionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/crm-automation-service/actions")
public class CrmAutomationController {

    private final CrmAutomationActionRepository repository;

    public CrmAutomationController(CrmAutomationActionRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<CrmAutomationAction> list() {
        return repository.findAll();
    }
}

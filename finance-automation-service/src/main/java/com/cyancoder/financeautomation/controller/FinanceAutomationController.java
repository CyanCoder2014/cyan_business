package com.cyancoder.financeautomation.controller;

import com.cyancoder.financeautomation.entity.FinanceAutomationAction;
import com.cyancoder.financeautomation.repository.FinanceAutomationActionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/finance-automation-service/actions")
public class FinanceAutomationController {

    private final FinanceAutomationActionRepository repository;

    public FinanceAutomationController(FinanceAutomationActionRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<FinanceAutomationAction> list() {
        return repository.findAll();
    }
}

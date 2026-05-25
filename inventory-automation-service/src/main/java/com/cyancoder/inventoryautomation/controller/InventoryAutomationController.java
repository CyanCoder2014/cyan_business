package com.cyancoder.inventoryautomation.controller;

import com.cyancoder.inventoryautomation.entity.InventoryAutomationAction;
import com.cyancoder.inventoryautomation.repository.InventoryAutomationActionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory-automation-service/actions")
public class InventoryAutomationController {

    private final InventoryAutomationActionRepository repository;

    public InventoryAutomationController(InventoryAutomationActionRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<InventoryAutomationAction> list() {
        return repository.findAll();
    }
}

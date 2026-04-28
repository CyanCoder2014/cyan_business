package com.cyancoder.inventory.controller;

import com.cyancoder.inventory.entity.InventoryItem;
import com.cyancoder.inventory.repository.InventoryItemRepository;
import com.cyancoder.inventory.service.EventPublisherSupport;
import com.cyancoder.inventory.service.ProcessorSupport;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory-service/items")
public class InventoryController {

    private final InventoryItemRepository repository;
    private final ProcessorSupport processorSupport;
    private final EventPublisherSupport eventPublisherSupport;

    public InventoryController(InventoryItemRepository repository, ProcessorSupport processorSupport, EventPublisherSupport eventPublisherSupport) {
        this.repository = repository;
        this.processorSupport = processorSupport;
        this.eventPublisherSupport = eventPublisherSupport;
    }

    @PostMapping
    public InventoryItem create(@RequestParam(required = false) String processorKey, @RequestBody InventoryItem item) {
        item = processorSupport.apply(processorKey, "INVENTORY", item, InventoryItem.class);
        InventoryItem saved = repository.save(item);
        eventPublisherSupport.publish("inventory-service", "INVENTORY", saved.getItemKey(), "CREATE", "inventory item created", saved);
        return saved;
    }

    @GetMapping
    public List<InventoryItem> list() {
        return repository.findAll();
    }

    @GetMapping("/{itemKey}")
    public InventoryItem get(@PathVariable String itemKey) {
        return repository.findByItemKey(itemKey).orElseThrow();
    }

    @PutMapping("/{itemKey}")
    public InventoryItem update(@PathVariable String itemKey, @RequestParam(required = false) String processorKey, @RequestBody InventoryItem input) {
        input = processorSupport.apply(processorKey, "INVENTORY", input, InventoryItem.class);
        InventoryItem existing = repository.findByItemKey(itemKey).orElseThrow();
        existing.setCatalogItemKey(input.getCatalogItemKey());
        existing.setWarehouseKey(input.getWarehouseKey());
        existing.setOnHandQuantity(input.getOnHandQuantity());
        existing.setReservedQuantity(input.getReservedQuantity());
        existing.setReorderPoint(input.getReorderPoint());
        existing.setUnit(input.getUnit());
        InventoryItem saved = repository.save(existing);
        eventPublisherSupport.publish("inventory-service", "INVENTORY", saved.getItemKey(), "UPDATE", "inventory item updated", saved);
        return saved;
    }

    @DeleteMapping("/{itemKey}")
    public void delete(@PathVariable String itemKey) {
        repository.findByItemKey(itemKey).ifPresent(existing -> {
            repository.delete(existing);
            eventPublisherSupport.publish("inventory-service", "INVENTORY", existing.getItemKey(), "DELETE", "inventory item deleted", existing);
        });
    }

    @GetMapping("/internal/export")
    public List<Map<String, Object>> export() {
        return repository.findAll().stream().map(item -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("itemKey", item.getItemKey());
            row.put("catalogItemKey", item.getCatalogItemKey());
            row.put("warehouseKey", item.getWarehouseKey());
            row.put("onHandQuantity", item.getOnHandQuantity());
            row.put("reservedQuantity", item.getReservedQuantity());
            row.put("reorderPoint", item.getReorderPoint());
            row.put("unit", item.getUnit());
            return row;
        }).toList();
    }
}

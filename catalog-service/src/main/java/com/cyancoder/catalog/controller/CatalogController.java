package com.cyancoder.catalog.controller;

import com.cyancoder.catalog.entity.CatalogItem;
import com.cyancoder.catalog.repository.CatalogItemRepository;
import com.cyancoder.catalog.service.EventPublisherSupport;
import com.cyancoder.catalog.service.ProcessorSupport;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catalog-service/items")
public class CatalogController {

    private final CatalogItemRepository repository;
    private final ProcessorSupport processorSupport;
    private final EventPublisherSupport eventPublisherSupport;

    public CatalogController(CatalogItemRepository repository, ProcessorSupport processorSupport, EventPublisherSupport eventPublisherSupport) {
        this.repository = repository;
        this.processorSupport = processorSupport;
        this.eventPublisherSupport = eventPublisherSupport;
    }

    @PostMapping
    @Transactional
    public CatalogItem create(@RequestParam(required = false) String processorKey, @RequestBody CatalogItem item) {
        item = processorSupport.apply(processorKey, "CATALOG", item, CatalogItem.class);
        CatalogItem saved = repository.save(item);
        eventPublisherSupport.publish("catalog-service", "CATALOG", saved.getItemKey(), "CREATE", "catalog item created", saved);
        return saved;
    }

    @GetMapping
    public List<CatalogItem> list(@RequestParam(required = false) String itemType) {
        return itemType == null || itemType.isBlank() ? repository.findAll() : repository.findByItemType(itemType);
    }

    @GetMapping("/{itemKey}")
    public CatalogItem get(@PathVariable String itemKey) {
        return repository.findByItemKey(itemKey).orElseThrow();
    }

    @PutMapping("/{itemKey}")
    @Transactional
    public CatalogItem update(@PathVariable String itemKey, @RequestParam(required = false) String processorKey, @RequestBody CatalogItem input) {
        input = processorSupport.apply(processorKey, "CATALOG", input, CatalogItem.class);
        CatalogItem existing = repository.findByItemKey(itemKey).orElseThrow();
        existing.setItemType(input.getItemType());
        existing.setName(input.getName());
        existing.setSku(input.getSku());
        existing.setCategoryKey(input.getCategoryKey());
        existing.setUnit(input.getUnit());
        existing.setDefaultPrice(input.getDefaultPrice());
        existing.setCurrency(input.getCurrency());
        existing.setActive(input.getActive());
        existing.setAttributesJson(input.getAttributesJson());
        CatalogItem saved = repository.save(existing);
        eventPublisherSupport.publish("catalog-service", "CATALOG", saved.getItemKey(), "UPDATE", "catalog item updated", saved);
        return saved;
    }

    @DeleteMapping("/{itemKey}")
    @Transactional
    public void delete(@PathVariable String itemKey) {
        repository.findByItemKey(itemKey).ifPresent(existing -> {
            repository.delete(existing);
            eventPublisherSupport.publish("catalog-service", "CATALOG", existing.getItemKey(), "DELETE", "catalog item deleted", existing);
        });
    }

    @GetMapping("/internal/export")
    public List<Map<String, Object>> export() {
        return repository.findAll().stream().map(item -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("itemKey", item.getItemKey());
            row.put("itemType", item.getItemType());
            row.put("name", item.getName());
            row.put("sku", item.getSku());
            row.put("categoryKey", item.getCategoryKey());
            row.put("unit", item.getUnit());
            row.put("defaultPrice", item.getDefaultPrice());
            row.put("currency", item.getCurrency());
            row.put("active", item.getActive());
            return row;
        }).toList();
    }
}

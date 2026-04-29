package com.cyancoder.commerce.controller;

import com.cyancoder.commerce.entity.CommerceDocument;
import com.cyancoder.commerce.repository.CommerceDocumentRepository;
import com.cyancoder.commerce.service.EventPublisherSupport;
import com.cyancoder.commerce.service.ProcessorSupport;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/commerce-service/documents")
public class CommerceController {

    private final CommerceDocumentRepository repository;
    private final ProcessorSupport processorSupport;
    private final EventPublisherSupport eventPublisherSupport;

    public CommerceController(CommerceDocumentRepository repository, ProcessorSupport processorSupport, EventPublisherSupport eventPublisherSupport) {
        this.repository = repository;
        this.processorSupport = processorSupport;
        this.eventPublisherSupport = eventPublisherSupport;
    }

    @PostMapping
    @Transactional
    public CommerceDocument create(@RequestParam(required = false) String processorKey, @RequestBody CommerceDocument document) {
        document = processorSupport.apply(processorKey, "COMMERCE", document, CommerceDocument.class);
        CommerceDocument saved = repository.save(document);
        eventPublisherSupport.publish("commerce-service", "COMMERCE", saved.getDocumentKey(), "CREATE", "commerce document created", saved);
        return saved;
    }

    @GetMapping
    public List<CommerceDocument> list(@RequestParam(required = false) String documentType) {
        return documentType == null || documentType.isBlank() ? repository.findAll() : repository.findByDocumentType(documentType);
    }

    @GetMapping("/{documentKey}")
    public CommerceDocument get(@PathVariable String documentKey) {
        return repository.findByDocumentKey(documentKey).orElseThrow();
    }

    @PutMapping("/{documentKey}")
    @Transactional
    public CommerceDocument update(@PathVariable String documentKey, @RequestParam(required = false) String processorKey, @RequestBody CommerceDocument input) {
        input = processorSupport.apply(processorKey, "COMMERCE", input, CommerceDocument.class);
        CommerceDocument existing = repository.findByDocumentKey(documentKey).orElseThrow();
        existing.setDocumentType(input.getDocumentType());
        existing.setCustomerKey(input.getCustomerKey());
        existing.setCurrency(input.getCurrency());
        existing.setDocumentStatus(input.getDocumentStatus());
        existing.setItemsJson(input.getItemsJson());
        existing.setSubtotal(input.getSubtotal());
        existing.setDiscountTotal(input.getDiscountTotal());
        existing.setTaxTotal(input.getTaxTotal());
        existing.setGrandTotal(input.getGrandTotal());
        CommerceDocument saved = repository.save(existing);
        eventPublisherSupport.publish("commerce-service", "COMMERCE", saved.getDocumentKey(), "UPDATE", "commerce document updated", saved);
        return saved;
    }

    @DeleteMapping("/{documentKey}")
    @Transactional
    public void delete(@PathVariable String documentKey) {
        repository.findByDocumentKey(documentKey).ifPresent(existing -> {
            repository.delete(existing);
            eventPublisherSupport.publish("commerce-service", "COMMERCE", existing.getDocumentKey(), "DELETE", "commerce document deleted", existing);
        });
    }

    @GetMapping("/internal/export")
    public List<Map<String, Object>> export() {
        return repository.findAll().stream().map(item -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("documentKey", item.getDocumentKey());
            row.put("documentType", item.getDocumentType());
            row.put("customerKey", item.getCustomerKey());
            row.put("currency", item.getCurrency());
            row.put("documentStatus", item.getDocumentStatus());
            row.put("subtotal", item.getSubtotal());
            row.put("discountTotal", item.getDiscountTotal());
            row.put("taxTotal", item.getTaxTotal());
            row.put("grandTotal", item.getGrandTotal());
            row.put("createdAt", item.getCreatedAt());
            return row;
        }).toList();
    }
}

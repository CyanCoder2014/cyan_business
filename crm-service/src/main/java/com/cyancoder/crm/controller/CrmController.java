package com.cyancoder.crm.controller;

import com.cyancoder.crm.entity.CrmRecord;
import com.cyancoder.crm.repository.CrmRecordRepository;
import com.cyancoder.crm.service.EventPublisherSupport;
import com.cyancoder.crm.service.ProcessorSupport;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/crm-service/records")
public class CrmController {

    private final CrmRecordRepository repository;
    private final ProcessorSupport processorSupport;
    private final EventPublisherSupport eventPublisherSupport;

    public CrmController(CrmRecordRepository repository, ProcessorSupport processorSupport, EventPublisherSupport eventPublisherSupport) {
        this.repository = repository;
        this.processorSupport = processorSupport;
        this.eventPublisherSupport = eventPublisherSupport;
    }

    @PostMapping
    public CrmRecord create(@RequestParam(required = false) String processorKey, @RequestBody CrmRecord record) {
        record = processorSupport.apply(processorKey, "CRM", record, CrmRecord.class);
        CrmRecord saved = repository.save(record);
        eventPublisherSupport.publish("crm-service", "CRM", saved.getRecordKey(), "CREATE", "crm record created", saved);
        return saved;
    }

    @GetMapping
    public List<CrmRecord> list(@RequestParam(required = false) String recordType) {
        return recordType == null || recordType.isBlank() ? repository.findAll() : repository.findByRecordType(recordType);
    }

    @GetMapping("/{recordKey}")
    public CrmRecord get(@PathVariable String recordKey) {
        return repository.findByRecordKey(recordKey).orElseThrow();
    }

    @PutMapping("/{recordKey}")
    public CrmRecord update(@PathVariable String recordKey, @RequestParam(required = false) String processorKey, @RequestBody CrmRecord input) {
        input = processorSupport.apply(processorKey, "CRM", input, CrmRecord.class);
        CrmRecord existing = repository.findByRecordKey(recordKey).orElseThrow();
        existing.setRecordType(input.getRecordType());
        existing.setFullName(input.getFullName());
        existing.setCompanyName(input.getCompanyName());
        existing.setEmail(input.getEmail());
        existing.setMobile(input.getMobile());
        existing.setStatus(input.getStatus());
        existing.setSource(input.getSource());
        existing.setOwnerUserId(input.getOwnerUserId());
        existing.setNotes(input.getNotes());
        CrmRecord saved = repository.save(existing);
        eventPublisherSupport.publish("crm-service", "CRM", saved.getRecordKey(), "UPDATE", "crm record updated", saved);
        return saved;
    }

    @DeleteMapping("/{recordKey}")
    public void delete(@PathVariable String recordKey) {
        repository.findByRecordKey(recordKey).ifPresent(existing -> {
            repository.delete(existing);
            eventPublisherSupport.publish("crm-service", "CRM", existing.getRecordKey(), "DELETE", "crm record deleted", existing);
        });
    }

    @GetMapping("/internal/export")
    public List<Map<String, Object>> export() {
        return repository.findAll().stream().map(item -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("recordKey", item.getRecordKey());
            row.put("recordType", item.getRecordType());
            row.put("fullName", item.getFullName());
            row.put("companyName", item.getCompanyName());
            row.put("email", item.getEmail());
            row.put("mobile", item.getMobile());
            row.put("status", item.getStatus());
            row.put("source", item.getSource());
            row.put("ownerUserId", item.getOwnerUserId());
            row.put("createdAt", item.getCreatedAt());
            return row;
        }).toList();
    }
}

package com.cyancoder.finance.controller;

import com.cyancoder.finance.entity.FinanceTransaction;
import com.cyancoder.finance.repository.FinanceTransactionRepository;
import com.cyancoder.finance.service.EventPublisherSupport;
import com.cyancoder.finance.service.ProcessorSupport;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance-service/transactions")
public class FinanceController {

    private final FinanceTransactionRepository repository;
    private final ProcessorSupport processorSupport;
    private final EventPublisherSupport eventPublisherSupport;

    public FinanceController(FinanceTransactionRepository repository, ProcessorSupport processorSupport, EventPublisherSupport eventPublisherSupport) {
        this.repository = repository;
        this.processorSupport = processorSupport;
        this.eventPublisherSupport = eventPublisherSupport;
    }

    @PostMapping
    @Transactional
    public FinanceTransaction create(@RequestParam(required = false) String processorKey, @RequestBody FinanceTransaction transaction) {
        transaction = processorSupport.apply(processorKey, "FINANCE", transaction, FinanceTransaction.class);
        FinanceTransaction saved = repository.save(transaction);
        eventPublisherSupport.publish("finance-service", "FINANCE", saved.getTransactionKey(), "CREATE", "finance transaction created", saved);
        return saved;
    }

    @GetMapping
    public List<FinanceTransaction> list(@RequestParam(required = false) String transactionType) {
        return transactionType == null || transactionType.isBlank() ? repository.findAll() : repository.findByTransactionType(transactionType);
    }

    @GetMapping("/{transactionKey}")
    public FinanceTransaction get(@PathVariable String transactionKey) {
        return repository.findByTransactionKey(transactionKey).orElseThrow();
    }

    @PutMapping("/{transactionKey}")
    @Transactional
    public FinanceTransaction update(@PathVariable String transactionKey, @RequestParam(required = false) String processorKey, @RequestBody FinanceTransaction input) {
        input = processorSupport.apply(processorKey, "FINANCE", input, FinanceTransaction.class);
        FinanceTransaction existing = repository.findByTransactionKey(transactionKey).orElseThrow();
        existing.setTransactionType(input.getTransactionType());
        existing.setReferenceType(input.getReferenceType());
        existing.setReferenceKey(input.getReferenceKey());
        existing.setAccountKey(input.getAccountKey());
        existing.setCurrency(input.getCurrency());
        existing.setAmount(input.getAmount());
        existing.setStatus(input.getStatus());
        existing.setDescription(input.getDescription());
        FinanceTransaction saved = repository.save(existing);
        eventPublisherSupport.publish("finance-service", "FINANCE", saved.getTransactionKey(), "UPDATE", "finance transaction updated", saved);
        return saved;
    }

    @DeleteMapping("/{transactionKey}")
    @Transactional
    public void delete(@PathVariable String transactionKey) {
        repository.findByTransactionKey(transactionKey).ifPresent(existing -> {
            repository.delete(existing);
            eventPublisherSupport.publish("finance-service", "FINANCE", existing.getTransactionKey(), "DELETE", "finance transaction deleted", existing);
        });
    }

    @GetMapping("/internal/export")
    public List<Map<String, Object>> export() {
        return repository.findAll().stream().map(item -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("transactionKey", item.getTransactionKey());
            row.put("transactionType", item.getTransactionType());
            row.put("referenceType", item.getReferenceType());
            row.put("referenceKey", item.getReferenceKey());
            row.put("accountKey", item.getAccountKey());
            row.put("currency", item.getCurrency());
            row.put("amount", item.getAmount());
            row.put("status", item.getStatus());
            row.put("createdAt", item.getCreatedAt());
            return row;
        }).toList();
    }
}

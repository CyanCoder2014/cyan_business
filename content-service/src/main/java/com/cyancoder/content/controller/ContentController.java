package com.cyancoder.content.controller;

import com.cyancoder.content.entity.ContentEntry;
import com.cyancoder.content.repository.ContentEntryRepository;
import com.cyancoder.content.service.EventPublisherSupport;
import com.cyancoder.content.service.ProcessorSupport;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/content-service/content")
public class ContentController {

    private final ContentEntryRepository repository;
    private final ProcessorSupport processorSupport;
    private final EventPublisherSupport eventPublisherSupport;

    public ContentController(ContentEntryRepository repository, ProcessorSupport processorSupport, EventPublisherSupport eventPublisherSupport) {
        this.repository = repository;
        this.processorSupport = processorSupport;
        this.eventPublisherSupport = eventPublisherSupport;
    }

    @PostMapping
    @Transactional
    public ContentEntry create(@RequestParam(required = false) String processorKey, @RequestBody ContentEntry entry) {
        entry = processorSupport.apply(processorKey, "CONTENT", entry, ContentEntry.class);
        ContentEntry saved = repository.save(entry);
        eventPublisherSupport.publish("content-service", "CONTENT", saved.getKey(), "CREATE", "content created", saved);
        return saved;
    }

    @GetMapping
    public List<ContentEntry> list(@RequestParam(required = false) String contentType) {
        return contentType == null || contentType.isBlank() ? repository.findAll() : repository.findByContentType(contentType);
    }

    @GetMapping("/{key}")
    public ContentEntry getByKey(@PathVariable String key) {
        return repository.findByKey(key).orElseThrow();
    }

    @GetMapping("/slug/{slug}")
    public ContentEntry getBySlug(@PathVariable String slug) {
        return repository.findBySlug(slug).orElseThrow();
    }

    @PutMapping("/{key}")
    @Transactional
    public ContentEntry update(@PathVariable String key, @RequestParam(required = false) String processorKey, @RequestBody ContentEntry input) {
        input = processorSupport.apply(processorKey, "CONTENT", input, ContentEntry.class);
        ContentEntry existing = repository.findByKey(key).orElseThrow();
        existing.setContentType(input.getContentType());
        existing.setSlug(input.getSlug());
        existing.setTitle(input.getTitle());
        existing.setSummary(input.getSummary());
        existing.setBody(input.getBody());
        existing.setTemplateKey(input.getTemplateKey());
        existing.setPublicationStatus(input.getPublicationStatus());
        existing.setSeoTitle(input.getSeoTitle());
        existing.setSeoDescription(input.getSeoDescription());
        existing.setAuthor(input.getAuthor());
        existing.setTags(input.getTags());
        ContentEntry saved = repository.save(existing);
        eventPublisherSupport.publish("content-service", "CONTENT", saved.getKey(), "UPDATE", "content updated", saved);
        return saved;
    }

    @DeleteMapping("/{key}")
    @Transactional
    public void delete(@PathVariable String key) {
        repository.findByKey(key).ifPresent(existing -> {
            repository.delete(existing);
            eventPublisherSupport.publish("content-service", "CONTENT", existing.getKey(), "DELETE", "content deleted", existing);
        });
    }

    @GetMapping("/internal/export")
    public List<Map<String, Object>> export() {
        return repository.findAll().stream().map(item -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("key", item.getKey());
            row.put("contentType", item.getContentType());
            row.put("slug", item.getSlug());
            row.put("title", item.getTitle());
            row.put("summary", item.getSummary());
            row.put("publicationStatus", item.getPublicationStatus());
            row.put("author", item.getAuthor());
            row.put("createdAt", item.getCreatedAt());
            row.put("updatedAt", item.getUpdatedAt());
            return row;
        }).toList();
    }
}

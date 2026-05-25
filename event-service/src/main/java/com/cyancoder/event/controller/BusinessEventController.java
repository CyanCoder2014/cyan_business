package com.cyancoder.event.controller;

import com.cyancoder.event.entity.BusinessEvent;
import com.cyancoder.event.model.BusinessEventRequest;
import com.cyancoder.event.repository.BusinessEventRepository;
import com.cyancoder.event.service.BusinessEventService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/event-service/events")
public class BusinessEventController {

    private final BusinessEventRepository repository;
    private final BusinessEventService service;

    public BusinessEventController(BusinessEventRepository repository, BusinessEventService service) {
        this.repository = repository;
        this.service = service;
    }

    @PostMapping
    public BusinessEvent publish(@RequestBody BusinessEventRequest request) {
        return service.publish(request);
    }

    @GetMapping
    public List<BusinessEvent> list(
            @RequestParam(required = false) String sourceService,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityKey,
            @RequestParam(required = false) String actionType
    ) {
        return service.list(sourceService, entityType, entityKey, actionType);
    }

    @GetMapping("/{eventKey}")
    public BusinessEvent get(@PathVariable String eventKey) {
        return repository.findByEventKey(eventKey).orElseThrow();
    }
}

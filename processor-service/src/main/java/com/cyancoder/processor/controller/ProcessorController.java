package com.cyancoder.processor.controller;

import com.cyancoder.processor.entity.ProcessorDefinition;
import com.cyancoder.processor.model.ProcessorRunRequest;
import com.cyancoder.processor.model.ProcessorRunResponse;
import com.cyancoder.processor.repository.ProcessorDefinitionRepository;
import com.cyancoder.processor.service.ProcessorExecutionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/processor-service/processors")
public class ProcessorController {

    private final ProcessorDefinitionRepository repository;
    private final ProcessorExecutionService executionService;

    public ProcessorController(ProcessorDefinitionRepository repository, ProcessorExecutionService executionService) {
        this.repository = repository;
        this.executionService = executionService;
    }

    @PostMapping
    public ProcessorDefinition create(@RequestBody ProcessorDefinition definition) {
        return repository.save(definition);
    }

    @GetMapping
    public List<ProcessorDefinition> list() {
        return repository.findAll();
    }

    @GetMapping("/{processorKey}")
    public ProcessorDefinition get(@PathVariable String processorKey) {
        return repository.findByProcessorKey(processorKey).orElseThrow();
    }

    @PutMapping("/{processorKey}")
    public ProcessorDefinition update(@PathVariable String processorKey, @RequestBody ProcessorDefinition input) {
        ProcessorDefinition existing = repository.findByProcessorKey(processorKey).orElseThrow();
        existing.setTargetType(input.getTargetType());
        existing.setValidatorsJson(input.getValidatorsJson());
        existing.setOperatorsJson(input.getOperatorsJson());
        existing.setDescription(input.getDescription());
        existing.setActive(input.isActive());
        return repository.save(existing);
    }

    @DeleteMapping("/{processorKey}")
    public void delete(@PathVariable String processorKey) {
        repository.findByProcessorKey(processorKey).ifPresent(repository::delete);
    }

    @PostMapping("/{processorKey}/run")
    public ProcessorRunResponse run(@PathVariable String processorKey, @RequestBody ProcessorRunRequest request) {
        return executionService.run(processorKey, request);
    }
}

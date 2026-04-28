package com.cyancoder.report.controller;

import com.cyancoder.report.entity.ReportDefinition;
import com.cyancoder.report.model.ReportRunRequest;
import com.cyancoder.report.model.ReportRunResponse;
import com.cyancoder.report.repository.ReportDefinitionRepository;
import com.cyancoder.report.service.ReportQueryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/report-service/reports")
public class ReportController {

    private final ReportDefinitionRepository repository;
    private final ReportQueryService reportQueryService;

    public ReportController(ReportDefinitionRepository repository, ReportQueryService reportQueryService) {
        this.repository = repository;
        this.reportQueryService = reportQueryService;
    }

    @PostMapping
    public ReportDefinition create(@RequestBody ReportDefinition definition) {
        return repository.save(definition);
    }

    @GetMapping
    public List<ReportDefinition> list() {
        return repository.findAll();
    }

    @GetMapping("/{reportKey}")
    public ReportDefinition get(@PathVariable String reportKey) {
        return repository.findByReportKey(reportKey).orElseThrow();
    }

    @PutMapping("/{reportKey}")
    public ReportDefinition update(@PathVariable String reportKey, @RequestBody ReportDefinition input) {
        ReportDefinition existing = repository.findByReportKey(reportKey).orElseThrow();
        existing.setTitle(input.getTitle());
        existing.setSourceType(input.getSourceType());
        existing.setDefaultFilterField(input.getDefaultFilterField());
        existing.setDefaultSumField(input.getDefaultSumField());
        existing.setGroupByField(input.getGroupByField());
        return repository.save(existing);
    }

    @DeleteMapping("/{reportKey}")
    public void delete(@PathVariable String reportKey) {
        repository.findByReportKey(reportKey).ifPresent(repository::delete);
    }

    @PostMapping("/{reportKey}/run")
    public ReportRunResponse run(@PathVariable String reportKey, @RequestBody ReportRunRequest request) {
        return reportQueryService.run(reportKey, request);
    }
}

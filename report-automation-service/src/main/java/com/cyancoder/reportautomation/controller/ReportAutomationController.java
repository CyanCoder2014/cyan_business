package com.cyancoder.reportautomation.controller;

import com.cyancoder.reportautomation.entity.ReportAutomationRecord;
import com.cyancoder.reportautomation.repository.ReportAutomationRecordRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/report-automation-service/records")
public class ReportAutomationController {

    private final ReportAutomationRecordRepository repository;

    public ReportAutomationController(ReportAutomationRecordRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<ReportAutomationRecord> list() {
        return repository.findAll();
    }
}

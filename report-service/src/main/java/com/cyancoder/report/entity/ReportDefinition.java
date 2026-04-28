package com.cyancoder.report.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "report_definitions")
@Data
public class ReportDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reportKey;

    private String title;
    private String sourceType;
    private String defaultFilterField;
    private String defaultSumField;
    private String groupByField;
}

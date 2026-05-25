package com.cyancoder.bpm.config;

import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import com.cyancoder.dynamiccore.template.DynamicTemplateProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BpmDynamicTemplateConfig {

    @Bean
    public DynamicTemplateProvider bpmDynamicTemplateProvider() {
        return () -> List.of(
                new DynamicEntityTemplate("screening-intake-form", "BPM_FORM", "Screening Intake Form", "Applicant intake form for hybrid screening flows.", """
                        {
                          "entityKey":"screening-intake-form",
                          "entityType":"BPM_FORM",
                          "title":"Screening Intake Form",
                          "defaultValues":{"submissionType":"SCREENING_INTAKE"},
                          "fields":{
                            "submissionType":{"id":"submissionType","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["SCREENING_INTAKE"]}}]},
                            "fullName":{"id":"fullName","type":"string","validations":[{"validation":"REQUIRED","order":1},{"validation":"MIN_LENGTH","order":2,"validationParams":{"min":3}}]},
                            "nationalId":{"id":"nationalId","type":"string","validations":[{"validation":"REQUIRED","order":1},{"validation":"MIN_LENGTH","order":2,"validationParams":{"min":6}}]},
                            "requestedAmount":{"id":"requestedAmount","type":"number","validations":[{"validation":"REQUIRED","order":1}]},
                            "notes":{"id":"notes","type":"string"}
                          }
                        }
                        """),
                new DynamicEntityTemplate("screening-review-form", "BPM_FORM", "Screening Review Form", "Manual review form used after automation routes an application for analyst review.", """
                        {
                          "entityKey":"screening-review-form",
                          "entityType":"BPM_FORM",
                          "title":"Screening Review Form",
                          "defaultValues":{"submissionType":"SCREENING_REVIEW","reviewDecision":"APPROVE"},
                          "fields":{
                            "submissionType":{"id":"submissionType","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["SCREENING_REVIEW"]}}]},
                            "reviewDecision":{"id":"reviewDecision","type":"string","validations":[{"validation":"REQUIRED","order":1},{"validation":"ENUM","order":2,"validationParams":{"values":["APPROVE","REJECT"]}}]},
                            "reviewComment":{"id":"reviewComment","type":"string","validations":[{"validation":"REQUIRED","order":1},{"validation":"MIN_LENGTH","order":2,"validationParams":{"min":5}}]},
                            "reviewerReference":{"id":"reviewerReference","type":"string"}
                          }
                        }
                        """)
        );
    }
}

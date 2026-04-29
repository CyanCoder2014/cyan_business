package com.cyancoder.taxpaysys.config;

import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import com.cyancoder.dynamiccore.template.DynamicTemplateProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class TaxDynamicTemplateConfig {

    @Bean
    public DynamicTemplateProvider taxDynamicTemplateProvider() {
        return () -> List.of(
                new DynamicEntityTemplate("tax-submission", "TAX", "Tax Submission", "Structured tax submission request for factor/company sync.", """
                        {
                          "entityKey":"tax-submission",
                          "entityType":"TAX",
                          "title":"Tax Submission",
                          "defaultValues":{"sendType":"INVOICE","priority":"HIGH"},
                          "relationDefinitions":{"factor":{"service":"factor-service","entityKey":"factor-document"},"company":{"service":"client-service","entityKey":"company-profile"}},
                          "fields":{
                            "companyId":{"id":"companyId","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "factorId":{"id":"factorId","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "sendType":{"id":"sendType","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["INVOICE","VOID","INQUIRY"]}}]},
                            "priority":{"id":"priority","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["LOW","NORMAL","HIGH"]}}]},
                            "fiscalId":{"id":"fiscalId","type":"string"},
                            "memo":{"id":"memo","type":"string"}
                          }
                        }
                        """)
        );
    }
}

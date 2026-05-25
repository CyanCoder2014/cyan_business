package com.cyancoder.report.config;

import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import com.cyancoder.dynamiccore.template.DynamicTemplateProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ReportDynamicTemplateConfig {

    @Bean
    public DynamicTemplateProvider reportDynamicTemplateProvider() {
        return () -> List.of(
                new DynamicEntityTemplate("dynamic-report", "REPORT", "Dynamic Report", "Reusable report definition template for service/entity reporting.", """
                        {
                          "entityKey":"dynamic-report",
                          "entityType":"REPORT",
                          "title":"Dynamic Report",
                          "defaultValues":{"sourceType":"DYNAMIC","groupByField":"","defaultSumField":""},
                          "fields":{
                            "reportKey":{"id":"reportKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "title":{"id":"title","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "sourceType":{"id":"sourceType","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["DYNAMIC","CONTENT","CATALOG","CRM","COMMERCE","FINANCE","INVENTORY"]}}]},
                            "serviceKey":{"id":"serviceKey","type":"string"},
                            "entityKey":{"id":"entityKey","type":"string"},
                            "defaultFilterField":{"id":"defaultFilterField","type":"string"},
                            "defaultSumField":{"id":"defaultSumField","type":"string"},
                            "groupByField":{"id":"groupByField","type":"string"},
                            "filters":{"id":"filters","type":"list","itemValidations":{
                              "field":{"id":"field","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "operator":{"id":"operator","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["EQ","CONTAINS","GT","GTE","LT","LTE"]}}]},
                              "value":{"id":"value","type":"string"}
                            }}
                          }
                        }
                        """)
        );
    }
}

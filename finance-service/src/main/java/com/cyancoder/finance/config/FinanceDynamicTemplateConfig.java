package com.cyancoder.finance.config;

import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import com.cyancoder.dynamiccore.template.DynamicTemplateProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class FinanceDynamicTemplateConfig {

    @Bean
    public DynamicTemplateProvider financeDynamicTemplateProvider() {
        return () -> List.of(
                new DynamicEntityTemplate("finance-transaction", "PAYMENT", "Finance Transaction", "Generic money movement template.", """
                        {
                          "entityKey":"finance-transaction",
                          "entityType":"PAYMENT",
                          "title":"Finance Transaction",
                          "defaultValues":{"currency":"IRR","status":"PENDING"},
                          "relationDefinitions":{"reference":{"service":"commerce-service","entityKey":"sales-invoice"}},
                          "fields":{
                            "transactionType":{"id":"transactionType","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["PAYMENT","REFUND","RECEIPT","EXPENSE","SETTLEMENT"]}}]},
                            "referenceType":{"id":"referenceType","type":"string"},
                            "referenceKey":{"id":"referenceKey","type":"string"},
                            "accountKey":{"id":"accountKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "currency":{"id":"currency","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "amount":{"id":"amount","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0}}]},
                            "status":{"id":"status","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["PENDING","CONFIRMED","FAILED","REVERSED"]}}]},
                            "description":{"id":"description","type":"string"}
                          }
                        }
                        """)
        );
    }
}

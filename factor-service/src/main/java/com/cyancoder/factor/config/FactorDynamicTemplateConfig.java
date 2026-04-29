package com.cyancoder.factor.config;

import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import com.cyancoder.dynamiccore.template.DynamicTemplateProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class FactorDynamicTemplateConfig {

    @Bean
    public DynamicTemplateProvider factorDynamicTemplateProvider() {
        return () -> List.of(
                new DynamicEntityTemplate("factor-document", "FACTOR", "Factor Document", "Legacy-style factor/invoice template with nested buyer and items.", """
                        {
                          "entityKey":"factor-document",
                          "entityType":"FACTOR",
                          "title":"Factor Document",
                          "defaultValues":{"currency":"IRR"},
                          "relationDefinitions":{"buyer":{"service":"buyer-service","entityKey":"buyer-profile"}},
                          "fields":{
                            "factorId":{"id":"factorId","type":"string"},
                            "companyId":{"id":"companyId","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "code":{"id":"code","type":"string"},
                            "currency":{"id":"currency","type":"string"},
                            "buyer":{"id":"buyer","type":"object","itemValidations":{
                              "buyerId":{"id":"buyerId","type":"string"},
                              "nationalCode":{"id":"nationalCode","type":"number"},
                              "economicCode":{"id":"economicCode","type":"string"},
                              "buyerType":{"id":"buyerType","type":"string"},
                              "tell":{"id":"tell","type":"string"},
                              "address":{"id":"address","type":"string"},
                              "postCode":{"id":"postCode","type":"string"},
                              "cityId":{"id":"cityId","type":"string"}
                            }},
                            "items":{"id":"items","type":"list","itemValidations":{
                              "productId":{"id":"productId","type":"string"},
                              "productName":{"id":"productName","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "count":{"id":"count","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0.0001}}]},
                              "unitPrice":{"id":"unitPrice","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0}}]},
                              "sumPrice":{"id":"sumPrice","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0}}]}
                            }}
                          }
                        }
                        """)
        );
    }
}

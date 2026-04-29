package com.cyancoder.client.config;

import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import com.cyancoder.dynamiccore.template.DynamicTemplateProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BuyerDynamicTemplateConfig {

    @Bean
    public DynamicTemplateProvider buyerDynamicTemplateProvider() {
        return () -> List.of(
                new DynamicEntityTemplate("buyer-profile", "BUYER", "Buyer Profile", "Customer or buyer template for invoice flows.", """
                        {
                          "entityKey":"buyer-profile",
                          "entityType":"BUYER",
                          "title":"Buyer Profile",
                          "fields":{
                            "buyerId":{"id":"buyerId","type":"string"},
                            "nationalCode":{"id":"nationalCode","type":"number"},
                            "economicCode":{"id":"economicCode","type":"string"},
                            "buyerType":{"id":"buyerType","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["REAL","LEGAL"]}}]},
                            "tell":{"id":"tell","type":"string"},
                            "address":{"id":"address","type":"string"},
                            "postCode":{"id":"postCode","type":"string"},
                            "cityId":{"id":"cityId","type":"string"},
                            "note":{"id":"note","type":"string"}
                          }
                        }
                        """)
        );
    }
}

package com.cyancoder.client.config;

import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import com.cyancoder.dynamiccore.template.DynamicTemplateProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ClientDynamicTemplateConfig {

    @Bean
    public DynamicTemplateProvider clientDynamicTemplateProvider() {
        return () -> List.of(
                new DynamicEntityTemplate("company-profile", "COMPANY", "Company Profile", "Company registration template with tax and key fields.", """
                        {
                          "entityKey":"company-profile",
                          "entityType":"COMPANY",
                          "title":"Company Profile",
                          "fields":{
                            "companyId":{"id":"companyId","type":"string"},
                            "name":{"id":"name","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "nationalCode":{"id":"nationalCode","type":"number"},
                            "economicCode":{"id":"economicCode","type":"string"},
                            "uniqueCode":{"id":"uniqueCode","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "pk":{"id":"pk","type":"string"},
                            "address":{"id":"address","type":"string"},
                            "postCode":{"id":"postCode","type":"string"},
                            "tell":{"id":"tell","type":"string"}
                          }
                        }
                        """),
                new DynamicEntityTemplate("client-profile", "CLIENT", "Client Profile", "Tenant or user-owned client profile.", """
                        {
                          "entityKey":"client-profile",
                          "entityType":"CLIENT",
                          "title":"Client Profile",
                          "fields":{
                            "clientId":{"id":"clientId","type":"string"},
                            "firstName":{"id":"firstName","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "lastName":{"id":"lastName","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "mobile":{"id":"mobile","type":"string"},
                            "email":{"id":"email","type":"string"},
                            "nationalCode":{"id":"nationalCode","type":"number"}
                          }
                        }
                        """)
        );
    }
}

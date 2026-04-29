package com.cyancoder.crm.config;

import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import com.cyancoder.dynamiccore.template.DynamicTemplateProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CrmDynamicTemplateConfig {

    @Bean
    public DynamicTemplateProvider crmDynamicTemplateProvider() {
        return () -> List.of(
                new DynamicEntityTemplate("crm-lead", "LEAD", "CRM Lead", "Lead capture and qualification template.", """
                        {
                          "entityKey":"crm-lead",
                          "entityType":"LEAD",
                          "title":"CRM Lead",
                          "defaultValues":{"recordType":"LEAD","status":"NEW","source":"WEB"},
                          "fields":{
                            "recordType":{"id":"recordType","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["LEAD"]}}]},
                            "fullName":{"id":"fullName","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "companyName":{"id":"companyName","type":"string"},
                            "email":{"id":"email","type":"string","validations":[{"validation":"REGEX","order":1,"validationParams":{"pattern":"^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"},"validationMessage":"invalid email"}]},
                            "mobile":{"id":"mobile","type":"string"},
                            "status":{"id":"status","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["NEW","QUALIFIED","CONTACTED","WON","LOST"]}}]},
                            "source":{"id":"source","type":"string"},
                            "ownerUserId":{"id":"ownerUserId","type":"string"},
                            "notes":{"id":"notes","type":"string"}
                          }
                        }
                        """),
                new DynamicEntityTemplate("crm-contact", "CONTACT", "CRM Contact", "Contact/account template for customer records.", """
                        {
                          "entityKey":"crm-contact",
                          "entityType":"CONTACT",
                          "title":"CRM Contact",
                          "defaultValues":{"recordType":"CONTACT","status":"ACTIVE"},
                          "relationDefinitions":{"companyEntity":{"service":"client-service","entityKey":"company-profile"}},
                          "fields":{
                            "recordType":{"id":"recordType","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["CONTACT"]}}]},
                            "fullName":{"id":"fullName","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "companyName":{"id":"companyName","type":"string"},
                            "email":{"id":"email","type":"string"},
                            "mobile":{"id":"mobile","type":"string"},
                            "status":{"id":"status","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["ACTIVE","INACTIVE","BLACKLISTED"]}}]},
                            "source":{"id":"source","type":"string"},
                            "notes":{"id":"notes","type":"string"}
                          }
                        }
                        """)
        );
    }
}

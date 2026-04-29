package com.cyancoder.commerce.config;

import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import com.cyancoder.dynamiccore.template.DynamicTemplateProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CommerceDynamicTemplateConfig {

    @Bean
    public DynamicTemplateProvider commerceDynamicTemplateProvider() {
        return () -> List.of(
                new DynamicEntityTemplate("sales-order", "ORDER", "Sales Order", "Shop order with nested items and totals.", """
                        {
                          "entityKey":"sales-order",
                          "entityType":"ORDER",
                          "title":"Sales Order",
                          "defaultValues":{"documentType":"ORDER","currency":"IRR","documentStatus":"DRAFT","discountTotal":0,"taxTotal":0},
                          "relationDefinitions":{"customer":{"service":"crm-service","entityKey":"crm-contact"}},
                          "fields":{
                            "documentType":{"id":"documentType","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["ORDER"]}}]},
                            "customerKey":{"id":"customerKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "currency":{"id":"currency","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "documentStatus":{"id":"documentStatus","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["DRAFT","SUBMITTED","PAID","CANCELLED"]}}]},
                            "subtotal":{"id":"subtotal","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0}}]},
                            "discountTotal":{"id":"discountTotal","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0}}]},
                            "taxTotal":{"id":"taxTotal","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0}}]},
                            "grandTotal":{"id":"grandTotal","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0}}]},
                            "items":{"id":"items","type":"list","itemValidations":{
                              "itemKey":{"id":"itemKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "name":{"id":"name","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "quantity":{"id":"quantity","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0.0001}}]},
                              "unitPrice":{"id":"unitPrice","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0}}]},
                              "lineTotal":{"id":"lineTotal","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0}}]}
                            }}
                          }
                        }
                        """),
                new DynamicEntityTemplate("sales-invoice", "INVOICE", "Sales Invoice", "Invoice entity with order-compatible structure.", """
                        {
                          "entityKey":"sales-invoice",
                          "entityType":"INVOICE",
                          "title":"Sales Invoice",
                          "defaultValues":{"documentType":"INVOICE","currency":"IRR","documentStatus":"ISSUED","discountTotal":0,"taxTotal":0},
                          "relationDefinitions":{"order":{"service":"commerce-service","entityKey":"sales-order"}},
                          "fields":{
                            "documentType":{"id":"documentType","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["INVOICE"]}}]},
                            "customerKey":{"id":"customerKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "currency":{"id":"currency","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "documentStatus":{"id":"documentStatus","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["ISSUED","PAID","VOID"]}}]},
                            "subtotal":{"id":"subtotal","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0}}]},
                            "discountTotal":{"id":"discountTotal","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0}}]},
                            "taxTotal":{"id":"taxTotal","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0}}]},
                            "grandTotal":{"id":"grandTotal","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0}}]},
                            "items":{"id":"items","type":"list","itemValidations":{
                              "itemKey":{"id":"itemKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "quantity":{"id":"quantity","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0.0001}}]},
                              "unitPrice":{"id":"unitPrice","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0}}]},
                              "lineTotal":{"id":"lineTotal","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0}}]}
                            }}
                          }
                        }
                        """)
        );
    }
}

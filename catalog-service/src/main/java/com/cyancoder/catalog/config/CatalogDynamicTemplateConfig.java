package com.cyancoder.catalog.config;

import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import com.cyancoder.dynamiccore.template.DynamicTemplateProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CatalogDynamicTemplateConfig {

    @Bean
    public DynamicTemplateProvider catalogDynamicTemplateProvider() {
        return () -> List.of(
                new DynamicEntityTemplate("catalog-product", "PRODUCT", "Catalog Product", "Sellable product with controlled price and attributes.", """
                        {
                          "entityKey":"catalog-product",
                          "entityType":"PRODUCT",
                          "title":"Catalog Product",
                          "defaultValues":{"itemType":"PRODUCT","currency":"IRR","active":true},
                          "fields":{
                            "itemType":{"id":"itemType","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["PRODUCT"]}}]},
                            "name":{"id":"name","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "sku":{"id":"sku","type":"string","validations":[{"validation":"REQUIRED","order":1},{"validation":"REGEX","order":2,"validationParams":{"pattern":"^[A-Z0-9_-]+$"}}]},
                            "categoryKey":{"id":"categoryKey","type":"string"},
                            "unit":{"id":"unit","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "defaultPrice":{"id":"defaultPrice","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0}}]},
                            "currency":{"id":"currency","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "active":{"id":"active","type":"boolean"},
                            "details":{"id":"details","type":"object","itemValidations":{
                              "brand":{"id":"brand","type":"string"},
                              "model":{"id":"model","type":"string"},
                              "shortDescription":{"id":"shortDescription","type":"string"}
                            }}
                          }
                        }
                        """),
                new DynamicEntityTemplate("catalog-service-offer", "SERVICE", "Catalog Service", "Service offering with duration and pricing.", """
                        {
                          "entityKey":"catalog-service-offer",
                          "entityType":"SERVICE",
                          "title":"Catalog Service",
                          "defaultValues":{"itemType":"SERVICE","currency":"IRR","active":true},
                          "fields":{
                            "itemType":{"id":"itemType","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["SERVICE"]}}]},
                            "name":{"id":"name","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "serviceCode":{"id":"serviceCode","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "defaultPrice":{"id":"defaultPrice","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0}}]},
                            "currency":{"id":"currency","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "durationMinutes":{"id":"durationMinutes","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0}}]},
                            "deliveryMode":{"id":"deliveryMode","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["ONSITE","REMOTE","DIGITAL"]}}]},
                            "active":{"id":"active","type":"boolean"}
                          }
                        }
                        """)
        );
    }
}

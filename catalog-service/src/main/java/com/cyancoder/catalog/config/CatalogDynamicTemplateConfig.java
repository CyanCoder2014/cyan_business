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
                            "compareAtPrice":{"id":"compareAtPrice","type":"number"},
                            "currency":{"id":"currency","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "active":{"id":"active","type":"boolean"},
                            "slug":{"id":"slug","type":"string"},
                            "seo":{"id":"seo","type":"object","itemValidations":{
                              "title":{"id":"title","type":"string"},
                              "description":{"id":"description","type":"string"},
                              "canonicalUrl":{"id":"canonicalUrl","type":"string"},
                              "robots":{"id":"robots","type":"string"},
                              "schemaType":{"id":"schemaType","type":"string"},
                              "faqEntries":{"id":"faqEntries","type":"list","itemValidations":{
                                "question":{"id":"question","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                                "answer":{"id":"answer","type":"string","validations":[{"validation":"REQUIRED","order":1}]}
                              }}
                            }},
                            "media":{"id":"media","type":"list","itemValidations":{
                              "assetRef":{"id":"assetRef","type":"object","itemValidations":{
                                "service":{"id":"service","type":"string"},
                                "entityKey":{"id":"entityKey","type":"string"},
                                "recordKey":{"id":"recordKey","type":"string"}
                              }},
                              "url":{"id":"url","type":"string"},
                              "alt":{"id":"alt","type":"string"},
                              "sortOrder":{"id":"sortOrder","type":"number"},
                              "primary":{"id":"primary","type":"boolean"}
                            }},
                            "attributes":{"id":"attributes","type":"list","itemValidations":{
                              "attributeKey":{"id":"attributeKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "label":{"id":"label","type":"string"},
                              "valueType":{"id":"valueType","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["TEXT","NUMBER","BOOLEAN","LIST"]}}]},
                              "stringValue":{"id":"stringValue","type":"string"},
                              "numberValue":{"id":"numberValue","type":"number"},
                              "booleanValue":{"id":"booleanValue","type":"boolean"},
                              "listValues":{"id":"listValues","type":"list"}
                            }},
                            "variants":{"id":"variants","type":"list","itemValidations":{
                              "variantKey":{"id":"variantKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "title":{"id":"title","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "sku":{"id":"sku","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "price":{"id":"price","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0}}]},
                              "compareAtPrice":{"id":"compareAtPrice","type":"number"},
                              "inventory":{"id":"inventory","type":"object","itemValidations":{
                                "stockQuantity":{"id":"stockQuantity","type":"number"},
                                "trackInventory":{"id":"trackInventory","type":"boolean"},
                                "allowBackorder":{"id":"allowBackorder","type":"boolean"}
                              }},
                              "optionValues":{"id":"optionValues","type":"list","itemValidations":{
                                "optionKey":{"id":"optionKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                                "value":{"id":"value","type":"string","validations":[{"validation":"REQUIRED","order":1}]}
                              }}
                            }},
                            "routing":{"id":"routing","type":"object","itemValidations":{
                              "primaryPath":{"id":"primaryPath","type":"string"},
                              "collectionPaths":{"id":"collectionPaths","type":"list"},
                              "sitemapPriority":{"id":"sitemapPriority","type":"number"},
                              "changeFrequency":{"id":"changeFrequency","type":"string"}
                            }},
                            "searchIndex":{"id":"searchIndex","type":"object","itemValidations":{
                              "keywords":{"id":"keywords","type":"list"},
                              "filterEntries":{"id":"filterEntries","type":"list","itemValidations":{
                                "key":{"id":"key","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                                "label":{"id":"label","type":"string"},
                                "valueType":{"id":"valueType","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["TEXT","NUMBER","BOOLEAN","LIST"]}}]},
                                "stringValue":{"id":"stringValue","type":"string"},
                                "numberValue":{"id":"numberValue","type":"number"},
                                "booleanValue":{"id":"booleanValue","type":"boolean"},
                                "listValues":{"id":"listValues","type":"list"}
                              }},
                              "sortEntries":{"id":"sortEntries","type":"list","itemValidations":{
                                "key":{"id":"key","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                                "numberValue":{"id":"numberValue","type":"number"},
                                "stringValue":{"id":"stringValue","type":"string"}
                              }}
                            }},
                            "details":{"id":"details","type":"object","itemValidations":{
                              "brand":{"id":"brand","type":"string"},
                              "model":{"id":"model","type":"string"},
                              "shortDescription":{"id":"shortDescription","type":"string"},
                              "longDescription":{"id":"longDescription","type":"string"}
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

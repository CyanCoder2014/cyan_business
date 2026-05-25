package com.cyancoder.pricingpromotion.config;

import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import com.cyancoder.dynamiccore.template.DynamicTemplateProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PricingPromotionDynamicTemplateConfig {

    @Bean
    public DynamicTemplateProvider pricingPromotionDynamicTemplateProvider() {
        return () -> List.of(
                new DynamicEntityTemplate("promotion-rule", "PRICING_PROMOTION", "Promotion Rule", "Coupons, discounts, stackability, and audience conditions for carts and orders.", """
                        {
                          "entityKey":"promotion-rule",
                          "entityType":"PRICING_PROMOTION",
                          "title":"Promotion Rule",
                          "defaultValues":{"status":"ACTIVE","discountType":"PERCENTAGE"},
                          "fields":{
                            "promotionKey":{"id":"promotionKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "code":{"id":"code","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "status":{"id":"status","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["ACTIVE","INACTIVE","EXPIRED","DRAFT"]}}]},
                            "discountType":{"id":"discountType","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["PERCENTAGE","FIXED_AMOUNT","FREE_SHIPPING"]}}]},
                            "discountValue":{"id":"discountValue","type":"number"},
                            "conditions":{"id":"conditions","type":"list","itemValidations":{
                              "field":{"id":"field","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "operator":{"id":"operator","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["EQ","NEQ","GT","GTE","LT","LTE","IN","CONTAINS"]}}]},
                              "value":{"id":"value","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "scope":{"id":"scope","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["CART","ITEM","CUSTOMER","CHANNEL","SHIPPING"]}}]}
                            }},
                            "targets":{"id":"targets","type":"list","itemValidations":{
                              "scope":{"id":"scope","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["CART","ITEM","CATEGORY","SHIPPING"]}}]},
                              "entityRef":{"id":"entityRef","type":"object","itemValidations":{
                                "service":{"id":"service","type":"string"},
                                "entityKey":{"id":"entityKey","type":"string"},
                                "recordKey":{"id":"recordKey","type":"string"}
                              }}
                            }},
                            "stacking":{"id":"stacking","type":"object","itemValidations":{
                              "exclusive":{"id":"exclusive","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["true","false"]}}]},
                              "priority":{"id":"priority","type":"number"}
                            }},
                            "schedule":{"id":"schedule","type":"object","itemValidations":{
                              "startsAt":{"id":"startsAt","type":"string"},
                              "endsAt":{"id":"endsAt","type":"string"},
                              "timezone":{"id":"timezone","type":"string"}
                            }}
                          }
                        }
                        """),
                new DynamicEntityTemplate("tax-rule", "PRICING_PROMOTION", "Tax Rule", "Dynamic tax profile with geo conditions and product/category mappings.", """
                        {
                          "entityKey":"tax-rule",
                          "entityType":"PRICING_PROMOTION",
                          "title":"Tax Rule",
                          "defaultValues":{"status":"ACTIVE","calculationMode":"PERCENTAGE"},
                          "fields":{
                            "taxRuleKey":{"id":"taxRuleKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "status":{"id":"status","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["ACTIVE","INACTIVE"]}}]},
                            "calculationMode":{"id":"calculationMode","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["PERCENTAGE","FIXED"]}}]},
                            "rate":{"id":"rate","type":"number"},
                            "jurisdictions":{"id":"jurisdictions","type":"list","itemValidations":{
                              "country":{"id":"country","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "province":{"id":"province","type":"string"},
                              "city":{"id":"city","type":"string"}
                            }},
                            "appliesTo":{"id":"appliesTo","type":"list","itemValidations":{
                              "scope":{"id":"scope","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["ORDER","SHIPPING","PRODUCT","CATEGORY"]}}]},
                              "targetKey":{"id":"targetKey","type":"string"}
                            }}
                          }
                        }
                        """)
        );
    }
}

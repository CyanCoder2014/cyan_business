package com.cyancoder.cart.config;

import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import com.cyancoder.dynamiccore.template.DynamicTemplateProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CartDynamicTemplateConfig {

    @Bean
    public DynamicTemplateProvider cartDynamicTemplateProvider() {
        return () -> List.of(
                new DynamicEntityTemplate("shopping-cart", "CART", "Shopping Cart", "Cart/session entity with nested lines, pricing snapshot, and relation links to product and customer records.", """
                        {
                          "entityKey":"shopping-cart",
                          "entityType":"CART",
                          "title":"Shopping Cart",
                          "defaultValues":{"status":"ACTIVE","currency":"IRR"},
                          "fields":{
                            "cartKey":{"id":"cartKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "sessionKey":{"id":"sessionKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "customerRef":{"id":"customerRef","type":"object","itemValidations":{
                              "service":{"id":"service","type":"string"},
                              "entityKey":{"id":"entityKey","type":"string"},
                              "recordKey":{"id":"recordKey","type":"string"}
                            }},
                            "status":{"id":"status","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["ACTIVE","CHECKOUT","ABANDONED","CONVERTED","EXPIRED"]}}]},
                            "currency":{"id":"currency","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "items":{"id":"items","type":"list","itemValidations":{
                              "lineKey":{"id":"lineKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "productRef":{"id":"productRef","type":"object","itemValidations":{
                                "service":{"id":"service","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                                "entityKey":{"id":"entityKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                                "recordKey":{"id":"recordKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]}
                              }},
                              "sku":{"id":"sku","type":"string"},
                              "title":{"id":"title","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "quantity":{"id":"quantity","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":"1"}}]},
                              "unitPrice":{"id":"unitPrice","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":"0"}}]},
                              "compareAtPrice":{"id":"compareAtPrice","type":"number"},
                              "currency":{"id":"currency","type":"string"},
                              "variant":{"id":"variant","type":"object","itemValidations":{
                                "variantKey":{"id":"variantKey","type":"string"},
                                "title":{"id":"title","type":"string"},
                                "sku":{"id":"sku","type":"string"}
                              }},
                              "attributes":{"id":"attributes","type":"object","itemValidations":{
                                "optionValues":{"id":"optionValues","type":"list","itemValidations":{
                                  "optionKey":{"id":"optionKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                                  "label":{"id":"label","type":"string"},
                                  "value":{"id":"value","type":"string","validations":[{"validation":"REQUIRED","order":1}]}
                                }},
                                "customizations":{"id":"customizations","type":"list","itemValidations":{
                                  "fieldKey":{"id":"fieldKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                                  "label":{"id":"label","type":"string"},
                                  "value":{"id":"value","type":"string"},
                                  "priceDelta":{"id":"priceDelta","type":"number"}
                                }},
                                "fulfillment":{"id":"fulfillment","type":"object","itemValidations":{
                                  "requiresShipping":{"id":"requiresShipping","type":"boolean"},
                                  "isDigital":{"id":"isDigital","type":"boolean"},
                                  "weight":{"id":"weight","type":"number"},
                                  "weightUnit":{"id":"weightUnit","type":"string"}
                                }}
                              }},
                              "lineTotals":{"id":"lineTotals","type":"object","itemValidations":{
                                "subtotal":{"id":"subtotal","type":"number"},
                                "discountTotal":{"id":"discountTotal","type":"number"},
                                "taxTotal":{"id":"taxTotal","type":"number"},
                                "grandTotal":{"id":"grandTotal","type":"number"}
                              }}
                            }},
                            "pricing":{"id":"pricing","type":"object","itemValidations":{
                              "subtotal":{"id":"subtotal","type":"number"},
                              "discountTotal":{"id":"discountTotal","type":"number"},
                              "shippingTotal":{"id":"shippingTotal","type":"number"},
                              "taxTotal":{"id":"taxTotal","type":"number"},
                              "grandTotal":{"id":"grandTotal","type":"number"},
                              "currency":{"id":"currency","type":"string"},
                              "breakdown":{"id":"breakdown","type":"list","itemValidations":{
                                "code":{"id":"code","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                                "type":{"id":"type","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["SUBTOTAL","DISCOUNT","SHIPPING","TAX","SURCHARGE","TOTAL"]}}]},
                                "title":{"id":"title","type":"string"},
                                "amount":{"id":"amount","type":"number","validations":[{"validation":"REQUIRED","order":1}]}
                              }}
                            }},
                            "appliedPromotions":{"id":"appliedPromotions","type":"list"},
                            "shippingPreference":{"id":"shippingPreference","type":"object","itemValidations":{
                              "methodKey":{"id":"methodKey","type":"string"},
                              "carrier":{"id":"carrier","type":"string"},
                              "serviceLevel":{"id":"serviceLevel","type":"string"},
                              "requestedDate":{"id":"requestedDate","type":"string"}
                            }},
                            "notes":{"id":"notes","type":"string"},
                            "metadata":{"id":"metadata","type":"object","itemValidations":{
                              "channel":{"id":"channel","type":"string"},
                              "campaignKey":{"id":"campaignKey","type":"string"},
                              "locale":{"id":"locale","type":"string"}
                            }}
                          }
                        }
                        """)
        );
    }
}

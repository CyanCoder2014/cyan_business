package com.cyancoder.checkout.config;

import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import com.cyancoder.dynamiccore.template.DynamicTemplateProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CheckoutDynamicTemplateConfig {

    @Bean
    public DynamicTemplateProvider checkoutDynamicTemplateProvider() {
        return () -> List.of(
                new DynamicEntityTemplate("checkout-session", "CHECKOUT", "Checkout Session", "Structured checkout entity with nested addresses, shipping, payment preference, lifecycle, and notification state.", """
                        {
                          "entityKey":"checkout-session",
                          "entityType":"CHECKOUT",
                          "title":"Checkout Session",
                          "defaultValues":{"status":"CREATED","currency":"IRR","notificationStatus":"PENDING"},
                          "fields":{
                            "checkoutKey":{"id":"checkoutKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "cartRef":{"id":"cartRef","type":"object","itemValidations":{
                              "service":{"id":"service","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "entityKey":{"id":"entityKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "recordKey":{"id":"recordKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]}
                            }},
                            "customer":{"id":"customer","type":"object","itemValidations":{
                              "customerRef":{"id":"customerRef","type":"object","itemValidations":{
                                "service":{"id":"service","type":"string"},
                                "entityKey":{"id":"entityKey","type":"string"},
                                "recordKey":{"id":"recordKey","type":"string"}
                              }},
                              "email":{"id":"email","type":"string"},
                              "mobile":{"id":"mobile","type":"string"},
                              "fullName":{"id":"fullName","type":"string","validations":[{"validation":"REQUIRED","order":1}]}
                            }},
                            "billingAddress":{"id":"billingAddress","type":"object","itemValidations":{
                              "country":{"id":"country","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "city":{"id":"city","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "postalCode":{"id":"postalCode","type":"string"},
                              "line1":{"id":"line1","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "line2":{"id":"line2","type":"string"}
                            }},
                            "shippingAddress":{"id":"shippingAddress","type":"object","itemValidations":{
                              "country":{"id":"country","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "city":{"id":"city","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "postalCode":{"id":"postalCode","type":"string"},
                              "line1":{"id":"line1","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "line2":{"id":"line2","type":"string"}
                            }},
                            "shippingOption":{"id":"shippingOption","type":"object","itemValidations":{
                              "methodKey":{"id":"methodKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "carrier":{"id":"carrier","type":"string"},
                              "serviceLevel":{"id":"serviceLevel","type":"string"},
                              "trackingMode":{"id":"trackingMode","type":"string"},
                              "price":{"id":"price","type":"number"},
                              "estimatedDelivery":{"id":"estimatedDelivery","type":"object","itemValidations":{
                                "minDays":{"id":"minDays","type":"number"},
                                "maxDays":{"id":"maxDays","type":"number"},
                                "label":{"id":"label","type":"string"}
                              }}
                            }},
                            "totals":{"id":"totals","type":"object","itemValidations":{
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
                            "paymentPreference":{"id":"paymentPreference","type":"object","itemValidations":{
                              "methodKey":{"id":"methodKey","type":"string"},
                              "region":{"id":"region","type":"string"},
                              "gatewayType":{"id":"gatewayType","type":"string"},
                              "captureMode":{"id":"captureMode","type":"string"},
                              "installmentPlanKey":{"id":"installmentPlanKey","type":"string"},
                              "paymentTransactionKey":{"id":"paymentTransactionKey","type":"string"},
                              "paymentUrl":{"id":"paymentUrl","type":"string"}
                            }},
                            "status":{"id":"status","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["CREATED","ADDRESS_COMPLETED","PRICED","PAYMENT_PENDING","PAYMENT_VERIFIED","ORDER_CONFIRMED","FAILED","CANCELLED"]}}]},
                            "notificationStatus":{"id":"notificationStatus","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["PENDING","SENT","FAILED"]}}]},
                            "orderLifecycle":{"id":"orderLifecycle","type":"object","itemValidations":{
                              "orderStatus":{"id":"orderStatus","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["DRAFT","PENDING_PAYMENT","PAID","CONFIRMED","FULFILLMENT_PENDING","FULFILLED","DELIVERED","COMPLETED","CANCELLED","REFUNDED"]}}]},
                              "paymentStatus":{"id":"paymentStatus","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["UNPAID","AUTHORIZED","PAID","FAILED","REFUNDED","PARTIALLY_REFUNDED"]}}]},
                              "fulfillmentStatus":{"id":"fulfillmentStatus","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["UNFULFILLED","PARTIAL","FULFILLED","DELIVERED","RETURNED"]}}]},
                              "orderRef":{"id":"orderRef","type":"object","itemValidations":{
                                "service":{"id":"service","type":"string"},
                                "entityKey":{"id":"entityKey","type":"string"},
                                "recordKey":{"id":"recordKey","type":"string"}
                              }}
                            }},
                            "notifications":{"id":"notifications","type":"list","itemValidations":{
                              "channel":{"id":"channel","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["EMAIL","SMS","PUSH","WEBHOOK"]}}]},
                              "templateKey":{"id":"templateKey","type":"string"},
                              "recipient":{"id":"recipient","type":"string"},
                              "status":{"id":"status","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["PENDING","QUEUED","SENT","FAILED"]}}]}
                            }}
                          }
                        }
                        """)
        );
    }
}

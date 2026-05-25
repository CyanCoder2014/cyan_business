package com.cyancoder.paymentorchestrator.config;

import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import com.cyancoder.dynamiccore.template.DynamicTemplateProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PaymentOrchestratorDynamicTemplateConfig {

    @Bean
    public DynamicTemplateProvider paymentOrchestratorDynamicTemplateProvider() {
        return () -> List.of(
                new DynamicEntityTemplate("payment-session", "PAYMENT_ORCHESTRATOR", "Payment Session", "Gateway abstraction session linking checkout/order to payment-service methods and webhook state.", """
                        {
                          "entityKey":"payment-session",
                          "entityType":"PAYMENT_ORCHESTRATOR",
                          "title":"Payment Session",
                          "defaultValues":{"status":"CREATED","webhookStatus":"PENDING"},
                          "fields":{
                            "paymentSessionKey":{"id":"paymentSessionKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "checkoutRef":{"id":"checkoutRef","type":"object","itemValidations":{
                              "service":{"id":"service","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "entityKey":{"id":"entityKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "recordKey":{"id":"recordKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]}
                            }},
                            "orderRef":{"id":"orderRef","type":"object","itemValidations":{
                              "service":{"id":"service","type":"string"},
                              "entityKey":{"id":"entityKey","type":"string"},
                              "recordKey":{"id":"recordKey","type":"string"}
                            }},
                            "methodSelection":{"id":"methodSelection","type":"object","itemValidations":{
                              "paymentMethodKey":{"id":"paymentMethodKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "region":{"id":"region","type":"string"},
                              "providerCode":{"id":"providerCode","type":"string"},
                              "currency":{"id":"currency","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "amount":{"id":"amount","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":"0.01"}}]}
                            }},
                            "gatewayRequest":{"id":"gatewayRequest","type":"object"},
                            "gatewayResponse":{"id":"gatewayResponse","type":"object"},
                            "webhookSubscriptions":{"id":"webhookSubscriptions","type":"list","itemValidations":{
                              "eventType":{"id":"eventType","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "targetUrl":{"id":"targetUrl","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "secretKey":{"id":"secretKey","type":"string"}
                            }},
                            "status":{"id":"status","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["CREATED","INITIATED","WAITING_CALLBACK","VERIFIED","FAILED","CANCELLED"]}}]},
                            "webhookStatus":{"id":"webhookStatus","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["PENDING","DELIVERED","FAILED"]}}]}
                          }
                        }
                        """)
        );
    }
}

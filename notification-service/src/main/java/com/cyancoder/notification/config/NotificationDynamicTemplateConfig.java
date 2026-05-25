package com.cyancoder.notification.config;

import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import com.cyancoder.dynamiccore.template.DynamicTemplateProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class NotificationDynamicTemplateConfig {

    @Bean
    public DynamicTemplateProvider notificationDynamicTemplateProvider() {
        return () -> List.of(
                new DynamicEntityTemplate("notification-template", "NOTIFICATION", "Notification Template", "Reusable email, SMS, push, and webhook notification template.", """
                        {
                          "entityKey":"notification-template",
                          "entityType":"NOTIFICATION",
                          "title":"Notification Template",
                          "defaultValues":{"active":"true","provider":"default"},
                          "fields":{
                            "templateKey":{"id":"templateKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "channel":{"id":"channel","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["EMAIL","SMS","PUSH","MQTT","WEBHOOK","REST"]}}]},
                            "provider":{"id":"provider","type":"string"},
                            "subjectTemplate":{"id":"subjectTemplate","type":"string"},
                            "bodyTemplate":{"id":"bodyTemplate","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "active":{"id":"active","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["true","false"]}}]}
                          }
                        }
                        """),
                new DynamicEntityTemplate("notification-message", "NOTIFICATION", "Notification Message", "Rendered delivery record for email, SMS, push, and webhook notifications.", """
                        {
                          "entityKey":"notification-message",
                          "entityType":"NOTIFICATION",
                          "title":"Notification Message",
                          "defaultValues":{"status":"QUEUED","provider":"default"},
                          "fields":{
                            "messageKey":{"id":"messageKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "channel":{"id":"channel","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["EMAIL","SMS","PUSH","MQTT","WEBHOOK","REST"]}}]},
                            "templateKey":{"id":"templateKey","type":"string"},
                            "recipient":{"id":"recipient","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "subject":{"id":"subject","type":"string"},
                            "body":{"id":"body","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "provider":{"id":"provider","type":"string"},
                            "providerMessageId":{"id":"providerMessageId","type":"string"},
                            "status":{"id":"status","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["QUEUED","SENT","FAILED"]}}]},
                            "sentAt":{"id":"sentAt","type":"string"},
                            "errorMessage":{"id":"errorMessage","type":"string"},
                            "relatedRef":{"id":"relatedRef","type":"object","itemValidations":{
                              "service":{"id":"service","type":"string"},
                              "entityKey":{"id":"entityKey","type":"string"},
                              "recordKey":{"id":"recordKey","type":"string"}
                            }},
                            "payload":{"id":"payload","type":"object","itemValidations":{
                              "summary":{"id":"summary","type":"string"},
                              "eventCode":{"id":"eventCode","type":"string"}
                            }}
                          }
                        }
                        """)
        );
    }
}

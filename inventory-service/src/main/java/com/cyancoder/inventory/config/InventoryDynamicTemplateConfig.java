package com.cyancoder.inventory.config;

import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import com.cyancoder.dynamiccore.template.DynamicTemplateProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class InventoryDynamicTemplateConfig {

    @Bean
    public DynamicTemplateProvider inventoryDynamicTemplateProvider() {
        return () -> List.of(
                new DynamicEntityTemplate("stock-item", "INVENTORY", "Stock Item", "Inventory quantity tracking template.", """
                        {
                          "entityKey":"stock-item",
                          "entityType":"INVENTORY",
                          "title":"Stock Item",
                          "defaultValues":{"reservedQuantity":0},
                          "relationDefinitions":{"catalogItem":{"service":"catalog-service","entityKey":"catalog-product"}},
                          "fields":{
                            "catalogItemKey":{"id":"catalogItemKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "warehouseKey":{"id":"warehouseKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "onHandQuantity":{"id":"onHandQuantity","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0}}]},
                            "reservedQuantity":{"id":"reservedQuantity","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0}}]},
                            "reorderPoint":{"id":"reorderPoint","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0}}]},
                            "unit":{"id":"unit","type":"string"}
                          }
                        }
                        """),
                new DynamicEntityTemplate("work-order", "MANUFACTURING", "Work Order", "Later manufacturing workflow starter for BPM integration.", """
                        {
                          "entityKey":"work-order",
                          "entityType":"MANUFACTURING",
                          "title":"Work Order",
                          "defaultValues":{"status":"PLANNED"},
                          "fields":{
                            "workOrderCode":{"id":"workOrderCode","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "catalogItemKey":{"id":"catalogItemKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "plannedQuantity":{"id":"plannedQuantity","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0.0001}}]},
                            "status":{"id":"status","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["PLANNED","RELEASED","IN_PROGRESS","DONE","CANCELLED"]}}]},
                            "operations":{"id":"operations","type":"list","itemValidations":{
                              "name":{"id":"name","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "workCenter":{"id":"workCenter","type":"string"},
                              "durationMinutes":{"id":"durationMinutes","type":"number","validations":[{"validation":"DECIMAL_MIN","order":1,"validationParams":{"min":0}}]}
                            }}
                          }
                        }
                        """)
        );
    }
}

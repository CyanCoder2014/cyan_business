package com.cyancoder.client.config;

import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import com.cyancoder.dynamiccore.template.DynamicTemplateProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ProductDynamicTemplateConfig {

    @Bean
    public DynamicTemplateProvider productDynamicTemplateProvider() {
        return () -> List.of(
                new DynamicEntityTemplate("legacy-product", "PRODUCT", "Legacy Product", "Minimal product master template aligned with old product service.", """
                        {
                          "entityKey":"legacy-product",
                          "entityType":"PRODUCT",
                          "title":"Legacy Product",
                          "fields":{
                            "productId":{"id":"productId","type":"string"},
                            "note":{"id":"note","type":"string"}
                          }
                        }
                        """)
        );
    }
}

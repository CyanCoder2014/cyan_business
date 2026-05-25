package com.cyancoder.content.config;

import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import com.cyancoder.dynamiccore.template.DynamicTemplateProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ContentDynamicTemplateConfig {

    @Bean
    public DynamicTemplateProvider contentDynamicTemplateProvider() {
        return () -> List.of(
                new DynamicEntityTemplate("blog-page", "CONTENT", "Blog Page", "Blog or article page template.", """
                        {
                          "entityKey":"blog-page",
                          "entityType":"CONTENT",
                          "title":"Blog Page",
                          "defaultValues":{"contentType":"BLOG","publicationStatus":"DRAFT"},
                          "fields":{
                            "slug":{"id":"slug","type":"string","validations":[{"validation":"REQUIRED","order":1},{"validation":"REGEX","order":2,"validationParams":{"pattern":"^[a-z0-9-]+$"},"validationMessage":"slug must contain only lowercase letters, numbers, and dashes"}]},
                            "title":{"id":"title","type":"string","validations":[{"validation":"REQUIRED","order":1},{"validation":"MIN_LENGTH","order":2,"validationParams":{"min":3}}]},
                            "summary":{"id":"summary","type":"string","validations":[{"validation":"MAX_LENGTH","order":1,"validationParams":{"max":400}}]},
                            "body":{"id":"body","type":"string","validations":[{"validation":"REQUIRED","order":1},{"validation":"MIN_LENGTH","order":2,"validationParams":{"min":30}}]},
                            "author":{"id":"author","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "publicationStatus":{"id":"publicationStatus","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["DRAFT","REVIEW","PUBLISHED","ARCHIVED"]}}]},
                            "tags":{"id":"tags","type":"list"}
                          }
                        }
                        """),
                new DynamicEntityTemplate("landing-page", "CONTENT", "Landing Page", "Structured page template with nested sections.", """
                        {
                          "entityKey":"landing-page",
                          "entityType":"CONTENT",
                          "title":"Landing Page",
                          "defaultValues":{"contentType":"PAGE","publicationStatus":"DRAFT"},
                          "fields":{
                            "slug":{"id":"slug","type":"string","validations":[{"validation":"REQUIRED","order":1},{"validation":"REGEX","order":2,"validationParams":{"pattern":"^[a-z0-9-]+$"}}]},
                            "title":{"id":"title","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "heroTitle":{"id":"heroTitle","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "heroSubtitle":{"id":"heroSubtitle","type":"string"},
                            "publicationStatus":{"id":"publicationStatus","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["DRAFT","PUBLISHED"]}}]},
                            "sections":{"id":"sections","type":"list","itemValidations":{
                              "blockType":{"id":"blockType","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["TEXT","FEATURES","CTA","FAQ"]}}]},
                              "title":{"id":"title","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "body":{"id":"body","type":"string"},
                              "ctaLabel":{"id":"ctaLabel","type":"string"},
                              "ctaUrl":{"id":"ctaUrl","type":"string"}
                            }}
                          }
                        }
                        """)
        );
    }
}

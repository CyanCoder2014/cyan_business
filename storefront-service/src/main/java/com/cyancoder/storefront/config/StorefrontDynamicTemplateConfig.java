package com.cyancoder.storefront.config;

import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import com.cyancoder.dynamiccore.template.DynamicTemplateProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class StorefrontDynamicTemplateConfig {

    @Bean
    public DynamicTemplateProvider storefrontDynamicTemplateProvider() {
        return () -> List.of(
                new DynamicEntityTemplate("site-route", "STOREFRONT", "Site Route", "Dynamic route definition with modern SEO metadata, schema.org payloads, sitemap and indexing controls.", """
                        {
                          "entityKey":"site-route",
                          "entityType":"STOREFRONT",
                          "title":"Site Route",
                          "defaultValues":{"publicationStatus":"DRAFT","indexingEnabled":"true","sitemapPriority":"0.8"},
                          "fields":{
                            "routeKey":{"id":"routeKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "path":{"id":"path","type":"string","validations":[{"validation":"REQUIRED","order":1},{"validation":"REGEX","order":2,"validationParams":{"pattern":"^/[a-z0-9\\-/]*$"}}]},
                            "routeType":{"id":"routeType","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["LANDING","BLOG","PRODUCT","CATEGORY","SEARCH","CHECKOUT","CUSTOM"]}}]},
                            "entityRef":{"id":"entityRef","type":"object","itemValidations":{
                              "service":{"id":"service","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "entityKey":{"id":"entityKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "recordKey":{"id":"recordKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]}
                            }},
                            "seo":{"id":"seo","type":"object","itemValidations":{
                              "title":{"id":"title","type":"string","validations":[{"validation":"REQUIRED","order":1},{"validation":"MAX_LENGTH","order":2,"validationParams":{"max":70}}]},
                              "description":{"id":"description","type":"string","validations":[{"validation":"MAX_LENGTH","order":1,"validationParams":{"max":160}}]},
                              "canonicalUrl":{"id":"canonicalUrl","type":"string"},
                              "robots":{"id":"robots","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["index,follow","noindex,follow","index,nofollow","noindex,nofollow"]}}]},
                              "ogImage":{"id":"ogImage","type":"string"},
                              "twitterCard":{"id":"twitterCard","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["summary","summary_large_image"]}}]},
                              "structuredDataBlocks":{"id":"structuredDataBlocks","type":"list","itemValidations":{
                                "@context":{"id":"@context","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                                "@type":{"id":"@type","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                                "payloadJson":{"id":"payloadJson","type":"string","validations":[{"validation":"REQUIRED","order":1}]}
                              }}
                            }},
                            "rendering":{"id":"rendering","type":"object","itemValidations":{
                              "themeKey":{"id":"themeKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "templateKey":{"id":"templateKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "cacheTtlSeconds":{"id":"cacheTtlSeconds","type":"number"},
                              "preloadAssets":{"id":"preloadAssets","type":"list"}
                            }},
                            "indexingEnabled":{"id":"indexingEnabled","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["true","false"]}}]},
                            "sitemapPriority":{"id":"sitemapPriority","type":"string"},
                            "publicationStatus":{"id":"publicationStatus","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["DRAFT","PUBLISHED","ARCHIVED"]}}]}
                          }
                        }
                        """),
                new DynamicEntityTemplate("theme-layout", "STOREFRONT", "Theme Layout", "Theme, blocks, navigation, and rendering configuration for public storefronts.", """
                        {
                          "entityKey":"theme-layout",
                          "entityType":"STOREFRONT",
                          "title":"Theme Layout",
                          "defaultValues":{"status":"ACTIVE"},
                          "fields":{
                            "themeKey":{"id":"themeKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "brandName":{"id":"brandName","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "status":{"id":"status","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["ACTIVE","INACTIVE","DRAFT"]}}]},
                            "navigation":{"id":"navigation","type":"list","itemValidations":{
                              "label":{"id":"label","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "path":{"id":"path","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "children":{"id":"children","type":"list"}
                            }},
                            "globalSeo":{"id":"globalSeo","type":"object","itemValidations":{
                              "siteName":{"id":"siteName","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "defaultTitleTemplate":{"id":"defaultTitleTemplate","type":"string"},
                              "defaultDescription":{"id":"defaultDescription","type":"string"},
                              "organizationJsonLd":{"id":"organizationJsonLd","type":"string"}
                            }},
                            "blocks":{"id":"blocks","type":"list","itemValidations":{
                              "blockKey":{"id":"blockKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "componentType":{"id":"componentType","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "props":{"id":"props","type":"object"}
                            }}
                          }
                        }
                        """)
        );
    }
}

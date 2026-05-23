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
                            "path":{"id":"path","type":"string","validations":[{"validation":"REQUIRED","order":1},{"validation":"REGEX","order":2,"validationParams":{"pattern":"^/[a-z0-9/-]*$"}}]},
                            "routeType":{"id":"routeType","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["LANDING","BLOG","PRODUCT","CATEGORY","SEARCH","CHECKOUT","CUSTOM"]}}]},
                            "entityRef":{"id":"entityRef","type":"object","itemValidations":{
                              "service":{"id":"service","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "entityKey":{"id":"entityKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "recordKey":{"id":"recordKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]}
                            }},
                            "navigation":{"id":"navigation","type":"object","itemValidations":{
                              "label":{"id":"label","type":"string"},
                              "menuKey":{"id":"menuKey","type":"string"},
                              "sortOrder":{"id":"sortOrder","type":"number"},
                              "visible":{"id":"visible","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["true","false"]}}]}
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
                              "preloadAssets":{"id":"preloadAssets","type":"list"},
                              "hydrateTargetEntity":{"id":"hydrateTargetEntity","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["true","false"]}}]}
                            }},
                            "indexingEnabled":{"id":"indexingEnabled","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["true","false"]}}]},
                            "sitemapPriority":{"id":"sitemapPriority","type":"string"},
                            "publicationStatus":{"id":"publicationStatus","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["DRAFT","PUBLISHED","ARCHIVED"]}}]},
                            "routeLifecycle":{"id":"routeLifecycle","type":"object","itemValidations":{
                              "validFrom":{"id":"validFrom","type":"string"},
                              "validTo":{"id":"validTo","type":"string"},
                              "redirectTo":{"id":"redirectTo","type":"string"},
                              "httpStatus":{"id":"httpStatus","type":"number"}
                            }}
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
                            "templateKey":{"id":"templateKey","type":"string"},
                            "themeCategory":{"id":"themeCategory","type":"string"},
                            "previewImage":{"id":"previewImage","type":"string"},
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
                        """),
                new DynamicEntityTemplate("theme-template", "STOREFRONT", "Theme Template", "Reusable theme registry record that can be applied across multiple projects and sites.", """
                        {
                          "entityKey":"theme-template",
                          "entityType":"STOREFRONT",
                          "title":"Theme Template",
                          "defaultValues":{"status":"ACTIVE"},
                          "fields":{
                            "templateKey":{"id":"templateKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "title":{"id":"title","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "category":{"id":"category","type":"string"},
                            "description":{"id":"description","type":"string"},
                            "previewImage":{"id":"previewImage","type":"string"},
                            "status":{"id":"status","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["ACTIVE","DRAFT","ARCHIVED"]}}]},
                            "themeLayout":{"id":"themeLayout","type":"object"},
                            "recommendedRoute":{"id":"recommendedRoute","type":"object"}
                          }
                        }
                        """),
                new DynamicEntityTemplate("domain-binding", "STOREFRONT", "Domain Binding", "Custom domain mapping and verification workflow for storefront routes and themes.", """
                        {
                          "entityKey":"domain-binding",
                          "entityType":"STOREFRONT",
                          "title":"Domain Binding",
                          "defaultValues":{"status":"PENDING","sslStatus":"PENDING","verificationMethod":"DNS_TXT"},
                          "fields":{
                            "bindingKey":{"id":"bindingKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "hostname":{"id":"hostname","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "routeKey":{"id":"routeKey","type":"string"},
                            "themeKey":{"id":"themeKey","type":"string"},
                            "targetPath":{"id":"targetPath","type":"string"},
                            "verificationMethod":{"id":"verificationMethod","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["DNS_TXT","CNAME","HTTP_FILE"]}}]},
                            "verificationToken":{"id":"verificationToken","type":"string"},
                            "dnsTarget":{"id":"dnsTarget","type":"string"},
                            "status":{"id":"status","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["PENDING","VERIFYING","VERIFIED","ACTIVE","FAILED"]}}]},
                            "sslStatus":{"id":"sslStatus","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["PENDING","PROVISIONING","ACTIVE","FAILED"]}}]},
                            "canonicalPolicy":{"id":"canonicalPolicy","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["PRIMARY","REDIRECT_TO_PRIMARY","KEEP_PATH"]}}]},
                            "notes":{"id":"notes","type":"string"}
                          }
                        }
                        """),
                new DynamicEntityTemplate("theme-layout-version", "STOREFRONT", "Theme Layout Version", "Immutable snapshot of a theme layout for rollback and release history.", """
                        {
                          "entityKey":"theme-layout-version",
                          "entityType":"STOREFRONT",
                          "title":"Theme Layout Version",
                          "fields":{
                            "themeKey":{"id":"themeKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "versionKey":{"id":"versionKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "label":{"id":"label","type":"string"},
                            "snapshot":{"id":"snapshot","type":"object","validations":[{"validation":"REQUIRED","order":1}]},
                            "published":{"id":"published","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["true","false"]}}]}
                          }
                        }
                        """),
                new DynamicEntityTemplate("site-route-version", "STOREFRONT", "Site Route Version", "Immutable snapshot of a route record for rollback and release history.", """
                        {
                          "entityKey":"site-route-version",
                          "entityType":"STOREFRONT",
                          "title":"Site Route Version",
                          "fields":{
                            "routeKey":{"id":"routeKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "versionKey":{"id":"versionKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "label":{"id":"label","type":"string"},
                            "snapshot":{"id":"snapshot","type":"object","validations":[{"validation":"REQUIRED","order":1}]},
                            "published":{"id":"published","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["true","false"]}}]}
                          }
                        }
                        """)
        );
    }
}

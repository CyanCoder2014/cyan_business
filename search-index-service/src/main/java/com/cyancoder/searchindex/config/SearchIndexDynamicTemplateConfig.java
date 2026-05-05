package com.cyancoder.searchindex.config;

import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import com.cyancoder.dynamiccore.template.DynamicTemplateProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SearchIndexDynamicTemplateConfig {

    @Bean
    public DynamicTemplateProvider searchIndexDynamicTemplateProvider() {
        return () -> List.of(
                new DynamicEntityTemplate("index-definition", "SEARCH_INDEX", "Index Definition", "Declarative search/filter index metadata for content, product, CRM, order, and report projections.", """
                        {
                          "entityKey":"index-definition",
                          "entityType":"SEARCH_INDEX",
                          "title":"Index Definition",
                          "defaultValues":{"status":"ACTIVE","engine":"MONGO_PROJECTION"},
                          "fields":{
                            "indexKey":{"id":"indexKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "source":{"id":"source","type":"object","itemValidations":{
                              "serviceKey":{"id":"serviceKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "entityKey":{"id":"entityKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "entityType":{"id":"entityType","type":"string","validations":[{"validation":"REQUIRED","order":1}]}
                            }},
                            "engine":{"id":"engine","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["MONGO_PROJECTION","POSTGRES_READ_MODEL","ELASTICSEARCH","OPENSEARCH"]}}]},
                            "status":{"id":"status","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["ACTIVE","PAUSED","REBUILDING"]}}]},
                            "searchableFields":{"id":"searchableFields","type":"list","itemValidations":{
                              "fieldPath":{"id":"fieldPath","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "analyzer":{"id":"analyzer","type":"string"},
                              "boost":{"id":"boost","type":"number"}
                            }},
                            "facetFields":{"id":"facetFields","type":"list","itemValidations":{
                              "fieldPath":{"id":"fieldPath","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "facetType":{"id":"facetType","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["TERM","RANGE","DATE_HISTOGRAM"]}}]}
                            }},
                            "sortableFields":{"id":"sortableFields","type":"list","itemValidations":{
                              "fieldPath":{"id":"fieldPath","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "sortType":{"id":"sortType","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["TEXT","NUMBER","DATE"]}}]}
                            }},
                            "filterableFields":{"id":"filterableFields","type":"list","itemValidations":{
                              "fieldPath":{"id":"fieldPath","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "filterType":{"id":"filterType","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["TERM","BOOLEAN","NUMBER","NUMBER_RANGE","DATE"]}}]},
                              "label":{"id":"label","type":"string"}
                            }},
                            "suggestFields":{"id":"suggestFields","type":"list","itemValidations":{
                              "fieldPath":{"id":"fieldPath","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "weight":{"id":"weight","type":"number"}
                            }}
                          }
                        }
                        """),
                new DynamicEntityTemplate("search-document", "SEARCH_INDEX", "Search Document", "Projection document for fast search/filter queries across content, catalog, commerce, and reports.", """
                        {
                          "entityKey":"search-document",
                          "entityType":"SEARCH_INDEX",
                          "title":"Search Document",
                          "defaultValues":{"status":"ACTIVE"},
                          "fields":{
                            "documentKey":{"id":"documentKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "source":{"id":"source","type":"object","itemValidations":{
                              "serviceKey":{"id":"serviceKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "entityKey":{"id":"entityKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "recordKey":{"id":"recordKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]}
                            }},
                            "title":{"id":"title","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "summary":{"id":"summary","type":"string"},
                            "content":{"id":"content","type":"string"},
                            "status":{"id":"status","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["ACTIVE","ARCHIVED"]}}]},
                            "keywords":{"id":"keywords","type":"list"},
                            "entityType":{"id":"entityType","type":"string"},
                            "routing":{"id":"routing","type":"object","itemValidations":{
                              "path":{"id":"path","type":"string"},
                              "canonicalUrl":{"id":"canonicalUrl","type":"string"},
                              "sitemapPriority":{"id":"sitemapPriority","type":"number"},
                              "changeFrequency":{"id":"changeFrequency","type":"string"}
                            }},
                            "filters":{"id":"filters","type":"list","itemValidations":{
                              "key":{"id":"key","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "label":{"id":"label","type":"string"},
                              "valueType":{"id":"valueType","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["TEXT","NUMBER","BOOLEAN","LIST"]}}]},
                              "stringValue":{"id":"stringValue","type":"string"},
                              "numberValue":{"id":"numberValue","type":"number"},
                              "booleanValue":{"id":"booleanValue","type":"boolean"},
                              "listValues":{"id":"listValues","type":"list"}
                            }},
                            "sortValues":{"id":"sortValues","type":"list","itemValidations":{
                              "key":{"id":"key","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "numberValue":{"id":"numberValue","type":"number"},
                              "stringValue":{"id":"stringValue","type":"string"}
                            }},
                            "seoSignals":{"id":"seoSignals","type":"object","itemValidations":{
                              "canonicalUrl":{"id":"canonicalUrl","type":"string"},
                              "lastModified":{"id":"lastModified","type":"string"},
                              "indexable":{"id":"indexable","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["true","false"]}}]},
                              "metaTitle":{"id":"metaTitle","type":"string"},
                              "metaDescription":{"id":"metaDescription","type":"string"},
                              "schemaType":{"id":"schemaType","type":"string"}
                            }}
                          }
                        }
                        """)
        );
    }
}

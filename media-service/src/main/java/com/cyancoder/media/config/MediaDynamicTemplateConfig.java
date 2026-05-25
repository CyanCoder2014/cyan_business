package com.cyancoder.media.config;

import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;
import com.cyancoder.dynamiccore.template.DynamicTemplateProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MediaDynamicTemplateConfig {

    @Bean
    public DynamicTemplateProvider mediaDynamicTemplateProvider() {
        return () -> List.of(
                new DynamicEntityTemplate("media-asset", "MEDIA", "Media Asset", "Structured media asset with responsive variants, CDN URLs, alt text, and SEO-ready metadata.", """
                        {
                          "entityKey":"media-asset",
                          "entityType":"MEDIA",
                          "title":"Media Asset",
                          "defaultValues":{"storageStatus":"UPLOADED","visibility":"PUBLIC"},
                          "fields":{
                            "assetKey":{"id":"assetKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "assetType":{"id":"assetType","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["IMAGE","VIDEO","DOCUMENT","AUDIO","OTHER"]}}]},
                            "originalFileName":{"id":"originalFileName","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "mimeType":{"id":"mimeType","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "visibility":{"id":"visibility","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["PUBLIC","PRIVATE","SIGNED"]}}]},
                            "seo":{"id":"seo","type":"object","itemValidations":{
                              "altText":{"id":"altText","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "caption":{"id":"caption","type":"string"},
                              "title":{"id":"title","type":"string"},
                              "license":{"id":"license","type":"string"}
                            }},
                            "storage":{"id":"storage","type":"object","itemValidations":{
                              "bucket":{"id":"bucket","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "path":{"id":"path","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "cdnUrl":{"id":"cdnUrl","type":"string"},
                              "width":{"id":"width","type":"number"},
                              "height":{"id":"height","type":"number"},
                              "sizeBytes":{"id":"sizeBytes","type":"number"}
                            }},
                            "variants":{"id":"variants","type":"list","itemValidations":{
                              "variantKey":{"id":"variantKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                              "width":{"id":"width","type":"number"},
                              "height":{"id":"height","type":"number"},
                              "format":{"id":"format","type":"string"},
                              "cdnUrl":{"id":"cdnUrl","type":"string","validations":[{"validation":"REQUIRED","order":1}]}
                            }},
                            "storageStatus":{"id":"storageStatus","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["UPLOADING","UPLOADED","OPTIMIZED","FAILED","ARCHIVED"]}}]}
                          }
                        }
                        """),
                new DynamicEntityTemplate("media-folder", "MEDIA", "Media Folder", "Hierarchical folder and tagging structure for files and assets.", """
                        {
                          "entityKey":"media-folder",
                          "entityType":"MEDIA",
                          "title":"Media Folder",
                          "defaultValues":{"status":"ACTIVE"},
                          "fields":{
                            "folderKey":{"id":"folderKey","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "name":{"id":"name","type":"string","validations":[{"validation":"REQUIRED","order":1}]},
                            "parentFolderKey":{"id":"parentFolderKey","type":"string"},
                            "status":{"id":"status","type":"string","validations":[{"validation":"ENUM","order":1,"validationParams":{"values":["ACTIVE","ARCHIVED"]}}]},
                            "tags":{"id":"tags","type":"list"}
                          }
                        }
                        """)
        );
    }
}

package com.cyancoder.dynamiccore.runtime;

import com.cyancoder.dynamiccore.config.DynamicRuntimeProperties;
import com.cyancoder.dynamiccore.service.DynamicDefinitionParser;
import com.cyancoder.dynamiccore.store.jpa.StoredEntityDefinition;
import com.cyancoder.platformopenapi.PlatformApiSecurity;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DynamicEntityOpenApiServiceTest {

    @Test
    void generatesStrictNestedSchemaPathsHeadersAndBearerSecurity() {
        DynamicRuntimeService runtime = mock(DynamicRuntimeService.class);
        DynamicScope scope = new DynamicScope("demo-tenant", "main-site");
        StoredEntityDefinition stored = new StoredEntityDefinition();
        stored.setEntityKey("importer-order");
        stored.setEntityType("ORDER");
        stored.setTitle("Importer Order");
        stored.setDefinitionJson("""
                {
                  "entityType":"ORDER",
                  "title":"Importer Order",
                  "fields":{
                    "customer":{
                      "id":"customer",
                      "type":"object",
                      "itemValidations":{
                        "customerKey":{
                          "id":"customerKey",
                          "type":"string",
                          "validations":[{"validation":"REQUIRED","order":1}]
                        }
                      }
                    },
                    "totalAmount":{
                      "id":"totalAmount",
                      "type":"number",
                      "validations":[{
                        "validation":"DECIMAL_MIN",
                        "validationParams":{"min":"0"},
                        "order":1
                      }]
                    }
                  }
                }
                """);
        when(runtime.getDefinition("importer-order", scope)).thenReturn(stored);
        DynamicRuntimeProperties properties = new DynamicRuntimeProperties();
        properties.setServiceKey("commerce-service");

        OpenAPI openApi = new DynamicEntityOpenApiService(
                runtime,
                new DynamicDefinitionParser(new ObjectMapper()),
                properties)
                .generate("importer-order", scope, PlatformApiSecurity.BEARER);

        String path = "/endpoint/entities/records/importer-order";
        assertThat(openApi.getPaths()).containsKeys(
                path,
                path + "/{recordKey}",
                path + "/validate");
        assertThat(openApi.getPaths().get(path).getPost().getSecurity().get(0))
                .containsKey("bearerAuth");
        assertThat(openApi.getPaths().get(path).getGet().getParameters())
                .extracting(parameter -> parameter.getName())
                .contains("page", "size", "sort", "X-Tenant-Key", "X-Site-Key");
        Schema<?> listResponse = openApi.getPaths().get(path).getGet()
                .getResponses().get("200").getContent()
                .get("application/json").getSchema();
        assertThat(listResponse.getOneOf()).hasSize(2);

        Schema<?> data = openApi.getComponents().getSchemas().get("ImporterOrderData");
        assertThat(data.getAdditionalProperties()).isEqualTo(false);
        Schema<?> customer = (Schema<?>) data.getProperties().get("customer");
        assertThat(customer.getRequired()).containsExactly("customerKey");
        Schema<?> amount = (Schema<?>) data.getProperties().get("totalAmount");
        assertThat(amount.getMinimum()).isEqualByComparingTo("0");
        assertThat(openApi.getExtensions())
                .containsEntry("x-platform-service-key", "commerce-service")
                .containsEntry("x-platform-entity-key", "importer-order");
    }

    @Test
    void generatesBasicInternalPaths() {
        DynamicRuntimeService runtime = mock(DynamicRuntimeService.class);
        DynamicScope scope = new DynamicScope(null, null);
        StoredEntityDefinition stored = new StoredEntityDefinition();
        stored.setEntityKey("order");
        stored.setDefinitionJson(
                "{\"entityType\":\"ORDER\",\"title\":\"Order\",\"fields\":{}}");
        when(runtime.getDefinition("order", scope)).thenReturn(stored);

        OpenAPI openApi = new DynamicEntityOpenApiService(
                runtime,
                new DynamicDefinitionParser(new ObjectMapper()),
                new DynamicRuntimeProperties())
                .generate("order", scope, PlatformApiSecurity.BASIC);

        assertThat(openApi.getPaths().get("/internal/entities/records/order")
                .getPost().getSecurity().get(0)).containsKey("basicAuth");
    }
}

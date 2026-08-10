package com.cyancoder.processor.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "platform.openapi.docs-access=BASIC",
                "platform.openapi.docs-username=controller_docs",
                "platform.openapi.docs-password=controller_secret",
                "spring.datasource.url=jdbc:postgresql://localhost:5432/processor_test",
                "spring.datasource.username=postgres",
                "spring.datasource.password=postgres"
        }
)
@AutoConfigureMockMvc
class ProcessorControllerOpenApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generatesControllerRoutesSchemasAndBearerAuthenticationAtRuntime() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/v3/api-docs.yaml"))
                .andExpect(status().isUnauthorized());

        String body = mockMvc.perform(get("/v3/api-docs")
                        .with(org.springframework.security.test.web.servlet.request
                                .SecurityMockMvcRequestPostProcessors.httpBasic(
                                        "controller_docs", "controller_secret")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode openApi = objectMapper.readTree(body);

        JsonNode create = openApi.path("paths")
                .path("/api/processor-service/processors")
                .path("post");
        JsonNode run = openApi.path("paths")
                .path("/api/processor-service/processors/{processorKey}/run")
                .path("post");

        assertThat(create.isObject()).isTrue();
        assertThat(run.isObject()).isTrue();
        assertThat(run.path("x-platform-auth").asText()).isEqualTo("BEARER");
        assertThat(run.path("security").get(0).has("bearerAuth")).isTrue();
        assertThat(openApi.path("components").path("securitySchemes")
                .has("basicAuth")).isTrue();
        assertThat(openApi.path("components").path("schemas")
                .has("ProcessorRunRequest")).isTrue();
    }
}

package com.cyancoder.apidocs.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "platform.api-docs.targets-json=[]",
                "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:1/jwks"
        }
)
@AutoConfigureMockMvc
class ApiDocsCatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void protectsInternalCatalogWithItsConfiguredBasicClient() throws Exception {
        mockMvc.perform(get("/internal/api-docs/services"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/internal/api-docs/services")
                        .with(httpBasic("api_docs_internal", "api_docs_secret")))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void protectsEndpointCatalogWithBearerAuthentication() throws Exception {
        mockMvc.perform(get("/endpoint/api-docs/services"))
                .andExpect(status().isUnauthorized());
    }
}

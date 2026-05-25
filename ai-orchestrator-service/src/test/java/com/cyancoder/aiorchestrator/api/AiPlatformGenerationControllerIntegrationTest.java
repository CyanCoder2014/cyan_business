package com.cyancoder.aiorchestrator.api;

import com.cyancoder.aiorchestrator.api.dto.FollowUpQuestionDto;
import com.cyancoder.aiorchestrator.client.LlmClient;
import com.cyancoder.aiorchestrator.client.PlatformMetadataClient;
import com.cyancoder.aiorchestrator.client.PlatformProvisioningClient;
import com.cyancoder.aiorchestrator.domain.AppDescriptor;
import com.cyancoder.aiorchestrator.domain.DeliveryBlueprint;
import com.cyancoder.aiorchestrator.domain.EntityBlueprint;
import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;
import com.cyancoder.aiorchestrator.domain.PlatformAppType;
import com.cyancoder.aiorchestrator.domain.RouteBlueprint;
import com.cyancoder.aiorchestrator.exception.DownstreamServiceException;
import com.cyancoder.aiorchestrator.repo.ProvisioningRunRepository;
import com.cyancoder.aiorchestrator.service.AiPromptBuilder;
import com.cyancoder.aiorchestrator.service.AppDraftService;
import com.cyancoder.aiorchestrator.service.ConversationSessionService;
import com.cyancoder.aiorchestrator.service.FollowUpQuestionService;
import com.cyancoder.aiorchestrator.service.RetrievalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "rag.enabled=false",
                "rag.bootstrap-enabled=false"
        }
)
@AutoConfigureMockMvc
class AiPlatformGenerationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private LlmClient llmClient;

    @MockBean
    private PlatformMetadataClient metadataClient;

    @MockBean
    private PlatformProvisioningClient provisioningClient;

    @MockBean
    private ProvisioningRunRepository provisioningRunRepository;

    @MockBean
    private RetrievalService retrievalService;

    @MockBean
    private AiPromptBuilder promptBuilder;

    @MockBean
    private AppDraftService appDraftService;

    @MockBean
    private FollowUpQuestionService followUpQuestionService;

    @MockBean
    private ConversationSessionService conversationSessionService;

    private static final Map<String, Object> BUILDER_PERMISSIONS = Map.of(
            "permissions", List.of("builder:use")
    );

    @BeforeEach
    void setUp() {
        when(appDraftService.resolveKnownAppDraft(any(), any(), any(), any(), any())).thenReturn(Optional.empty());
        when(metadataClient.fetchMetadata(anyString(), anyString())).thenReturn(platformMetadata());
        when(retrievalService.retrieveContext(anyString(), anyMap(), any(), any(), any())).thenReturn(List.of("catalog-product template available"));
        when(promptBuilder.buildPlatformPrompt(anyString(), anyMap(), any(), anyString(), anyString())).thenReturn("compiled-platform-prompt");
    }

    @Test
    void generateEndpointExecutesProvisioningWhenNoFollowUpQuestionsRemain() throws Exception {
        when(llmClient.generateDsl("compiled-platform-prompt")).thenReturn(storefrontShopDsl());
        when(followUpQuestionService.resolveForBlueprint(any(), any(), any(), anyString())).thenReturn(List.of());
        when(provisioningClient.createDefinitionFromTemplate(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenAnswer(invocation -> Map.of(
                        "serviceKey", invocation.getArgument(0),
                        "templateKey", invocation.getArgument(1),
                        "entityKey", invocation.getArgument(2)
                ));
        when(provisioningClient.createRecord(anyString(), anyString(), anyString(), anyMap(), anyString(), anyString()))
                .thenAnswer(invocation -> Map.of(
                        "serviceKey", invocation.getArgument(0),
                        "entityKey", invocation.getArgument(1),
                        "recordKey", invocation.getArgument(2)
                ));

        mockMvc.perform(post("/endpoint/ai-orchestrator/generate/app")
                        .with(jwt().jwt(jwt -> jwt.subject("tester").claim("realm_access", BUILDER_PERMISSIONS)))
                        .contentType("application/json")
                        .content("""
                                {
                                  "prompt": "Build a storefront with a starter product and homepage",
                                  "tenantKey": "tenant-demo",
                                  "siteKey": "site-demo",
                                  "clientKey": "client-demo",
                                  "execute": true,
                                  "answers": {
                                    "brandName": "Demo Shop"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dsl.app.appKey").value("demo-shop"))
                .andExpect(jsonPath("$.dsl.app.tenantKey").value("tenant-demo"))
                .andExpect(jsonPath("$.dsl.app.siteKey").value("site-demo"))
                .andExpect(jsonPath("$.nextQuestions").isEmpty())
                .andExpect(jsonPath("$.provisioningResult.status").value("PROVISIONED"))
                .andExpect(jsonPath("$.provisioningResult.createdDefinitions.length()").value(3))
                .andExpect(jsonPath("$.provisioningResult.createdRecords.length()").value(4))
                .andExpect(jsonPath("$.provisioningResult.deliveryEndpoints.length()").value(6))
                .andExpect(jsonPath("$.provisioningResult.deliveryEndpoints[0].path").value("/public/storefront/render?path=/"));

        verify(provisioningClient).createDefinitionFromTemplate("storefront-service", "theme-layout", "theme-layout", "tenant-demo", "site-demo");
        verify(provisioningClient).createDefinitionFromTemplate("content-service", "landing-page", "landing-page", "tenant-demo", "site-demo");
        verify(provisioningClient).createDefinitionFromTemplate("catalog-service", "catalog-product", "shop-product", "tenant-demo", "site-demo");
        verify(provisioningClient).createDefinitionFromTemplate("storefront-service", "site-route", "site-route", "tenant-demo", "site-demo");
        verify(provisioningClient).createRecord(eq("storefront-service"), eq("theme-layout"), eq("theme-main"), anyMap(), eq("tenant-demo"), eq("site-demo"));
        verify(provisioningClient).createRecord(eq("content-service"), eq("landing-page"), eq("landing-home"), anyMap(), eq("tenant-demo"), eq("site-demo"));
        verify(provisioningClient).createRecord(eq("catalog-service"), eq("shop-product"), eq("starter-product"), anyMap(), eq("tenant-demo"), eq("site-demo"));
        verify(provisioningClient).createRecord(eq("storefront-service"), eq("site-route"), eq("home-route"), anyMap(), eq("tenant-demo"), eq("site-demo"));
    }

    @Test
    void generateEndpointReturnsFollowUpQuestionsAndSkipsProvisioningWhenInputIsIncomplete() throws Exception {
        when(llmClient.generateDsl("compiled-platform-prompt")).thenReturn(storefrontShopDsl());
        when(followUpQuestionService.resolveForBlueprint(any(), any(), any(), anyString())).thenReturn(List.of(
                new FollowUpQuestionDto(
                        "paymentProvider",
                        "Which payment provider should be preferred?",
                        true,
                        "Checkout provisioning should align to an explicit provider choice before release.",
                        List.of("zarinpal-default", "sandbox")
                )
        ));

        mockMvc.perform(post("/endpoint/ai-orchestrator/generate/app")
                        .with(jwt().jwt(jwt -> jwt.subject("tester").claim("realm_access", BUILDER_PERMISSIONS)))
                        .contentType("application/json")
                        .content("""
                                {
                                  "prompt": "Build a storefront with checkout",
                                  "tenantKey": "tenant-demo",
                                  "siteKey": "site-demo",
                                  "execute": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextQuestions.length()").value(1))
                .andExpect(jsonPath("$.nextQuestions[0]").value("Which payment provider should be preferred?"))
                .andExpect(jsonPath("$.followUpQuestions[0].key").value("paymentProvider"))
                .andExpect(jsonPath("$.provisioningResult").isEmpty());

        verify(provisioningClient, never()).createDefinitionFromTemplate(anyString(), anyString(), anyString(), anyString(), anyString());
        verifyNoInteractions(provisioningRunRepository);
    }

    @Test
    void generateEndpointReturnsStructuredDownstreamErrors() throws Exception {
        when(llmClient.generateDsl("compiled-platform-prompt")).thenReturn(storefrontShopDsl());
        when(followUpQuestionService.resolveForBlueprint(any(), any(), any(), anyString())).thenReturn(List.of());
        when(provisioningClient.createDefinitionFromTemplate(eq("storefront-service"), eq("theme-layout"), eq("theme-layout"), anyString(), anyString()))
                .thenThrow(new DownstreamServiceException(
                        "Downstream service returned an error: storefront-service /internal/entities/records/theme-layout",
                        "storefront-service",
                        "/internal/entities/records/theme-layout",
                        500,
                        "{\"message\":\"themeKey is required\"}",
                        null
                ));

        mockMvc.perform(post("/endpoint/ai-orchestrator/generate/app")
                        .with(jwt().jwt(jwt -> jwt.subject("tester").claim("realm_access", BUILDER_PERMISSIONS)))
                        .contentType("application/json")
                        .content("""
                                {
                                  "prompt": "Build a storefront with a starter product and homepage",
                                  "tenantKey": "tenant-demo",
                                  "siteKey": "site-demo",
                                  "clientKey": "client-demo",
                                  "execute": true,
                                  "answers": {
                                    "brandName": "Demo Shop"
                                  }
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.errorCode").value("ERR_DOWNSTREAM_SERVICE"))
                .andExpect(jsonPath("$.details.serviceKey").value("storefront-service"))
                .andExpect(jsonPath("$.details.path").value("/internal/entities/records/theme-layout"))
                .andExpect(jsonPath("$.details.downstreamStatus").value(500))
                .andExpect(jsonPath("$.details.downstreamBody").value("{\"message\":\"themeKey is required\"}"));
    }

    private Map<String, Object> platformMetadata() {
        return Map.of(
                "content-service", Map.of(),
                "catalog-service", Map.of(),
                "storefront-service", Map.of(),
                "bpm-service", Map.of()
        );
    }

    private PlatformAppDslDefinition storefrontShopDsl() {
        PlatformAppDslDefinition dsl = new PlatformAppDslDefinition();

        AppDescriptor app = new AppDescriptor();
        app.setAppKey("demo-shop");
        app.setTitle("Demo Shop");
        app.setType(PlatformAppType.SHOP);
        app.setCapabilities(List.of("website", "shop"));
        dsl.setApp(app);

        EntityBlueprint landingPage = new EntityBlueprint();
        landingPage.setServiceKey("content-service");
        landingPage.setTemplateKey("landing-page");
        landingPage.setEntityKey("landing-page");
        landingPage.setRecordKey("landing-home");
        landingPage.setCreateDefinition(true);
        landingPage.setCreateRecord(true);
        landingPage.setRecordData(Map.of(
                "title", "Demo Shop",
                "slug", "home",
                "publicationStatus", "PUBLISHED"
        ));

        EntityBlueprint product = new EntityBlueprint();
        product.setServiceKey("catalog-service");
        product.setTemplateKey("catalog-product");
        product.setEntityKey("shop-product");
        product.setRecordKey("starter-product");
        product.setCreateDefinition(true);
        product.setCreateRecord(true);
        product.setRecordData(Map.of(
                "name", "Starter Product",
                "sku", "STARTER-001",
                "active", true
        ));

        dsl.setEntities(List.of(landingPage, product));

        RouteBlueprint homeRoute = new RouteBlueprint();
        homeRoute.setRouteKey("home-route");
        homeRoute.setPath("/");
        homeRoute.setTargetServiceKey("content-service");
        homeRoute.setTargetEntityKey("landing-page");
        homeRoute.setTargetRecordKey("landing-home");
        homeRoute.setThemeRecordKey("theme-main");
        homeRoute.setPageType("LANDING");
        dsl.setRoutes(List.of(homeRoute));

        DeliveryBlueprint delivery = new DeliveryBlueprint();
        delivery.setPublicApis(List.of("/public/storefront/render?path=/", "/public/storefront/sitemap"));
        delivery.setBotApis(List.of("/api/catalog-service/**", "/api/content-service/**"));
        dsl.setDelivery(delivery);

        return dsl;
    }
}

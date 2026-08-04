package com.cyancoder.apidocs.service;

import com.cyancoder.apidocs.config.ApiDocsCatalogProperties;
import com.cyancoder.apidocs.model.ApiDocsServiceSummary;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ApiDocsCatalogServiceTest {
    private HttpServer server;

    @AfterEach
    void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void fetchesWithBasicAuthCachesAndPrefixesMergedOwnership() throws Exception {
        AtomicReference<String> authorization = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v3/api-docs", exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] body = """
                    {
                      "openapi":"3.1.0",
                      "info":{"title":"Orders","version":"2"},
                      "paths":{
                        "/orders":{
                          "get":{
                            "responses":{
                              "200":{
                                "content":{
                                  "application/json":{
                                    "schema":{"$ref":"#/components/schemas/Order"}
                                  }
                                }
                              }
                            }
                          }
                        }
                      },
                      "components":{
                        "schemas":{
                          "Order":{"type":"object","properties":{"id":{"type":"string"}}}
                        }
                      }
                    }
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        ApiDocsCatalogProperties properties = new ApiDocsCatalogProperties();
        properties.setCacheSeconds(60);
        properties.setTargetsJson("""
                [{
                  "serviceKey":"order-service",
                  "baseUrl":"http://localhost:%d",
                  "username":"order_internal",
                  "password":"order_secret"
                }]
                """.formatted(server.getAddress().getPort()));
        ObjectMapper mapper = new ObjectMapper();
        ApiDocsCatalogService service = new ApiDocsCatalogService(
                new ApiDocsTargetRegistry(properties, mapper),
                properties,
                mapper);

        List<ApiDocsServiceSummary> summaries = service.list();
        JsonNode merged = service.aggregate(false);

        assertThat(summaries).singleElement().satisfies(summary -> {
            assertThat(summary.status()).isEqualTo("AVAILABLE");
            assertThat(summary.pathCount()).isEqualTo(1);
            assertThat(summary.title()).isEqualTo("Orders");
            assertThat(summary.toString()).doesNotContain("order_secret");
        });
        assertThat(authorization.get()).startsWith("Basic ");
        assertThat(merged.path("paths").has("/services/order-service/orders")).isTrue();
        assertThat(merged.path("paths")
                .path("/services/order-service/orders")
                .path("get")
                .path("x-platform-service-key")
                .asText()).isEqualTo("order-service");
        assertThat(merged.path("paths")
                .path("/services/order-service/orders")
                .path("get")
                .findValue("$ref")
                .asText()).isEqualTo("#/components/schemas/OrderService_Order");
        assertThat(merged.path("components").path("schemas")
                .has("OrderService_Order")).isTrue();
        assertThat(merged.path("components").path("securitySchemes")
                .has("bearerAuth")).isTrue();
        assertThat(merged.path("components").path("securitySchemes")
                .has("basicAuth")).isTrue();
        assertThat(merged.path("x-platform-unavailable-services").isEmpty()).isTrue();
    }

    @Test
    void aggregateReportsUnavailableServicesWithoutDiscardingHealthyDocuments() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v3/api-docs", exchange -> {
            byte[] body = """
                    {
                      "openapi":"3.1.0",
                      "info":{"title":"Healthy","version":"1"},
                      "paths":{"/health-data":{"get":{"responses":{"200":{"description":"OK"}}}}}
                    }
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        int unavailablePort;
        try (java.net.ServerSocket socket = new java.net.ServerSocket(0)) {
            unavailablePort = socket.getLocalPort();
        }
        ApiDocsCatalogProperties properties = new ApiDocsCatalogProperties();
        properties.setConnectTimeoutMs(500);
        properties.setReadTimeoutMs(1000);
        properties.setTargetsJson("""
                [
                  {
                    "serviceKey":"healthy-service",
                    "baseUrl":"http://localhost:%d"
                  },
                  {
                    "serviceKey":"unavailable-service",
                    "baseUrl":"http://localhost:%d"
                  }
                ]
                """.formatted(server.getAddress().getPort(), unavailablePort));
        ObjectMapper mapper = new ObjectMapper();
        ApiDocsCatalogService service = new ApiDocsCatalogService(
                new ApiDocsTargetRegistry(properties, mapper),
                properties,
                mapper);

        JsonNode merged = service.aggregate(false);

        assertThat(merged.path("paths").has(
                "/services/healthy-service/health-data")).isTrue();
        assertThat(merged.path("x-platform-unavailable-services").get(0)
                .path("serviceKey").asText()).isEqualTo("unavailable-service");
    }
}

package com.cyancoder.report.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicServicesScenarioHttpIT {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    @Test
    void shopifyStyleScenarioShouldPassAcrossDynamicServices() throws Exception {
        Assumptions.assumeTrue(
                Boolean.parseBoolean(System.getProperty("dynamic.http.it", "false")),
                "Set -Ddynamic.http.it=true and run the live dynamic services first."
        );

        assumeServiceUp("http://127.0.0.1:9101/internal/entities/templates", basicAuth("content_internal", "content_secret"));
        assumeServiceUp("http://127.0.0.1:9102/internal/entities/templates", basicAuth("catalog_internal", "catalog_secret"));
        assumeServiceUp("http://127.0.0.1:9103/internal/entities/templates", basicAuth("crm_internal", "crm_secret"));
        assumeServiceUp("http://127.0.0.1:9104/internal/entities/templates", basicAuth("commerce_internal", "commerce_secret"));
        assumeServiceUp("http://127.0.0.1:9105/internal/entities/templates", basicAuth("finance_internal", "finance_secret"));
        assumeServiceUp("http://127.0.0.1:9107/internal/entities/templates", basicAuth("report_internal", "report_secret"));

        Map<String, Object> landingDefinition = postJson(
                "http://127.0.0.1:9101/internal/entities/templates/landing-page/definitions",
                basicAuth("content_internal", "content_secret"),
                """
                        {"entityKey":"shop-landing-page"}
                        """
        );
        assertEquals("shop-landing-page", landingDefinition.get("entityKey"));

        Map<String, Object> blogDefinition = postJson(
                "http://127.0.0.1:9101/internal/entities/templates/blog-page/definitions",
                basicAuth("content_internal", "content_secret"),
                """
                        {"entityKey":"shop-blog-post"}
                        """
        );
        assertEquals("shop-blog-post", blogDefinition.get("entityKey"));

        Map<String, Object> productDefinition = postJson(
                "http://127.0.0.1:9102/internal/entities/templates/catalog-product/definitions",
                basicAuth("catalog_internal", "catalog_secret"),
                """
                        {"entityKey":"shop-product"}
                        """
        );
        assertEquals("shop-product", productDefinition.get("entityKey"));

        Map<String, Object> contactDefinition = postJson(
                "http://127.0.0.1:9103/internal/entities/templates/crm-contact/definitions",
                basicAuth("crm_internal", "crm_secret"),
                """
                        {"entityKey":"shop-contact"}
                        """
        );
        assertEquals("shop-contact", contactDefinition.get("entityKey"));

        Map<String, Object> leadDefinition = postJson(
                "http://127.0.0.1:9103/internal/entities/templates/crm-lead/definitions",
                basicAuth("crm_internal", "crm_secret"),
                """
                        {"entityKey":"shop-lead"}
                        """
        );
        assertEquals("shop-lead", leadDefinition.get("entityKey"));

        Map<String, Object> orderDefinition = postJson(
                "http://127.0.0.1:9104/internal/entities/templates/sales-order/definitions",
                basicAuth("commerce_internal", "commerce_secret"),
                """
                        {"entityKey":"shop-order"}
                        """
        );
        assertEquals("shop-order", orderDefinition.get("entityKey"));

        Map<String, Object> transactionDefinition = postJson(
                "http://127.0.0.1:9105/internal/entities/templates/finance-transaction/definitions",
                basicAuth("finance_internal", "finance_secret"),
                """
                        {"entityKey":"shop-transaction"}
                        """
        );
        assertEquals("shop-transaction", transactionDefinition.get("entityKey"));

        Map<String, Object> reportDefinition = postJson(
                "http://127.0.0.1:9107/internal/entities/templates/dynamic-report/definitions",
                basicAuth("report_internal", "report_secret"),
                """
                        {"entityKey":"shop-order-report"}
                        """
        );
        assertEquals("shop-order-report", reportDefinition.get("entityKey"));

        Map<String, Object> landingRecord = postJson(
                "http://127.0.0.1:9101/internal/entities/submit/shop-landing-page?recordKey=landing-home",
                basicAuth("content_internal", "content_secret"),
                """
                        {
                          "slug":"acme-store",
                          "title":"Acme Store",
                          "heroTitle":"Launch your next purchase",
                          "heroSubtitle":"Curated gadgets with fast delivery.",
                          "publicationStatus":"PUBLISHED",
                          "sections":[
                            {
                              "blockType":"FEATURES",
                              "title":"Why Acme",
                              "body":"Fast shipping, curated products and live support.",
                              "ctaLabel":"",
                              "ctaUrl":""
                            },
                            {
                              "blockType":"CTA",
                              "title":"Shop now",
                              "body":"",
                              "ctaLabel":"Browse catalog",
                              "ctaUrl":"/shop"
                            }
                          ]
                        }
                        """
        );
        assertEquals("landing-home", landingRecord.get("recordKey"));

        Map<String, Object> blogRecord = postJson(
                "http://127.0.0.1:9101/internal/entities/submit/shop-blog-post?recordKey=blog-smartwatch-guide",
                basicAuth("content_internal", "content_secret"),
                """
                        {
                          "slug":"how-to-choose-a-smartwatch",
                          "title":"How to choose a smartwatch",
                          "summary":"A short guide for choosing the right smartwatch.",
                          "body":"Choosing a smartwatch depends on battery life, display quality, comfort, fitness features and the apps you actually use every week.",
                          "author":"Acme Editorial",
                          "publicationStatus":"PUBLISHED",
                          "tags":["wearables","guide"]
                        }
                        """
        );
        assertEquals("blog-smartwatch-guide", blogRecord.get("recordKey"));

        Map<String, Object> productOne = postJson(
                "http://127.0.0.1:9102/internal/entities/submit/shop-product?recordKey=product-watch-x1",
                basicAuth("catalog_internal", "catalog_secret"),
                """
                        {
                          "name":"Acme Smartwatch X1",
                          "sku":"WATCH-X1",
                          "categoryKey":"wearables",
                          "unit":"pcs",
                          "defaultPrice":12500000,
                          "currency":"IRR",
                          "active":true,
                          "details":{
                            "brand":"Acme",
                            "model":"X1",
                            "shortDescription":"AMOLED smartwatch with health tracking"
                          }
                        }
                        """
        );
        assertEquals("product-watch-x1", productOne.get("recordKey"));

        Map<String, Object> productTwo = postJson(
                "http://127.0.0.1:9102/internal/entities/submit/shop-product?recordKey=product-earbuds-pro",
                basicAuth("catalog_internal", "catalog_secret"),
                """
                        {
                          "name":"Acme Earbuds Pro",
                          "sku":"EARBUDS-PRO",
                          "categoryKey":"audio",
                          "unit":"pcs",
                          "defaultPrice":4800000,
                          "currency":"IRR",
                          "active":true,
                          "details":{
                            "brand":"Acme",
                            "model":"Pro",
                            "shortDescription":"Noise cancelling wireless earbuds"
                          }
                        }
                        """
        );
        assertEquals("product-earbuds-pro", productTwo.get("recordKey"));

        Map<String, Object> contactRecord = postJson(
                "http://127.0.0.1:9103/internal/entities/submit/shop-contact?recordKey=contact-sara-ahmadi",
                basicAuth("crm_internal", "crm_secret"),
                """
                        {
                          "fullName":"Sara Ahmadi",
                          "companyName":"Acme Retail",
                          "email":"sara@acme.example",
                          "mobile":"09121234567",
                          "status":"ACTIVE",
                          "source":"SHOP",
                          "notes":"Primary e-commerce customer profile"
                        }
                        """
        );
        assertEquals("contact-sara-ahmadi", contactRecord.get("recordKey"));

        Map<String, Object> leadRecord = postJson(
                "http://127.0.0.1:9103/internal/entities/submit/shop-lead?recordKey=lead-sara-ahmadi",
                basicAuth("crm_internal", "crm_secret"),
                """
                        {
                          "fullName":"Sara Ahmadi",
                          "companyName":"Acme Retail",
                          "email":"sara@acme.example",
                          "mobile":"09121234567",
                          "status":"QUALIFIED",
                          "source":"LANDING_PAGE",
                          "ownerUserId":"sales-01",
                          "notes":"Requested pricing for smartwatch bundle"
                        }
                        """
        );
        assertEquals("lead-sara-ahmadi", leadRecord.get("recordKey"));

        Map<String, Object> orderRecord = postJson(
                "http://127.0.0.1:9104/internal/entities/submit/shop-order?recordKey=order-1001",
                basicAuth("commerce_internal", "commerce_secret"),
                """
                        {
                          "customerKey":"contact-sara-ahmadi",
                          "currency":"IRR",
                          "documentStatus":"SUBMITTED",
                          "subtotal":17300000,
                          "discountTotal":0,
                          "taxTotal":1730000,
                          "grandTotal":19030000,
                          "items":[
                            {
                              "itemKey":"product-watch-x1",
                              "name":"Acme Smartwatch X1",
                              "quantity":1,
                              "unitPrice":12500000,
                              "lineTotal":12500000
                            },
                            {
                              "itemKey":"product-earbuds-pro",
                              "name":"Acme Earbuds Pro",
                              "quantity":1,
                              "unitPrice":4800000,
                              "lineTotal":4800000
                            }
                          ]
                        }
                        """
        );
        assertEquals("order-1001", orderRecord.get("recordKey"));

        Map<String, Object> transactionRecord = postJson(
                "http://127.0.0.1:9105/internal/entities/submit/shop-transaction?recordKey=txn-1001",
                basicAuth("finance_internal", "finance_secret"),
                """
                        {
                          "transactionType":"PAYMENT",
                          "referenceType":"ORDER",
                          "referenceKey":"order-1001",
                          "accountKey":"gateway-zarinpal",
                          "currency":"IRR",
                          "amount":19030000,
                          "status":"CONFIRMED",
                          "description":"Card payment captured for order-1001"
                        }
                        """
        );
        assertEquals("txn-1001", transactionRecord.get("recordKey"));

        Map<String, Object> reportRecord = postJson(
                "http://127.0.0.1:9107/internal/entities/submit/shop-order-report?recordKey=report-order-status",
                basicAuth("report_internal", "report_secret"),
                """
                        {
                          "reportKey":"shop-order-status-report",
                          "title":"Shop Order Status Report",
                          "sourceType":"DYNAMIC",
                          "serviceKey":"commerce-service",
                          "entityKey":"shop-order",
                          "defaultFilterField":"",
                          "defaultSumField":"grandTotal",
                          "groupByField":"documentStatus",
                          "filters":[]
                        }
                        """
        );
        assertEquals("report-order-status", reportRecord.get("recordKey"));

        Map<String, Object> orderLookup = getJson(
                "http://127.0.0.1:9104/internal/entities/records/shop-order/order-1001",
                basicAuth("commerce_internal", "commerce_secret")
        );
        assertEquals("order-1001", orderLookup.get("recordKey"));
        assertTrue(orderLookup.toString().contains("SUBMITTED"));
    }

    private static void assumeServiceUp(String url, String authHeader) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("Authorization", authHeader)
                    .GET()
                    .build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            Assumptions.assumeTrue(response.statusCode() == 200, "Service not ready: " + url);
        } catch (Exception ex) {
            Assumptions.assumeTrue(false, "Service not reachable: " + url + " -> " + ex.getMessage());
        }
    }

    private static Map<String, Object> postJson(String url, String authHeader, String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), () -> "POST failed for " + url + ": " + response.body());
        return OBJECT_MAPPER.readValue(response.body(), new TypeReference<>() {});
    }

    private static Map<String, Object> getJson(String url, String authHeader) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", authHeader)
                .GET()
                .build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), () -> "GET failed for " + url + ": " + response.body());
        return OBJECT_MAPPER.readValue(response.body(), new TypeReference<>() {});
    }

    private static String basicAuth(String username, String password) {
        String token = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }
}

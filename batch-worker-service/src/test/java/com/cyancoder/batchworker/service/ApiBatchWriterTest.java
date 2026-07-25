package com.cyancoder.batchworker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cyancoder.batchworker.api.BatchDefinitionSpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.Chunk;

class ApiBatchWriterTest {
    private HttpServer server;

    @AfterEach
    void stop() {
        if (server != null) server.stop(0);
    }

    @Test
    void repeatsUseSameCompactIdempotencyKey() throws Exception {
        List<String> keys = new ArrayList<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/target", exchange -> {
            keys.add(exchange.getRequestHeaders().getFirst("Idempotency-Key"));
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });
        server.start();
        ApiBatchWriter writer = writer("POST");
        Chunk<Map<String, Object>> chunk = new Chunk<>(List.of(Map.of("id", "customer-1")));
        writer.write(chunk);
        writer.write(chunk);
        assertThat(keys).hasSize(2).allSatisfy(key -> assertThat(key).startsWith("batch:"));
        assertThat(keys.get(0)).isEqualTo(keys.get(1));
        assertThat(keys.get(0)).hasSize(70);
    }

    @Test
    void serverFailureIsRetryable() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/target", exchange -> {
            exchange.sendResponseHeaders(503, -1);
            exchange.close();
        });
        server.start();
        assertThatThrownBy(() -> writer("PUT").write(
                new Chunk<>(List.of(Map.of("id", "customer-1")))))
                .isInstanceOf(RetryableApiException.class);
    }

    @Test
    void mapsNestedFields() {
        Map<String, Object> mapped = ApiBatchWriter.mapFields(
                Map.of("customer", Map.of("id", "c1", "name", "Ada")),
                Map.of("external.customerId", "customer.id", "displayName", "customer.name"));
        assertThat(mapped).containsEntry("displayName", "Ada");
        assertThat(mapped.get("external")).isEqualTo(Map.of("customerId", "c1"));
    }

    private ApiBatchWriter writer(String method) {
        BatchDefinitionSpec.Destination destination = new BatchDefinitionSpec.Destination(
                "http://localhost:" + server.getAddress().getPort() + "/target",
                method, "id", Map.of(), null, null);
        return new ApiBatchWriter(destination, new ObjectMapper(),
                "tenant:site:customers:2026-07-23");
    }
}

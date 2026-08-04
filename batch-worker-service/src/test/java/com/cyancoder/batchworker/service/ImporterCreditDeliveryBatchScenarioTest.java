package com.cyancoder.batchworker.service;

import com.cyancoder.batchworker.api.BatchDefinitionRequest;
import com.cyancoder.batchworker.api.BatchDefinitionSpec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ExecutionContext;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ImporterCreditDeliveryBatchScenarioTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<Map<String, Object>> projectedOrders = new ArrayList<>();
    private final List<Map<String, Object>> dispatchedLoadings = new ArrayList<>();
    private final List<String> idempotencyKeys = new ArrayList<>();
    private HttpServer server;

    @AfterEach
    void stop() {
        if (server != null) server.stop(0);
    }

    @Test
    void runsPaginatedOrderProjectionThenClustersByCustomerAtIdempotentReceiver() throws Exception {
        startServer();
        BatchDefinitionRequest fixture = fixture("order-projection-batch.json");
        BatchDefinitionSpec.Source source = localSource(
                fixture.spec().source(), "/orders", 2);
        BatchDefinitionSpec.Destination destination = localDestination(
                fixture.spec().destination(), "/projection");

        List<Map<String, Object>> sourceOrders = drain(new ApiBatchReader(source, mapper));
        List<Map<String, Object>> projectionEvents = sourceOrders.stream()
                .map(item -> ApiBatchWriter.mapFields(item, fixture.spec().fieldMappings()))
                .toList();
        new ApiBatchWriter(destination, mapper, "tenant:site:orders:2026-07-24T08:00:00Z")
                .write(new Chunk<>(projectionEvents));

        assertThat(sourceOrders).hasSize(3);
        assertThat(projectedOrders).hasSize(3);
        assertThat(idempotencyKeys).hasSize(3).doesNotHaveDuplicates();

        Map<String, CustomerProjection> clusters = cluster(projectedOrders);
        assertThat(clusters.get("customer-1").orderCount()).isEqualTo(2);
        assertThat(clusters.get("customer-1").totalAmount())
                .isEqualByComparingTo("350000000");
        assertThat(clusters.get("customer-2").maximumOverdueDays()).isEqualTo(95);
    }

    @Test
    void runsPaginatedMorningLoadingDispatchWithStablePerLoadingKeys() throws Exception {
        startServer();
        BatchDefinitionRequest fixture = fixture("due-loading-dispatch-batch.json");
        BatchDefinitionSpec.Source source = localSource(
                fixture.spec().source(), "/loadings", 2);
        BatchDefinitionSpec.Destination destination = localDestination(
                fixture.spec().destination(), "/delivery");

        List<Map<String, Object>> sourceLoadings = drain(new ApiBatchReader(source, mapper));
        List<Map<String, Object>> dispatches = sourceLoadings.stream()
                .map(item -> ApiBatchWriter.mapFields(item, fixture.spec().fieldMappings()))
                .toList();
        ApiBatchWriter writer = new ApiBatchWriter(
                destination, mapper, "tenant:site:loadings:2026-07-24T08:30:00Z");
        writer.write(new Chunk<>(dispatches));
        writer.write(new Chunk<>(List.of(dispatches.getFirst())));

        assertThat(sourceLoadings).hasSize(2);
        assertThat(dispatchedLoadings).hasSize(3);
        assertThat(dispatchedLoadings.getFirst())
                .containsEntry("dispatchKey", "loading-1")
                .containsKey("recipient")
                .containsKey("address");
        assertThat(idempotencyKeys.get(0)).isEqualTo(idempotencyKeys.get(2));
    }

    private void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/orders", exchange -> page(exchange, List.of(
                order("order-1", "customer-1", 100_000_000, 5),
                order("order-2", "customer-1", 250_000_000, 15),
                order("order-3", "customer-2", 900_000_000, 95)
        )));
        server.createContext("/loadings", exchange -> page(exchange, List.of(
                loading("loading-1", "customer-1"),
                loading("loading-2", "customer-2")
        )));
        server.createContext("/projection", exchange -> receive(exchange, projectedOrders));
        server.createContext("/delivery", exchange -> receive(exchange, dispatchedLoadings));
        server.start();
    }

    private void page(HttpExchange exchange, List<Map<String, Object>> rows) throws java.io.IOException {
        Map<String, String> query = query(exchange.getRequestURI());
        int page = Integer.parseInt(query.getOrDefault("page", "0"));
        int size = Integer.parseInt(query.getOrDefault("size", "2"));
        int from = Math.min(page * size, rows.size());
        int to = Math.min(from + size, rows.size());
        byte[] body = mapper.writeValueAsBytes(Map.of("content", rows.subList(from, to)));
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    private void receive(HttpExchange exchange, List<Map<String, Object>> target) throws java.io.IOException {
        idempotencyKeys.add(exchange.getRequestHeaders().getFirst("Idempotency-Key"));
        target.add(mapper.readValue(exchange.getRequestBody(), new TypeReference<>() {}));
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }

    private Map<String, String> query(URI uri) {
        Map<String, String> values = new LinkedHashMap<>();
        if (uri.getQuery() == null) return values;
        for (String pair : uri.getQuery().split("&")) {
            String[] parts = pair.split("=", 2);
            values.put(parts[0], parts.length == 2 ? parts[1] : "");
        }
        return values;
    }

    private BatchDefinitionRequest fixture(String name) throws Exception {
        return mapper.readValue(Files.readString(scenarioFile(name)), BatchDefinitionRequest.class);
    }

    private BatchDefinitionSpec.Source localSource(
            BatchDefinitionSpec.Source fixture, String path, int pageSize) {
        return new BatchDefinitionSpec.Source(
                baseUrl(path), fixture.itemsPath(), fixture.pageParameter(),
                fixture.sizeParameter(), pageSize, Map.of(), null, null);
    }

    private BatchDefinitionSpec.Destination localDestination(
            BatchDefinitionSpec.Destination fixture, String path) {
        return new BatchDefinitionSpec.Destination(
                baseUrl(path), fixture.method(), fixture.itemKeyPath(), Map.of(), null, null);
    }

    private String baseUrl(String path) {
        return "http://localhost:" + server.getAddress().getPort() + path;
    }

    private List<Map<String, Object>> drain(ApiBatchReader reader) {
        reader.open(new ExecutionContext());
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> row;
        while ((row = reader.read()) != null) rows.add(row);
        return rows;
    }

    private Map<String, CustomerProjection> cluster(List<Map<String, Object>> events) {
        Map<String, CustomerProjection> result = new LinkedHashMap<>();
        for (Map<String, Object> event : events) {
            String customer = String.valueOf(event.get("customerKey"));
            BigDecimal amount = new BigDecimal(String.valueOf(event.get("totalAmount")));
            int overdue = Integer.parseInt(String.valueOf(event.get("maximumOverdueDays")));
            result.compute(customer, (ignored, current) -> current == null
                    ? new CustomerProjection(1, amount, overdue)
                    : new CustomerProjection(
                            current.orderCount() + 1,
                            current.totalAmount().add(amount),
                            Math.max(current.maximumOverdueDays(), overdue)));
        }
        return result;
    }

    private Map<String, Object> order(String orderId, String customerId, long total, int overdue) {
        return Map.of(
                "orderId", orderId,
                "customer", Map.of("id", customerId),
                "createdAt", "2026-07-23T10:00:00Z",
                "totalAmount", total,
                "currency", "IRR",
                "payment", Map.of("maximumOverdueDays", overdue),
                "loading", Map.of(
                        "id", "loading-" + orderId,
                        "promisedDeliveryAt", "2026-07-24T09:00:00Z",
                        "status", "READY"));
    }

    private Map<String, Object> loading(String loadingId, String customerId) {
        return Map.of(
                "loadingId", loadingId,
                "customer", Map.of("id", customerId),
                "recipient", Map.of("name", "Receiver " + customerId, "mobile", "09120000000"),
                "deliveryAddress", Map.of("city", "Tehran", "postalCode", "1234567890"),
                "promisedDeliveryAt", "2026-07-24T09:00:00Z",
                "packages", List.of(Map.of("packageKey", "pkg-" + loadingId, "weight", 12)));
    }

    private Path scenarioFile(String name) {
        Path fromModule = Path.of("..", "docs", "examples", "importer-credit-delivery", name);
        return Files.exists(fromModule)
                ? fromModule
                : Path.of("docs", "examples", "importer-credit-delivery", name);
    }

    private record CustomerProjection(int orderCount, BigDecimal totalAmount, int maximumOverdueDays) {}
}
